```
package de.consorsbank.core.trauthsc.integration.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import javax.sql.DataSource;

@TestConfiguration
@Profile("integration-test")
public class TestDataSourceConfig {

    @Bean("tamDataSource")
    public DataSource tamDataSource() {
        return new HikariDataSource(hikariConfig("tamdb"));
    }

    @Bean("pvmDataSource")
    public DataSource pvmDataSource() {
        return new HikariDataSource(hikariConfig("pvmdb"));
    }

    @Bean("auditRoutingDataSource")
    public DataSource auditRoutingDataSource() {
        return new HikariDataSource(hikariConfig("tamdb")); // shares tam
    }

    private com.zaxxer.hikari.HikariConfig hikariConfig(String dbName) {
        var config = new com.zaxxer.hikari.HikariConfig();
        config.setJdbcUrl(
            "jdbc:h2:mem:" + dbName + ";" +
            "DB_CLOSE_DELAY=-1;" +
            "MODE=Oracle;" +
            "DATABASE_TO_LOWER=true"
        );
        config.setUsername("sa");
        config.setPassword("");
        config.setDriverClassName("org.h2.Driver");
        config.setMaximumPoolSize(10);
        return config;
    }
}
```

```
package de.consorsbank.core.trauthsc.integration.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@TestConfiguration
@Profile("integration-test")
public class TestTransactionManagerConfig {

    @Bean("jtaTransactionManager")
    @Primary
    public PlatformTransactionManager transactionManager(
            @Qualifier("tamEntityManagerFactory") 
            EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}
```


```
package de.consorsbank.core.trauthsc.integration.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import javax.sql.DataSource;

@TestConfiguration
@Profile("integration-test")
public class TestJpaConfig {

    @Bean("tamEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean tamEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("tamDataSource") DataSource tamDataSource) {
        return builder
            .dataSource(tamDataSource)
            .packages("de.consorsbank.core.trauthsc.tam.entity")
            .persistenceUnit("tam")
            .build();
    }

    @Bean("pvmEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean pvmEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("pvmDataSource") DataSource pvmDataSource) {
        return builder
            .dataSource(pvmDataSource)
            .packages("de.consorsbank.core.trauthsc.pvm.entity")
            .persistenceUnit("pvm")
            .build();
    }

    @Bean("auditEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean auditEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("auditRoutingDataSource") DataSource auditDataSource) {
        return builder
            .dataSource(auditDataSource)
            .packages("com.consorsbank.common.audit.entity")
            .persistenceUnit("audit")
            .build();
    }
}
```


```
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integration-test")
@ContextConfiguration(classes = {
    WireMockConfig.class,
    TestDataSourceConfig.class,
    TestTransactionManagerConfig.class,
    TestJpaConfig.class
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
spring:
  main:
    allow-bean-definition-override: true

  jta:
    enabled: false

  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
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
    liquibase: INFO
```

```

testImplementation 'com.h2database:h2'

```
