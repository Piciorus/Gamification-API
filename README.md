```
package de.consorsbank.core.trauthsc.tam.core.authorizationattemptstatus.service;

import de.consorsbank.core.trauthsc.rest.api.tam.get.authorization.attempt.status.model.GetAuthorizationAttemptStatusResponse;
import de.consorsbank.core.trauthsc.tam.controller.AuthorizationAttemptStatusApi;
import de.consorsbank.core.trauthsc.tam.core.authorizationattemptstatus.mapper.AuthorizationAttemptStatusMapper;
import de.consorsbank.core.trauthsc.tam.core.authorizationattemptstatus.repository.AuthorizationAttemptRepository;
import de.consorsbank.core.trauthsc.tam.core.authorizationstatus.repository.AuthorizationRepository;
import de.consorsbank.core.trauthsc.tam.exception.CommonException;
import de.consorsbank.core.trauthsc.tam.exception.TamExceptionCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthorizationAttemptStatusServiceImpl implements AuthorizationAttemptStatusApi {

    private final AuthorizationRepository authorizationRepository;
    private final AuthorizationAttemptRepository authorizationAttemptRepository;
    private final AuthorizationAttemptStatusMapper authorizationAttemptStatusMapper;

    @Override
    @Transactional(readOnly = true)
    public GetAuthorizationAttemptStatusResponse getAuthorizationAttemptStatus(String authorizationId, String attemptId) {
        var authorizationUuid = UUID.fromString(authorizationId);
        var attemptUuid = UUID.fromString(attemptId);

        authorizationRepository.findById(authorizationUuid)
                .orElseThrow(() -> new CommonException(TamExceptionCode.AUTHORIZATION_NOT_FOUND, List.of(authorizationId)));

        var attemptEntity = authorizationAttemptRepository.findByIdAndAuthorizationId(attemptUuid, authorizationUuid)
                .orElseThrow(() -> new CommonException(TamExceptionCode.AUTHORIZATION_ATTEMPT_NOT_FOUND, List.of(attemptId)));

        log.debug("Found attempt {} with status {}", attemptId, attemptEntity.getStatus());

        return authorizationAttemptStatusMapper.toResponse(attemptEntity);
    }
}

```




```

package de.consorsbank.core.trauthsc.tam.core.authorizationattemptstatus.service;

import de.consorsbank.core.trauthsc.rest.api.tam.get.authorization.attempt.status.model.GetAuthorizationAttemptStatusResponse;
import de.consorsbank.core.trauthsc.tam.core.authorizationattemptstatus.mapper.AuthorizationAttemptStatusMapper;
import de.consorsbank.core.trauthsc.tam.core.authorizationattemptstatus.repository.AuthorizationAttemptRepository;
import de.consorsbank.core.trauthsc.tam.core.authorizationstatus.repository.AuthorizationRepository;
import de.consorsbank.core.trauthsc.tam.entity.AuthorizationAttemptEntity;
import de.consorsbank.core.trauthsc.tam.entity.AuthorizationEntity;
import de.consorsbank.core.trauthsc.tam.exception.CommonException;
import de.consorsbank.core.trauthsc.tam.exception.TamExceptionCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthorizationAttemptStatusServiceImplTest {

    private static final UUID AUTHORIZATION_ID = UUID.randomUUID();
    private static final String AUTHORIZATION_ID_STRING = AUTHORIZATION_ID.toString();
    private static final UUID ATTEMPT_ID = UUID.randomUUID();
    private static final String ATTEMPT_ID_STRING = ATTEMPT_ID.toString();

    @Mock
    private AuthorizationRepository authorizationRepository;

    @Mock
    private AuthorizationAttemptRepository authorizationAttemptRepository;

    @Mock
    private AuthorizationAttemptStatusMapper authorizationAttemptStatusMapper;

    @InjectMocks
    private AuthorizationAttemptStatusServiceImpl service;

    @Test
    void getAuthorizationAttemptStatus_happyPath_returnsResponse() {
        // given
        var authorizationEntity = mock(AuthorizationEntity.class);
        var attemptEntity = mock(AuthorizationAttemptEntity.class);
        var expectedResponse = mock(GetAuthorizationAttemptStatusResponse.class);

        when(authorizationRepository.findById(AUTHORIZATION_ID))
                .thenReturn(Optional.of(authorizationEntity));
        when(authorizationAttemptRepository.findByIdAndAuthorizationId(ATTEMPT_ID, AUTHORIZATION_ID))
                .thenReturn(Optional.of(attemptEntity));
        when(authorizationAttemptStatusMapper.toResponse(attemptEntity))
                .thenReturn(expectedResponse);

        // when
        var result = service.getAuthorizationAttemptStatus(AUTHORIZATION_ID_STRING, ATTEMPT_ID_STRING);

        // then
        assertEquals(expectedResponse, result);
        verify(authorizationRepository).findById(AUTHORIZATION_ID);
        verify(authorizationAttemptRepository).findByIdAndAuthorizationId(ATTEMPT_ID, AUTHORIZATION_ID);
        verify(authorizationAttemptStatusMapper).toResponse(attemptEntity);
    }

    @Test
    void getAuthorizationAttemptStatus_authorizationNotFound_throwsCommonException() {
        // given
        when(authorizationRepository.findById(AUTHORIZATION_ID))
                .thenReturn(Optional.empty());

        // when & then
        var exception = assertThrows(CommonException.class,
                () -> service.getAuthorizationAttemptStatus(AUTHORIZATION_ID_STRING, ATTEMPT_ID_STRING));

        assertEquals(TamExceptionCode.AUTHORIZATION_NOT_FOUND, exception.getExceptionCode());
        verifyNoInteractions(authorizationAttemptRepository);
        verifyNoInteractions(authorizationAttemptStatusMapper);
    }

    @Test
    void getAuthorizationAttemptStatus_attemptNotFound_throwsCommonException() {
        // given
        var authorizationEntity = mock(AuthorizationEntity.class);

        when(authorizationRepository.findById(AUTHORIZATION_ID))
                .thenReturn(Optional.of(authorizationEntity));
        when(authorizationAttemptRepository.findByIdAndAuthorizationId(ATTEMPT_ID, AUTHORIZATION_ID))
                .thenReturn(Optional.empty());

        // when & then
        var exception = assertThrows(CommonException.class,
                () -> service.getAuthorizationAttemptStatus(AUTHORIZATION_ID_STRING, ATTEMPT_ID_STRING));

        assertEquals(TamExceptionCode.AUTHORIZATION_ATTEMPT_NOT_FOUND, exception.getExceptionCode());
        verifyNoInteractions(authorizationAttemptStatusMapper);
    }

    @Test
    void getAuthorizationAttemptStatus_invalidAuthorizationId_throwsIllegalArgumentException() {
        // when & then
        assertThrows(IllegalArgumentException.class,
                () -> service.getAuthorizationAttemptStatus("invalid-uuid", ATTEMPT_ID_STRING));

        verifyNoInteractions(authorizationRepository);
        verifyNoInteractions(authorizationAttemptRepository);
        verifyNoInteractions(authorizationAttemptStatusMapper);
    }

    @Test
    void getAuthorizationAttemptStatus_invalidAttemptId_throwsIllegalArgumentException() {
        // when & then
        assertThrows(IllegalArgumentException.class,
                () -> service.getAuthorizationAttemptStatus(AUTHORIZATION_ID_STRING, "invalid-uuid"));

        verifyNoInteractions(authorizationRepository);
        verifyNoInteractions(authorizationAttemptRepository);
        verifyNoInteractions(authorizationAttemptStatusMapper);
    }
}

```



```
package de.consorsbank.core.trauthsc.tam.controller;

import de.consorsbank.core.trauthsc.rest.api.tam.get.authorization.attempt.status.model.GetAuthorizationAttemptStatusResponse;

public interface AuthorizationAttemptStatusApi {
    GetAuthorizationAttemptStatusResponse getAuthorizationAttemptStatus(String authorizationId, String attemptId);
}


```



```
package de.consorsbank.core.trauthsc.tam.controller;

import de.consorsbank.core.trauthsc.rest.api.tam.get.authorization.attempt.status.model.GetAuthorizationAttemptStatusResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthorizationAttemptStatusController implements GetAuthorizationAttemptStatusApi {

    private final AuthorizationAttemptStatusApi authorizationAttemptStatus;

    @Override
    public ResponseEntity<GetAuthorizationAttemptStatusResponse> getAuthorizationAttemptStatus(
            String feId, String language, String traceId, String userAgent,
            String xSourceService, String xRequestId, String authorizationId, String attemptId) {
        var response = authorizationAttemptStatus.getAuthorizationAttemptStatus(authorizationId, attemptId);
        return ResponseEntity.ok().body(response);
    }
}

```



```
package de.consorsbank.core.trauthsc.tam.core.authorizationattemptstatus.mapper;

import de.consorsbank.core.trauthsc.rest.api.tam.get.authorization.attempt.status.model.GetAuthorizationAttemptStatusResponse;
import de.consorsbank.core.trauthsc.tam.entity.AuthorizationAttemptEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuthorizationAttemptStatusMapper {

    GetAuthorizationAttemptStatusResponse toResponse(AuthorizationAttemptEntity entity);
}

```


```
package de.consorsbank.core.trauthsc.rest.api.tam.get.authorization.attempt.status.model;

import de.consorsbank.core.trauthsc.rest.api.tam.get.authorization.status.model.AuthorizationAttemptStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetAuthorizationAttemptStatusResponse {

    private AuthorizationAttemptStatus status;
}

```


```
package de.consorsbank.core.trauthsc.tam.core.authorizationattemptstatus.repository;

import de.consorsbank.core.trauthsc.tam.entity.AuthorizationAttemptEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AuthorizationAttemptRepository extends JpaRepository<AuthorizationAttemptEntity, UUID> {

    @Query("SELECT a FROM AuthorizationAttemptEntity a " +
           "LEFT JOIN FETCH a.authorizationMethodEntity " +
           "WHERE a.authorizationEntity.id = :authorizationId")
    List<AuthorizationAttemptEntity> findByAuthorizationIdWithMethod(@Param("authorizationId") UUID authorizationId);

    @Query("SELECT a FROM AuthorizationAttemptEntity a " +
           "WHERE a.id = :attemptId AND a.authorizationEntity.id = :authorizationId")
    Optional<AuthorizationAttemptEntity> findByIdAndAuthorizationId(
            @Param("attemptId") UUID attemptId,
            @Param("authorizationId") UUID authorizationId);
}
```
