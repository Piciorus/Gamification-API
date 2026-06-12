```

@Component("dbHealthCheckIndicator")
public class DbHealthCheckIndicator extends AbstractHealthIndicator {

    private final String tamUrl;
    private final String tamUser;
    private final String tamPassword;
    private final String pvmUrl;
    private final String pvmUser;
    private final String pvmPassword;

    public DbHealthCheckIndicator(
            @Qualifier("tamDataSource") AtomikosDataSourceBean tamDataSource,
            @Qualifier("pvmDataSource") AtomikosDataSourceBean pvmDataSource) {
        super("Database health check failed");
        // Extract from XA properties — no pool involved
        Properties tamProps = tamDataSource.getXaProperties();
        this.tamUrl = tamProps.getProperty("URL");
        this.tamUser = tamProps.getProperty("user");
        this.tamPassword = tamProps.getProperty("password");

        Properties pvmProps = pvmDataSource.getXaProperties();
        this.pvmUrl = pvmProps.getProperty("URL");
        this.pvmUser = pvmProps.getProperty("user");
        this.pvmPassword = pvmProps.getProperty("password");
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception {
        CompletableFuture<Boolean> tamFuture = checkDb(tamUrl, tamUser, tamPassword);
        CompletableFuture<Boolean> pvmFuture = checkDb(pvmUrl, pvmUser, pvmPassword);

        boolean tamUp = getResult(tamFuture);
        boolean pvmUp = getResult(pvmFuture);

        if (tamUp && pvmUp) {
            builder.up();
        } else {
            builder.down();
        }

        builder
            .withDetail("tam", tamUp ? "UP" : "DOWN")
            .withDetail("pvm", pvmUp ? "UP" : "DOWN")
            .withDetail("time", LocalDateTime.now())
            .build();
    }

    private CompletableFuture<Boolean> checkDb(String url, String user, String password) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = DriverManager.getConnection(url, user, password)) {
                return conn.isValid(3);
            } catch (SQLException e) {
                return false;
            }
        });
    }

    private boolean getResult(CompletableFuture<Boolean> future) {
        try {
            return future.get(3, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            return false;
        } catch (Exception e) {
            return false;
        }
    }
}
```
