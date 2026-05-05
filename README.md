```
src/test/resources/
  tests/
    changelog/
      changelog-master.yaml          ← trauth (already exists)
      changelog-master-tam.yaml      ← new
      changelog-master-pvm.yaml      ← new
      migrations.v1.0/
        001-create-table-dummy.sql   ← already exists (trauth)
        002-insert-into-dummy.sql    ← already exists (trauth)
      tam/
        migrations.v1.0/
          001-create-tam-tables.sql  ← new
      pvm/
        migrations.v1.0/
          001-create-pvm-tables.sql  ← new

```

```
databaseChangeLog:
  - includeAll:
      path: classpath:/tests/changelog/migrations.v1.0/
      relativeToChangelogFile: false
```


```
databaseChangeLog:
  - includeAll:
      path: classpath:/tests/changelog/tam/migrations.v1.0/
      relativeToChangelogFile: false
```

```
databaseChangeLog:
  - includeAll:
      path: classpath:/tests/changelog/pvm/migrations.v1.0/
      relativeToChangelogFile: false
```

```
spring:
  datasource:
    trauth:
      url: jdbc:h2:mem:trauth;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
      username: sa
      password: password
      driverClassName: org.h2.Driver
      xa-data-source-class-name: org.h2.jdbcx.JdbcDataSource
      configuration:
        uniqueResourceName: trauth-${random.uuid}
        minPoolSize: 5
        maxPoolSize: 30
        readonly: false
        idle-timeout: 240
        borrowConnectionTimeout: 5

    tam:
      url: jdbc:h2:mem:tam;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
      username: sa
      password: password
      driverClassName: org.h2.Driver
      xa-data-source-class-name: org.h2.jdbcx.JdbcDataSource
      configuration:
        uniqueResourceName: tam-${random.uuid}
        minPoolSize: 5
        maxPoolSize: 30
        readonly: false
        idle-timeout: 240
        borrowConnectionTimeout: 5

    pvm:
      url: jdbc:h2:mem:pvm;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
      username: sa
      password: password
      driverClassName: org.h2.Driver
      xa-data-source-class-name: org.h2.jdbcx.JdbcDataSource
      configuration:
        uniqueResourceName: pvm-${random.uuid}
        minPoolSize: 5
        maxPoolSize: 30
        readonly: false
        idle-timeout: 240
        borrowConnectionTimeout: 5

  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    hibernate:
      ddl-auto: none

  liquibase:
    change-log: classpath:/tests/changelog/changelog-master.yaml
    enabled: true
    drop-first: true

# Per-datasource Liquibase config
tam:
  liquibase:
    change-log: classpath:/tests/changelog/changelog-master-tam.yaml
    enabled: true
    drop-first: true

pvm:
  liquibase:
    change-log: classpath:/tests/changelog/changelog-master-pvm.yaml
    enabled: true
    drop-first: true

```
```
package de.consorsbank.core.trauthsc.persistence.config;

import liquibase.integration.spring.SpringLiquibase;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
public class LiquibaseConfig {

    // ── TRAUTH (primary, already handled by Spring Boot auto-config) ──

    @Bean("tamLiquibase")
    public SpringLiquibase tamLiquibase(
            @Qualifier("tamDataSource") DataSource tamDataSource,
            @Value("${tam.liquibase.change-log}") String changeLog,
            @Value("${tam.liquibase.enabled:true}") boolean enabled,
            @Value("${tam.liquibase.drop-first:false}") boolean dropFirst) {

        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(tamDataSource);
        liquibase.setChangeLog(changeLog);
        liquibase.setShouldRun(enabled);
        liquibase.setDropFirst(dropFirst);
        return liquibase;
    }

    @Bean("pvmLiquibase")
    public SpringLiquibase pvmLiquibase(
            @Qualifier("pvmDataSource") DataSource pvmDataSource,
            @Value("${pvm.liquibase.change-log}") String changeLog,
            @Value("${pvm.liquibase.enabled:true}") boolean enabled,
            @Value("${pvm.liquibase.drop-first:false}") boolean dropFirst) {

        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(pvmDataSource);
        liquibase.setChangeLog(changeLog);
        liquibase.setShouldRun(enabled);
        liquibase.setDropFirst(dropFirst);
        return liquibase;
    }
}

```



```
src/main/resources/
  db/
    changelog/
      changelog-master.yaml          ← trauth (already exists)
      changelog-master-tam.yaml      ← new
      changelog-master-pvm.yaml      ← new
      migrations.v1.0/
        001-create-trauth-tables.sql ← already exists
        002-insert-into-trauth.sql   ← already exists
      tam/
        migrations.v1.0/
          001-create-tam-tables.sql  ← new
      pvm/
        migrations.v1.0/
          001-create-pvm-tables.sql  ← new
```


```
spring:
  liquibase:
    change-log: classpath:/db/changelog/changelog-master.yaml
    enabled: true
    drop-first: true

tam:
  liquibase:
    change-log: classpath:/db/changelog/changelog-master-tam.yaml
    enabled: true
    drop-first: true

pvm:
  liquibase:
    change-log: classpath:/db/changelog/changelog-master-pvm.yaml
    enabled: true
    drop-first: true


databaseChangeLog:
  - includeAll:
      path: classpath:/db/changelog/tam/migrations.v1.0/
      relativeToChangelogFile: false

databaseChangeLog:
  - includeAll:
      path: classpath:/db/changelog/pvm/migrations.v1.0/
      relativeToChangelogFile: false
```

