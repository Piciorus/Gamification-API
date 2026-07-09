```
package de.consorsbank.core.trauthsc.integration;

import com.github.tomakehurst.wiremock.WireMockServer;
import de.consorsbank.core.trauthsc.integration.wiremock.WireMockConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integration-test")
@ContextConfiguration(classes = {WireMockConfig.class})
@AutoConfigureTestRestTemplate
public abstract class IntegrationBaseTest {

    protected static final String LOCALHOST = "http://localhost:";

    // Singleton container — started once, shared across all test classes
    static final GlobalPostgresContainer postgres =
        GlobalPostgresContainer.getInstance();

    @LocalServerPort
    protected int port;

    @Autowired
    protected TestRestTemplate testRestTemplate;

    @Autowired
    protected WireMockServer mockAuthorizationService;

    /**
     * Wires the running Postgres container into Spring's datasource properties
     * before the application context starts.
     *
     * Overrides both the TAM and PVM datasource configs so Atomikos XA
     * connects to the container instead of the production DB.
     */
    @DynamicPropertySource
    static void configureDataSources(DynamicPropertyRegistry registry) {

        // ── TAM datasource ────────────────────────────────────────────────────
        registry.add("spring.datasource.tam.url",
            postgres::getJdbcUrl);
        registry.add("spring.datasource.tam.username",
            postgres::getUsername);
        registry.add("spring.datasource.tam.password",
            postgres::getPassword);
        // Atomikos reads XA properties separately
        registry.add("spring.datasource.tam.xa-properties.URL",
            postgres::getJdbcUrl);
        registry.add("spring.datasource.tam.xa-properties.user",
            postgres::getUsername);
        registry.add("spring.datasource.tam.xa-properties.password",
            postgres::getPassword);
        // Static name — Atomikos requires stable uniqueResourceName
        registry.add("spring.datasource.tam.configuration.uniqueResourceName",
            () -> "tam-test");

        // ── PVM datasource ────────────────────────────────────────────────────
        registry.add("spring.datasource.pvm.url",
            postgres::getJdbcUrl);
        registry.add("spring.datasource.pvm.username",
            postgres::getUsername);
        registry.add("spring.datasource.pvm.password",
            postgres::getPassword);
        registry.add("spring.datasource.pvm.xa-properties.URL",
            postgres::getJdbcUrl);
        registry.add("spring.datasource.pvm.xa-properties.user",
            postgres::getUsername);
        registry.add("spring.datasource.pvm.xa-properties.password",
            postgres::getPassword);
        registry.add("spring.datasource.pvm.configuration.uniqueResourceName",
            () -> "pvm-test");
    }

    protected String url() {
        return LOCALHOST + port + path();
    }

    protected abstract String path();
}
```


```
# ─────────────────────────────────────────────────────────────────────────────
# Integration test profile
# DB connection properties are injected at runtime by @DynamicPropertySource
# in IntegrationBaseTest — no hardcoded URLs here.
# ─────────────────────────────────────────────────────────────────────────────

spring:

  # Atomikos XA works fine with Postgres — no JTA override needed
  jpa:
    database-platform: org.hibernate.dialect.PostgreSQLDialect
    hibernate:
      ddl-auto: none
    show-sql: true
    properties:
      hibernate:
        format_sql: true

  # Datasource pool sizing — smaller for tests
  datasource:
    tam:
      configuration:
        minPoolSize: 1
        maxPoolSize: 5
        idle-timeout: 60
        borrowConnectionTimeout: 30
      xa-data-source-class-name: org.postgresql.xa.PGXADataSource
      driverClassName: org.postgresql.Driver

    pvm:
      configuration:
        minPoolSize: 1
        maxPoolSize: 5
        idle-timeout: 60
        borrowConnectionTimeout: 30
      xa-data-source-class-name: org.postgresql.xa.PGXADataSource
      driverClassName: org.postgresql.Driver

liquibase:
  tam:
    change-log: classpath:/db/changelog/changelog-master-tam.yaml
    enabled: true
    drop-first: false
  pvm:
    change-log: classpath:/db/changelog/changelog-master-pvm.yaml
    enabled: true
    drop-first: false

# WireMock port for downstream authorization service stub
wiremock:
  server:
    port: 9680

# Disable Kafka for integration tests — use mocks if needed
spring.kafka.bootstrap-servers: localhost:9092
spring.autoconfigure.exclude:
  - org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration

logging:
  level:
    de.consorsbank: DEBUG
    org.springframework.web: DEBUG
    org.springframework.transaction: DEBUG
    com.atomikos: WARN
    liquibase: INFO
```

```

```
