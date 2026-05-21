```
package de.consorsbank.core.trauthsc.tam.core.initiatetransaction.mapper;

import de.consorsbank.core.trauthsc.rest.api.tam.initiate.transaction.authorization.model.InitiateTransactionAuthorizationRequest;
import de.consorsbank.core.trauthsc.rest.api.tam.initiate.transaction.authorization.model.InitiateTransactionAuthorizationResponse;
import de.consorsbank.core.trauthsc.tam.entity.AuthorizationEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface InitiateTransactionAuthorizationMapper {

    @Mapping(target = "id", 
             expression = "java(java.util.UUID.randomUUID())")
    @Mapping(target = "externalId", 
             expression = "java(java.util.UUID.randomUUID())")
    @Mapping(target = "transactionId", 
             source = "transactionId")
    @Mapping(target = "service", 
             source = "transactionService")
    @Mapping(target = "serviceVersion", 
             source = "transactionTypeVersion")
    @Mapping(target = "status", 
             expression = "java(de.consorsbank.core.trauthsc.tam.entity.enums.AuthorizationStatusEnum.WAITING_FOR_METHOD)")
    @Mapping(target = "tenant",
             source = "tenant")
    @Mapping(target = "crmCustomerNumber",
             source = "crmCustomerNumber")
    @Mapping(target = "expiresAt",
             source = "expiresAt")
    @Mapping(target = "transactionPayloadId",
             expression = "java(java.util.UUID.randomUUID())")
    @Mapping(target = "isDeleted",
             constant = "0")
    @Mapping(target = "serviceEntity",
             ignore = true)
    AuthorizationEntity toEntity(
        InitiateTransactionAuthorizationRequest request);

    @Mapping(target = "authorizationId", 
             source = "id")
    InitiateTransactionAuthorizationResponse toResponse(
        AuthorizationEntity authorization);
}

```
```
package de.consorsbank.core.trauthsc.tam.core.initiatetransaction.service;

import de.consorsbank.core.trauthsc.rest.api.tam.initiate.transaction.authorization.model.InitiateTransactionAuthorizationRequest;
import de.consorsbank.core.trauthsc.rest.api.tam.initiate.transaction.authorization.model.InitiateTransactionAuthorizationResponse;
import de.consorsbank.core.trauthsc.tam.core.initiatetransaction.mapper.InitiateTransactionAuthorizationMapper;
import de.consorsbank.core.trauthsc.tam.entity.AuthorizationEntity;
import de.consorsbank.core.trauthsc.tam.repository.AuthorizationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class InitiateTransactionAuthorizationService {

    private final AuthorizationRepository authorizationRepository;
    private final InitiateTransactionAuthorizationMapper mapper;

    @Transactional
    public InitiateTransactionAuthorizationResponse initiateAuthorization(
            InitiateTransactionAuthorizationRequest request) {

        log.info("Initiating authorization for transactionId {}.",
                request.getTransactionId());

        // Validare - daca exista deja o autorizare pentru acest transaction
        validateAlreadyExistingTransaction(request.getTransactionId());

        // Creare entitate
        AuthorizationEntity authorizationEntity = 
                mapper.toEntity(request);

        // Salvare
        AuthorizationEntity saved = 
                authorizationRepository.save(authorizationEntity);

        log.info("Authorization having transactionId {} was initiated.",
                request.getTransactionId());

        return mapper.toResponse(saved);
    }

    /**
     * Validates if the provided transaction id already exists.
     * If it does, then an exception is raised.
     *
     * @param transactionId -- the transaction id to validate
     * @throws InitiateTransactionAuthorizationException -- thrown in case 
     *         there is already an existing authorization for the given tx id
     */
    private void validateAlreadyExistingTransaction(UUID transactionId) {
        List<AuthorizationEntity> existingAuthorizations =
                authorizationRepository
                        .findByTransactionId(transactionId);

        if (!existingAuthorizations.isEmpty()) {
            log.error(
                "An exception was raised while initiating " +
                "authorization for transactionId {}. " +
                "Authorization already exists.",
                transactionId);

            throw new InitiateTransactionAuthorizationException(
                    InitiateTransactionAuthorizationErrorType
                            .AUTHORIZATION_ALREADY_EXISTS,
                    List.of(transactionId.toString()));
        }
    }
}

```

```
package de.consorsbank.core.trauthsc.tam.core.initiatetransaction.exception;

public enum InitiateTransactionAuthorizationErrorType {
    AUTHORIZATION_ALREADY_EXISTS,
    INVALID_REQUEST,
    SERVICE_NOT_FOUND
}

```

```
package de.consorsbank.core.trauthsc.tam.core.initiatetransaction.exception;

import java.util.List;

public class InitiateTransactionAuthorizationException 
        extends RuntimeException {

    private final InitiateTransactionAuthorizationErrorType errorType;
    private final List<String> arguments;

    public InitiateTransactionAuthorizationException(
            InitiateTransactionAuthorizationErrorType errorType,
            List<String> arguments) {
        super(errorType.name());
        this.errorType = errorType;
        this.arguments = arguments;
    }

    public InitiateTransactionAuthorizationErrorType getErrorType() {
        return errorType;
    }

    public String getLogMessage() {
        return errorType.name() + " - arguments: " + arguments;
    }
}

```


```
package de.consorsbank.core.trauthsc.tam.repository;

import de.consorsbank.core.trauthsc.tam.entity.AuthorizationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AuthorizationRepository 
        extends JpaRepository<AuthorizationEntity, UUID> {

    List<AuthorizationEntity> findByTransactionId(UUID transactionId);
}

```

```
package de.consorsbank.core.trauthsc.tam.core.initiatetransaction.controller;

import de.consorsbank.core.trauthsc.rest.api.tam.initiate.transaction.authorization.model.InitiateTransactionAuthorizationRequest;
import de.consorsbank.core.trauthsc.rest.api.tam.initiate.transaction.authorization.model.InitiateTransactionAuthorizationResponse;
import de.consorsbank.core.trauthsc.tam.core.initiatetransaction.service.InitiateTransactionAuthorizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/v1/authorizations")
@RequiredArgsConstructor
@Slf4j
public class InitiateTransactionAuthorizationController {

    private final InitiateTransactionAuthorizationService service;

    @PostMapping
    public ResponseEntity<InitiateTransactionAuthorizationResponse>
            initiateAuthorization(
                @RequestBody 
                InitiateTransactionAuthorizationRequest request) {

        log.info("Received initiate authorization request " +
                 "for transactionId {}.",
                 request.getTransactionId());

        InitiateTransactionAuthorizationResponse response =
                service.initiateAuthorization(request);

        return ResponseEntity
                .created(URI.create(
                    "/v1/authorizations/" + 
                    response.getAuthorizationId()))
                .body(response);
    }
}

```

```



```

