```
package de.consorsbank.core.trauthsc.integration.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import javax.sql.DataSource;

@TestConfiguration
@Profile("integration-test")
public class TestDataSourceConfig {

    @Bean("tamDataSource")
    @Primary
    public DataSource tamDataSource() {
        var ds = new HikariDataSource();
        ds.setJdbcUrl("jdbc:h2:mem:tam;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;DATABASE_TO_LOWER=true;DEFAULT_NULL_ORDERING=HIGH");
        ds.setUsername("sa");
        ds.setPassword("");
        ds.setDriverClassName("org.h2.Driver");
        return ds;
    }

    @Bean("pvmDataSource")
    @Primary
    public DataSource pvmDataSource() {
        var ds = new HikariDataSource();
        ds.setJdbcUrl("jdbc:h2:mem:pvm;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;DATABASE_TO_LOWER=true;DEFAULT_NULL_ORDERING=HIGH");
        ds.setUsername("sa");
        ds.setPassword("");
        ds.setDriverClassName("org.h2.Driver");
        return ds;
    }
}

```

```
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integration-test")
@ContextConfiguration(classes = {
    WireMockConfig.class,
    TestDataSourceConfig.class,        // ← add
    TestTransactionManagerConfig.class // ← add
})
@AutoConfigureTestRestTemplate
public abstract class IntegrationBaseTest {

    protected static final String LOCALHOST = "http://localhost:";

    @LocalServerPort
    protected int port;

    @Autowired
    protected TestRestTemplate testRestTemplate;

    @Autowired
    protected WireMockServer mockAuthorizationService;

    protected String url() {
        return LOCALHOST + port + path();
    }

    protected abstract String path();
}
```


```
package de.consorsbank.core.trauthsc.integration.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;

@TestConfiguration
@Profile("integration-test")
public class TestTransactionManagerConfig {

    @Bean("jtaTransactionManager")
    @Primary
    public PlatformTransactionManager transactionManager(
            @Qualifier("tamDataSource") DataSource tamDataSource) {
        return new DataSourceTransactionManager(tamDataSource);
    }
}
```


```
spring:
  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    hibernate:
      ddl-auto: none
    show-sql: true

  # Disable JTA entirely for tests
  jta:
    enabled: false

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
    org.springframework.transaction: DEBUG
```
