```package de.consorsbank.core.trauthsc.transactionprocessor.listener;

import de.consorsbank.core.trauthsc.transactionprocessor.model.DummyEntity;
import de.consorsbank.core.trauthsc.transactionprocessor.repository.DummyRepository;
import jakarta.jms.TextMessage;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.jms.config.JmsListenerEndpointRegistry;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * ROOT CAUSE OF "expected: 0L but was: 3L":
 *
 * The JMS listener runs ASYNCHRONOUSLY in a background thread.
 * When @BeforeEach fires for test N, the listener from test N-1
 * is STILL processing messages and inserting rows into H2.
 * So the deleteAll() completes, but the listener immediately inserts again.
 *
 * FIX STRATEGY:
 * 1. @AfterEach: STOP the JmsListenerEndpointRegistry (pauses all listeners)
 *    → no more async inserts can happen
 * 2. @BeforeEach: clean DB while listeners are stopped (safe, no racing)
 *    → then START the registry again for the next test
 *
 * This guarantees: deleteAll() runs in silence, count() check passes.
 */
@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ExternalEventMessageListenerTransactionTest {

    @Autowired
    private JmsTemplate jmsTemplate;

    @SpyBean
    private DummyRepository dummyRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * JmsListenerEndpointRegistry controls ALL @JmsListener containers.
     * Stopping it prevents the async listener from inserting during cleanup.
     */
    @Autowired
    private JmsListenerEndpointRegistry jmsListenerEndpointRegistry;

    @Value("${public-message-connector.activemq.queue}")
    private String queue;

    @BeforeEach
    void setUp() {
        // 1. Stop all JMS listeners → no more async inserts can race with cleanup
        jmsListenerEndpointRegistry.stop();

        // 2. Wait briefly for any in-flight listener execution to finish
        try { Thread.sleep(300); } catch (InterruptedException ignored) {}

        // 3. Clean DB while listeners are stopped — guaranteed no racing
        new TransactionTemplate(transactionManager).execute(status -> {
            entityManager.createNativeQuery("DELETE FROM dummy_entity").executeUpdate();
            return null;
        });

        // 4. Reset spy counters AFTER cleanup
        reset(dummyRepository);

        // 5. Verify DB is clean before starting the test
        assertThat(dummyRepository.count())
                .as("DB must be empty before test starts")
                .isZero();

        // 6. Restart listeners — ready for the next test
        jmsListenerEndpointRegistry.start();
    }

    @AfterEach
    void tearDown() {
        // Stop listeners after each test so @BeforeEach of the next test
        // can safely clean without racing against async processing
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

        // maxRedeliveries=2 → 3 total attempts (initial + 2 redeliveries)
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
        // Direct instantiation bypasses Spring AOP → no @Transactional
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

        // Extra wait to rule out duplicate processing
        try { Thread.sleep(500); } catch (InterruptedException ignored) {}

        assertThat(dummyRepository.count())
                .as("Exactly one entity — no duplicate processing")
                .isEqualTo(1L);

        verify(dummyRepository, times(1)).save(any());
    }
}



```
