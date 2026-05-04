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
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Integration tests verifying @Transactional behaviour of ExternalEventMessageListener.
 *
 * ROOT CAUSE OF PREVIOUS FAILURES:
 * Tests were sharing the same Spring context + H2 in-memory DB.
 * Data committed by test A was still visible when test B ran.
 * Result: "Expected size: 1 but was: 3", "Expected empty but was: [3 entities]".
 *
 * FIX: @DirtiesContext(AFTER_EACH_TEST_METHOD) forces a fresh Spring context
 * (and fresh H2 DB) for every test. This guarantees isolation.
 *
 * Trade-off: slower startup per test. For large test suites consider
 * using @Sql("classpath:truncate.sql") instead as a lighter alternative.
 */
@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ExternalEventMessageListenerTransactionTest {

    @Autowired
    private JmsTemplate jmsTemplate;

    /**
     * @SpyBean keeps the real bean wired into Spring context,
     * but allows us to stub/verify individual method calls.
     * This is critical — @MockBean would remove the real repo,
     * breaking actual DB interaction we want to test.
     */
    @SpyBean
    private DummyRepository dummyRepository;

    @Value("${public-message-connector.activemq.queue}")
    private String queue;

    @BeforeEach
    void setUp() {
        reset(dummyRepository);
    }

    @AfterEach
    void tearDown() {
        try {
            dummyRepository.deleteAll();
        } catch (Exception ignored) {
            // May fail if context is already being torn down — safe to ignore
        }
    }

    // -----------------------------------------------------------------------
    // CU @Transactional, FARA exceptie → commit
    // -----------------------------------------------------------------------
    @Test
    @DisplayName("Cu @Transactional, fara exceptie → entity committed to DB")
    void withTransactional_noException_entityIsCommitted() {
        // Sanity: DB must be empty before we start
        assertThat(dummyRepository.findAll())
                .as("DB should be empty at test start")
                .isEmpty();

        // when
        jmsTemplate.convertAndSend(queue, "ok-payload");

        // then — exactly 1 entity committed
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
        // given — force save() to always throw → triggers @Transactional rollback
        AtomicInteger callCount = new AtomicInteger(0);
        doAnswer(inv -> {
            callCount.incrementAndGet();
            throw new RuntimeException("Simulated DB error – rollback expected");
        }).when(dummyRepository).save(any(DummyEntity.class));

        // Sanity: DB empty before test
        assertThat(dummyRepository.findAll()).isEmpty();

        // when
        jmsTemplate.convertAndSend(queue, "fail-payload");

        // Wait for all redeliveries to exhaust:
        // maxRedeliveries=2 in test config → 3 total calls (initial + 2 redeliveries)
        await()
                .atMost(30, TimeUnit.SECONDS)
                .pollInterval(500, TimeUnit.MILLISECONDS)
                .until(() -> callCount.get() >= 3);

        // then — DB must be empty: every transaction was rolled back
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
        // Direct instantiation bypasses Spring AOP proxy → no @Transactional
        ExternalEventMessageListener rawListener =
                new ExternalEventMessageListener(dummyRepository, jmsTemplate);

        TextMessage textMessage = mock(TextMessage.class);
        when(textMessage.getText()).thenReturn("direct-call-payload");

        assertThat(dummyRepository.findAll()).isEmpty();

        // when — no proxy, no TX wrapping
        rawListener.onMessage(textMessage);

        // then — method logic ran and save was called; entity exists in DB
        verify(dummyRepository, times(1)).save(any());
        assertThat(dummyRepository.findAll()).hasSize(1);
    }

    // -----------------------------------------------------------------------
    // Via Spring proxy → exactly 1 save, no duplicates
    // -----------------------------------------------------------------------
    @Test
    @DisplayName("Via Spring proxy → save called exactly once within transaction")
    void viaSpringProxy_saveIsCalledExactlyOnce() {
        assertThat(dummyRepository.findAll()).isEmpty();

        jmsTemplate.convertAndSend(queue, "tx-verify-payload");

        await()
                .atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() -> assertThat(dummyRepository.count()).isEqualTo(1L));

        // Wait extra to confirm no duplicate processing
        try { Thread.sleep(500); } catch (InterruptedException ignored) {}

        assertThat(dummyRepository.count())
                .as("Exactly one entity should exist — no duplicate processing")
                .isEqualTo(1L);

        verify(dummyRepository, times(1)).save(any());
    }
}
```
