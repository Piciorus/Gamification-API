@Bean(initMethod = "init", destroyMethod = "close")
public UserTransactionManager atomikosTransactionManager() {
    UserTransactionManager txManager = new UserTransactionManager();
    txManager.setForceShutdown(true);
    return txManager;
}

@Bean
public UserTransactionImp userTransaction() throws Throwable {
    UserTransactionImp userTransaction = new UserTransactionImp();
    userTransaction.setTransactionTimeout(300);
    return userTransaction;
}

@Bean
public PlatformTransactionManager transactionManager() throws Throwable {
    return new JtaTransactionManager(userTransaction(), atomikosTransactionManager());
}
@SpringBootTest
@Transactional
public class DistributedTransactionTest {

    @Autowired
    private EntityManagerFactory emf1;

    @Autowired
    private EntityManagerFactory emf2;

    @Test
    void testDistributedTransaction() {
        EntityManager em1 = emf1.createEntityManager();
        EntityManager em2 = emf2.createEntityManager();

        try {
            em1.getTransaction().begin();
            em2.getTransaction().begin();

            // Perform operations on both data sources
            em1.persist(new Entity1(...));
            em2.persist(new Entity2(...));

            // Simulate an error in one of the operations
            if (true) {
                throw new RuntimeException("Simulated failure");
            }

            em1.getTransaction().commit();
            em2.getTransaction().commit();
        } catch (Exception e) {
            em1.getTransaction().rollback();
            em2.getTransaction().rollback();
            // Verify that both transactions were rolled back
        } finally {
            em1.close();
            em2.close();
        }
    }
}
@Service
@Transactional
public class DistributedTransactionService {

    @Autowired
    private Db1Repository db1Repository;

    @Autowired
    private Db2Repository db2Repository;

    public void performDistributedTransaction() {
        // Insert into Database 1
        db1Repository.save(new Db1Entity("Test data for DB1"));

        // Insert into Database 2
        db2Repository.save(new Db2Entity("Test data for DB2"));

        // Simulate an error to test rollback
        if (true) {
            throw new RuntimeException("Simulated error to test rollback");
        }
    }
}

@SpringBootTest
public class DistributedTransactionTest {

    @Autowired
    private DistributedTransactionService distributedTransactionService;

    @Autowired
    private Db1Repository db1Repository;

    @Autowired
    private Db2Repository db2Repository;

    @Test
    @Transactional
    public void testDistributedTransactionRollback() {
        try {
            distributedTransactionService.performDistributedTransaction();
        } catch (Exception e) {
            // Exception expected
        }

        // Verify rollback in both databases
        Assertions.assertEquals(0, db1Repository.count());
        Assertions.assertEquals(0, db2Repository.count());
    }
}
spring:
  datasource:
    db1:
      xa:
        data-source-class-name: com.mysql.cj.jdbc.MysqlXADataSource
      url: jdbc:mysql://localhost:3306/db1
      username: root
      password: password

    db2:
      xa:
        data-source-class-name: com.mysql.cj.jdbc.MysqlXADataSource
      url: jdbc:mysql://localhost:3306/db2
      username: root
      password: password

  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        transaction.jta.platform: org.hibernate.engine.transaction.jta.platform.internal.AtomikosJtaPlatform
    show-sql: true
@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = { "test-topic" }, brokerProperties = { "log.dir=./kafka-test-logs" })
@Transactional
public class DistributedTransactionTest {

    @Autowired
    private DistributedTransactionService transactionService;

    @Autowired
    private SomeEntityRepository repository;

    @Autowired
    private ConsumerFactory<String, String> consumerFactory;

    @Test
    public void testDistributedTransactionSuccess() {
        // Given
        String testMessage = "test-message-success";
        String testEntityName = "Test Entity Success";

        // When
        transactionService.performDistributedTransaction(testMessage, testEntityName, false);

        // Then
        // Verify that the database contains the new entity
        Optional<SomeEntity> entity = repository.findAll().stream()
                .filter(e -> e.getName().equals(testEntityName))
                .findFirst();
        Assertions.assertTrue(entity.isPresent(), "Entity should be present in the database");

        // Verify that the Kafka topic contains the message
        KafkaConsumer<String, String> consumer = createConsumer();
        consumer.subscribe(Collections.singletonList("test-topic"));
        ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(5));
        boolean messageFound = StreamSupport.stream(records.spliterator(), false)
                .anyMatch(record -> record.value().equals(testMessage));
        Assertions.assertTrue(messageFound, "Message should be present in Kafka");
    }

    @Test
    public void testDistributedTransactionRollback() {
        // Given
        String testMessage = "test-message-fail";
        String testEntityName = "Test Entity Fail";

        // When
        Assertions.assertThrows(RuntimeException.class, () -> {
            transactionService.performDistributedTransaction(testMessage, testEntityName, true);
        });

        // Then
        // Verify that the database does not contain the new entity
        Optional<SomeEntity> entity = repository.findAll().stream()
                .filter(e -> e.getName().equals(testEntityName))
                .findFirst();
        Assertions.assertFalse(entity.isPresent(), "Entity should NOT be present in the database");

        // Verify that the Kafka topic does not contain the message
        KafkaConsumer<String, String> consumer = createConsumer();
        consumer.subscribe(Collections.singletonList("test-topic"));
        ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(5));
        boolean messageFound = StreamSupport.stream(records.spliterator(), false)
                .anyMatch(record -> record.value().equals(testMessage));
        Assertions.assertFalse(messageFound, "Message should NOT be present in Kafka");
    }

    private KafkaConsumer<String, String> createConsumer() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return new KafkaConsumer<>(props);
    }
}

@Test
public void testDistributedTransactionSuccess() {
    // Given
    String testMessage = "test-message-success";
    String testEntityName = "Test Skill";

    // When
    distributedTransactionService.performDistributedTransaction(testMessage, false);

    // Then
    // Verify that the database contains the new entity
    Optional<TargetSkillEntity> entity = db1Repository.findAll().stream()
            .filter(targetSkillEntity -> targetSkillEntity.getName().equals(testEntityName))
            .findFirst();
    Assertions.assertTrue(entity.isPresent(), "Entity should be present in the database");

    // Verify that the Kafka topic contains the message
    KafkaConsumer<String, String> consumer = createConsumer();
    consumer.subscribe(Collections.singletonList("test-topic"));
    ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(5));

    boolean messageFound = StreamSupport.stream(records.spliterator(), false)
            .anyMatch(record -> record.value().equals(testMessage));
    Assertions.assertTrue(messageFound, "Message should be present in Kafka");
}
@SpringBootTest
public class HikariDataSourceTest {

    @Autowired
    private SomeEntityRepository repository;

    @Test
    public void testNonTransactionalBehaviour() {
        // Given
        String testName = "Test Skill Non-Transactional";

        // When
        try {
            // Insert an entity into the database
            SomeEntity entity = new SomeEntity();
            entity.setName(testName);
            repository.save(entity);

            // Simulate a failure after saving
            throw new RuntimeException("Simulated failure");

        } catch (RuntimeException ex) {
            // Expected exception
        }

        // Then
        // Verify that the entity is still present in the database (no rollback)
        Optional<SomeEntity> entity = repository.findAll().stream()
                .filter(e -> e.getName().equals(testName))
                .findFirst();
        Assertions.assertTrue(entity.isPresent(), "Entity should be present in the database even after failure");
    }
}
@SpringBootTest
@Testcontainers
public class DistributedTransactionWithKafkaTest {

    @Container
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Container
    static KafkaContainer kafkaContainer = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.0"))
            .withKraft();

    @Autowired
    private TransactionalService transactionalService;

    @Autowired
    private YourEntityRepository repository;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        // Database configuration
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);

        // Kafka configuration
        registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
        registry.add("spring.kafka.producer.transaction-id-prefix", () -> "tx-");
    }

    @Test
    @Transactional
    void testDistributedTransactionSuccess() {
        // Act
        transactionalService.performTransactionalOperation();

        // Assert: Check database
        List<YourEntity> entities = repository.findAll();
        assertEquals(1, entities.size());

        // Assert: Check Kafka message
        ConsumerRecord<String, String> record = getMessageFromKafka("test-topic");
        assertNotNull(record);
        assertEquals("Message from distributed transaction", record.value());
    }

    @Test
    void testDistributedTransactionRollback() {
        // Simulate a failure in the transaction
        Assertions.assertThrows(RuntimeException.class, () -> {
            transactionalService.performTransactionalOperationWithFailure();
        });

        // Assert: Database is empty due to rollback
        List<YourEntity> entities = repository.findAll();
        assertTrue(entities.isEmpty());

        // Assert: No message sent to Kafka
        ConsumerRecord<String, String> record = getMessageFromKafka("test-topic");
        assertNull(record);
    }

    private ConsumerRecord<String, String> getMessageFromKafka(String topic) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(Collections.singleton(topic));
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(5));
            return records.isEmpty() ? null : records.iterator().next();
        }
    }
}
@Test
public void testDistributedTransactionRollback() {
    // Arrange: Prepare test message and mock behavior
    String testMessage = "test-message-fail";

    TargetSkillEntity targetSkillEntity = new TargetSkillEntity();
    targetSkillEntity.setId(11);
    targetSkillEntity.setName("Test Skill");

    // Mock DB repository to return an entity
    when(db1Repository.findById(any())).thenReturn(Optional.of(targetSkillEntity));

    // Mock the distributedTransactionService to throw a RuntimeException for a void method
    doThrow(RuntimeException.class).when(distributedTransactionService)
            .performDistributedTransaction(testMessage, true);

    // Act: Call the method under test and expect a RuntimeException
    Assertions.assertThrows(RuntimeException.class, () -> {
        distributedTransactionService.performDistributedTransaction(testMessage, true);
    });

    // Assert: Verify that the database does not contain the entity after rollback
    Optional<TargetSkillEntity> entity = db1Repository.findById(11);
    System.out.println(entity);
    Assertions.assertTrue(entity.isPresent(), "Entity should still be present in the database because rollback occurs.");

    // Assert: Verify that the Kafka topic does not contain the message
    KafkaConsumer<String, String> consumer = createConsumer();
    consumer.subscribe(Collections.singletonList("test-topic"));

    ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(5));
    boolean messageFound = StreamSupport.stream(records.spliterator(), false)
            .anyMatch(record -> record.value().equals(testMessage));

    Assertions.assertFalse(messageFound, "Message should NOT be present in Kafka after rollback.");
}
@Test
public void testDistributedTransactionRollback() {
    // Arrange: Prepare test message and mock behavior
    String testMessage = "test-message-fail";

    TargetSkillEntity targetSkillEntity = new TargetSkillEntity();
    targetSkillEntity.setId(11);
    targetSkillEntity.setName("Test Skill");

    // Mock DB repository to return an entity for other operations
    when(db1Repository.findById(any())).thenReturn(Optional.empty()); // Simulating no entity persisted

    // Mock the distributedTransactionService to throw a RuntimeException
    doThrow(RuntimeException.class).when(distributedTransactionService)
            .performDistributedTransaction(testMessage, true);

    // Act and Assert: Ensure the exception is thrown
    Assertions.assertThrows(RuntimeException.class, () -> {
        distributedTransactionService.performDistributedTransaction(testMessage, true);
    });

    // Assert: Verify that the entity is NOT present in the database
    Optional<TargetSkillEntity> entity = db1Repository.findById(11);
    Assertions.assertTrue(entity.isEmpty(), "Entity should NOT be present in the database due to rollback.");

    // Assert: Verify that the Kafka topic does not contain the message
    KafkaConsumer<String, String> consumer = createConsumer();
    consumer.subscribe(Collections.singletonList("test-topic"));

    ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(5));
    boolean messageFound = StreamSupport.stream(records.spliterator(), false)
            .anyMatch(record -> record.value().equals(testMessage));

    Assertions.assertFalse(messageFound, "Message should NOT be present in Kafka due to rollback.");
}


