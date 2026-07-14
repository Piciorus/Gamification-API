
package de.consorsbank.core.trauthsc.tam.messaging.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.consorsbank.common.error.handling.exception.CommonException;
import de.consorsbank.core.trauthsc.tam.entity.AuthorizationEntity;
import de.consorsbank.core.trauthsc.tam.messaging.dto.AuthzStatus;
import de.consorsbank.core.trauthsc.tam.messaging.dto.TransactionAuthStatusDto;
import de.consorsbank.core.trauthsc.tam.messaging.dto.TxAuthzResult;
import de.consorsbank.core.trauthsc.tam.messaging.sender.TransactionAuthResultEventJmsSender;
import de.consorsbank.core.trauthsc.tam.repository.AuthorizationRepository;
import jakarta.jms.Message;
import jakarta.jms.TextMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KobilAuthorizationJmsListenerTest {

    @Mock private ObjectMapper objectMapper;
    @Mock private TransactionAuthResultEventJmsSender transactionAuthResultEventJmsSender;
    @Mock private AuthorizationRepository authorizationRepository;
    @Mock private TextMessage textMessage;

    @InjectMocks
    private KobilAuthorizationJmsListener listener;

    private static final UUID TX_ID = UUID.randomUUID();
    private static final UUID PAYLOAD_ID = UUID.randomUUID();
    private static final String RAW_JSON = "{\"txId\":\"" + TX_ID + "\"}";

    // -------------------------------------------------------------------------
    // Status mapping
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("AuthzStatus → TransactionAuthStatusDto mapping")
    class StatusMapping {

        @ParameterizedTest(name = "{0} → FAILED")
        @EnumSource(value = AuthzStatus.class,
                names = {"PENDING", "USER_TIMEOUT", "USER_OFFLINE", "TIMEOUT", "ERROR"})
        void failureStatuses_mapToFailed(AuthzStatus status) throws Exception {
            TxAuthzResult event = buildEvent(status);
            setupMocks(event);

            listener.onMessage(textMessage);

            verify(transactionAuthResultEventJmsSender)
                    .sendMessage(eq(TX_ID), eq(TransactionAuthStatusDto.FAILED), any());
        }

        @ParameterizedTest(name = "{0} → AUTHORIZED")
        @EnumSource(value = AuthzStatus.class, names = {"ACCEPTED", "ACCEPTED_DONE"})
        void acceptedStatuses_mapToAuthorized(AuthzStatus status) throws Exception {
            TxAuthzResult event = buildEvent(status);
            setupMocks(event);

            listener.onMessage(textMessage);

            verify(transactionAuthResultEventJmsSender)
                    .sendMessage(eq(TX_ID), eq(TransactionAuthStatusDto.AUTHORIZED), any());
        }

        @Test
        @DisplayName("REJECTED → CANCELED")
        void rejectedStatus_mapsToCanceled() throws Exception {
            TxAuthzResult event = buildEvent(AuthzStatus.REJECTED);
            setupMocks(event);

            listener.onMessage(textMessage);

            verify(transactionAuthResultEventJmsSender)
                    .sendMessage(eq(TX_ID), eq(TransactionAuthStatusDto.CANCELED), any());
        }
    }

    // -------------------------------------------------------------------------
    // PayloadId resolution
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("PayloadId resolution from AuthorizationRepository")
    class PayloadIdResolution {

        @Test
        @DisplayName("entity present → payloadId forwarded to sender")
        void entityFound_payloadIdPassedToSender() throws Exception {
            TxAuthzResult event = buildEvent(AuthzStatus.ACCEPTED);
            AuthorizationEntity entity = mock(AuthorizationEntity.class);
            when(entity.getTransactionPayloadId()).thenReturn(PAYLOAD_ID);
            when(authorizationRepository.findById(TX_ID)).thenReturn(Optional.of(entity));
            setupMocks(event);

            listener.onMessage(textMessage);

            verify(transactionAuthResultEventJmsSender)
                    .sendMessage(TX_ID, TransactionAuthStatusDto.AUTHORIZED, PAYLOAD_ID);
        }

        @Test
        @DisplayName("entity absent → payloadId null, sender still called")
        void entityNotFound_payloadIdIsNull() throws Exception {
            TxAuthzResult event = buildEvent(AuthzStatus.ACCEPTED);
            when(authorizationRepository.findById(TX_ID)).thenReturn(Optional.empty());
            setupMocks(event);

            listener.onMessage(textMessage);

            verify(transactionAuthResultEventJmsSender)
                    .sendMessage(TX_ID, TransactionAuthStatusDto.AUTHORIZED, null);
        }
    }

    // -------------------------------------------------------------------------
    // Exception / rollback handling
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Exception handling")
    class ExceptionHandling {

        @Test
        @DisplayName("CommonException from sender → rethrown (triggers @Transactional rollback)")
        void commonExceptionFromSender_isRethrown() throws Exception {
            TxAuthzResult event = buildEvent(AuthzStatus.ACCEPTED);
            setupMocks(event);
            CommonException ex = mock(CommonException.class);
            when(ex.getMessage()).thenReturn("send failure");
            doThrow(ex).when(transactionAuthResultEventJmsSender)
                    .sendMessage(any(), any(), any());

            assertThatThrownBy(() -> listener.onMessage(textMessage))
                    .isSameAs(ex);
        }

        @Test
        @DisplayName("JMS getText() failure → exception propagates")
        void textMessageReadFailure_propagates() throws Exception {
            when(textMessage.getText()).thenThrow(new RuntimeException("JMS error"));

            assertThatThrownBy(() -> listener.onMessage(textMessage))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("JMS error");
        }

        @Test
        @DisplayName("Jackson parse failure → exception propagates")
        void jacksonParseFailure_propagates() throws Exception {
            when(textMessage.getText()).thenReturn(RAW_JSON);
            when(objectMapper.readValue(RAW_JSON, TxAuthzResult.class))
                    .thenThrow(new RuntimeException("parse error"));

            assertThatThrownBy(() -> listener.onMessage(textMessage))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("parse error");
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private TxAuthzResult buildEvent(AuthzStatus status) {
        return new TxAuthzResult(TX_ID.toString(), status, null, null, null, null);
    }

    private void setupMocks(TxAuthzResult event) throws Exception {
        when(textMessage.getText()).thenReturn(RAW_JSON);
        when(objectMapper.readValue(RAW_JSON, TxAuthzResult.class)).thenReturn(event);
        when(authorizationRepository.findById(TX_ID)).thenReturn(Optional.empty());
    }
}


```
@ExtendWith(MockitoExtension.class)
class KobilAuthorizationJmsListenerTest {

    @Mock private ObjectMapper objectMapper;
    @Mock private TransactionAuthResultEventJmsSender transactionAuthResultEventJmsSender;
    @Mock private AuthorizationRepository authorizationRepository;
    @Mock private TextMessage textMessage;

    @InjectMocks
    private KobilAuthorizationJmsListener listener;

    private static final UUID TX_ID = UUID.randomUUID();
    private static final UUID PAYLOAD_ID = UUID.randomUUID();
    private static final String JSON_PAYLOAD = "{\"txId\":\"" + TX_ID + "\",\"authzStatus\":\"ACCEPTED\"}";

    @Nested
    @DisplayName("authzStatus → TransactionAuthStatusDto mapping")
    class StatusMapping {

        @ParameterizedTest(name = "{0} → FAILED")
        @EnumSource(value = TxAuthzResult.AuthzStatus.class,
                names = {"PENDING", "USER_TIMEOUT", "USER_OFFLINE", "TIMEOUT", "ERROR"})
        void shouldMapToFailed(TxAuthzResult.AuthzStatus status) throws Exception {
            // given
            var event = buildEvent(TX_ID, status);
            when(objectMapper.readValue(JSON_PAYLOAD, TxAuthzResult.class)).thenReturn(event);
            when(authorizationRepository.findById(TX_ID)).thenReturn(Optional.empty());

            // when
            listener.onMessage(textMessage);

            // then
            verify(transactionAuthResultEventJmsSender)
                    .sendMessage(TX_ID, TransactionAuthStatusDto.FAILED, null);
        }

        @ParameterizedTest(name = "{0} → AUTHORIZED")
        @EnumSource(value = TxAuthzResult.AuthzStatus.class,
                names = {"ACCEPTED", "ACCEPTED_DONE"})
        void shouldMapToAuthorized(TxAuthzResult.AuthzStatus status) throws Exception {
            // given
            var event = buildEvent(TX_ID, status);
            when(objectMapper.readValue(JSON_PAYLOAD, TxAuthzResult.class)).thenReturn(event);
            when(authorizationRepository.findById(TX_ID)).thenReturn(Optional.empty());

            // when
            listener.onMessage(textMessage);

            // then
            verify(transactionAuthResultEventJmsSender)
                    .sendMessage(TX_ID, TransactionAuthStatusDto.AUTHORIZED, null);
        }

        @Test
        @DisplayName("REJECTED → CANCELED")
        void shouldMapToCanceled() throws Exception {
            // given
            var event = buildEvent(TX_ID, TxAuthzResult.AuthzStatus.REJECTED);
            when(objectMapper.readValue(JSON_PAYLOAD, TxAuthzResult.class)).thenReturn(event);
            when(authorizationRepository.findById(TX_ID)).thenReturn(Optional.empty());

            // when
            listener.onMessage(textMessage);

            // then
            verify(transactionAuthResultEventJmsSender)
                    .sendMessage(TX_ID, TransactionAuthStatusDto.CANCELED, null);
        }
    }

    @Nested
    @DisplayName("payloadId resolution from AuthorizationRepository")
    class PayloadIdResolution {

        @Test
        @DisplayName("entity found → payloadId forwarded to sender")
        void shouldForwardPayloadIdWhenEntityPresent() throws Exception {
            // given
            var event = buildEvent(TX_ID, TxAuthzResult.AuthzStatus.ACCEPTED);
            when(objectMapper.readValue(JSON_PAYLOAD, TxAuthzResult.class)).thenReturn(event);
            var entity = mock(AuthorizationEntity.class);
            when(entity.getTransactionPayloadId()).thenReturn(PAYLOAD_ID);
            when(authorizationRepository.findById(TX_ID)).thenReturn(Optional.of(entity));

            // when
            listener.onMessage(textMessage);

            // then
            verify(transactionAuthResultEventJmsSender)
                    .sendMessage(TX_ID, TransactionAuthStatusDto.AUTHORIZED, PAYLOAD_ID);
        }

        @Test
        @DisplayName("entity not found → payloadId is null")
        void shouldSendNullPayloadIdWhenEntityAbsent() throws Exception {
            // given
            var event = buildEvent(TX_ID, TxAuthzResult.AuthzStatus.ACCEPTED);
            when(objectMapper.readValue(JSON_PAYLOAD, TxAuthzResult.class)).thenReturn(event);
            when(authorizationRepository.findById(TX_ID)).thenReturn(Optional.empty());

            // when
            listener.onMessage(textMessage);

            // then
            verify(transactionAuthResultEventJmsSender)
                    .sendMessage(TX_ID, TransactionAuthStatusDto.AUTHORIZED, null);
        }
    }

    @Nested
    @DisplayName("Error handling")
    class ErrorHandling {

        @Test
        @DisplayName("CommonException triggers rollback and rethrow")
        void shouldRethrowCommonException() throws Exception {
            // given
            var event = buildEvent(TX_ID, TxAuthzResult.AuthzStatus.ACCEPTED);
            when(objectMapper.readValue(JSON_PAYLOAD, TxAuthzResult.class)).thenReturn(event);
            when(authorizationRepository.findById(TX_ID)).thenReturn(Optional.empty());
            doThrow(new CommonException("sender failure"))
                    .when(transactionAuthResultEventJmsSender)
                    .sendMessage(any(), any(), any());

            // when / then
            assertThrows(CommonException.class, () -> listener.onMessage(textMessage));
        }

        @Test
        @DisplayName("Malformed JSON → ObjectMapper throws → propagates")
        void shouldPropagateJsonParseException() throws Exception {
            // given
            when(objectMapper.readValue(anyString(), eq(TxAuthzResult.class)))
                    .thenThrow(new JsonProcessingException("bad json") {});

            // when / then
            assertThrows(Exception.class, () -> listener.onMessage(textMessage));
            verifyNoInteractions(authorizationRepository, transactionAuthResultEventJmsSender);
        }

        @Test
        @DisplayName("Non-TextMessage cast → ClassCastException propagates")
        void shouldFailOnNonTextMessage() {
            // given
            Message nonTextMessage = mock(Message.class);

            // when / then
            assertThrows(ClassCastException.class, () -> listener.onMessage(nonTextMessage));
        }
    }

    private TxAuthzResult buildEvent(UUID txId, TxAuthzResult.AuthzStatus status) {
        var event = mock(TxAuthzResult.class);
        when(event.txId()).thenReturn(txId.toString());
        when(event.authzStatus()).thenReturn(status);
        return event;
    }
}
```
