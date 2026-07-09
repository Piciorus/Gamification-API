```
@TestConfiguration
@Profile("integration-test")
public class TestDataSourceConfig {

    @Bean("tamDataSource")
    public DataSource tamDataSource() {
        var ds = new HikariDataSource();
        ds.setJdbcUrl(GlobalOracleContainer.getInstance().getJdbcUrl());
        ds.setUsername("tam");
        ds.setPassword("tam");
        ds.setDriverClassName("oracle.jdbc.OracleDriver");
        ds.setMaximumPoolSize(5);
        return ds;
    }

    @Bean("pvmDataSource")
    public DataSource pvmDataSource() {
        var ds = new HikariDataSource();
        ds.setJdbcUrl(GlobalOracleContainer.getInstance().getJdbcUrl());
        ds.setUsername("tam"); // same user, same schema
        ds.setPassword("tam");
        ds.setDriverClassName("oracle.jdbc.OracleDriver");
        ds.setMaximumPoolSize(5);
        return ds;
    }

    @Bean("auditRoutingDataSource")
    public DataSource auditRoutingDataSource(
            @Qualifier("tamDataSource") DataSource tam,
            @Qualifier("pvmDataSource") DataSource pvm) {
        var routing = new AbstractRoutingDataSource() {
            @Override
            protected Object determineCurrentLookupKey() {
                return Optional.ofNullable(DataSourceContext.CONTEXT.get())
                    .orElse("tam");
            }
        };
        routing.setTargetDataSources(Map.of("tam", tam, "pvm", pvm));
        routing.setDefaultTargetDataSource(tam);
        routing.afterPropertiesSet();
        return routing;
    }
}
```
