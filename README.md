package de.consorsbank.core.trauthsc.common.actuator;

import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;

@Component("dbHealthCheckIndicator")
public class DbHealthCheckIndicator extends AbstractHealthIndicator {

    private final DataSource tamDataSource;

    public DbHealthCheckIndicator(DataSource tamDataSource) {
        super("Database health check failed");
        this.tamDataSource = tamDataSource;
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception {
        try (Connection conn = tamDataSource.getConnection()) {
            boolean valid = conn.isValid(5);
            String vendor = conn.getMetaData().getDatabaseProductName();

            if (valid) {
                builder.up()
                        .withDetail("database", vendor)
                        .withDetail("time", LocalDateTime.now())
                        .build();
            } else {
                builder.down()
                        .withDetail("database", vendor)
                        .withDetail("reason", "Connection isValid() returned false")
                        .withDetail("time", LocalDateTime.now())
                        .build();
            }
        } catch (SQLException e) {
            builder.down(e)
                    .withDetail("reason", e.getMessage())
                    .withDetail("time", LocalDateTime.now())
                    .build();
        }
    }
}



```
private Schema<?> buildSchema(List<T> codes) {
    T first = codes.get(0);

    return new ObjectSchema()
        .addProperty("code",    new StringSchema()
            .example(getErrorCode(first)))
        .addProperty("detail",  new StringSchema()
            .example(getMessage(first)))
        .addProperty("errors",  new ArraySchema()
            .items(new StringSchema())
            .example(List.of()))
        .addProperty("status",  new StringSchema()
            .example(String.valueOf(getHttpStatus(first).value())))
        .addProperty("title",   new StringSchema()
            .example(getName(first)))                      // use enum name, never null
        .addProperty("traceId", new StringSchema()
            .example("c929594d-4142-4e0b-a63d-87f920137623")); // real UUID format
}
```
