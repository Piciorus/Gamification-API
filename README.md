```
package de.consorsbank.core.trauthsc.transactionprocessor.listener;

import de.consorsbank.core.trauthsc.common.transactionprocessor.listener.ExternalEventMessageListener;
import jakarta.jms.Message;
import jakarta.jms.TextMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExternalEventMessageListenerTest {

    @InjectMocks
    private ExternalEventMessageListener listener;

    // ── Scenario 1: Happy path ─────────────────────────────────────────────

    @Test
    void onMessage_happyPath_processesTextMessageWithoutException() throws Exception {
        // Arrange
        TextMessage textMessage = mock(TextMessage.class);
        when(textMessage.getText()).thenReturn("{ \"event\": \"test\" }");

        // Act & Assert — no exception expected
        listener.onMessage(textMessage);

        verify(textMessage, times(1)).getText();
    }

    // ── Scenario 2: CommonException thrown → propagated (triggers rollback) ─

    @Test
    void onMessage_whenCommonExceptionThrown_exceptionIsPropagated() throws Exception {
        // Arrange
        TextMessage textMessage = mock(TextMessage.class);
        when(textMessage.getText())
                .thenThrow(new CommonException("Simulated processing error"));

        // Act & Assert
        assertThrows(CommonException.class, () -> listener.onMessage(textMessage));
    }

    // ── Scenario 3: Non-text message received → skipped ───────────────────

    @Test
    void onMessage_whenNonTextMessageReceived_messageIsSkipped() throws Exception {
        // Arrange — plain Message, NOT a TextMessage
        Message nonTextMessage = mock(Message.class);

        // Act
        listener.onMessage(nonTextMessage);

        // Assert — nothing on the message was called after the instanceof check
        verifyNoMoreInteractions(nonTextMessage);
    }
}
```



```
package de.consorsbank.core.trauthsc.tam.core.initiatetransaction.service;

import de.consorsbank.core.trauthsc.tam.core.initiatetransaction.mapper.InitiateTransactionAuthorizationMapper;
import de.consorsbank.core.trauthsc.tam.core.initiatetransaction.messaging.InitiateTransactionEventSender;
import de.consorsbank.core.trauthsc.tam.core.initiatetransaction.payloadprocessor.JsonTransactionPayloadProcessor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InitiateTransactionAuthorizationServiceTest {

    private static final String OWNER = "taxsrv-sc";

    @Mock
    private AuthorizationRepository authorizationRepository;

    @Mock
    private InitiateTransactionAuthorizationMapper initiateTransactionAuthorizationMapper;

    @Mock
    private InitiateTransactionEventSender initiateTransactionEventSender;

    @Mock
    private JsonTransactionPayloadProcessor jsonPayloadProcessor;

    @Mock
    private PayloadVaultManager payloadVaultManager;

    @Mock
    private ServiceRepository serviceRepository;

    @InjectMocks
    private InitiateTransactionAuthorizationService service;

    // ── initiateTransactionAuthorization - happy path ──────────────────────

    @Test
    void initiateTransactionAuthorization_happyPath_returnsResponse() throws Exception {
        // Arrange
        var request = buildRequest("txn-001");
        var payloadVaultRequest = mock(PayloadVaultRequest.class);
        var payloadVaultResponse = mock(PayloadVaultResponse.class);
        var payloadId = UUID.randomUUID();
        var authorizationEntity = mock(AuthorizationEntity.class);
        var savedEntity = mock(AuthorizationEntity.class);
        var serviceEntity = mock(ServiceEntity.class);
        var serviceId = buildServiceId();
        var expectedResponse = mock(InitiateTransactionAuthorizationResponse.class);

        when(jsonPayloadProcessor.process(
                request.getTransactionServicePayload(), request.getExpiresAt()))
                .thenReturn(payloadVaultRequest);
        when(payloadVaultManager.savePayload(payloadVaultRequest, OWNER))
                .thenReturn(payloadVaultResponse);
        when(payloadVaultResponse.getPayloadId()).thenReturn(payloadId);

        when(authorizationRepository.findAllByTransactionId("txn-001"))
                .thenReturn(Collections.emptyList());

        when(initiateTransactionAuthorizationMapper.toEntity(
                eq(request), any(OffsetDateTime.class), eq(OWNER)))
                .thenReturn(authorizationEntity);
        when(authorizationEntity.getTransactionPayloadId()).thenReturn(String.valueOf(payloadId));

        when(serviceRepository.findById(any(ServiceId.class)))
                .thenReturn(Optional.of(serviceEntity));
        doNothing().when(authorizationEntity).setServiceEntity(serviceEntity);
        doNothing().when(authorizationEntity).setTransactionPayloadId(any());

        when(authorizationRepository.save(authorizationEntity)).thenReturn(savedEntity);
        when(savedEntity.getTransactionId()).thenReturn("txn-001");

        when(initiateTransactionAuthorizationMapper.toResponse(savedEntity))
                .thenReturn(expectedResponse);

        doNothing().when(initiateTransactionEventSender).sendMessage(
                any(), eq(AuthorizationStatusEnum.PENDING));

        // Act
        var response = service.initiateTransactionAuthorization(request);

        // Assert
        assertNotNull(response);
        assertSame(expectedResponse, response);

        verify(jsonPayloadProcessor).process(
                request.getTransactionServicePayload(), request.getExpiresAt());
        verify(payloadVaultManager).savePayload(payloadVaultRequest, OWNER);
        verify(authorizationRepository).save(authorizationEntity);
        verify(initiateTransactionEventSender).sendMessage(any(), eq(AuthorizationStatusEnum.PENDING));
    }

    // ── getOrCreateAuthorizationEntity - transactionId null → creates new ──

    @Test
    void initiateTransactionAuthorization_nullTransactionId_createsNewEntity() throws Exception {
        // Arrange
        var request = buildRequest(null);
        var payloadVaultRequest = mock(PayloadVaultRequest.class);
        var payloadVaultResponse = mock(PayloadVaultResponse.class);
        var payloadId = UUID.randomUUID();
        var authorizationEntity = mock(AuthorizationEntity.class);
        var savedEntity = mock(AuthorizationEntity.class);
        var serviceEntity = mock(ServiceEntity.class);
        var expectedResponse = mock(InitiateTransactionAuthorizationResponse.class);

        when(jsonPayloadProcessor.process(any(), any())).thenReturn(payloadVaultRequest);
        when(payloadVaultManager.savePayload(any(), eq(OWNER))).thenReturn(payloadVaultResponse);
        when(payloadVaultResponse.getPayloadId()).thenReturn(payloadId);

        // transactionId is null → skip repository lookup
        when(initiateTransactionAuthorizationMapper.toEntity(any(), any(), eq(OWNER)))
                .thenReturn(authorizationEntity);
        when(serviceRepository.findById(any())).thenReturn(Optional.of(serviceEntity));
        when(authorizationRepository.save(authorizationEntity)).thenReturn(savedEntity);
        when(initiateTransactionAuthorizationMapper.toResponse(savedEntity))
                .thenReturn(expectedResponse);

        // Act
        var result = service.initiateTransactionAuthorization(request);

        // Assert
        assertNotNull(result);
        verify(authorizationRepository, never()).findAllByTransactionId(any());
    }

    // ── getOrCreateAuthorizationEntity - PENDING status → throws ──────────

    @Test
    void initiateTransactionAuthorization_pendingEntityExists_throwsCommonException()
            throws Exception {
        // Arrange
        var request = buildRequest("txn-duplicate");
        var payloadVaultRequest = mock(PayloadVaultRequest.class);
        var payloadVaultResponse = mock(PayloadVaultResponse.class);
        var existingEntity = mock(AuthorizationEntity.class);

        when(jsonPayloadProcessor.process(any(), any())).thenReturn(payloadVaultRequest);
        when(payloadVaultManager.savePayload(any(), any())).thenReturn(payloadVaultResponse);
        when(payloadVaultResponse.getPayloadId()).thenReturn(UUID.randomUUID());

        when(authorizationRepository.findAllByTransactionId("txn-duplicate"))
                .thenReturn(List.of(existingEntity));
        when(existingEntity.getStatus()).thenReturn(AuthorizationStatusEnum.PENDING);

        // Act & Assert
        var ex = assertThrows(CommonException.class,
                () -> service.initiateTransactionAuthorization(request));

        assertEquals(
                TransactionAuthorizationExceptionCode.TRANSACTION_WAS_ALREADY_INITIATED,
                ex.getExceptionCode());

        verify(authorizationRepository, never()).save(any());
    }

    // ── getOrCreateAuthorizationEntity - AUTHORIZED status → throws ────────

    @Test
    void initiateTransactionAuthorization_authorizedEntityExists_throwsCommonException()
            throws Exception {
        // Arrange
        var request = buildRequest("txn-authorized");
        var payloadVaultRequest = mock(PayloadVaultRequest.class);
        var payloadVaultResponse = mock(PayloadVaultResponse.class);
        var existingEntity = mock(AuthorizationEntity.class);

        when(jsonPayloadProcessor.process(any(), any())).thenReturn(payloadVaultRequest);
        when(payloadVaultManager.savePayload(any(), any())).thenReturn(payloadVaultResponse);
        when(payloadVaultResponse.getPayloadId()).thenReturn(UUID.randomUUID());

        when(authorizationRepository.findAllByTransactionId("txn-authorized"))
                .thenReturn(List.of(existingEntity));
        when(existingEntity.getStatus()).thenReturn(AuthorizationStatusEnum.AUTHORIZED);

        // Act & Assert
        var ex = assertThrows(CommonException.class,
                () -> service.initiateTransactionAuthorization(request));

        assertEquals(
                TransactionAuthorizationExceptionCode.TRANSACTION_TERMINAL_STATE,
                ex.getExceptionCode());
    }

    // ── getOrCreateAuthorizationEntity - FAILED status → throws ───────────

    @Test
    void initiateTransactionAuthorization_failedEntityExists_throwsCommonException()
            throws Exception {
        var request = buildRequest("txn-failed");
        setupPayloadMocks(request);

        var existingEntity = mock(AuthorizationEntity.class);
        when(authorizationRepository.findAllByTransactionId("txn-failed"))
                .thenReturn(List.of(existingEntity));
        when(existingEntity.getStatus()).thenReturn(AuthorizationStatusEnum.FAILED);

        assertThrows(CommonException.class,
                () -> service.initiateTransactionAuthorization(request));
    }

    // ── getOrCreateAuthorizationEntity - EXPIRED status → throws ──────────

    @Test
    void initiateTransactionAuthorization_expiredEntityExists_throwsCommonException()
            throws Exception {
        var request = buildRequest("txn-expired");
        setupPayloadMocks(request);

        var existingEntity = mock(AuthorizationEntity.class);
        when(authorizationRepository.findAllByTransactionId("txn-expired"))
                .thenReturn(List.of(existingEntity));
        when(existingEntity.getStatus()).thenReturn(AuthorizationStatusEnum.EXPIRED);

        assertThrows(CommonException.class,
                () -> service.initiateTransactionAuthorization(request));
    }

    // ── createPendingAuthorizationEntity - service not found → throws ──────

    @Test
    void initiateTransactionAuthorization_serviceNotFound_throwsCommonException()
            throws Exception {
        var request = buildRequest("txn-no-service");
        setupPayloadMocks(request);

        when(authorizationRepository.findAllByTransactionId("txn-no-service"))
                .thenReturn(Collections.emptyList());
        when(initiateTransactionAuthorizationMapper.toEntity(any(), any(), eq(OWNER)))
                .thenReturn(mock(AuthorizationEntity.class));
        when(serviceRepository.findById(any())).thenReturn(Optional.empty());

        var ex = assertThrows(CommonException.class,
                () -> service.initiateTransactionAuthorization(request));

        assertEquals(
                TransactionAuthorizationExceptionCode.SERVICE_NOT_FOUND,
                ex.getExceptionCode());

        verify(authorizationRepository, never()).save(any());
    }

    // ── helper methods ─────────────────────────────────────────────────────

    private InitiateTransactionAuthorizationRequest buildRequest(String transactionId) {
        var request = mock(InitiateTransactionAuthorizationRequest.class);
        when(request.getTransactionId()).thenReturn(transactionId);
        when(request.getTransactionServicePayload()).thenReturn("{\"amount\":100}");
        when(request.getExpiresAt()).thenReturn(OffsetDateTime.now().plusHours(1));
        when(request.getTenant()).thenReturn("tenant-1");
        when(request.getTransactionService()).thenReturn("payment-service");
        when(request.getTransactionServiceVersion()).thenReturn("v1");
        return request;
    }

    private ServiceId buildServiceId() {
        var serviceId = new ServiceId();
        serviceId.setOwner(OWNER);
        serviceId.setService("payment-service");
        serviceId.setServiceVersion("v1");
        return serviceId;
    }

    private void setupPayloadMocks(InitiateTransactionAuthorizationRequest request)
            throws Exception {
        var payloadVaultRequest = mock(PayloadVaultRequest.class);
        var payloadVaultResponse = mock(PayloadVaultResponse.class);
        when(jsonPayloadProcessor.process(any(), any())).thenReturn(payloadVaultRequest);
        when(payloadVaultManager.savePayload(any(), any())).thenReturn(payloadVaultResponse);
        when(payloadVaultResponse.getPayloadId()).thenReturn(UUID.randomUUID());
    }
}

```

```
package de.consorsbank.core.trauthsc.tam.core.initiatetransaction.payloadprocessor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JsonTransactionPayloadProcessorTest {

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private JsonTransactionPayloadProcessor processor;

    // ── happy path: valid JSON → normalized PayloadVaultRequest ───────────

    @Test
    void process_validJson_returnsPayloadVaultRequest() throws Exception {
        // Arrange
        var rawJson = "{\"amount\":  100 }";
        var normalizedJson = "{\"amount\":100}";
        var expiresAt = OffsetDateTime.now().plusHours(1);
        var jsonNode = mock(JsonNode.class);

        when(objectMapper.readTree(rawJson)).thenReturn(jsonNode);
        when(objectMapper.writeValueAsString(jsonNode)).thenReturn(normalizedJson);

        // Act
        var result = processor.process(rawJson, expiresAt);

        // Assert
        assertNotNull(result);
        assertEquals(normalizedJson, result.getPayload());
        assertEquals(expiresAt, result.getExpiresAt());

        verify(objectMapper).readTree(rawJson);
        verify(objectMapper).writeValueAsString(jsonNode);
    }

    // ── invalid JSON → throws CommonException with INVALID_JSON_FORMAT ────

    @Test
    void process_invalidJson_throwsCommonException() throws Exception {
        // Arrange
        var invalidJson = "not-json";
        var expiresAt = OffsetDateTime.now().plusHours(1);

        when(objectMapper.readTree(invalidJson))
                .thenThrow(new JsonProcessingException("Invalid JSON") {});

        // Act & Assert
        var ex = assertThrows(CommonException.class,
                () -> processor.process(invalidJson, expiresAt));

        assertEquals(
                TransactionAuthorizationExceptionCode.INVALID_JSON_FORMAT,
                ex.getExceptionCode());

        verify(objectMapper).readTree(invalidJson);
        verify(objectMapper, never()).writeValueAsString(any());
    }

    // ── writeValueAsString fails → throws CommonException ─────────────────

    @Test
    void process_writeValueFails_throwsCommonException() throws Exception {
        // Arrange
        var rawJson = "{\"amount\":100}";
        var expiresAt = OffsetDateTime.now().plusHours(1);
        var jsonNode = mock(JsonNode.class);

        when(objectMapper.readTree(rawJson)).thenReturn(jsonNode);
        when(objectMapper.writeValueAsString(jsonNode))
                .thenThrow(new JsonProcessingException("Serialization failed") {});

        // Act & Assert
        var ex = assertThrows(CommonException.class,
                () -> processor.process(rawJson, expiresAt));

        assertEquals(
                TransactionAuthorizationExceptionCode.INVALID_JSON_FORMAT,
                ex.getExceptionCode());
    }

    // ── null payload → throws CommonException ─────────────────────────────

    @Test
    void process_nullPayload_throwsCommonException() throws Exception {
        var expiresAt = OffsetDateTime.now().plusHours(1);

        when(objectMapper.readTree((String) null))
                .thenThrow(new JsonProcessingException("null input") {});

        assertThrows(CommonException.class, () -> processor.process(null, expiresAt));
    }
}

```
