```
@TestConfiguration
@Profile("integration-test")
public class TestTransactionManagerConfig {

    @Bean("jtaTransactionManager")
    @Primary
    public PlatformTransactionManager transactionManager(
            @Qualifier("tamDataSource") DataSource tamDataSource) {
        return new DataSourceTransactionManager(tamDataSource);
    }

    // Needed because AuditDataSourceConfig's EntityManagerFactory is excluded
    @Bean("auditEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean auditEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("auditRoutingDataSource") DataSource dataSource) {
        return builder
            .dataSource(dataSource)
            .packages("com.consorsbank.common.audit.entity")
            .persistenceUnit("audit")
            .build();
    }
}
```
