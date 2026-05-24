@Component
@Slf4j
public class InitiateTransactionEventSender {

    private final JmsTemplate jmsTemplate;

    @Value("${spring-artemis.activemq.queue}")
    private String queue;

    public InitiateTransactionEventSender(
            @Qualifier("atomikosConnectionFactory") ConnectionFactory connectionFactory) {
        this.jmsTemplate = new JmsTemplate(connectionFactory);
    }

    public void sendMessage(UUID transactionId, AuthorizationStatusEnum authorizationStatus) {
        log.debug("Sending event for transactionId {} with status {} to queue.", 
            transactionId, authorizationStatus);
        jmsTemplate.convertAndSend(queue, 
            new TransactionAuthorizationEvent(transactionId, authorizationStatus));
    }
}
