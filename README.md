```
spring:
  datasource:
    tam:
      # H2 In-Memory (default local) - uncommented
      url: jdbc:h2:mem:tam;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
      username: sa
      password: password
      driverClassName: org.h2.Driver
      xa-data-source-class-name: org.h2.jdbcx.JdbcDataSource
      configuration:
        uniqueResourceName: tam
        minPoolSize: 5
        maxPoolSize: 30
        readonly: false
        idle-timeout: 240
        borrowConnectionTimeout: 5
      # Oracle locally - commented
      # url: jdbc:oracle:thin:@localhost:1521/XEPDB1
      # username: system
      # password: 12345
      # driverClassName: oracle.jdbc.OracleDriver
      # xa-data-source-class-name: oracle.jdbc.xa.client.OracleXADataSource
      # configuration:
      #   uniqueResourceName: tam
      #   minPoolSize: 5
      #   maxPoolSize: 30
      #   readonly: false
      #   idle-timeout: 240
      #   borrowConnectionTimeout: 5

    pvm:
      # H2 In-Memory (default local) - uncommented
      url: jdbc:h2:mem:pvm;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
      username: sa
      password: password
      driverClassName: org.h2.Driver
      xa-data-source-class-name: org.h2.jdbcx.JdbcDataSource
      configuration:
        uniqueResourceName: pvm
        minPoolSize: 5
        maxPoolSize: 30
        readonly: false
        idle-timeout: 240
        borrowConnectionTimeout: 5
      # Oracle locally - commented
      # url: jdbc:oracle:thin:@localhost:1521/XEPDB1
      # username: system
      # password: 12345
      # driverClassName: oracle.jdbc.OracleDriver
      # xa-data-source-class-name: oracle.jdbc.xa.client.OracleXADataSource
      # configuration:
      #   uniqueResourceName: pvm
      #   minPoolSize: 5
      #   maxPoolSize: 30
      #   readonly: false
      #   idle-timeout: 240
      #   borrowConnectionTimeout: 5

  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    hibernate:
      ddl-auto: none

  liquibase:
    change-log: classpath:/db/changelog/changelog-master.yaml
    enabled: true
    drop-first: true

  h2:
    console:
      enabled: true
      path: /h2-console

```
```
package de.consorsbank.core.trauthsc.tam.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "spring.datasource.tam")
@Component
@Getter
@Setter
public class TamDataSourceProperties {
    private String url;
    private String username;
    private String password;
    private String driverClassName;
    private String xaDataSourceClassName;
    private Configuration configuration;

    @Getter
    @Setter
    public static class Configuration {
        private String uniqueResourceName;
        private int minPoolSize;
        private int maxPoolSize;
        private boolean readonly;
        private int idleTimeout;
        private int borrowConnectionTimeout;
    }
}


```


```
package de.consorsbank.core.trauthsc.tam.config;

import com.atomikos.jdbc.AtomikosDataSourceBean;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@EnableJpaRepositories(
        entityManagerFactoryRef = "tamEntityManagerFactory",
        transactionManagerRef = "jtaTransactionManager",
        basePackages = "de.consorsbank.core.trauthsc.tam.repository"
)
public class TamDataSourceConfig {

    private final TamDataSourceProperties tamDataSourceProperties;

    public TamDataSourceConfig(TamDataSourceProperties tamDataSourceProperties) {
        this.tamDataSourceProperties = tamDataSourceProperties;
    }

    @Bean("tamDataSource")
    public DataSource tamDataSource() {
        var xaProperties = new Properties();
        xaProperties.setProperty("user", tamDataSourceProperties.getUsername());
        xaProperties.setProperty("password", tamDataSourceProperties.getPassword());
        xaProperties.setProperty("url", tamDataSourceProperties.getUrl());

        var atomikosDataSourceBean = new AtomikosDataSourceBean();
        atomikosDataSourceBean.setUniqueResourceName(
                tamDataSourceProperties.getConfiguration().getUniqueResourceName());
        atomikosDataSourceBean.setXaDataSourceClassName(
                tamDataSourceProperties.getXaDataSourceClassName());
        atomikosDataSourceBean.setXaProperties(xaProperties);
        atomikosDataSourceBean.setPoolSize(
                tamDataSourceProperties.getConfiguration().getMinPoolSize());
        atomikosDataSourceBean.setMaxPoolSize(
                tamDataSourceProperties.getConfiguration().getMaxPoolSize());
        return atomikosDataSourceBean;
    }

    @Bean("tamEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean tamEntityManagerFactory(
            EntityManagerFactoryBuilder builder) {
        return builder
                .dataSource(tamDataSource())
                .packages("de.consorsbank.core.trauthsc.tam.persistence.entity")
                .persistenceUnit("tam")
                .build();
    }
}

```




```
package de.consorsbank.core.trauthsc.pvm.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "spring.datasource.pvm")
@Component
@Getter
@Setter
public class PvmDataSourceProperties {
    private String url;
    private String username;
    private String password;
    private String driverClassName;
    private String xaDataSourceClassName;
    private Configuration configuration;

    @Getter
    @Setter
    public static class Configuration {
        private String uniqueResourceName;
        private int minPoolSize;
        private int maxPoolSize;
        private boolean readonly;
        private int idleTimeout;
        private int borrowConnectionTimeout;
    }
}

```


```
package de.consorsbank.core.trauthsc.pvm.config;

import com.atomikos.jdbc.AtomikosDataSourceBean;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@EnableJpaRepositories(
        entityManagerFactoryRef = "pvmEntityManagerFactory",
        transactionManagerRef = "jtaTransactionManager",
        basePackages = "de.consorsbank.core.trauthsc.pvm.repository"
)
public class PvmDataSourceConfig {

    private final PvmDataSourceProperties pvmDataSourceProperties;

    public PvmDataSourceConfig(PvmDataSourceProperties pvmDataSourceProperties) {
        this.pvmDataSourceProperties = pvmDataSourceProperties;
    }

    @Bean("pvmDataSource")
    public DataSource pvmDataSource() {
        var xaProperties = new Properties();
        xaProperties.setProperty("user", pvmDataSourceProperties.getUsername());
        xaProperties.setProperty("password", pvmDataSourceProperties.getPassword());
        xaProperties.setProperty("url", pvmDataSourceProperties.getUrl());

        var atomikosDataSourceBean = new AtomikosDataSourceBean();
        atomikosDataSourceBean.setUniqueResourceName(
                pvmDataSourceProperties.getConfiguration().getUniqueResourceName());
        atomikosDataSourceBean.setXaDataSourceClassName(
                pvmDataSourceProperties.getXaDataSourceClassName());
        atomikosDataSourceBean.setXaProperties(xaProperties);
        atomikosDataSourceBean.setPoolSize(
                pvmDataSourceProperties.getConfiguration().getMinPoolSize());
        atomikosDataSourceBean.setMaxPoolSize(
                pvmDataSourceProperties.getConfiguration().getMaxPoolSize());
        return atomikosDataSourceBean;
    }

    @Bean("pvmEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean pvmEntityManagerFactory(
            EntityManagerFactoryBuilder builder) {
        return builder
                .dataSource(pvmDataSource())
                .packages("de.consorsbank.core.trauthsc.pvm.persistence.entity")
                .persistenceUnit("pvm")
                .build();
    }
}


```


```
<dependency>
    <groupId>com.oracle.database.jdbc</groupId>
    <artifactId>ojdbc11</artifactId>
    <version>23.3.0.23.09</version>
</dependency>


```
