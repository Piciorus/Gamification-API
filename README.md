```
private GlobalOracleContainer() {
    super(IMAGE);
    withReuse(true);
    withStartupTimeoutSeconds(240);
    // Create TAM and PVM schemas (Oracle schemas = users)
    withInitScript("sql/init-schemas.sql");
}
```

```
-- Create TAM schema/user
CREATE USER tam IDENTIFIED BY tam DEFAULT TABLESPACE USERS TEMPORARY TABLESPACE TEMP;
GRANT CONNECT, RESOURCE, DBA TO tam;
GRANT CREATE SESSION TO tam;
GRANT UNLIMITED TABLESPACE TO tam;

-- Create PVM schema/user  
CREATE USER pvm IDENTIFIED BY pvm DEFAULT TABLESPACE USERS TEMPORARY TABLESPACE TEMP;
GRANT CONNECT, RESOURCE, DBA TO pvm;
GRANT CREATE SESSION TO pvm;
GRANT UNLIMITED TABLESPACE TO pvm;
```


```
@DynamicPropertySource
static void configureDataSources(DynamicPropertyRegistry registry) {

    String baseUrl = oracle.getJdbcUrl(); // jdbc:oracle:thin:@host:port/FREEPDB1

    // TAM — connects as TAM user (= TAM schema)
    registry.add("spring.datasource.tam.url", () -> baseUrl);
    registry.add("spring.datasource.tam.username", () -> "tam");
    registry.add("spring.datasource.tam.password", () -> "tam");
    registry.add("spring.datasource.tam.xa-properties.URL", () -> baseUrl);
    registry.add("spring.datasource.tam.xa-properties.user", () -> "tam");
    registry.add("spring.datasource.tam.xa-properties.password", () -> "tam");
    registry.add("spring.datasource.tam.configuration.uniqueResourceName",
        () -> "tam-test");

    // PVM — connects as PVM user (= PVM schema)
    registry.add("spring.datasource.pvm.url", () -> baseUrl);
    registry.add("spring.datasource.pvm.username", () -> "pvm");
    registry.add("spring.datasource.pvm.password", () -> "pvm");
    registry.add("spring.datasource.pvm.xa-properties.URL", () -> baseUrl);
    registry.add("spring.datasource.pvm.xa-properties.user", () -> "pvm");
    registry.add("spring.datasource.pvm.xa-properties.password", () -> "pvm");
    registry.add("spring.datasource.pvm.configuration.uniqueResourceName",
        () -> "pvm-test");
}
```
