```
package de.consorsbank.core.trauthsc.transactionprocessor.listener;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.transaction.annotation.Transactional;

/**
 * Test-only helper that cleans the DB inside a proper @Transactional boundary.
 *
 * WHY THIS EXISTS:
 * Atomikos XA transaction manager requires that the EntityManager is enlisted
 * via Spring's @Transactional proxy — not via TransactionTemplate + native query.
 * Using TransactionTemplate directly causes:
 *   "No active transaction for update or delete query"
 * because Atomikos doesn't see the EntityManager as part of its XA transaction.
 *
 * @TestComponent makes it available only in test context (not production).
 */
@TestComponent
public class DatabaseCleaner {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public void cleanDummyTable() {
        entityManager.createNativeQuery("DELETE FROM dummy_entity").executeUpdate();
        entityManager.flush();
        entityManager.clear();
    }
}


```


```
package de.consorsbank.core.trauthsc.transactionprocessor.listener;

import de.consorsbank.core.trauthsc.transactionprocessor.model.DummyEntity;
import de.consorsbank.core.trauthsc.transactionprocessor.repository.DummyRepository;
import jakarta.jms.TextMessage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jms.config.JmsListenerEndpointRegistry;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Fix history:
 * 1. "Expected 0 but was 3"     → JMS listener racing with @BeforeEach → fixed with registry stop/start
 * 2. "No active transaction"     → flush() via SpyBean proxy → fixed with EntityManager direct
 * 3. "No active tx for delete"   → TransactionTemplate + native query + Atomikos XA → fixed with
 *                                   @TestComponent DatabaseCleaner that uses @Transactional proxy,
 *                                   which Atomikos correctly enlists in the XA transaction.
 */
@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Import(DatabaseCleaner.class)  // bring in the @TestComponent cleaner
class ExternalEventMessageListenerTransactionTest {

    @Autowired
    private JmsTemplate jmsTemplate;

    @MockitoSpyBean
    private DummyRepository dummyRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private JmsListenerEndpointRegistry jmsListenerEndpointRegistry;

    /**
     * Injected @TestComponent — uses @Transactional so Atomikos XA
     * properly enlists the EntityManager. This is the ONLY safe way
     * to run native DELETE with Atomikos.
     */
    @Autowired
    private DatabaseCleaner databaseCleaner;

    @Value("${public-message-connector.activemq.queue}")
    private String queue;

    @BeforeEach
    void setUp() {
        // 1. Stop JMS listeners — prevent async inserts racing with cleanup
        jmsListenerEndpointRegistry.stop();

        // 2. Drain any in-flight listener execution
        try { Thread.sleep(300); } catch (InterruptedException ignored) {}

        // 3. Clean DB via @Transactional proxy — Atomikos XA safe
        databaseCleaner.cleanDummyTable();

        // 4. Reset spy counters AFTER cleanup
        reset(dummyRepository);

        // 5. Verify DB is truly empty before test starts
        assertThat(dummyRepository.count())
                .as("DB must be empty before test starts")
                .isZero();

        // 6. Restart listeners for the test
        jmsListenerEndpointRegistry.start();
    }

    @AfterEach
    void tearDown() {
        jmsListenerEndpointRegistry.stop();
        reset(dummyRepository);
    }

    // -----------------------------------------------------------------------
    // CU @Transactional, FARA exceptie → commit
    // -----------------------------------------------------------------------
    @Test
    @DisplayName("Cu @Transactional, fara exceptie → entity committed to DB")
    void withTransactional_noException_entityIsCommitted() {
        jmsTemplate.convertAndSend(queue, "ok-payload");

        await()
                .atMost(10, TimeUnit.SECONDS)
                .pollInterval(200, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {
                    var all = dummyRepository.findAll();
                    assertThat(all).hasSize(1);
                    assertThat(all.get(0).getPayload()).isEqualTo("ok-payload");
                    assertThat(all.get(0).getStatus()).isEqualTo("PROCESSED");
                });
    }

    // -----------------------------------------------------------------------
    // CU @Transactional, CU exceptie → rollback
    // -----------------------------------------------------------------------
    @Test
    @DisplayName("Cu @Transactional, cu exceptie → rollback, entity NOT in DB")
    void withTransactional_withException_entityIsRolledBack() {
        AtomicInteger callCount = new AtomicInteger(0);
        doAnswer(inv -> {
            callCount.incrementAndGet();
            throw new RuntimeException("Simulated DB error – rollback expected");
        }).when(dummyRepository).save(any(DummyEntity.class));

        jmsTemplate.convertAndSend(queue, "fail-payload");

        // maxRedeliveries=2 → 3 total attempts
        await()
                .atMost(30, TimeUnit.SECONDS)
                .pollInterval(500, TimeUnit.MILLISECONDS)
                .until(() -> callCount.get() >= 3);

        doCallRealMethod().when(dummyRepository).findAll();
        assertThat(dummyRepository.findAll())
                .as("All transactions should have been rolled back — DB must be empty")
                .isEmpty();
    }

    // -----------------------------------------------------------------------
    // FARA proxy Spring → instantiere directa, fara TX boundary
    // -----------------------------------------------------------------------
    @Test
    @DisplayName("Fara proxy Spring → metoda se executa, dar fara TX boundary")
    void withoutTransactionalProxy_methodExecutesDirectly() throws Exception {
        ExternalEventMessageListener rawListener =
                new ExternalEventMessageListener(dummyRepository, jmsTemplate);

        TextMessage textMessage = mock(TextMessage.class);
        when(textMessage.getText()).thenReturn("direct-call-payload");

        rawListener.onMessage(textMessage);

        verify(dummyRepository, times(1)).save(any());

        new TransactionTemplate(transactionManager).execute(status -> {
            var all = dummyRepository.findAll();
            assertThat(all).hasSize(1);
            assertThat(all.get(0).getPayload()).isEqualTo("direct-call-payload");
            return null;
        });
    }

    // -----------------------------------------------------------------------
    // Via Spring proxy → exactly 1 save, no duplicates
    // -----------------------------------------------------------------------
    @Test
    @DisplayName("Via Spring proxy → save called exactly once within transaction")
    void viaSpringProxy_saveIsCalledExactlyOnce() {
        jmsTemplate.convertAndSend(queue, "tx-verify-payload");

        await()
                .atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() -> assertThat(dummyRepository.count()).isEqualTo(1L));

        try { Thread.sleep(500); } catch (InterruptedException ignored) {}

        assertThat(dummyRepository.count())
                .as("Exactly one entity — no duplicate processing")
                .isEqualTo(1L);

        verify(dummyRepository, times(1)).save(any());
    }
}

```
