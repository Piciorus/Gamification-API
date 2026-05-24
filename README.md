```
package de.consorsbank.core.trauthsc.tam.core.initiatetransaction.payload;

import de.consorsbank.core.trauthsc.rest.api.pvm.model.PayloadVaultRequest;
import de.consorsbank.core.trauthsc.rest.api.tam.initiate.transaction.authorization.model.InitiateTransactionAuthorizationRequest;

import java.time.OffsetDateTime;

/**
 * Generic contract for processing the transactionServicePayload
 * from the InitiateTransactionAuthorizationRequest into a PayloadVaultRequest.
 *
 * Each service (CompensationPot, etc.) implements this to handle
 * their specific payload format.
 */
public interface TransactionPayloadProcessor {

    /**
     * Returns the service name this processor handles.
     * Matches request.getTransactionService().
     */
    String getSupportedService();

    /**
     * Converts the raw JSON payload string from the request
     * into a PayloadVaultRequest ready to be saved.
     *
     * @param payloadJson the raw JSON string from transactionServicePayload
     * @param expiresAt   expiration timestamp from the request
     * @return PayloadVaultRequest with encrypted-ready payload
     */
    PayloadVaultRequest process(String payloadJson, OffsetDateTime expiresAt);
}
```

```
package de.consorsbank.core.trauthsc.tam.core.initiatetransaction.payload;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.consorsbank.core.trauthsc.rest.api.pvm.model.PayloadVaultRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

/**
 * Processes the CompensationPot transactionServicePayload.
 * Validates the JSON can be deserialized as CompensationPotInitiateTransactionRequest
 * and re-serializes it as a clean JSON string for PayloadVault storage.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class CompensationPotPayloadProcessor implements TransactionPayloadProcessor {

    private static final String COMPENSATION_POT_SERVICE = "compensation-pot-change";

    private final ObjectMapper objectMapper;

    @Override
    public String getSupportedService() {
        return COMPENSATION_POT_SERVICE;
    }

    @Override
    public PayloadVaultRequest process(String payloadJson, OffsetDateTime expiresAt) {
        try {
            // Validate and normalize the payload by deserializing then re-serializing
            var compensationPotRequest = objectMapper.readValue(
                    payloadJson,
                    de.consorsbank.customer.taxsrvsc.rest.api.model
                            .CompensationPotInitiateTransactionRequest.class);

            var normalizedJson = objectMapper.writeValueAsString(compensationPotRequest);

            log.debug("Processed CompensationPot payload for storage.");

            return new PayloadVaultRequest(expiresAt, normalizedJson);

        } catch (Exception e) {
            log.error("Failed to process CompensationPot payload: {}", e.getMessage());
            throw new CommonException(
                    CommonExceptionCode.SERVER_ERROR,
                    List.of("Invalid CompensationPot payload: " + e.getMessage()));
        }
    }
}
```


```
package de.consorsbank.core.trauthsc.tam.core.initiatetransaction.payload;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Factory that selects the correct TransactionPayloadProcessor
 * based on the transactionService name from the request.
 *
 * New services just need to implement TransactionPayloadProcessor
 * and register as a Spring bean — no changes needed here.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class TransactionPayloadProcessorFactory {

    private final List<TransactionPayloadProcessor> processors;

    private Map<String, TransactionPayloadProcessor> processorMap;

    @jakarta.annotation.PostConstruct
    public void init() {
        processorMap = processors.stream()
                .collect(Collectors.toMap(
                        TransactionPayloadProcessor::getSupportedService,
                        Function.identity()));
        log.debug("Registered payload processors: {}", processorMap.keySet());
    }

    /**
     * Returns the processor for the given service name.
     *
     * @param transactionService the service name from the request
     * @return the matching TransactionPayloadProcessor
     * @throws CommonException if no processor is registered for the service
     */
    public TransactionPayloadProcessor getProcessor(String transactionService) {
        var processor = processorMap.get(transactionService);
        if (processor == null) {
            log.error("No payload processor found for service: {}", transactionService);
            throw new CommonException(
                    CommonExceptionCode.SERVER_ERROR,
                    List.of("No payload processor for service: " + transactionService));
        }
        return processor;
    }
}
```


```
package de.consorsbank.core.trauthsc.tam.core.initiatetransaction.service;

import com.consorsbank.common.error.handling.exception.CommonException;
import com.consorsbank.common.error.handling.exception.CommonExceptionCode;
import de.consorsbank.core.trauthsc.tam.core.initiatetransaction.mapper.InitiateTransactionAuthorizationMapper;
import de.consorsbank.core.trauthsc.tam.core.initiatetransaction.messaging.InitiateTransactionEventSender;
import de.consorsbank.core.trauthsc.tam.core.initiatetransaction.payload.TransactionPayloadProcessorFactory;
import de.consorsbank.core.trauthsc.tam.entity.AuthorizationEntity;
import de.consorsbank.core.trauthsc.tam.entity.ServiceEntity;
import de.consorsbank.core.trauthsc.tam.entity.enums.AuthorizationStatusEnum;
import de.consorsbank.core.trauthsc.tam.repository.AuthorizationRepository;
import de.consorsbank.core.trauthsc.tam.repository.ServiceRepository;
import de.consorsbank.core.trauthsc.pvm.PayloadVaultManager;
import de.consorsbank.core.trauthsc.rest.api.tam.initiate.transaction.authorization.model.InitiateTransactionAuthorizationRequest;
import de.consorsbank.core.trauthsc.rest.api.tam.initiate.transaction.authorization.model.InitiateTransactionAuthorizationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class InitiateTransactionAuthorizationService implements InitiateTransactionAuthorization {

    private final AuthorizationRepository authorizationRepository;
    private final ServiceRepository serviceRepository;
    private final InitiateTransactionAuthorizationMapper initiateTransactionAuthorizationMapper;
    private final InitiateTransactionEventSender initiateTransactionEventSender;
    private final PayloadVaultManager payloadVaultManager;
    private final TransactionPayloadProcessorFactory payloadProcessorFactory;

    /**
     * Initiates a new transaction authorization.
     * Saves the transactionServicePayload to PayloadVault,
     * creates a new AuthorizationEntity with status PENDING,
     * persists it, and sends a PENDING event to the tx-queue.
     *
     * @param request the initiate transaction authorization request
     * @param owner   the owner identifier from request header (x-source-service)
     * @return InitiateTransactionAuthorizationResponse containing the authorizationId
     * @throws CommonException if authorization already exists or is in terminal state
     */
    @Override
    @Transactional(rollbackFor = CommonException.class)
    public InitiateTransactionAuthorizationResponse initiateTransactionAuthorization(
            InitiateTransactionAuthorizationRequest request,
            String owner) {

        log.info("Initiating authorization for transactionId {}.", request.getTransactionId());

        // 1. Save payload to PayloadVault
        var payloadId = savePayloadToVault(request, owner);

        // 2. Get or create authorization entity
        var authorizationEntity = getOrCreateAuthorizationEntity(request, owner, payloadId);

        // 3. Persist
        var savedEntity = authorizationRepository.save(authorizationEntity);

        log.info("Authorization having transactionId {} was initiated.", request.getTransactionId());

        // 4. Map to response
        var response = initiateTransactionAuthorizationMapper.toResponse(savedEntity);

        // 5. Send PENDING event to queue
        initiateTransactionEventSender.sendMessage(
                savedEntity.getTransactionId(),
                AuthorizationStatusEnum.PENDING);

        return response;
    }

    /**
     * Saves the transactionServicePayload to PayloadVault.
     * Uses the appropriate TransactionPayloadProcessor based on transactionService.
     *
     * @return the saved payloadVaultId (stored as transactionPayloadId on entity)
     */
    private String savePayloadToVault(
            InitiateTransactionAuthorizationRequest request,
            String owner) {

        var processor = payloadProcessorFactory.getProcessor(request.getTransactionService());
        var payloadVaultRequest = processor.process(
                request.getTransactionServicePayload(),
                request.getExpiresAt());

        var payloadVaultResponse = payloadVaultManager.savePayload(payloadVaultRequest, owner);

        log.debug("Payload saved to vault with id {} for transactionId {}.",
                payloadVaultResponse.getPayloadId(), request.getTransactionId());

        return payloadVaultResponse.getPayloadId();
    }

    /**
     * Get AuthorizationEntity by transactionId or create a new one if it does not exist.
     *
     * - PENDING exists           → throw already initiated
     * - AUTHORIZED/FAILED/EXPIRED → throw terminal state
     * - None exists              → create fresh PENDING entity
     */
    private AuthorizationEntity getOrCreateAuthorizationEntity(
            InitiateTransactionAuthorizationRequest request,
            String owner,
            String payloadId) {

        UUID transactionId = request.getTransactionId();

        if (transactionId != null) {
            for (AuthorizationEntity entity :
                    authorizationRepository.findAllByTransactionId(transactionId)) {

                if (entity.getStatus().equals(AuthorizationStatusEnum.PENDING)) {
                    log.error("Authorization for transactionId {} was already initiated.",
                            transactionId);
                    throw new CommonException(
                            CommonExceptionCode.SERVER_ERROR,
                            List.of("Transaction " + transactionId + " was already initiated."));
                }

                if (entity.getStatus().equals(AuthorizationStatusEnum.AUTHORIZED)
                        || entity.getStatus().equals(AuthorizationStatusEnum.FAILED)
                        || entity.getStatus().equals(AuthorizationStatusEnum.EXPIRED)) {
                    log.error("Authorization for transactionId {} is in terminal state {}.",
                            transactionId, entity.getStatus());
                    throw new CommonException(
                            CommonExceptionCode.SERVER_ERROR,
                            List.of(transactionId.toString()));
                }
            }
        }

        return createPendingAuthorizationEntity(request, owner, payloadId);
    }

    /**
     * Creates a new PENDING AuthorizationEntity from the request.
     * Resolves ServiceEntity via OWNER + SERVICE + SERVICE_VERSION composite key.
     * Sets transactionPayloadId from the saved PayloadVault response.
     */
    private AuthorizationEntity createPendingAuthorizationEntity(
            InitiateTransactionAuthorizationRequest request,
            String owner,
            String payloadId) {

        var authorizationEntity = initiateTransactionAuthorizationMapper
                .toEntity(request, OffsetDateTime.now(), owner);

        // Override transactionPayloadId with the actual saved vault id
        authorizationEntity.setTransactionPayloadId(payloadId);

        // Resolve ServiceEntity — provides OWNER + SERVICE + SERVICE_VERSION columns
        var serviceId = new ServiceId();
        serviceId.setOwner(owner);
        serviceId.setService(request.getTransactionService());
        serviceId.setServiceVersion(request.getTransactionTypeVersion());

        var serviceEntity = serviceRepository.findById(serviceId)
                .orElseThrow(() -> {
                    log.error("No service found for owner {}, service {}, version {}.",
                            owner, request.getTransactionService(),
                            request.getTransactionTypeVersion());
                    return new CommonException(
                            CommonExceptionCode.SERVER_ERROR,
                            List.of(request.getTransactionService()));
                });

        authorizationEntity.setServiceEntity(serviceEntity);
        return authorizationEntity;
    }

    /**
     * Validates if the provided transaction id already exists.
     * If it does, then an exception is raised.
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
```


```

package de.consorsbank.core.trauthsc.tam.core.initiatetransaction.payload;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.consorsbank.core.trauthsc.rest.api.pvm.model.PayloadVaultRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Generic JSON payload processor.
 * Validates that the transactionServicePayload is valid JSON
 * and wraps it into a PayloadVaultRequest for storage.
 *
 * Works for any service — no service-specific logic here.
 * New services integrating initiateTransactionAuthorization
 * just need to send a valid JSON string as transactionServicePayload.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class JsonTransactionPayloadProcessor {

    private final ObjectMapper objectMapper;

    /**
     * Processes the raw JSON payload string from the request
     * into a PayloadVaultRequest ready to be saved.
     *
     * Validates that the payload is valid JSON,
     * normalizes it, and wraps it with the expiration timestamp.
     *
     * @param payloadJson the raw JSON string from transactionServicePayload
     * @param expiresAt   expiration timestamp from the request
     * @return PayloadVaultRequest with normalized JSON payload
     * @throws CommonException if the payload is not valid JSON
     */
    public PayloadVaultRequest process(String payloadJson, OffsetDateTime expiresAt) {
        try {
            // Validate JSON by parsing — throws if invalid
            JsonNode jsonNode = objectMapper.readTree(payloadJson);

            // Normalize — re-serialize to ensure consistent format
            String normalizedJson = objectMapper.writeValueAsString(jsonNode);

            log.debug("Successfully processed JSON payload for PayloadVault storage.");

            return new PayloadVaultRequest(expiresAt, normalizedJson);

        } catch (Exception e) {
            log.error("Failed to process JSON payload: {}", e.getMessage());
            throw new CommonException(
                    CommonExceptionCode.SERVER_ERROR,
                    List.of("Invalid JSON payload: " + e.getMessage()));
        }
    }
}
```



```
package de.consorsbank.core.trauthsc.tam.core.initiatetransaction.service;

import com.consorsbank.common.error.handling.exception.CommonException;
import com.consorsbank.common.error.handling.exception.CommonExceptionCode;
import de.consorsbank.core.trauthsc.tam.core.initiatetransaction.mapper.InitiateTransactionAuthorizationMapper;
import de.consorsbank.core.trauthsc.tam.core.initiatetransaction.messaging.InitiateTransactionEventSender;
import de.consorsbank.core.trauthsc.tam.core.initiatetransaction.payload.JsonTransactionPayloadProcessor;
import de.consorsbank.core.trauthsc.tam.entity.AuthorizationEntity;
import de.consorsbank.core.trauthsc.tam.entity.ids.ServiceId;
import de.consorsbank.core.trauthsc.tam.entity.enums.AuthorizationStatusEnum;
import de.consorsbank.core.trauthsc.tam.repository.AuthorizationRepository;
import de.consorsbank.core.trauthsc.tam.repository.ServiceRepository;
import de.consorsbank.core.trauthsc.pvm.PayloadVaultManager;
import de.consorsbank.core.trauthsc.rest.api.tam.initiate.transaction.authorization.model.InitiateTransactionAuthorizationRequest;
import de.consorsbank.core.trauthsc.rest.api.tam.initiate.transaction.authorization.model.InitiateTransactionAuthorizationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class InitiateTransactionAuthorizationService implements InitiateTransactionAuthorization {

    private final AuthorizationRepository authorizationRepository;
    private final ServiceRepository serviceRepository;
    private final InitiateTransactionAuthorizationMapper initiateTransactionAuthorizationMapper;
    private final InitiateTransactionEventSender initiateTransactionEventSender;
    private final PayloadVaultManager payloadVaultManager;
    private final JsonTransactionPayloadProcessor jsonPayloadProcessor;

    /**
     * Initiates a new transaction authorization.
     *
     * <p>Flow:
     * 1. Save transactionServicePayload to PayloadVault as JSON
     * 2. Validate no authorization already exists for this transactionId
     * 3. Create new PENDING AuthorizationEntity
     * 4. Persist entity
     * 5. Send PENDING event to tx-queue
     *
     * @param request the initiate transaction authorization request containing
     *                transactionId, service, payload, tenant and expiration details
     * @param owner   the owner identifier from request header (x-source-service)
     * @return InitiateTransactionAuthorizationResponse containing the authorizationId
     * @throws CommonException if authorization already exists or is in terminal state
     */
    @Override
    @Transactional(rollbackFor = CommonException.class)
    public InitiateTransactionAuthorizationResponse initiateTransactionAuthorization(
            InitiateTransactionAuthorizationRequest request,
            String owner) {

        log.info("Initiating authorization for transactionId {}.", request.getTransactionId());

        // 1. Save payload to PayloadVault — returns the vault payloadId
        var payloadId = savePayloadToVault(request, owner);

        // 2. Get existing or create new authorization entity
        var authorizationEntity = getOrCreateAuthorizationEntity(request, owner, payloadId);

        // 3. Persist
        var savedEntity = authorizationRepository.save(authorizationEntity);

        log.info("Authorization having transactionId {} was initiated.", request.getTransactionId());

        // 4. Map to response
        var response = initiateTransactionAuthorizationMapper.toResponse(savedEntity);

        // 5. Send PENDING event to queue with transactionId + status
        initiateTransactionEventSender.sendMessage(
                savedEntity.getTransactionId(),
                AuthorizationStatusEnum.PENDING);

        return response;
    }

    /**
     * Saves the transactionServicePayload to PayloadVault as normalized JSON.
     * Generic — works for any service sending a valid JSON payload.
     *
     * @param request the initiate request containing the payload and expiry
     * @param owner   the owner identifier for PayloadVault storage
     * @return the payloadId returned from PayloadVault
     */
    private String savePayloadToVault(
            InitiateTransactionAuthorizationRequest request,
            String owner) {

        var payloadVaultRequest = jsonPayloadProcessor.process(
                request.getTransactionServicePayload(),
                request.getExpiresAt());

        var payloadVaultResponse = payloadVaultManager.savePayload(payloadVaultRequest, owner);

        log.debug("Payload saved to vault with id {} for transactionId {}.",
                payloadVaultResponse.getPayloadId(), request.getTransactionId());

        return payloadVaultResponse.getPayloadId();
    }

    /**
     * Get AuthorizationEntity by transactionId or create a new one if it does not exist.
     *
     * <p>
     * - PENDING exists              → throw already initiated (requirement 1)
     * - AUTHORIZED/FAILED/EXPIRED   → throw terminal state
     * - None exists                 → create fresh PENDING entity
     *
     * @param request   the initiate request
     * @param owner     the owner from request header
     * @param payloadId the saved PayloadVault id
     * @return AuthorizationEntity ready to be saved
     */
    private AuthorizationEntity getOrCreateAuthorizationEntity(
            InitiateTransactionAuthorizationRequest request,
            String owner,
            String payloadId) {

        UUID transactionId = request.getTransactionId();

        if (transactionId != null) {
            for (AuthorizationEntity entity :
                    authorizationRepository.findAllByTransactionId(transactionId)) {

                // Requirement 1: already initiated
                if (entity.getStatus().equals(AuthorizationStatusEnum.PENDING)) {
                    log.error("Authorization for transactionId {} was already initiated.",
                            transactionId);
                    throw new CommonException(
                            CommonExceptionCode.SERVER_ERROR,
                            List.of("Transaction " + transactionId + " was already initiated."));
                }

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
            }
        }

        return createPendingAuthorizationEntity(request, owner, payloadId);
    }

    /**
     * Creates a new PENDING AuthorizationEntity from the request.
     *
     * <p>Resolves ServiceEntity via OWNER + SERVICE + SERVICE_VERSION composite key.
     * Sets transactionPayloadId from the saved PayloadVault id.
     * id and externalId are generated by the mapper (requirement 2).
     *
     * @param request   the initiate request
     * @param owner     the owner from request header — maps to OWNER column
     * @param payloadId the saved PayloadVault id — maps to TRANSACTION_PAYLOAD_ID column
     * @return fully populated AuthorizationEntity ready to persist
     */
    private AuthorizationEntity createPendingAuthorizationEntity(
            InitiateTransactionAuthorizationRequest request,
            String owner,
            String payloadId) {

        // Map basic fields — id, externalId generated in mapper (requirement 2)
        // createdAt, updatedAt, createdBy, updatedBy, isDeleted, version set in mapper
        var authorizationEntity = initiateTransactionAuthorizationMapper
                .toEntity(request, OffsetDateTime.now(), owner);

        // Override transactionPayloadId with the actual saved vault id
        authorizationEntity.setTransactionPayloadId(payloadId);

        // Resolve ServiceEntity — provides OWNER + SERVICE + SERVICE_VERSION FK columns
        var serviceId = new ServiceId();
        serviceId.setOwner(owner);
        serviceId.setService(request.getTransactionService());
        serviceId.setServiceVersion(request.getTransactionTypeVersion());

        var serviceEntity = serviceRepository.findById(serviceId)
                .orElseThrow(() -> {
                    log.error("No service found for owner {}, service {}, version {}.",
                            owner, request.getTransactionService(),
                            request.getTransactionTypeVersion());
                    return new CommonException(
                            CommonExceptionCode.SERVER_ERROR,
                            List.of(request.getTransactionService()));
                });

        authorizationEntity.setServiceEntity(serviceEntity);

        return authorizationEntity;
    }

    /**
     * Validates if the provided transaction id already exists.
     * If it does, then an exception is raised.
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
```
