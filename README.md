```
package de.consorsbank.core.trauthsc.integration;

import org.testcontainers.oracle.OracleContainer;
import org.testcontainers.utility.DockerImageName;

public class GlobalOracleContainer 
        extends OracleContainer {

    // Oracle Free is the lightweight version — no license needed
    private static final DockerImageName IMAGE =
        DockerImageName.parse("gvenzl/oracle-free:23-slim");

    private static GlobalOracleContainer container;

    private GlobalOracleContainer() {
        super(IMAGE);
        withReuse(true);
        withStartupTimeoutSeconds(240); // Oracle takes longer than Postgres
        withDatabaseName("FREEPDB1");
    }

    public static GlobalOracleContainer getInstance() {
        if (container == null) {
            container = new GlobalOracleContainer();
            container.start();
        }
        return container;
    }
}
```
```
static final GlobalOracleContainer oracle =
    GlobalOracleContainer.getInstance();

@DynamicPropertySource
static void configureDataSources(DynamicPropertyRegistry registry) {

    // TAM
    registry.add("spring.datasource.tam.url", oracle::getJdbcUrl);
    registry.add("spring.datasource.tam.username", oracle::getUsername);
    registry.add("spring.datasource.tam.password", oracle::getPassword);
    registry.add("spring.datasource.tam.xa-properties.URL", oracle::getJdbcUrl);
    registry.add("spring.datasource.tam.xa-properties.user", oracle::getUsername);
    registry.add("spring.datasource.tam.xa-properties.password", oracle::getPassword);
    registry.add("spring.datasource.tam.configuration.uniqueResourceName",
        () -> "tam-test");

    // PVM
    registry.add("spring.datasource.pvm.url", oracle::getJdbcUrl);
    registry.add("spring.datasource.pvm.username", oracle::getUsername);
    registry.add("spring.datasource.pvm.password", oracle::getPassword);
    registry.add("spring.datasource.pvm.xa-properties.URL", oracle::getJdbcUrl);
    registry.add("spring.datasource.pvm.xa-properties.user", oracle::getUsername);
    registry.add("spring.datasource.pvm.xa-properties.password", oracle::getPassword);
    registry.add("spring.datasource.pvm.configuration.uniqueResourceName",
        () -> "pvm-test");
}
```
```
spring:
  jpa:
    database-platform: org.hibernate.dialect.Oracle12cDialect
    hibernate:
      ddl-auto: none
    show-sql: true

  datasource:
    tam:
      driverClassName: oracle.jdbc.OracleDriver
      xa-data-source-class-name: oracle.jdbc.xa.client.OracleXADataSource
      configuration:
        uniqueResourceName: tam-test
        minPoolSize: 1
        maxPoolSize: 5
        borrowConnectionTimeout: 30

    pvm:
      driverClassName: oracle.jdbc.OracleDriver
      xa-data-source-class-name: oracle.jdbc.xa.client.OracleXADataSource
      configuration:
        uniqueResourceName: pvm-test
        minPoolSize: 1
        maxPoolSize: 5
        borrowConnectionTimeout: 30

liquibase:
  tam:
    change-log: classpath:/db/changelog/changelog-master-tam.yaml
    enabled: true
    drop-first: false
  pvm:
    change-log: classpath:/db/changelog/changelog-master-pvm.yaml
    enabled: true
    drop-first: false
```

```
testImplementation 'org.testcontainers:oracle-free'
testImplementation 'org.testcontainers:junit-jupiter'
```
