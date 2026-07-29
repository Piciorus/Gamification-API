```
@TestConfiguration
@Profile("integration-test")
public class TestDataSourceConfig {

    @Bean("tamDataSource")
    @Primary
    @ConfigurationProperties("test.datasource.tam")
    public DataSource tamDataSource() {
        return DataSourceBuilder.create()
            .type(HikariDataSource.class)
            .build();
    }

    @Bean("pvmDataSource")
    @ConfigurationProperties("test.datasource.pvm")
    public DataSource pvmDataSource() {
        return DataSourceBuilder.create()
            .type(HikariDataSource.class)
            .build();
    }

    @Bean("auditRoutingDataSource")
    @ConfigurationProperties("test.datasource.audit")
    public DataSource auditRoutingDataSource() {
        return DataSourceBuilder.create()
            .type(HikariDataSource.class)
            .build();
    }
}
```


```
spring:
  main:
    allow-bean-definition-overriding: true

test:
  datasource:
    tam:
      jdbc-url: jdbc:h2:mem:tamdb;DB_CLOSE_DELAY=-1;MODE=Oracle;DATABASE_TO_LOWER=true
      username: sa
      password: ""
      driver-class-name: org.h2.Driver
      maximum-pool-size: 5
      connection-timeout: 30000
      idle-timeout: 120000
    pvm:
      jdbc-url: jdbc:h2:mem:pvmdb;DB_CLOSE_DELAY=-1;MODE=Oracle;DATABASE_TO_LOWER=true
      username: sa
      password: ""
      driver-class-name: org.h2.Driver
      maximum-pool-size: 5
    audit:
      jdbc-url: jdbc:h2:mem:tamdb;DB_CLOSE_DELAY=-1;MODE=Oracle;DATABASE_TO_LOWER=true
      username: sa
      password: ""
      driver-class-name: org.h2.Driver
      maximum-pool-size: 5
```

```
@TestConfiguration
@Profile("integration-test")
@EnableConfigurationProperties  // required for @ConfigurationProperties on @Bean methods
public class TestDataSourceConfig { ... }
```
