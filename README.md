```
@Bean
public MessageConverter jacksonJmsMessageConverter() {
    MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
    converter.setTargetType(MessageType.TEXT); // sends as TextMessage with JSON body
    converter.setTypeIdPropertyName("_type");
    return converter;
}

```

```
@Bean
public JmsTemplate jmsTemplate(
        @Qualifier("atomikosConnectionFactory") ConnectionFactory connectionFactory,
        MessageConverter messageConverter) {
    var jmsTemplate = new JmsTemplate();
    jmsTemplate.setConnectionFactory(connectionFactory);
    jmsTemplate.setSessionTransacted(true);
    jmsTemplate.setMessageConverter(messageConverter); // ← add
    return jmsTemplate;
}

// in jmsListenerContainerFactory bean:
defaultJmsListenerContainerFactory.setMessageConverter(messageConverter); // ← add
```
