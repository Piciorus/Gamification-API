```
package de.consorsbank.core.trauthsc.integration;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
public class GlobalPostgresContainer 
        extends PostgreSQLContainer<GlobalPostgresContainer> {

    // Use the same internal registry image as brkprcsc
    private static final String IMAGE_NAME = 
        "i-ckdregistry.pro.be.xpi.net.intra/dockerhub/postgres:17";
    private static final String POSTGRES = "postgres";

    private static final DockerImageName IMAGE = 
        DockerImageName.parse(IMAGE_NAME)
            .asCompatibleSubstituteFor(POSTGRES);

    private static GlobalPostgresContainer container;

    private GlobalPostgresContainer() {
        super(IMAGE);
    }

    public static GlobalPostgresContainer getInstance() {
        if (container == null) {
            container = new GlobalPostgresContainer();
            container.start();
        }
        return container;
    }
}
```


```
package de.consorsbank.core.trauthsc.integration;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import de.consorsbank.core.trauthsc.integration.wiremock.WireMockConfig;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integration-test")
@ContextConfiguration(classes = {WireMockConfig.class})
@AutoConfigureTestRestTemplate
public abstract class IntegrationBaseTest {

    protected static final String LOCALHOST = "http://localhost:";

    private static final GlobalPostgresContainer postgres = 
        GlobalPostgresContainer.getInstance();

    @LocalServerPort
    protected int port;

    @Autowired
    protected TestRestTemplate testRestTemplate;

    @Autowired
    protected WireMockServer mockAuthorizationService;

    @DynamicPropertySource
    static void configureDataSource(DynamicPropertyRegistry registry) {
        // TAM datasource — XA properties
        registry.add("spring.datasource.tam.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.tam.username", postgres::getUsername);
        registry.add("spring.datasource.tam.password", postgres::getPassword);
        registry.add("spring.datasource.tam.xa-properties.URL", postgres::getJdbcUrl);
        registry.add("spring.datasource.tam.xa-properties.user", postgres::getUsername);
        registry.add("spring.datasource.tam.xa-properties.password", postgres::getPassword);
        registry.add("spring.datasource.tam.configuration.uniqueResourceName", 
            () -> "tam-test");

        // PVM datasource — same container, different schema
        registry.add("spring.datasource.pvm.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.pvm.username", postgres::getUsername);
        registry.add("spring.datasource.pvm.password", postgres::getPassword);
        registry.add("spring.datasource.pvm.xa-properties.URL", postgres::getJdbcUrl);
        registry.add("spring.datasource.pvm.xa-properties.user", postgres::getUsername);
        registry.add("spring.datasource.pvm.xa-properties.password", postgres::getPassword);
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
spring:
  datasource:
    tam:
      driverClassName: org.postgresql.Driver
      xa-data-source-class-name: org.postgresql.xa.PGXADataSource
      configuration:
        minPoolSize: 1
        maxPoolSize: 5
        readonly: false
        idle-timeout: 240
        borrowConnectionTimeout: 5

    pvm:
      driverClassName: org.postgresql.Driver
      xa-data-source-class-name: org.postgresql.xa.PGXADataSource
      configuration:
        minPoolSize: 1
        maxPoolSize: 5
        readonly: false
        idle-timeout: 240
        borrowConnectionTimeout: 5

  jpa:
    database-platform: org.hibernate.dialect.PostgreSQLDialect
    hibernate:
      ddl-auto: none
    show-sql: true

liquibase:
  tam:
    change-log: classpath:/db/changelog/changelog-master-tam.yaml
    enabled: true
    drop-first: false
  pvm:
    change-log: classpath:/db/changelog/changelog-master-pvm.yaml
    enabled: true
    drop-first: false

logging:
  level:
    de.consorsbank: DEBUG
    org.springframework.web: DEBUG
```


```
// build.gradle — testImplementation dependencies
testImplementation 'org.testcontainers:postgresql'
testImplementation 'org.testcontainers:junit-jupiter'
```
