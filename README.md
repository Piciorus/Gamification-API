```
@Component
@Slf4j
@RequiredArgsConstructor
public class InitiateTransactionEventSender {
    private final JmsTemplate jmsTemplate;
    private final ArtemisProperties artemisProperties;
    private final ObjectMapper objectMapper;

    public void sendMessage(UUID transactionId, AuthorizationStatusEnum authorizationStatus) {
        String queue = artemisProperties.getQueue();
        TransactionAuthorizationEvent event = 
            new TransactionAuthorizationEvent(transactionId, authorizationStatus);
        try {
            String json = objectMapper.writeValueAsString(event);
            jmsTemplate.send(queue, session -> session.createTextMessage(json));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize event", e);
        }
    }
}

```


```
String json = textMessage.getText();
TransactionAuthorizationEvent event = 
    objectMapper.readValue(json, TransactionAuthorizationEvent.class);
```
