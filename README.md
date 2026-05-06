```
package de.consorsbank.core.trauthsc.pvm.config;

import com.atomikos.jdbc.AtomikosDataSourceBean;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import lombok.RequiredArgsConstructor;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@RequiredArgsConstructor
@EnableJpaRepositories(
        entityManagerFactoryRef = "pvmEntityManagerFactory",
        transactionManagerRef = "jtaTransactionManager",
        basePackages = "de.consorsbank.core.trauthsc.pvm.repository"
)
public class PvmDataSourceConfig {

    private final PvmDataSourceProperties pvmDataSourceProperties;

    @Bean("pvmDataSource")
    @Primary
    public DataSource createPvmDataSource() {
        validateProperties();
        var xaProperties = createXaProperties();
        var dataSource = new AtomikosDataSourceBean();
        configureDataSource(dataSource, xaProperties);
        return dataSource;
    }

    @Bean("pvmEntityManagerFactory")
    @Primary
    public LocalContainerEntityManagerFactoryBean pvmEntityManagerFactory(
            EntityManagerFactoryBuilder builder) {
        return builder
                .dataSource(createPvmDataSource())
                .packages("de.consorsbank.core.trauthsc.pvm.persistence.entity")
                .persistenceUnit("pvm")
                .build();
    }

    private void validateProperties() {
        if (pvmDataSourceProperties == null) {
            throw new IllegalStateException("pvmDataSourceProperties cannot be null");
        }
        if (pvmDataSourceProperties.getUsername() == null) {
            throw new IllegalStateException("Username is required");
        }
    }

    private Properties createXaProperties() {
        var properties = new Properties();
        properties.setProperty("user", pvmDataSourceProperties.getUsername());
        properties.setProperty("password", pvmDataSourceProperties.getPassword());
        properties.setProperty("URL", pvmDataSourceProperties.getUrl());
        return properties;
    }

    private void configureDataSource(AtomikosDataSourceBean dataSource,
                                      Properties xaProperties) {
        dataSource.setUniqueResourceName(
                pvmDataSourceProperties.getConfiguration().getUniqueResourceName());
        dataSource.setXaDataSourceClassName(
                pvmDataSourceProperties.getXaDataSourceClassName());
        dataSource.setXaProperties(xaProperties);

        int minPoolSize = pvmDataSourceProperties.getConfiguration().getMinPoolSize();
        int maxPoolSize = pvmDataSourceProperties.getConfiguration().getMaxPoolSize();
        dataSource.setPoolSize(minPoolSize);
        dataSource.setMaxPoolSize(maxPoolSize);
    }
}

```
