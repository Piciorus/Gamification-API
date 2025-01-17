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
