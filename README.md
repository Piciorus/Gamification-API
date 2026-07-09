```
@TestConfiguration
@Profile("integration-test")
public class TestDataSourceConfig {

    private static final String SHARED_URL =
        "jdbc:h2:mem:trauthsc;" +
        "DB_CLOSE_DELAY=-1;" +
        "MODE=Oracle;" +
        "DATABASE_TO_LOWER=true;" +
        "INIT=CREATE SCHEMA IF NOT EXISTS tam\\;" +
        "CREATE SCHEMA IF NOT EXISTS pvm";

    @Bean("tamDataSource")
    public DataSource tamDataSource() {
        return buildH2(SHARED_URL);
    }

    @Bean("pvmDataSource")
    public DataSource pvmDataSource() {
        return buildH2(SHARED_URL);
    }

    @Bean("auditRoutingDataSource")
    public DataSource auditRoutingDataSource() {
        return buildH2(SHARED_URL);
    }

    // ← ADD THIS — prevents DataSourceActuator from failing
    @Bean("dataSourceActuator")
    public Object dataSourceActuatorMock() {
        return new Object(); // no-op stub
    }

    private DataSource buildH2(String url) {
        var ds = new HikariDataSource();
        ds.setJdbcUrl(url);
        ds.setUsername("sa");
        ds.setPassword("");
        ds.setDriverClassName("org.h2.Driver");
        return ds;
    }
}
```
