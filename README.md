```

databaseChangeLog:
  - changeSet:
      id: 007-insert-authorization-attempts-data
      author: vlad.pop@externe.bnpparibas.com
      changes:
        - insert:
            schemaName: tam
            tableName: authorization_attempts
            columns:
              - column:
                  name: id
                  valueComputed: HEXTORAW(REPLACE('c3d4e5f6-a7b8-c9d0-e1f2-a3b4c5d6a7b8', '-', ''))
              - column:
                  name: external_id
                  valueComputed: HEXTORAW(REPLACE('33445566-7788-9900-1122-aabbccddeeff', '-', ''))
              - column:
                  name: authorization_id
                  valueComputed: HEXTORAW(REPLACE('a1b2c3d4-e5f6-a7b8-c9d0-e1f2a3b4c5d6', '-', ''))
              - column:
                  name: authorization_method
                  value: 'TAN_FROM_GENERATOR'
              - column:
                  name: authorization_credential
                  value: '123456'
              - column:
                  name: status
                  value: 'PENDING'
              - column:
                  name: is_deleted
                  valueNumeric: 0
              - column:
                  name: created_by
                  value: 'system'
              - column:
                  name: updated_by
                  value: 'system'
        - insert:
            schemaName: tam
            tableName: authorization_attempts
            columns:
              - column:
                  name: id
                  valueComputed: HEXTORAW(REPLACE('d4e5f6a7-b8c9-d0e1-f2a3-b4c5d6a7b8c9', '-', ''))
              - column:
                  name: external_id
                  valueComputed: HEXTORAW(REPLACE('44556677-8899-0011-2233-aabbccddeeff', '-', ''))
              - column:
                  name: authorization_id
                  valueComputed: HEXTORAW(REPLACE('a1b2c3d4-e5f6-a7b8-c9d0-e1f2a3b4c5d6', '-', ''))
              - column:
                  name: authorization_method
                  value: 'PUSH_NOTIFICATION_FORM_NEO_APP'
              - column:
                  name: status
                  value: 'FAILED'
              - column:
                  name: is_deleted
                  valueNumeric: 0
              - column:
                  name: created_by
                  value: 'system'
              - column:
                  name: updated_by
                  value: 'system'
        - insert:
            schemaName: tam
            tableName: authorization_attempts
            columns:
              - column:
                  name: id
                  valueComputed: HEXTORAW(REPLACE('e5f6a7b8-c9d0-e1f2-a3b4-c5d6a7b8c9d0', '-', ''))
              - column:
                  name: external_id
                  valueComputed: HEXTORAW(REPLACE('55667788-9900-1122-3344-aabbccddeeff', '-', ''))
              - column:
                  name: authorization_id
                  valueComputed: HEXTORAW(REPLACE('b2c3d4e5-f6a7-b8c9-d0e1-f2a3b4c5d6a7', '-', ''))
              - column:
                  name: authorization_method
                  value: 'NEO_SECURE_SIGNATURE_BOUND'
              - column:
                  name: authorization_credential
                  value: 'sig-credential'
              - column:
                  name: status
                  value: 'AUTHORIZED'
              - column:
                  name: is_deleted
                  valueNumeric: 0
              - column:
                  name: created_by
                  value: 'system'
              - column:
                  name: updated_by
                  value: 'system'

```

```
databaseChangeLog:
  - changeSet:
      id: 006-insert-authorizations-data
      author: vlad.pop@externe.bnpparibas.com
      changes:
        - insert:
            schemaName: tam
            tableName: authorizations
            columns:
              - column:
                  name: id
                  valueComputed: HEXTORAW(REPLACE('a1b2c3d4-e5f6-a7b8-c9d0-e1f2a3b4c5d6', '-', ''))
              - column:
                  name: external_id
                  valueComputed: HEXTORAW(REPLACE('11223344-5566-7788-9900-aabbccddeeff', '-', ''))
              - column:
                  name: transaction_id
                  valueComputed: HEXTORAW(REPLACE('ffeeddcc-bbaa-0099-8877-665544332211', '-', ''))
              - column:
                  name: owner
                  value: 'consorsbank'
              - column:
                  name: service
                  value: 'payment-service'
              - column:
                  name: service_version
                  value: 'v1'
              - column:
                  name: expires_at
                  valueComputed: "CURRENT_TIMESTAMP + INTERVAL '5' MINUTE"
              - column:
                  name: status
                  value: 'PENDING'
              - column:
                  name: tenant
                  value: 'B2C'
              - column:
                  name: crm_customer_number
                  value: '1234567890'
              - column:
                  name: transaction_payload_id
                  valueComputed: HEXTORAW(REPLACE('aabbccdd-1122-3344-aabb-ccdd11223344', '-', ''))
              - column:
                  name: is_deleted
                  valueNumeric: 0
              - column:
                  name: created_by
                  value: 'system'
              - column:
                  name: updated_by
                  value: 'system'
        - insert:
            schemaName: tam
            tableName: authorizations
            columns:
              - column:
                  name: id
                  valueComputed: HEXTORAW(REPLACE('b2c3d4e5-f6a7-b8c9-d0e1-f2a3b4c5d6a7', '-', ''))
              - column:
                  name: external_id
                  valueComputed: HEXTORAW(REPLACE('22334455-6677-8899-0011-aabbccddeeff', '-', ''))
              - column:
                  name: transaction_id
                  valueComputed: HEXTORAW(REPLACE('eeddccbb-aa00-9988-7766-5544332211ff', '-', ''))
              - column:
                  name: owner
                  value: 'consorsbank'
              - column:
                  name: service
                  value: 'transfer-service'
              - column:
                  name: service_version
                  value: 'v1'
              - column:
                  name: expires_at
                  valueComputed: "CURRENT_TIMESTAMP + INTERVAL '10' MINUTE"
              - column:
                  name: status
                  value: 'AUTHORIZED'
              - column:
                  name: tenant
                  value: 'B2B'
              - column:
                  name: crm_customer_number
                  value: '9876543210'
              - column:
                  name: transaction_payload_id
                  valueComputed: HEXTORAW(REPLACE('bbccddee-2233-4455-bbcc-ddee22334455', '-', ''))
              - column:
                  name: is_deleted
                  valueNumeric: 0
              - column:
                  name: created_by
                  value: 'system'
              - column:
                  name: updated_by
                  value: 'system'

```


```
package de.consorsbank.core.trauthsc.tam.core.authorizationstatus.repository;

import de.consorsbank.core.trauthsc.tam.entity.AuthorizationAttemptEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AuthorizationAttemptRepository extends JpaRepository<AuthorizationAttemptEntity, UUID> {

    @Query("SELECT a FROM AuthorizationAttemptEntity a " +
           "LEFT JOIN FETCH a.authorizationMethodEntity " +
           "WHERE a.authorizationEntity.id = :authorizationId")
    List<AuthorizationAttemptEntity> findByAuthorizationIdWithMethod(@Param("authorizationId") UUID authorizationId);
}
```


```
package de.consorsbank.core.trauthsc.tam.core.authorizationstatus.mapper;

import de.consorsbank.core.trauthsc.rest.api.tam.get.authorization.status.model.AttemptsDetail;
import de.consorsbank.core.trauthsc.rest.api.tam.get.authorization.status.model.DetailedAuthorizationStatusResponse;
import de.consorsbank.core.trauthsc.rest.api.tam.get.authorization.status.model.SimpleAuthorizationStatusResponse;
import de.consorsbank.core.trauthsc.tam.entity.AuthorizationAttemptEntity;
import de.consorsbank.core.trauthsc.tam.entity.AuthorizationEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AuthorizationStatusMapper {

    @Mapping(source = "externalId", target = "authorizationId")
    SimpleAuthorizationStatusResponse toSimpleResponse(AuthorizationEntity entity);

    @Mapping(source = "entity.externalId", target = "authorizationId")
    @Mapping(source = "entity.status", target = "status")
    @Mapping(source = "attempts", target = "items")
    DetailedAuthorizationStatusResponse toDetailedResponse(AuthorizationEntity entity, List<AuthorizationAttemptEntity> attempts);

    @Mapping(source = "externalId", target = "attemptId")
    @Mapping(source = "status", target = "statusAttempt")
    @Mapping(source = "authorizationMethodEntity.name", target = "authorizationMethod")
    AttemptsDetail toAttemptsDetail(AuthorizationAttemptEntity attempt);

    List<AttemptsDetail> toAttemptsDetailList(List<AuthorizationAttemptEntity> attempts);
}

```

```
package de.consorsbank.core.trauthsc.tam.core.authorizationstatus.service;

import de.consorsbank.core.trauthsc.rest.api.tam.get.authorization.status.model.GetAuthorizationStatusResponse;
import de.consorsbank.core.trauthsc.tam.controller.AuthorizationStatus;
import de.consorsbank.core.trauthsc.tam.core.authorizationstatus.mapper.AuthorizationStatusMapper;
import de.consorsbank.core.trauthsc.tam.core.authorizationstatus.repository.AuthorizationAttemptRepository;
import de.consorsbank.core.trauthsc.tam.core.authorizationstatus.repository.AuthorizationRepository;
import de.consorsbank.core.trauthsc.tam.entity.AuthorizationAttemptEntity;
import de.consorsbank.core.trauthsc.tam.entity.AuthorizationEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthorizationStatusServiceImpl implements AuthorizationStatus {

    private final AuthorizationRepository authorizationRepository;
    private final AuthorizationAttemptRepository authorizationAttemptRepository;
    private final AuthorizationStatusMapper authorizationStatusMapper;

    @Override
    @Transactional(readOnly = true)
    public GetAuthorizationStatusResponse getAuthorizationStatus(String authorizationId, boolean detailed) {
        UUID externalId = UUID.fromString(authorizationId);

        AuthorizationEntity authorization = authorizationRepository
                .findByExternalId(externalId)
                .orElseThrow(() -> new RuntimeException(
                        "Authorization not found for id: " + authorizationId));

        if (detailed) {
            List<AuthorizationAttemptEntity> attempts = authorizationAttemptRepository
                    .findByAuthorizationIdWithMethod(authorization.getId());

            log.debug("Found authorization {} with {} attempts", authorizationId, attempts.size());

            return authorizationStatusMapper.toDetailedResponse(authorization, attempts);
        }

        log.debug("Found authorization {} with status {}", authorizationId, authorization.getStatus());

        return authorizationStatusMapper.toSimpleResponse(authorization);
    }
}
```


```
package de.consorsbank.core.trauthsc.tam.core.authorizationstatus.mapper;

import de.consorsbank.core.trauthsc.rest.api.tam.get.authorization.status.model.AttemptsDetail;
import de.consorsbank.core.trauthsc.rest.api.tam.get.authorization.status.model.DetailedAuthorizationStatusResponse;
import de.consorsbank.core.trauthsc.rest.api.tam.get.authorization.status.model.SimpleAuthorizationStatusResponse;
import de.consorsbank.core.trauthsc.tam.entity.AuthorizationAttemptEntity;
import de.consorsbank.core.trauthsc.tam.entity.AuthorizationEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AuthorizationStatusMapper {

    @Mapping(source = "externalId", target = "authorizationId")
    SimpleAuthorizationStatusResponse toSimpleResponse(AuthorizationEntity entity);

    @Mapping(source = "externalId", target = "authorizationId")
    @Mapping(source = "attempts", target = "items")
    DetailedAuthorizationStatusResponse toDetailedResponse(AuthorizationEntity entity);

    @Mapping(source = "externalId", target = "attemptId")
    @Mapping(source = "status", target = "statusAttempt")
    @Mapping(source = "authorizationMethodEntity.name", target = "authorizationMethod")
    AttemptsDetail toAttemptsDetail(AuthorizationAttemptEntity attempt);

    List<AttemptsDetail> toAttemptsDetailList(List<AuthorizationAttemptEntity> attempts);
}

```

```
package de.consorsbank.core.trauthsc.tam.core.authorizationstatus.repository;

import de.consorsbank.core.trauthsc.tam.entity.AuthorizationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AuthorizationRepository extends JpaRepository<AuthorizationEntity, UUID> {

    @Query("SELECT a FROM AuthorizationEntity a " +
           "LEFT JOIN FETCH a.attempts att " +
           "LEFT JOIN FETCH att.authorizationMethodEntity " +
           "WHERE a.externalId = :externalId")
    Optional<AuthorizationEntity> findByExternalIdWithAttempts(@Param("externalId") UUID externalId);

    Optional<AuthorizationEntity> findByExternalId(UUID externalId);
}
```

```
databaseChangeLog:
  - changeSet:
      id: 006-insert-authorizations-data
      author: vlad.pop@externe.bnpparibas.com
      changes:
        - insert:
            schemaName: tam
            tableName: authorizations
            columns:
              - column:
                  name: id
                  valueComputed: HEXTORAW('A1B2C3D4E5F6A7B8C9D0E1F2A3B4C5D6')
              - column:
                  name: external_id
                  valueComputed: HEXTORAW('11223344556677889900AABBCCDDEEFF')
              - column:
                  name: transaction_id
                  valueComputed: HEXTORAW('FFEEDDCCBBAA00998877665544332211')
              - column:
                  name: owner
                  value: 'consorsbank'
              - column:
                  name: service
                  value: 'payment-service'
              - column:
                  name: service_version
                  value: 'v1'
              - column:
                  name: expires_at
                  valueComputed: "CURRENT_TIMESTAMP + INTERVAL '5' MINUTE"
              - column:
                  name: status
                  value: 'PENDING'
              - column:
                  name: tenant
                  value: 'B2C'
              - column:
                  name: crm_customer_number
                  value: '1234567890'
              - column:
                  name: transaction_payload_id
                  valueComputed: HEXTORAW('AABBCCDD11223344AABBCCDD11223344')
              - column:
                  name: is_deleted
                  valueNumeric: 0
              - column:
                  name: created_by
                  value: 'system'
              - column:
                  name: updated_by
                  value: 'system'
        - insert:
            schemaName: tam
            tableName: authorizations
            columns:
              - column:
                  name: id
                  valueComputed: HEXTORAW('B2C3D4E5F6A7B8C9D0E1F2A3B4C5D6A7')
              - column:
                  name: external_id
                  valueComputed: HEXTORAW('22334455667788990011AABBCCDDEEFF')
              - column:
                  name: transaction_id
                  valueComputed: HEXTORAW('EEDDCCBBAA009988776655443322110F')
              - column:
                  name: owner
                  value: 'consorsbank'
              - column:
                  name: service
                  value: 'transfer-service'
              - column:
                  name: service_version
                  value: 'v1'
              - column:
                  name: expires_at
                  valueComputed: "CURRENT_TIMESTAMP + INTERVAL '10' MINUTE"
              - column:
                  name: status
                  value: 'AUTHORIZED'
              - column:
                  name: tenant
                  value: 'B2B'
              - column:
                  name: crm_customer_number
                  value: '9876543210'
              - column:
                  name: transaction_payload_id
                  valueComputed: HEXTORAW('BBCCDDEE22334455BBCCDDEE22334455')
              - column:
                  name: is_deleted
                  valueNumeric: 0
              - column:
                  name: created_by
                  value: 'system'
              - column:
                  name: updated_by
                  value: 'system'
```



```

databaseChangeLog:
  - changeSet:
      id: 007-insert-authorization-attempts-data
      author: vlad.pop@externe.bnpparibas.com
      changes:
        - insert:
            schemaName: tam
            tableName: authorization_attempts
            columns:
              - column:
                  name: id
                  valueComputed: HEXTORAW('C3D4E5F6A7B8C9D0E1F2A3B4C5D6A7B8')
              - column:
                  name: external_id
                  valueComputed: HEXTORAW('33445566778899001122AABBCCDDEEFF')
              - column:
                  name: authorization_id
                  valueComputed: HEXTORAW('A1B2C3D4E5F6A7B8C9D0E1F2A3B4C5D6')
              - column:
                  name: authorization_method
                  value: 'TAN_FROM_GENERATOR'
              - column:
                  name: authorization_credential
                  value: '123456'
              - column:
                  name: status
                  value: 'PENDING'
              - column:
                  name: is_deleted
                  valueNumeric: 0
              - column:
                  name: created_by
                  value: 'system'
              - column:
                  name: updated_by
                  value: 'system'
        - insert:
            schemaName: tam
            tableName: authorization_attempts
            columns:
              - column:
                  name: id
                  valueComputed: HEXTORAW('D4E5F6A7B8C9D0E1F2A3B4C5D6A7B8C9')
              - column:
                  name: external_id
                  valueComputed: HEXTORAW('44556677889900112233AABBCCDDEEFF')
              - column:
                  name: authorization_id
                  valueComputed: HEXTORAW('A1B2C3D4E5F6A7B8C9D0E1F2A3B4C5D6')
              - column:
                  name: authorization_method
                  value: 'PUSH_NOTIFICATION_FORM_NEO_APP'
              - column:
                  name: status
                  value: 'FAILED'
              - column:
                  name: is_deleted
                  valueNumeric: 0
              - column:
                  name: created_by
                  value: 'system'
              - column:
                  name: updated_by
                  value: 'system'
        - insert:
            schemaName: tam
            tableName: authorization_attempts
            columns:
              - column:
                  name: id
                  valueComputed: HEXTORAW('E5F6A7B8C9D0E1F2A3B4C5D6A7B8C9D0')
              - column:
                  name: external_id
                  valueComputed: HEXTORAW('55667788990011223344AABBCCDDEEFF')
              - column:
                  name: authorization_id
                  valueComputed: HEXTORAW('B2C3D4E5F6A7B8C9D0E1F2A3B4C5D6A7')
              - column:
                  name: authorization_method
                  value: 'NEO_SECURE_SIGNATURE_BOUND'
              - column:
                  name: authorization_credential
                  value: 'sig-credential'
              - column:
                  name: status
                  value: 'AUTHORIZED'
              - column:
                  name: is_deleted
                  valueNumeric: 0
              - column:
                  name: created_by
                  value: 'system'
              - column:
                  name: updated_by
                  value: 'system'

```


```
package de.consorsbank.core.trauthsc.rest.api.tam.get.authorization.status.model;

public enum AuthorizationMethod {
    TAN_FROM_GENERATOR,
    TAN_FROM_NEOAPP,
    QRCODE_FROM_GENERATOR,
    PUSH_NOTIFICATION_FORM_NEO_APP,
    NEO_SECURE_SIGNATURE_UNBOUND,
    NEO_SECURE_SIGNATURE_BOUND,
    MTAN
}

```


```
package de.consorsbank.core.trauthsc.rest.api.tam.get.authorization.status.model;

public enum AuthorizationAttemptStatus {
    INITIATED,
    PENDING,
    AUTHORIZED,
    FAILED,
    REJECTED,
    EXPIRED
}

```



```
package de.consorsbank.core.trauthsc.rest.api.tam.get.authorization.status.model;

public enum AuthorizationStatus {
    INITIATED,
    PENDING,
    AUTHORIZED,
    EXPIRED,
    CANCELED
}

```


```
package de.consorsbank.core.trauthsc.rest.api.tam.get.authorization.status.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttemptsDetail {

    private String attemptId;
    private AuthorizationAttemptStatus statusAttempt;
    private AuthorizationMethod authorizationMethod;
}

```



```
package de.consorsbank.core.trauthsc.rest.api.tam.get.authorization.status.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetailedAuthorizationStatusResponse implements GetAuthorizationStatusResponse {

    private AuthorizationStatus status;
    private String authorizationId;
    private List<AttemptsDetail> items;
}

```


```
package de.consorsbank.core.trauthsc.rest.api.tam.get.authorization.status.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimpleAuthorizationStatusResponse implements GetAuthorizationStatusResponse {

    private AuthorizationStatus status;
    private String authorizationId;
}

```


```
package de.consorsbank.core.trauthsc.rest.api.tam.get.authorization.status.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response for GET /v1/authorizations/{authorizationId}/status
 * This is a oneOf: SimpleAuthorizationStatusResponse | DetailedAuthorizationStatusResponse
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public interface GetAuthorizationStatusResponse {
}

```


```
package de.consorsbank.core.trauthsc.tam.core.authorizationstatus.service;

import de.consorsbank.core.trauthsc.rest.api.tam.get.authorization.status.model.GetAuthorizationStatusResponse;
import de.consorsbank.core.trauthsc.tam.controller.AuthorizationStatus;
import de.consorsbank.core.trauthsc.tam.core.authorizationstatus.entity.AuthorizationEntity;
import de.consorsbank.core.trauthsc.tam.core.authorizationstatus.mapper.AuthorizationStatusMapper;
import de.consorsbank.core.trauthsc.tam.core.authorizationstatus.repository.AuthorizationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthorizationStatusServiceImpl implements AuthorizationStatus {

    private final AuthorizationRepository authorizationRepository;
    private final AuthorizationStatusMapper authorizationStatusMapper;

    @Override
    @Transactional(readOnly = true)
    public GetAuthorizationStatusResponse getAuthorizationStatus(String authorizationId, boolean detailed) {
        UUID externalId = UUID.fromString(authorizationId);

        if (detailed) {
            AuthorizationEntity authorization = authorizationRepository
                    .findByExternalIdWithAttempts(externalId)
                    .orElseThrow(() -> new RuntimeException(
                            "Authorization not found for id: " + authorizationId));

            log.debug("Found authorization {} with {} attempts",
                    authorizationId,
                    authorization.getAttempts() != null ? authorization.getAttempts().size() : 0);

            return authorizationStatusMapper.toDetailedResponse(authorization);
        }

        AuthorizationEntity authorization = authorizationRepository
                .findByExternalId(externalId)
                .orElseThrow(() -> new RuntimeException(
                        "Authorization not found for id: " + authorizationId));

        log.debug("Found authorization {} with status {}", authorizationId, authorization.getStatus());

        return authorizationStatusMapper.toSimpleResponse(authorization);
    }
}

```



```
package de.consorsbank.core.trauthsc.tam.core.authorizationstatus.repository;

import de.consorsbank.core.trauthsc.tam.core.authorizationstatus.entity.AuthorizationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AuthorizationRepository extends JpaRepository<AuthorizationEntity, UUID> {

    @Query("SELECT a FROM AuthorizationEntity a LEFT JOIN FETCH a.attempts WHERE a.externalId = :externalId")
    Optional<AuthorizationEntity> findByExternalIdWithAttempts(@Param("externalId") UUID externalId);

    Optional<AuthorizationEntity> findByExternalId(UUID externalId);
}

```



```

package de.consorsbank.core.trauthsc.tam.core.authorizationstatus.mapper;

import de.consorsbank.core.trauthsc.rest.api.tam.get.authorization.status.model.AttemptsDetail;
import de.consorsbank.core.trauthsc.rest.api.tam.get.authorization.status.model.DetailedAuthorizationStatusResponse;
import de.consorsbank.core.trauthsc.rest.api.tam.get.authorization.status.model.SimpleAuthorizationStatusResponse;
import de.consorsbank.core.trauthsc.tam.core.authorizationstatus.entity.AuthorizationAttemptEntity;
import de.consorsbank.core.trauthsc.tam.core.authorizationstatus.entity.AuthorizationEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AuthorizationStatusMapper {

    @Mapping(source = "externalId", target = "authorizationId")
    SimpleAuthorizationStatusResponse toSimpleResponse(AuthorizationEntity entity);

    @Mapping(source = "externalId", target = "authorizationId")
    @Mapping(source = "attempts", target = "items")
    DetailedAuthorizationStatusResponse toDetailedResponse(AuthorizationEntity entity);

    @Mapping(source = "externalId", target = "attemptId")
    @Mapping(source = "status", target = "statusAttempt")
    AttemptsDetail toAttemptsDetail(AuthorizationAttemptEntity attempt);

    List<AttemptsDetail> toAttemptsDetailList(List<AuthorizationAttemptEntity> attempts);
}
```
