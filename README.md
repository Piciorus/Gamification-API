```
package de.consorsbank.core.trauthsc.transactionprocessor.listener;

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
 * Integration tests verifying @Transactional behaviour.
 *
 * ROOT CAUSE OF FAILURES:
 * 1. H2 in-memory DB uses a fixed URL → same DB instance shared across
 *    all contexts even after @DirtiesContext restarts Spring.
 *    Fix: explicit TRUNCATE in @BeforeEach inside a real TX.
 *
 * 2. Wrong field accessors in assertions:
 *    getDescription() / getName() → corrected to getPayload() / getStatus()
 */
@SpringBootTest
@ActiveProfiles("test")
// Keep DirtiesContext to also reset JMS listener state & Atomikos pool
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ExternalEventMessageListenerTransactionTest {

    @Autowired
    private JmsTemplate jmsTemplate;

    @SpyBean
    private DummyRepository dummyRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Value("${public-message-connector.activemq.queue}")
    private String queue;

    /**
     * CRITICAL FIX: Truncate the table inside an explicit TX before each test.
     * This is necessary because H2 in-memory with a fixed URL survives
     * Spring context restarts — @DirtiesContext alone is NOT enough.
     */
    @BeforeEach
    void cleanDatabase() {
        new TransactionTemplate(transactionManager).execute(status -> {
            dummyRepository.deleteAll();
            dummyRepository.flush(); // force immediate DELETE, not deferred
            return null;
        });
        reset(dummyRepository); // reset spy counters after deleteAll()

        // Verify the cleanup actually worked before proceeding
        assertThat(dummyRepository.count())
                .as("DB must be empty before test starts")
                .isZero();
    }

    @AfterEach
    void resetSpy() {
        reset(dummyRepository);
    }

    // -----------------------------------------------------------------------
    // CU @Transactional, FARA exceptie → commit
    // -----------------------------------------------------------------------
    @Test
    @DisplayName("Cu @Transactional, fara exceptie → entity committed to DB")
    void withTransactional_noException_entityIsCommitted() {
        // when
        jmsTemplate.convertAndSend(queue, "ok-payload");

        // then — exactly 1 entity committed with correct fields
        await()
                .atMost(10, TimeUnit.SECONDS)
                .pollInterval(200, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {
                    var all = dummyRepository.findAll();
                    assertThat(all).hasSize(1);
                    // FIX: was getDescription()/getName() → correct fields:
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
        // given — force save() to always throw → @Transactional rolls back
        AtomicInteger callCount = new AtomicInteger(0);
        doAnswer(inv -> {
            callCount.incrementAndGet();
            throw new RuntimeException("Simulated DB error – rollback expected");
        }).when(dummyRepository).save(any(DummyEntity.class));

        // when
        jmsTemplate.convertAndSend(queue, "fail-payload");

        // Wait for all redeliveries to exhaust:
        // maxRedeliveries=2 → 3 total attempts (initial + 2 redeliveries)
        await()
                .atMost(30, TimeUnit.SECONDS)
                .pollInterval(500, TimeUnit.MILLISECONDS)
                .until(() -> callCount.get() >= 3);

        // then — DB must be empty: every TX was rolled back
        // Restore real findAll() to bypass the save() stub
        doCallRealMethod().when(dummyRepository).findAll();
        assertThat(dummyRepository.findAll())
                .as("All transactions should have been rolled back")
                .isEmpty();
    }

    // -----------------------------------------------------------------------
    // FARA proxy Spring — instantiere directa, fara TX
    // -----------------------------------------------------------------------
    @Test
    @DisplayName("Fara proxy Spring → metoda se executa, dar fara TX boundary")
    void withoutTransactionalProxy_methodExecutesDirectly() throws Exception {
        // Direct instantiation → no Spring AOP proxy → no @Transactional
        ExternalEventMessageListener rawListener =
                new ExternalEventMessageListener(dummyRepository, jmsTemplate);

        TextMessage textMessage = mock(TextMessage.class);
        when(textMessage.getText()).thenReturn("direct-call-payload");

        // when
        rawListener.onMessage(textMessage);

        // then — save was called; entity persisted (no TX to rollback since no proxy)
        verify(dummyRepository, times(1)).save(any());

        new TransactionTemplate(transactionManager).execute(status -> {
            assertThat(dummyRepository.findAll()).hasSize(1);
            assertThat(dummyRepository.findAll().get(0).getPayload())
                    .isEqualTo("direct-call-payload");
            return null;
        });
    }

    // -----------------------------------------------------------------------
    // Via Spring proxy → exactly 1 save, no duplicates
    // -----------------------------------------------------------------------
    @Test
    @DisplayName("Via Spring proxy → save called exactly once within transaction")
    void viaSpringProxy_saveIsCalledExactlyOnce() {
        // when
        jmsTemplate.convertAndSend(queue, "tx-verify-payload");

        // then
        await()
                .atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() -> assertThat(dummyRepository.count()).isEqualTo(1L));

        // Extra wait to confirm no duplicate processing
        try { Thread.sleep(500); } catch (InterruptedException ignored) {}

        assertThat(dummyRepository.count())
                .as("Exactly one entity — no duplicate processing")
                .isEqualTo(1L);

        verify(dummyRepository, times(1)).save(any());
    }
}

```
