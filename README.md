```
package de.consorsbank.core.trauthsc.tam.config;

import com.atomikos.jdbc.AtomikosDataSourceBean;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import lombok.RequiredArgsConstructor;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@RequiredArgsConstructor
@EnableJpaRepositories(
        entityManagerFactoryRef = "tamEntityManagerFactory",
        transactionManagerRef = "jtaTransactionManager",
        basePackages = "de.consorsbank.core.trauthsc.tam.repository"
)
public class TamDataSourceConfig {

    private final TamDataSourceProperties tamDataSourceProperties;

    @Bean("tamDataSource")
    public DataSource createTamDataSource() {
        validateProperties();
        var xaProperties = createXaProperties();
        var dataSource = new AtomikosDataSourceBean();
        configureDataSource(dataSource, xaProperties);
        return dataSource;
    }

    @Bean("tamEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean tamEntityManagerFactory(
            EntityManagerFactoryBuilder builder) {
        return builder
                .dataSource(createTamDataSource())
                .packages("de.consorsbank.core.trauthsc.tam.entity")
                .persistenceUnit("tam")
                .build();
    }

    private void validateProperties() {
        if (tamDataSourceProperties == null) {
            throw new IllegalStateException("tamDataSourceProperties cannot be null");
        }
        if (tamDataSourceProperties.getUsername() == null) {
            throw new IllegalStateException("Username is required");
        }
    }

    private Properties createXaProperties() {
        var properties = new Properties();
        properties.setProperty("user", tamDataSourceProperties.getUsername());
        properties.setProperty("password", tamDataSourceProperties.getPassword());
        properties.setProperty("URL", tamDataSourceProperties.getUrl());
        return properties;
    }

    private void configureDataSource(AtomikosDataSourceBean dataSource,
                                      Properties xaProperties) {
        dataSource.setUniqueResourceName(
                tamDataSourceProperties.getConfiguration().getUniqueResourceName());
        dataSource.setXaDataSourceClassName(
                tamDataSourceProperties.getXaDataSourceClassName());
        dataSource.setXaProperties(xaProperties);

        int minPoolSize = tamDataSourceProperties.getConfiguration().getMinPoolSize();
        int maxPoolSize = tamDataSourceProperties.getConfiguration().getMaxPoolSize();
        dataSource.setPoolSize(minPoolSize);
        dataSource.setMaxPoolSize(maxPoolSize);
    }
}

```
