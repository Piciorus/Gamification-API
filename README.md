```
package de.consorsbank.core.trauthsc.common.actuator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Custom Spring Boot Actuator Health Indicator for both Oracle schemas (TAM and PVM).
 *
 * Exposed at: GET /actuator/health/db
 * Or individually via the composite details.
 *
 * Required dependency in pom.xml:
 *   <dependency>
 *       <groupId>org.springframework.boot</groupId>
 *       <artifactId>spring-boot-starter-actuator</artifactId>
 *   </dependency>
 *
 * Required application.yml:
 *   management:
 *     endpoint:
 *       health:
 *         show-details: always
 *     endpoints:
 *       web:
 *         exposure:
 *           include: health, dbMonitor
 */
@Slf4j
@Component("db")  // becomes /actuator/health/db
@RequiredArgsConstructor
public class DatabaseSchemaHealthIndicator implements HealthIndicator {

    private final DataSource tamDataSource;
    private final DataSource pvmDataSource;

    @Override
    public Health health() {
        Map<String, Object> tamDetails = checkSchema("TAM", tamDataSource);
        Map<String, Object> pvmDetails = checkSchema("PVM", pvmDataSource);

        boolean tamUp = Boolean.TRUE.equals(tamDetails.get("connected"));
        boolean pvmUp = Boolean.TRUE.equals(pvmDetails.get("connected"));

        Health.Builder builder = (tamUp && pvmUp) ? Health.up() : Health.down();

        return builder
                .withDetail("tam", tamDetails)
                .withDetail("pvm", pvmDetails)
                .build();
    }

    private Map<String, Object> checkSchema(String schemaName, DataSource dataSource) {
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("schema", schemaName);

        try (Connection connection = dataSource.getConnection()) {
            details.put("connected", true);
            details.put("url", connection.getMetaData().getURL());
            details.put("username", connection.getMetaData().getUserName());
            details.put("databaseProductName", connection.getMetaData().getDatabaseProductName());
            details.put("databaseProductVersion", connection.getMetaData().getDatabaseProductVersion());
            details.put("validationQuery", "SELECT 1 FROM DUAL");

            // Run validation query
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT 1 FROM DUAL")) {
                details.put("validationQueryResult", rs.next() ? "OK" : "FAILED");
            }

        } catch (Exception e) {
            log.error("[DB Monitor] Failed to connect to schema {}: {}", schemaName, e.getMessage(), e);
            details.put("connected", false);
            details.put("error", e.getMessage());
        }

        return details;
    }
}
```


```
package de.consorsbank.core.trauthsc.common.actuator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Custom actuator endpoint exposing detailed DB connection pool info for both schemas.
 *
 * Exposed at: GET /actuator/dbMonitor
 *
 * Enable in application.yml:
 *   management:
 *     endpoints:
 *       web:
 *         exposure:
 *           include: health, dbMonitor
 */
@Slf4j
@Component
@Endpoint(id = "dbMonitor")
@RequiredArgsConstructor
public class DbMonitorEndpoint {

    private final DataSource tamDataSource;
    private final DataSource pvmDataSource;

    @ReadOperation
    public Map<String, Object> dbMonitor() {
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("timestamp", Instant.now().toString());
        report.put("tam", collectSchemaInfo("TAM", tamDataSource));
        report.put("pvm", collectSchemaInfo("PVM", pvmDataSource));
        return report;
    }

    private Map<String, Object> collectSchemaInfo(String schemaName, DataSource dataSource) {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("schema", schemaName);

        long startTime = System.currentTimeMillis();

        try (Connection connection = dataSource.getConnection()) {
            long responseTime = System.currentTimeMillis() - startTime;

            info.put("status", "UP");
            info.put("connectionResponseTimeMs", responseTime);
            info.put("url", connection.getMetaData().getURL());
            info.put("username", connection.getMetaData().getUserName());
            info.put("databaseProduct", connection.getMetaData().getDatabaseProductName()
                    + " " + connection.getMetaData().getDatabaseProductVersion());
            info.put("autoCommit", connection.getAutoCommit());
            info.put("transactionIsolation", isolationLevelName(connection.getTransactionIsolation()));
            info.put("readOnly", connection.isReadOnly());
            info.put("catalog", connection.getCatalog());

            // Session info from Oracle
            info.put("oracleSessionInfo", queryOracleSessionInfo(connection));

        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            log.error("[DbMonitor] Error connecting to schema {}: {}", schemaName, e.getMessage(), e);
            info.put("status", "DOWN");
            info.put("connectionResponseTimeMs", responseTime);
            info.put("error", e.getMessage());
        }

        return info;
    }

    private Map<String, Object> queryOracleSessionInfo(Connection connection) {
        Map<String, Object> sessionInfo = new LinkedHashMap<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT SYS_CONTEXT('USERENV','SESSION_USER') AS session_user, " +
                     "SYS_CONTEXT('USERENV','DB_NAME') AS db_name, " +
                     "SYS_CONTEXT('USERENV','SERVER_HOST') AS server_host, " +
                     "SYS_CONTEXT('USERENV','INSTANCE_NAME') AS instance_name " +
                     "FROM DUAL"
             )) {
            if (rs.next()) {
                sessionInfo.put("sessionUser", rs.getString("session_user"));
                sessionInfo.put("dbName", rs.getString("db_name"));
                sessionInfo.put("serverHost", rs.getString("server_host"));
                sessionInfo.put("instanceName", rs.getString("instance_name"));
            }
        } catch (Exception e) {
            log.warn("[DbMonitor] Could not fetch Oracle session info: {}", e.getMessage());
            sessionInfo.put("error", e.getMessage());
        }
        return sessionInfo;
    }

    private String isolationLevelName(int level) {
        return switch (level) {
            case Connection.TRANSACTION_NONE -> "NONE";
            case Connection.TRANSACTION_READ_UNCOMMITTED -> "READ_UNCOMMITTED";
            case Connection.TRANSACTION_READ_COMMITTED -> "READ_COMMITTED";
            case Connection.TRANSACTION_REPEATABLE_READ -> "REPEATABLE_READ";
            case Connection.TRANSACTION_SERIALIZABLE -> "SERIALIZABLE";
            default -> "UNKNOWN(" + level + ")";
        };
    }
}
```
