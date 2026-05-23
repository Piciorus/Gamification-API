package de.consorsbank.core.trauthsc.tam.core.initiatetransaction;

import de.consorsbank.core.trauthsc.tam.core.initiatetransaction.mapper.InitiateTransactionAuthorizationMapper;
import de.consorsbank.core.trauthsc.tam.entity.AuthorizationEntity;
import de.consorsbank.core.trauthsc.tam.repository.AuthorizationRepository;
import de.consorsbank.core.trauthsc.rest.api.tam.initiate.transaction.authorization.model.InitiateTransactionAuthorizationRequest;
import de.consorsbank.core.trauthsc.rest.api.tam.initiate.transaction.authorization.model.InitiateTransactionAuthorizationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class InitiateTransactionAuthorizationService implements InitiateTransactionAuthorization {

    private final AuthorizationRepository authorizationRepository;
    private final InitiateTransactionAuthorizationMapper initiateTransactionAuthorizationMapper;

    @Override
    @Transactional(rollbackOn = CommonException.class)
    public InitiateTransactionAuthorizationResponse initiateTransactionAuthorization(
            InitiateTransactionAuthorizationRequest initiateTransactionAuthorizationRequest) {

        log.info("Initiating authorization for transactionId {}.",
                initiateTransactionAuthorizationRequest.getTransactionId());

        // 1. Validate that no authorization already exists for this transactionId
        validateAlreadyExistingTransaction(initiateTransactionAuthorizationRequest.getTransactionId());

        // 2. Map request DTO -> entity (mapper now handles all field assignments correctly)
        var authorizationEntity = initiateTransactionAuthorizationMapper
                .toEntity(initiateTransactionAuthorizationRequest);

        // 3. Persist
        var saved = authorizationRepository.save(authorizationEntity);

        log.info("Authorization having transactionId {} was initiated.",
                initiateTransactionAuthorizationRequest.getTransactionId());

        // 4. Return response with the generated authorizationId
        return initiateTransactionAuthorizationMapper.toResponse(saved);
    }

    /**
     * Validates if the provided transaction id already exists.
     * If it does, then an exception is raised.
     *
     * @param transactionId -- the transaction id to validate
     */
    private void validateAlreadyExistingTransaction(UUID transactionId) {
        var existingAuthorizations = authorizationRepository.findByTransactionId(transactionId);

        if (existingAuthorizations.isPresent()) {
            log.error("An exception was raised while initiating authorization for transactionId {}. " +
                    "Authorization already exists.", transactionId);
            throw new CommonException(CommonExceptionCode.SERVER_ERROR,
                    List.of(transactionId.toString()));
        }
    }
}


package de.consorsbank.core.trauthsc.tam.core.initiatetransaction.mapper;

import de.consorsbank.core.trauthsc.tam.entity.AuthorizationEntity;
import de.consorsbank.core.trauthsc.tam.entity.enums.AuthorizationStatusEnum;
import de.consorsbank.core.trauthsc.tam.entity.enums.TenantEnum;
import de.consorsbank.core.trauthsc.rest.api.tam.initiate.transaction.authorization.model.InitiateTransactionAuthorizationRequest;
import de.consorsbank.core.trauthsc.rest.api.tam.initiate.transaction.authorization.model.InitiateTransactionAuthorizationResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Collections;

@Mapper(componentModel = "spring")
public interface InitiateTransactionAuthorizationMapper {

    @Mapping(target = "id",         expression = "java(java.util.UUID.randomUUID())")
    @Mapping(target = "externalId", expression = "java(java.util.UUID.randomUUID())")

    // Direct field mappings from request DTO
    @Mapping(target = "transactionId",         source = "transactionId")
    @Mapping(target = "service",               source = "transactionService")
    @Mapping(target = "serviceVersion",        source = "transactionTypeVersion")
    @Mapping(target = "tenant",                source = "tenant", qualifiedByName = "mapTenant")
    @Mapping(target = "crmCustomerNumber",     source = "crmCustomerNumber")
    @Mapping(target = "expiresAt",             source = "expiresAt")

    // transactionPayloadId comes from the request, NOT generated randomly
    @Mapping(target = "transactionPayloadId",  source = "transactionServicePayload")

    // Status is always PENDING on creation
    @Mapping(target = "status",
             expression = "java(de.consorsbank.core.trauthsc.tam.entity.enums.AuthorizationStatusEnum.PENDING)")

    // serviceEntity is resolved separately (lookup by service+version), ignore here
    @Mapping(target = "serviceEntity", ignore = true)

    // Audit / soft-delete fields – managed by BaseAuditEntity, ignore in mapper
    @Mapping(target = "isDeleted",  ignore = true)
    @Mapping(target = "createdBy",  ignore = true)
    @Mapping(target = "updatedBy",  ignore = true)
    @Mapping(target = "deletedBy",  ignore = true)
    @Mapping(target = "createdAt",  ignore = true)
    @Mapping(target = "updatedAt",  ignore = true)
    @Mapping(target = "deletedAt",  ignore = true)
    @Mapping(target = "version",    ignore = true)
    AuthorizationEntity toEntity(InitiateTransactionAuthorizationRequest request);

    @Mapping(target = "authorizationId", source = "id")
    InitiateTransactionAuthorizationResponse toResponse(AuthorizationEntity authorization);

    /**
     * Maps the API-level TenantEnum (B2_B / B2_C) coming from the generated
     * OpenAPI model to the internal TenantEnum stored on the entity.
     */
    @Named("mapTenant")
    default TenantEnum mapTenant(
            de.consorsbank.core.trauthsc.rest.api.tam.initiate.transaction.authorization.model
                    .InitiateTransactionAuthorizationRequest.TenantEnum tenantEnum) {

        if (tenantEnum == null) return null;

        return switch (tenantEnum.getValue()) {
            case "B2B" -> TenantEnum.B2B;
            case "B2C" -> TenantEnum.B2C;
            default    -> throw new CommonException(
                    CommonExceptionCode.SERVER_ERROR,
                    Collections.singletonList("Unknown tenant: " + tenantEnum));
        };
    }
}
