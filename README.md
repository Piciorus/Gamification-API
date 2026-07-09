```
@TestConfiguration
@Profile("integration-test")
public class TestDataSourceConfig {

    private static final String TAM_URL =
        "jdbc:h2:mem:tam_db;" +
        "DB_CLOSE_DELAY=-1;" +
        "MODE=Oracle;" +
        "DATABASE_TO_LOWER=true;" +
        "INIT=CREATE SCHEMA IF NOT EXISTS tam";

    private static final String PVM_URL =
        "jdbc:h2:mem:pvm_db;" +
        "DB_CLOSE_DELAY=-1;" +
        "MODE=Oracle;" +
        "DATABASE_TO_LOWER=true;" +
        "INIT=CREATE SCHEMA IF NOT EXISTS pvm";

    private static final String AUDIT_URL =
        "jdbc:h2:mem:audit_db;" +
        "DB_CLOSE_DELAY=-1;" +
        "MODE=Oracle;" +
        "DATABASE_TO_LOWER=true;" +
        "INIT=CREATE SCHEMA IF NOT EXISTS public";

    @Bean("tamDataSource")
    public DataSource tamDataSource() {
        return buildH2(TAM_URL);
    }

    @Bean("pvmDataSource")
    public DataSource pvmDataSource() {
        return buildH2(PVM_URL);
    }

    @Bean("auditRoutingDataSource")
    public DataSource auditRoutingDataSource(
            @Qualifier("tamDataSource") DataSource tam,
            @Qualifier("pvmDataSource") DataSource pvm) {
        // Replicate the routing logic from AuditDataSourceConfig
        var routing = new AbstractRoutingDataSource() {
            @Override
            protected Object determineCurrentLookupKey() {
                String key = DataSourceContext.CONTEXT.get();
                return (key != null) ? key : "tam";
            }
        };
        var sources = new HashMap<Object, Object>();
        sources.put("tam", tam);
        sources.put("pvm", pvm);
        routing.setTargetDataSources(sources);
        routing.setDefaultTargetDataSource(tam);
        routing.afterPropertiesSet();
        return routing;
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
