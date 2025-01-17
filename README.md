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

