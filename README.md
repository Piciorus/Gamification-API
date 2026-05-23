@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "spring-artemis.activemq.enableMQ", havingValue = "true")
public class ExternalEventMessageListener {

    private final AuthorizationRepository authorizationRepository;
    private final ObjectMapper objectMapper;

    @JmsListener(
        destination = "${spring-artemis.activemq.queue}",
        containerFactory = "jmsListenerContainerFactory"
    )
    @Transactional(rollbackFor = CommonException.class)
    public void onMessage(Message message) throws Exception {
        log.debug("Received JMS message: {}", message);

        try {
            if (!(message instanceof TextMessage textMessage)) {
                log.warn("Received non-text message, skipping: {}",
                    message.getClass().getName());
                return;
            }

            var payload = textMessage.getText();
            log.debug("Message payload: {}", payload);

            // Deserialize incoming event
            var authzResult = objectMapper.readValue(payload, TxAuthzResult.class);

            log.info("Received authorization result for authorizationId {} with status {}",
                authzResult.getTxId(), authzResult.getAuthzStatus());

            // Find existing PENDING authorization and update its status
            updateAuthorizationStatus(authzResult);

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

    private void updateAuthorizationStatus(TxAuthzResult authzResult) {
        var authorizationId = UUID.fromString(authzResult.getTxId());

        var entity = authorizationRepository.findById(authorizationId)
            .orElseThrow(() -> {
                log.error("No authorization found for authorizationId {}.",
                    authorizationId);
                return new CommonException(
                    CommonExceptionCode.SERVER_ERROR,
                    List.of(authorizationId.toString()));
            });

        // Guard — only PENDING can be updated
        if (!entity.getStatus().equals(AuthorizationStatusEnum.PENDING)) {
            log.error("Authorization {} is already in terminal state {}, cannot update.",
                authorizationId, entity.getStatus());
            throw new CommonException(
                CommonExceptionCode.SERVER_ERROR,
                List.of(authorizationId.toString()));
        }

        var newStatus = mapStatus(authzResult.getAuthzStatus());
        entity.setStatus(newStatus);
        authorizationRepository.save(entity);

        log.info("Authorization {} status updated to {}.", authorizationId, newStatus);
    }

    private AuthorizationStatusEnum mapStatus(AuthzStatus authzStatus) {
        return switch (authzStatus) {
            case AUTHORIZED -> AuthorizationStatusEnum.AUTHORIZED;
            case FAILED     -> AuthorizationStatusEnum.FAILED;
            case EXPIRED    -> AuthorizationStatusEnum.EXPIRED;
            default -> throw new CommonException(
                CommonExceptionCode.SERVER_ERROR,
                List.of("Unknown authz status: " + authzStatus));
        };
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

        // 3. Map to response
        var response = initiateTransactionAuthorizationMapper.toResponse(saved);

        // 4. Send PENDING event to the tx-queue using the authorizationId from the response
        sendPendingTransactionEvent(response.getAuthorizationId().toString());

        log.info("Authorization having transactionId {} was initiated.", request.getTransactionId());

        return response;
    }

    /**
     * Send a pending transaction event on the tx-queue.
     * Uses the authorizationId (TAM's own ID) as the txId in the event,
     * so downstream consumers can correlate by TAM's authorization identifier.
     *
     * @param authorizationId - the TAM authorization id from the response
     */
    private void sendPendingTransactionEvent(String authorizationId) {
        var authzResult = new TxAuthzResult();
        authzResult.setTxId(authorizationId);
        authzResult.setAuthzStatus(AuthzStatus.PENDING);
        authzResult.setOccurenceTimestamp(new Date());
        transactionEventSender.sendMessage(authzResult);
    }

    /**
     * Get AuthorizationEntity by transactionId or create a new one if it does not exist.
     *
     * - PENDING exists → reuse it (idempotent)
     * - AUTHORIZED / FAILED / EXPIRED exists → throw (terminal state, cannot re-initiate)
     * - None exists → create fresh PENDING entity
     */
    private AuthorizationEntity getOrCreateAuthorizationEntity(
            InitiateTransactionAuthorizationRequest request) {

        UUID transactionId = request.getTransactionId();
        AuthorizationEntity authorizationEntity = null;

        if (transactionId != null) {
            for (AuthorizationEntity entity : authorizationRepository.findAllByTransactionId(transactionId)) {

                // Terminal states — cannot re-initiate
                if (entity.getStatus().equals(AuthorizationStatusEnum.AUTHORIZED)
                        || entity.getStatus().equals(AuthorizationStatusEnum.FAILED)
                        || entity.getStatus().equals(AuthorizationStatusEnum.EXPIRED)) {
                    log.error("Authorization for transactionId {} is already in terminal state {}.",
                            transactionId, entity.getStatus());
                    throw new CommonException(
                            CommonExceptionCode.SERVER_ERROR,
                            List.of(transactionId.toString()));
                }

                // Reuse existing PENDING (idempotency)
                if (entity.getStatus().equals(AuthorizationStatusEnum.PENDING)) {
                    authorizationEntity = entity;
                }
            }
        }

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

        AuthorizationEntity entity = initiateTransactionAuthorizationMapper.toEntity(request);

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
            log.error("Authorization already exists for transactionId {}.", transactionId);
            throw new CommonException(
                    CommonExceptionCode.SERVER_ERROR,
                    List.of(transactionId.toString()));
        }
    }
}
package de.consorsbank.core.trauthsc.tam.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class TamTransactionEventSender {

    private final JmsTemplate jmsTemplate;

    @Value("${spring-artemis.activemq.queue}")
    private String queue;

    public void sendPendingEvent(String authorizationId) {
        log.debug("Sending PENDING event for authorizationId {} to queue.", authorizationId);
        jmsTemplate.convertAndSend(queue, authorizationId);
    }
}
