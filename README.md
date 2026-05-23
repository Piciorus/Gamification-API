package de.consorsbank.core.trauthsc.tam.core.initiatetransaction.service;

import com.consorsbank.common.error.handling.exception.CommonException;
import com.consorsbank.common.error.handling.exception.CommonExceptionCode;
import de.consorsbank.core.trauthsc.tam.core.initiatetransaction.mapper.InitiateTransactionAuthorizationMapper;
import de.consorsbank.core.trauthsc.tam.core.initiatetransaction.messaging.InitiateTransactionEventSender;
import de.consorsbank.core.trauthsc.tam.entity.AuthorizationEntity;
import de.consorsbank.core.trauthsc.tam.entity.enums.AuthorizationStatusEnum;
import de.consorsbank.core.trauthsc.tam.repository.AuthorizationRepository;
import de.consorsbank.core.trauthsc.rest.api.tam.initiate.transaction.authorization.model.InitiateTransactionAuthorizationRequest;
import de.consorsbank.core.trauthsc.rest.api.tam.initiate.transaction.authorization.model.InitiateTransactionAuthorizationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class InitiateTransactionAuthorizationService implements InitiateTransactionAuthorization {

    private final AuthorizationRepository authorizationRepository;
    private final InitiateTransactionAuthorizationMapper initiateTransactionAuthorizationMapper;
    private final InitiateTransactionEventSender initiateTransactionEventSender;

    @Override
    @Transactional(rollbackFor = CommonException.class)
    public InitiateTransactionAuthorizationResponse initiateTransactionAuthorization(
            InitiateTransactionAuthorizationRequest request) {

        log.info("Initiating authorization for transactionId {}.", request.getTransactionId());

        // 1. Get existing or create — handles duplicate detection
        var authorizationEntity = getOrCreateAuthorizationEntity(request);

        // 2. Persist
        var saved = authorizationRepository.save(authorizationEntity);

        log.info("Authorization having transactionId {} was initiated.", request.getTransactionId());

        // 3. Map to response
        var response = initiateTransactionAuthorizationMapper.toResponse(saved);

        // 4. Send PENDING event with transactionId + status
        initiateTransactionEventSender.sendMessage(saved.getTransactionId(), AuthorizationStatusEnum.PENDING);

        return response;
    }

    /**
     * Get AuthorizationEntity by transactionId or create a new one if it does not exist.
     *
     * Requirement 1: if called twice with the same transactionId → throw with clear message.
     * Terminal states (AUTHORIZED, FAILED, EXPIRED) → also throw.
     * No existing entity → create fresh one via mapper (id + externalId generated in mapper).
     */
    private AuthorizationEntity getOrCreateAuthorizationEntity(
            InitiateTransactionAuthorizationRequest request) {

        UUID transactionId = request.getTransactionId();

        if (transactionId != null) {
            for (AuthorizationEntity entity : authorizationRepository.findAllByTransactionId(transactionId)) {

                // Requirement 1: already initiated (PENDING) → inform caller
                if (entity.getStatus().equals(AuthorizationStatusEnum.PENDING)) {
                    log.error("Authorization for transactionId {} was already initiated.", transactionId);
                    throw new CommonException(
                            CommonExceptionCode.SERVER_ERROR,
                            List.of("Transaction " + transactionId + " was already initiated."));
                }

                // Terminal states → cannot re-initiate
                if (entity.getStatus().equals(AuthorizationStatusEnum.AUTHORIZED)
                        || entity.getStatus().equals(AuthorizationStatusEnum.FAILED)
                        || entity.getStatus().equals(AuthorizationStatusEnum.EXPIRED)) {
                    log.error("Authorization for transactionId {} is already in terminal state {}.",
                            transactionId, entity.getStatus());
                    throw new CommonException(
                            CommonExceptionCode.SERVER_ERROR,
                            List.of(transactionId.toString()));
                }
            }
        }

        // No existing entity — create via mapper (id + externalId generated there)
        return initiateTransactionAuthorizationMapper.toEntity(request);
    }
}

package de.consorsbank.core.trauthsc.tam.core.initiatetransaction.mapper;

import de.consorsbank.core.trauthsc.tam.entity.AuthorizationEntity;
import de.consorsbank.core.trauthsc.rest.api.tam.initiate.transaction.authorization.model.InitiateTransactionAuthorizationRequest;
import de.consorsbank.core.trauthsc.rest.api.tam.initiate.transaction.authorization.model.InitiateTransactionAuthorizationResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface InitiateTransactionAuthorizationMapper {

    /**
     * Requirement 2: id and externalId are generated here at mapper level.
     */
    @Mapping(target = "id",            expression = "java(java.util.UUID.randomUUID())")
    @Mapping(target = "externalId",    expression = "java(java.util.UUID.randomUUID())")
    @Mapping(target = "transactionId", source = "transactionId")
    @Mapping(target = "expiresAt",     source = "expiresAt")
    @Mapping(target = "status",
             expression = "java(de.consorsbank.core.trauthsc.tam.entity.enums.AuthorizationStatusEnum.PENDING)")
    @Mapping(target = "serviceEntity", ignore = true)
    @Mapping(target = "isDeleted",     ignore = true)
    @Mapping(target = "createdBy",     ignore = true)
    @Mapping(target = "updatedBy",     ignore = true)
    @Mapping(target = "deletedBy",     ignore = true)
    @Mapping(target = "createdAt",     ignore = true)
    @Mapping(target = "updatedAt",     ignore = true)
    @Mapping(target = "deletedAt",     ignore = true)
    @Mapping(target = "version",       ignore = true)
    AuthorizationEntity toEntity(InitiateTransactionAuthorizationRequest request);

    @Mapping(target = "authorizationId", source = "id")
    InitiateTransactionAuthorizationResponse toResponse(AuthorizationEntity authorization);
}

package de.consorsbank.core.trauthsc.tam.core.initiatetransaction.messaging;

import de.consorsbank.core.trauthsc.tam.entity.enums.AuthorizationStatusEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Requirement 3: sends messages with transactionId + status to the queue.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class InitiateTransactionEventSender {

    private final JmsTemplate jmsTemplate;

    @Value("${spring-artemis.activemq.queue}")
    private String queue;

    public void sendMessage(UUID transactionId, AuthorizationStatusEnum status) {
        log.debug("Sending event for transactionId {} with status {} to queue.", transactionId, status);
        jmsTemplate.convertAndSend(queue, new TransactionAuthorizationEvent(transactionId, status));
    }
}
package de.consorsbank.core.trauthsc.tam.core.initiatetransaction.messaging;

import de.consorsbank.core.trauthsc.tam.entity.enums.AuthorizationStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Event payload sent to the queue containing transactionId and status.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionAuthorizationEvent {

    private UUID transactionId;
    private AuthorizationStatusEnum status;
}

