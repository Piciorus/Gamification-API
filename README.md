package de.consorsbank.core.trauthsc.tam.messaging;

import de.consorsbank.core.trauthsc.tam.core.initiatetransaction.InitiateTransactionAuthorization;
import de.consorsbank.core.trauthsc.rest.api.tam.initiate.transaction.authorization.model.InitiateTransactionAuthorizationRequest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.jms.Message;
import jakarta.jms.TextMessage;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
@Getter
@Slf4j
@ConditionalOnProperty(name = "spring-artemis.activemq.enableMQ", havingValue = "true")
public class ExternalEventMessageListener {

    private final InitiateTransactionAuthorization initiateTransactionAuthorization;
    private final ObjectMapper objectMapper;

    @JmsListener(
        destination = "${spring-artemis.activemq.queue}",
        containerFactory = "jmsListenerContainerFactory"
    )
    @Transactional(rollbackFor = CommonException.class) // TODO: will have on CommonException
    public void onMessage(Message message) throws Exception {
        log.debug("Received JMS message: {}", message);

        try {
            if (!(message instanceof TextMessage textMessage)) {
                log.warn("Received non-text message, skipping: {}", message.getClass().getName());
                return;
            }

            var payload = textMessage.getText();
            log.debug("Message payload: {}", payload);

            // Deserialize the incoming JMS payload into the initiate request
            var request = objectMapper.readValue(
                payload,
                InitiateTransactionAuthorizationRequest.class
            );

            // Delegate to the service — this also sends the PENDING event to the queue
            var response = initiateTransactionAuthorization.initiateTransactionAuthorization(request);

            log.debug("Successfully processed message and initiated authorization with id: {}",
                response.getAuthorizationId());

        } catch (CommonException exception) {
            log.error("Error processing JMS message, transaction will rollback: {}",
                exception.getMessage(), exception);
            throw exception;
        } catch (Exception exception) {
            log.error("Error processing JMS message, transaction will rollback: {}",
                exception.getMessage(), exception);
            throw exception;
        }
    }
}


package de.consorsbank.core.trauthsc.tam.core.initiatetransaction;

import de.consorsbank.core.trauthsc.tam.core.initiatetransaction.mapper.InitiateTransactionAuthorizationMapper;
import de.consorsbank.core.trauthsc.tam.entity.AuthorizationEntity;
import de.consorsbank.core.trauthsc.tam.entity.ServiceEntity;
import de.consorsbank.core.trauthsc.tam.entity.ServiceId;
import de.consorsbank.core.trauthsc.tam.entity.enums.AuthorizationStatusEnum;
import de.consorsbank.core.trauthsc.tam.messaging.TransactionEventSender;
import de.consorsbank.core.trauthsc.tam.repository.AuthorizationRepository;
import de.consorsbank.core.trauthsc.tam.repository.ServiceRepository;
import de.consorsbank.core.trauthsc.rest.api.tam.initiate.transaction.authorization.model.InitiateTransactionAuthorizationRequest;
import de.consorsbank.core.trauthsc.rest.api.tam.initiate.transaction.authorization.model.InitiateTransactionAuthorizationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class InitiateTransactionAuthorizationService implements InitiateTransactionAuthorization {

    private final AuthorizationRepository authorizationRepository;
    private final ServiceRepository serviceRepository;
    private final InitiateTransactionAuthorizationMapper initiateTransactionAuthorizationMapper;
    private final TransactionEventSender transactionEventSender;

    @Override
    @Transactional(rollbackFor = CommonException.class)
    public InitiateTransactionAuthorizationResponse initiateTransactionAuthorization(
            InitiateTransactionAuthorizationRequest request) {

        log.info("Initiating authorization for transactionId {}.", request.getTransactionId());

        // 1. Get existing PENDING or create a new AuthorizationEntity
        var authorizationEntity = getOrCreateAuthorizationEntity(request);

        // 2. Persist
        var saved = authorizationRepository.save(authorizationEntity);

        // 3. Send PENDING event to the tx-queue (mirrors sendPendingTransactionEvent from old service)
        sendPendingTransactionEvent(saved.getTransactionId().toString());

        log.info("Authorization having transactionId {} was initiated.", request.getTransactionId());

        // 4. Return response with the generated authorizationId
        return initiateTransactionAuthorizationMapper.toResponse(saved);
    }

    /**
     * Send a pending transaction event on the tx-queue.
     * Mirrors sendPendingTransactionEvent from AuthorizationServiceImpl.
     *
     * @param transactionId - the id of the transaction that was started
     */
    private void sendPendingTransactionEvent(String transactionId) {
        var authzResult = new TxAuthzResult();
        authzResult.setTxId(transactionId);
        authzResult.setAuthzStatus(AuthzStatus.PENDING);
        authzResult.setOccurenceTimestamp(new Date());
        transactionEventSender.sendMessage(authzResult);
    }

    /**
     * Get AuthorizationEntity by transactionId or create a new one if it does not exist.
     * Mirrors getOrCreateAuthorizationEntity from AuthorizationServiceImpl.
     *
     * - If a PENDING authorization already exists for this transactionId → return it (idempotent).
     * - If an ACCEPTED or REJECTED authorization exists → throw (terminal state).
     * - If none exists → create a fresh PENDING entity.
     *
     * @param request the initiate request from the controller/listener
     * @return AuthorizationEntity ready to be saved
     */
    private AuthorizationEntity getOrCreateAuthorizationEntity(
            InitiateTransactionAuthorizationRequest request) {

        UUID transactionId = request.getTransactionId();
        AuthorizationEntity authorizationEntity = null;

        if (transactionId != null) {
            for (AuthorizationEntity entity : authorizationRepository.findAllByTransactionId(transactionId)) {

                // Terminal states — cannot re-initiate
                if (entity.getStatus().equals(AuthorizationStatusEnum.ACCEPTED)
                        || entity.getStatus().equals(AuthorizationStatusEnum.REJECTED)) {
                    log.error("Authorization for transactionId {} is already in terminal state {}.",
                            transactionId, entity.getStatus());
                    throw new CommonException(
                            CommonExceptionCode.SERVER_ERROR,
                            List.of(transactionId.toString()));
                }

                // Reuse existing PENDING authorization (idempotency)
                if (entity.getStatus().equals(AuthorizationStatusEnum.PENDING)) {
                    authorizationEntity = entity;
                }
            }
        }

        // No existing entity found — create a new one
        if (authorizationEntity == null) {
            authorizationEntity = createPendingAuthorizationEntity(request);
        }

        return authorizationEntity;
    }

    /**
     * Creates a brand-new PENDING AuthorizationEntity from the request.
     * Resolves the ServiceEntity (OWNER + SERVICE composite FK) from the DB.
     */
    private AuthorizationEntity createPendingAuthorizationEntity(
            InitiateTransactionAuthorizationRequest request) {

        // Map basic fields via MapStruct (status=PENDING, fresh id/externalId)
        AuthorizationEntity entity = initiateTransactionAuthorizationMapper.toEntity(request);

        // Resolve ServiceEntity by composite key (OWNER + SERVICE)
        ServiceId serviceId = new ServiceId(request.getTransactionService());
        ServiceEntity serviceEntity = serviceRepository.findById(serviceId)
                .orElseThrow(() -> {
                    log.error("No service found for transactionService {} while initiating transactionId {}.",
                            request.getTransactionService(), request.getTransactionId());
                    return new CommonException(
                            CommonExceptionCode.SERVER_ERROR,
                            List.of(request.getTransactionService()));
                });

        entity.setServiceEntity(serviceEntity);
        return entity;
    }

    /**
     * Validates if the provided transaction id already exists.
     * Kept for direct use if needed outside getOrCreateAuthorizationEntity.
     *
     * @param transactionId -- the transaction id to validate
     */
    private void validateAlreadyExistingTransaction(UUID transactionId) {
        if (authorizationRepository.existsByTransactionId(transactionId)) {
            log.error("An exception was raised while initiating authorization for transactionId {}. " +
                    "Authorization already exists.", transactionId);
            throw new CommonException(
                    CommonExceptionCode.SERVER_ERROR,
                    List.of(transactionId.toString()));
        }
    }
}
