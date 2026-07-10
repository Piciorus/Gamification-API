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
