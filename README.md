```
package de.consorsbank.core.trauthsc.tam.core.initiatetransaction.mapper;

import de.consorsbank.core.trauthsc.rest.api.tam.initiate.transaction.authorization.model.InitiateTransactionAuthorizationRequest;
import de.consorsbank.core.trauthsc.rest.api.tam.initiate.transaction.authorization.model.InitiateTransactionAuthorizationResponse;
import de.consorsbank.core.trauthsc.rest.api.tam.initiate.transaction.authorization.model.InitiateTransactionAuthorizationRequest.TenantEnum;
import de.consorsbank.core.trauthsc.tam.entity.AuthorizationEntity;
import de.consorsbank.core.trauthsc.tam.entity.enums.AuthorizationStatusEnum;
import de.consorsbank.core.trauthsc.tam.entity.enums.TenantEnum;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface InitiateTransactionAuthorizationMapper {

    @Mapping(
        target = "id",
        expression = "java(java.util.UUID.randomUUID())"
    )
    @Mapping(
        target = "externalId",
        expression = "java(java.util.UUID.randomUUID())"
    )
    @Mapping(
        target = "transactionId",
        source = "transactionId"
    )
    @Mapping(
        target = "service",
        source = "transactionService"
    )
    @Mapping(
        target = "serviceVersion",
        source = "transactionTypeVersion"
    )
    @Mapping(
        target = "status",
        expression = "java(de.consorsbank.core.trauthsc.tam.entity.enums.AuthorizationStatusEnum.PENDING)"
    )
    @Mapping(
        target = "tenant",
        source = "tenant",
        qualifiedByName = "mapTenant"
    )
    @Mapping(
        target = "crmCustomerNumber",
        source = "crmCustomerNumber"
    )
    @Mapping(
        target = "expiresAt",
        source = "expiresAt"
    )
    @Mapping(
        target = "transactionPayloadId",
        expression = "java(java.util.UUID.randomUUID().toString())"
    )
    // BaseAuditEntity fields
    @Mapping(
        target = "isDeleted",
        constant = "false"
    )
    @Mapping(
        target = "createdBy",
        constant = "SYSTEM"
    )
    @Mapping(
        target = "updatedBy",
        constant = "SYSTEM"
    )
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    // ServiceEntity nu se seteaza din request
    @Mapping(target = "serviceEntity", ignore = true)
    AuthorizationEntity toEntity(
        InitiateTransactionAuthorizationRequest request);

    @Mapping(
        target = "authorizationId",
        source = "id"
    )
    InitiateTransactionAuthorizationResponse toResponse(
        AuthorizationEntity authorization);

    /**
     * Maps TenantEnum from request model to entity TenantEnum
     * Request has: B2_B("B2B"), B2_C("B2C")
     * Entity has: B2B, B2C
     */
    @Named("mapTenant")
    default de.consorsbank.core.trauthsc.tam.entity.enums.TenantEnum
            mapTenant(
        de.consorsbank.core.trauthsc.rest.api.tam.initiate.transaction
            .authorization.model.InitiateTransactionAuthorizationRequest
            .TenantEnum tenantEnum) {

        if (tenantEnum == null) return null;

        return switch (tenantEnum.getValue()) {
            case "B2B" -> de.consorsbank.core.trauthsc.tam.entity
                            .enums.TenantEnum.B2B;
            case "B2C" -> de.consorsbank.core.trauthsc.tam.entity
                            .enums.TenantEnum.B2C;
            default -> throw new IllegalArgumentException(
                "Unknown tenant: " + tenantEnum.getValue());
        };
    }
}
```
