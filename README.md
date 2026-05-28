```
package de.consorsbank.core.trauthsc.tam.core.payloadtransactionauthorization.service;

import de.consorsbank.core.trauthsc.rest.api.tam.get.payload.transaction.authorization.status.model.GetPayloadTransactionAuthorizationResponse;
import de.consorsbank.core.trauthsc.tam.controller.PayloadTransactionAuthorization;
import de.consorsbank.core.trauthsc.tam.core.authorizationstatus.repository.AuthorizationRepository;
import de.consorsbank.core.trauthsc.tam.core.payloadtransactionauthorization.mapper.PayloadTransactionAuthorizationMapper;
import de.consorsbank.core.trauthsc.tam.exception.CommonException;
import de.consorsbank.core.trauthsc.tam.exception.TamExceptionCode;
import de.consorsbank.core.trauthsc.tam.payloadvault.PayloadVaultManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayloadTransactionAuthorizationServiceImpl implements PayloadTransactionAuthorization {

    private final AuthorizationRepository authorizationRepository;
    private final PayloadVaultManager payloadVaultManager;
    private final PayloadTransactionAuthorizationMapper payloadTransactionAuthorizationMapper;

    @Override
    @Transactional(readOnly = true)
    public GetPayloadTransactionAuthorizationResponse getPayloadTransactionAuthorization(String authorizationId) {
        var authorizationEntity = authorizationRepository.findById(UUID.fromString(authorizationId))
                .orElseThrow(() -> new CommonException(TamExceptionCode.AUTHORIZATION_NOT_FOUND, List.of(authorizationId)));

        var payload = payloadVaultManager.getPayload(authorizationEntity.getTransactionPayloadId());

        log.debug("Found payload for authorization {}", authorizationId);

        return payloadTransactionAuthorizationMapper.map(payload);
    }
}

```



```
package de.consorsbank.core.trauthsc.tam.core.payloadtransactionauthorization.service;

import de.consorsbank.core.trauthsc.tam.core.authorizationstatus.repository.AuthorizationRepository;
import de.consorsbank.core.trauthsc.tam.core.payloadtransactionauthorization.mapper.PayloadTransactionAuthorizationMapper;
import de.consorsbank.core.trauthsc.tam.entity.AuthorizationEntity;
import de.consorsbank.core.trauthsc.tam.exception.CommonException;
import de.consorsbank.core.trauthsc.tam.exception.TamExceptionCode;
import de.consorsbank.core.trauthsc.tam.payloadvault.PayloadVaultManager;
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
class PayloadTransactionAuthorizationServiceImplTest {

    private static final UUID AUTHORIZATION_ID = UUID.randomUUID();
    private static final String AUTHORIZATION_ID_STRING = AUTHORIZATION_ID.toString();
    private static final String TRANSACTION_PAYLOAD_ID = UUID.randomUUID().toString();
    private static final String PAYLOAD_JSON = "{\"amount\":100.00,\"currency\":\"EUR\"}";

    @Mock
    private AuthorizationRepository authorizationRepository;

    @Mock
    private PayloadVaultManager payloadVaultManager;

    @Mock
    private PayloadTransactionAuthorizationMapper payloadTransactionAuthorizationMapper;

    @InjectMocks
    private PayloadTransactionAuthorizationServiceImpl service;

    @Test
    void getPayloadTransactionAuthorization_happyPath_returnsPayload() {
        // given
        var authorizationEntity = mock(AuthorizationEntity.class);
        var expectedResponse = de.consorsbank.core.trauthsc.rest.api.tam.get.payload.transaction.authorization.status.model
                .GetPayloadTransactionAuthorizationResponse.builder()
                .transactionServicePayload(PAYLOAD_JSON)
                .build();

        when(authorizationEntity.getTransactionPayloadId()).thenReturn(TRANSACTION_PAYLOAD_ID);
        when(authorizationRepository.findById(AUTHORIZATION_ID))
                .thenReturn(Optional.of(authorizationEntity));
        when(payloadVaultManager.getPayload(TRANSACTION_PAYLOAD_ID))
                .thenReturn(PAYLOAD_JSON);
        when(payloadTransactionAuthorizationMapper.map(PAYLOAD_JSON))
                .thenReturn(expectedResponse);

        // when
        var result = service.getPayloadTransactionAuthorization(AUTHORIZATION_ID_STRING);

        // then
        assertEquals(expectedResponse, result);
        assertEquals(PAYLOAD_JSON, result.getTransactionServicePayload());
        verify(authorizationRepository).findById(AUTHORIZATION_ID);
        verify(payloadVaultManager).getPayload(TRANSACTION_PAYLOAD_ID);
        verify(payloadTransactionAuthorizationMapper).map(PAYLOAD_JSON);
    }

    @Test
    void getPayloadTransactionAuthorization_authorizationNotFound_throwsCommonException() {
        // given
        when(authorizationRepository.findById(AUTHORIZATION_ID))
                .thenReturn(Optional.empty());

        // when & then
        var exception = assertThrows(CommonException.class,
                () -> service.getPayloadTransactionAuthorization(AUTHORIZATION_ID_STRING));

        assertEquals(TamExceptionCode.AUTHORIZATION_NOT_FOUND, exception.getExceptionCode());
        verifyNoInteractions(payloadVaultManager);
        verifyNoInteractions(payloadTransactionAuthorizationMapper);
    }

    @Test
    void getPayloadTransactionAuthorization_invalidUuid_throwsIllegalArgumentException() {
        // when & then
        assertThrows(IllegalArgumentException.class,
                () -> service.getPayloadTransactionAuthorization("not-a-valid-uuid"));

        verifyNoInteractions(authorizationRepository);
        verifyNoInteractions(payloadVaultManager);
        verifyNoInteractions(payloadTransactionAuthorizationMapper);
    }
}

```


```
package de.consorsbank.core.trauthsc.tam.controller;

import de.consorsbank.core.trauthsc.rest.api.tam.get.payload.transaction.authorization.status.model.GetPayloadTransactionAuthorizationResponse;
import de.consorsbank.core.trauthsc.tam.config.SecurityConfiguration;
import de.consorsbank.core.trauthsc.tam.test.ControllerUnitTestConfig;
import de.consorsbank.core.trauthsc.tam.test.TestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(controllers = PayloadTransactionAuthorizationController.class)
@Import({SecurityConfiguration.class})
@ActiveProfiles("test")
class PayloadTransactionAuthorizationControllerTest extends ControllerUnitTestConfig {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PayloadTransactionAuthorization payloadTransactionAuthorization;

    @Test
    void should_ReturnPayload_When_AuthorizationIdIsValid() throws Exception {
        // given
        when(payloadTransactionAuthorization.getPayloadTransactionAuthorization(any(String.class))).thenReturn(
                GetPayloadTransactionAuthorizationResponse.builder()
                        .transactionServicePayload("{\"amount\":100.00,\"currency\":\"EUR\"}")
                        .build());

        // when
        mockMvc.perform(
                        get(uriTemplate: "/v1/authorizations/8b146851-7ee8-4ba6-ad2b-af1724b2b5d3/payload")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .headers(TestUtils.getAllHttpHeadersWithoutOwner(source: "get-payload-transaction-authorization")))
                // then
                .andExpect(status().isOk());
    }

    @Test
    void should_ReturnBadRequest_When_RequiredHeadersAreNotPassed() throws Exception {
        // given
        when(payloadTransactionAuthorization.getPayloadTransactionAuthorization(any(String.class))).thenReturn(
                GetPayloadTransactionAuthorizationResponse.builder()
                        .transactionServicePayload("{\"amount\":100.00,\"currency\":\"EUR\"}")
                        .build());

        // when
        mockMvc.perform(
                        get(uriTemplate: "/v1/authorizations/8b146851-7ee8-4ba6-ad2b-af1724b2b5d3/payload")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .headers(TestUtils.getMissingHttpHeadersWithoutOwner()))
                // then
                .andExpect(status().isBadRequest());
    }
}


```



```
package de.consorsbank.core.trauthsc.tam.controller;

import de.consorsbank.core.trauthsc.rest.api.tam.get.payload.transaction.authorization.status.model.GetPayloadTransactionAuthorizationResponse;

public interface PayloadTransactionAuthorization {
    GetPayloadTransactionAuthorizationResponse getPayloadTransactionAuthorization(String authorizationId);
}

```



```
package de.consorsbank.core.trauthsc.tam.controller;

import de.consorsbank.core.trauthsc.rest.api.tam.get.payload.transaction.authorization.status.api.GetPayloadTransactionAuthorizationApi;
import de.consorsbank.core.trauthsc.rest.api.tam.get.payload.transaction.authorization.status.model.GetPayloadTransactionAuthorizationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PayloadTransactionAuthorizationController implements GetPayloadTransactionAuthorizationApi {

    private final PayloadTransactionAuthorization payloadTransactionAuthorization;

    @Override
    public ResponseEntity<GetPayloadTransactionAuthorizationResponse> getPayloadTransactionAuthorization(
            String feId, String language, String traceId, String xSourceService, String userAgent,
            String xRequestId, String authorizationId) {
        var response = payloadTransactionAuthorization.getPayloadTransactionAuthorization(authorizationId);
        return ResponseEntity.ok().body(response);
    }
}

```

```
package de.consorsbank.core.trauthsc.tam.core.payloadtransactionauthorization.mapper;

import de.consorsbank.core.trauthsc.rest.api.tam.get.payload.transaction.authorization.status.model.GetPayloadTransactionAuthorizationResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PayloadTransactionAuthorizationMapper {

    @Mapping(source = "payload", target = "transactionServicePayload")
    GetPayloadTransactionAuthorizationResponse toResponse(String payload);

    default GetPayloadTransactionAuthorizationResponse map(String payload) {
        return GetPayloadTransactionAuthorizationResponse.builder()
                .transactionServicePayload(payload)
                .build();
    }
}


```


```
package de.consorsbank.core.trauthsc.rest.api.tam.get.payload.transaction.authorization.status.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetPayloadTransactionAuthorizationResponse {

    private String transactionServicePayload;
}

```
```

import org.springframework.cloud.contract.spec.Contract

[
    Contract.make {
        priority( priority: 1)
        description( description: """
        Represents a successful scenario for getting payload transaction authorization
        ...
        given:
            authorizationId : any
        when:
            api request to get payload transaction authorization
        then:
            return OK with GetPayloadTransactionAuthorizationResponse
        ...
        """)
        request {
            method method: 'GET'
            urlPath($(consumer(regex( regex: '/v1/authorizations/[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}/payload'))))
            headers {
                header 'FeId': value(consumer(regex( regex: '.+')), producer( serverValue: 'WEB'))
                header 'Language': value(consumer(regex( regex: '.+')), producer( serverValue: 'DE'))
                header 'TraceId': value(consumer(regex( regex: '.+')), producer( serverValue: 'traceId'))
                header 'User-Agent': value(consumer(regex( regex: '.+')), producer( serverValue: 'User-Agent'))
                header 'x-source-service': value(consumer(regex( regex: '.+')), producer( serverValue: 'xSourceService'))
                header 'x-request-id': value(consumer(regex( regex: '.+')), producer( serverValue: '123456'))
            }
        }
        response {
            status OK()
            headers {
                header 'x-correlation-id': fromRequest().header( key: "x-request-id")
                contentType applicationJson()
            }
            body(
                    "transactionServicePayload": "{\"amount\":100.00,\"currency\":\"EUR\"}"
            )
        }
    }
]

```



```
import org.springframework.cloud.contract.spec.Contract

[
    Contract.make {
        priority( priority: 10)
        description( description: """
        Represents a failure scenario for getting payload transaction authorization
        ...
        given:
            authorizationId : any
        when:
            api request to get payload transaction authorization
        then:
            return BAD_REQUEST
        ...
        """)
        request {
            method method: 'GET'
            urlPath($(consumer(regex( regex: '/v1/authorizations/[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}/payload'))))
            headers {
                header 'FeId': value(consumer(regex( regex: '.+')), producer( serverValue: 'WEB'))
                header 'Language': value(consumer(regex( regex: '.+')), producer( serverValue: 'DE'))
                header 'TraceId': value(consumer(regex( regex: '.+')), producer( serverValue: 'traceId'))
                header 'User-Agent': value(consumer(regex( regex: '.+')), producer( serverValue: 'User-Agent'))
                header 'x-request-id': value(consumer(regex( regex: '.+')), producer( serverValue: '123456'))
            }
        }
        response {
            status BAD_REQUEST()
            headers {
                header 'x-correlation-id': fromRequest().header( key: "x-request-id")
                contentType applicationJson()
            }
            body(
                    "code": "",
                    "detail": "Required request header 'x-source-service' for method parameter type String is not present",
                    "errors": [],
                    "status": "400",
                    "title": "Header x-source-service is required"
            )
        }
    }
]

```
