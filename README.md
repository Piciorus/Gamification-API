```
public void sendMessage(UUID transactionId, AuthorizationStatusEnum authorizationStatus) {
    try {
        var event = new TransactionAuthorizationEvent(transactionId, authorizationStatus);
        jmsTemplate.convertAndSend(
            artemisProperties.getQueue(), 
            objectMapper.writeValueAsString(event)
        );
    } catch (JsonProcessingException e) {
        throw new CommonException(TamExceptionCode.SERIALIZE_EVENT_FAILED);
    }
}
```

```
public void onMessage(Message message) throws Exception {
    try {
        String json = ((TextMessage) message).getText();
        var event = objectMapper.readValue(json, TransactionAuthorizationEvent.class);
        log.info("Received JMS message: {}", event);
    } catch (CommonException e) {
        log.error("Error processing JMS message, transaction will rollback: {}", e.getMessage());
        throw e;
    }
}
```
