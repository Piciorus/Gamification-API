```
@Component("dbHealthCheckIndicator")
public class DbHealthCheckIndicator extends AbstractHealthIndicator {

    private static final int TIMEOUT = 3;

    private final String tamUrl;
    private final String tamUser;
    private final String tamPassword;
    private final String pvmUrl;
    private final String pvmUser;
    private final String pvmPassword;

    public DbHealthCheckIndicator(
            TamDataSourceProperties tamDataSourceProperties,
            PvmDataSourceProperties pvmDataSourceProperties) {
        super("Database health check failed");
        this.tamUrl = tamDataSourceProperties.getUrl();
        this.tamUser = tamDataSourceProperties.getUsername();
        this.tamPassword = tamDataSourceProperties.getPassword();
        this.pvmUrl = pvmDataSourceProperties.getUrl();
        this.pvmUser = pvmDataSourceProperties.getUsername();
        this.pvmPassword = pvmDataSourceProperties.getPassword();
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception {
        var tamFuture = checkDb(tamUrl, tamUser, tamPassword);
        var pvmFuture = checkDb(pvmUrl, pvmUser, pvmPassword);

        Health tamHealth = buildComponentHealth(tamFuture);
        Health pvmHealth = buildComponentHealth(pvmFuture);

        boolean allUp = tamHealth.getStatus() == Status.UP
                      && pvmHealth.getStatus() == Status.UP;

        builder.status(allUp ? Status.UP : Status.DOWN)
               .withDetail("tam", tamHealth)
               .withDetail("pvm", pvmHealth)
               .build();
    }

    private Health buildComponentHealth(CompletableFuture<Boolean> future) {
        boolean up = getResult(future);
        return up
            ? Health.up()
                .withDetail("database", "Oracle")
                .withDetail("validationQuery", "isValid()")
                .build()
            : Health.down()
                .withDetail("database", "Oracle")
                .withDetail("validationQuery", "isValid()")
                .build();
    }

    private CompletableFuture<Boolean> checkDb(String url, String user, String password) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = DriverManager.getConnection(url, user, password)) {
                return conn.isValid(TIMEOUT);
            } catch (SQLException e) {
                return false;
            }
        });
    }

    private boolean getResult(CompletableFuture<Boolean> future) {
        try {
            return future.get(TIMEOUT, TimeUnit.SECONDS);
        } catch (TimeoutException | InterruptedException | ExecutionException e) {
            future.cancel(true);
            return false;
        }
    }
}

```
