docker pull container-registry.oracle.com/database/express:21.3.0-xe



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

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Import(DatabaseCleaner.class)
class ExternalEventMessageListenerTransactionTest {

    @Autowired
    private JmsTemplate jmsTemplate;

    @MockitoSpyBean
    private DummyRepository dummyRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private JmsListenerEndpointRegistry jmsListenerEndpointRegistry;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @Value("${public-message-connector.activemq.queue}")
    private String queue;

    @BeforeEach
    void setUp() throws Exception {
        // 1. Stop JMS listeners — prevent async inserts racing with cleanup
        jmsListenerEndpointRegistry.stop();

        // 2. Drain any in-flight listener execution
        try { Thread.sleep(300); } catch (InterruptedException ignored) {}

        // 3. Reset spy BEFORE clean so deleteAll() calls aren't counted
        reset(dummyRepository);

        // 4. Clean DB via JTA UserTransaction directly (Atomikos-safe)
        databaseCleaner.cleanDummyTable();

        // 5. Reset spy again AFTER clean so test starts with zero interactions
        reset(dummyRepository);

        // 6. Verify DB is truly empty
        assertThat(dummyRepository.count())
                .as("DB must be empty before test starts")
                .isZero();

        // 7. Restart listeners for the test
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
                .as("All transactions rolled back — DB must be empty")
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
```

package de.consorsbank.core.trauthsc.transactionprocessor.listener;

import com.atomikos.icatch.jta.UserTransactionImp;
import de.consorsbank.core.trauthsc.transactionprocessor.repository.DummyRepository;
import jakarta.transaction.UserTransaction;
import org.springframework.boot.test.context.TestComponent;

/**
 * Cleans the DB using JTA UserTransaction directly.
 *
 * WHY DIRECT JTA:
 * - @Transactional("transactionManager") fails if the bean name doesn't match
 * - TransactionTemplate + native query: Atomikos doesn't enlist EntityManager
 * - deleteAllInBatch() uses a bulk JPQL query which also needs TX enlistment
 *
 * Using UserTransaction directly (Atomikos UserTransactionImp) is the most
 * reliable way to open an XA transaction in a test without relying on
 * Spring's AOP proxy or bean name resolution.
 */
@TestComponent
public class DatabaseCleaner {

    private final DummyRepository dummyRepository;

    public DatabaseCleaner(DummyRepository dummyRepository) {
        this.dummyRepository = dummyRepository;
    }

    public void cleanDummyTable() throws Exception {
        UserTransaction utx = new UserTransactionImp();
        utx.begin();
        try {
            dummyRepository.deleteAll();
            utx.commit();
        } catch (Exception e) {
            utx.rollback();
            throw e;
        }
    }
}
```
