```
public class DataSourceContext {
    public static final ThreadLocal<String> CONTEXT = new ThreadLocal<>();
}
```


```
@Bean("auditRoutingDataSource")
@DependsOn({"tamDataSource", "pvmDataSource"})
public DataSource auditRoutingDataSource(
        @Qualifier("tamDataSource") DataSource tam,
        @Qualifier("pvmDataSource") DataSource pvm) {
    
    AbstractRoutingDataSource routing = new AbstractRoutingDataSource() {
        @Override
        protected Object determineCurrentLookupKey() {
            String key = DataSourceContext.CONTEXT.get();
            return (key != null) ? key : "tam"; // fallback
        }
    };
    
    Map<Object, Object> sources = new HashMap<>();
    sources.put("tam", tam);
    sources.put("pvm", pvm);
    routing.setTargetDataSources(sources);
    routing.setDefaultTargetDataSource(tam);
    routing.afterPropertiesSet();
    return routing;
}

@Bean("auditEntityManagerFactory")
public LocalContainerEntityManagerFactoryBean auditEntityManagerFactory(
        EntityManagerFactoryBuilder builder,
        @Qualifier("auditRoutingDataSource") DataSource ds) {
    return builder
        .dataSource(ds)
        .packages("com.consorsbank.common.audit.entity")
        .persistenceUnit("audit")
        .build();
}
```


```
@Configuration
@EnableJpaRepositories(
    basePackages = "com.consorsbank.common.audit.repository",
    entityManagerFactoryRef = "auditEntityManagerFactory",
    transactionManagerRef = "jtaTransactionManager"
)
public class AuditDataSourceConfig {
    // empty, just the annotation
}
```


```
@Aspect
@Component
public class DataSourceRoutingAspect {

    @Before("within(de.consorsbank.core.trauthsc.tam.service..*)")
    public void setTam() {
        DataSourceContext.CONTEXT.set("tam");
    }

    @Before("within(de.consorsbank.core.trauthsc.pvm.service..*)")
    public void setPvm() {
        DataSourceContext.CONTEXT.set("pvm");
    }

    @After("within(de.consorsbank.core.trauthsc.tam.service..*) || " +
           "within(de.consorsbank.core.trauthsc.pvm.service..*)")
    public void clear() {
        DataSourceContext.CONTEXT.remove();
    }
}

```
