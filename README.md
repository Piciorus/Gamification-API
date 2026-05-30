```
package de.consorsbank.core.trauthsc.common.actuator;

import com.atomikos.jdbc.AtomikosDataSourceBean;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.http.HttpStatus;
import org.springframework.boot.actuate.endpoint.web.WebEndpointResponse;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Component
@Endpoint(id = "dbMonitor")
@RequiredArgsConstructor
public class DbMonitorEndpoint {

    private final DataSource tamDataSource;
    private final DataSource pvmDataSource;

    @ReadOperation
    public WebEndpointResponse<Map<String, Object>> getDataSourceInfo() {
        var tam = getTamDataSource();
        var pvm = getPvmDataSource();

        if (tam != null && pvm != null) {
            return new WebEndpointResponse<>(getDBInfo(tam, pvm));
        }

        return new WebEndpointResponse<>(
                getExceptionMap("One or more DataSources not found."),
                HttpStatus.NOT_FOUND.value()
        );
    }

    private Map<String, Object> getDBInfo(AtomikosDataSourceBean tam, AtomikosDataSourceBean pvm) {
        var result = new HashMap<String, Object>();
        result.put("tam", buildSchemaInfo(tam, "TAM"));
        result.put("pvm", buildSchemaInfo(pvm, "PVM"));
        return result;
    }

    private Map<String, Object> buildSchemaInfo(AtomikosDataSourceBean dataSource, String schema) {
        var info = new HashMap<String, Object>();
        info.put("schema", schema);
        info.put("MaxPoolSize", dataSource.getMaxPoolSize());
        info.put("MinPoolSize", dataSource.getMinPoolSize());
        info.put("ConnectionTimeout", dataSource.getBorrowConnectionTimeout());
        info.put("MaxIdleTimeout", dataSource.getMaxIdleTime());
        info.put("ConnectionCount", dataSource.poolTotalSize());
        info.put("AvailableConnectionCount", dataSource.poolAvailableSize());
        info.put("MaxConnectionsInUseCount", dataSource.getMaxPoolSize());
        info.put("InUseConnectionCount", dataSource.poolTotalSize() - dataSource.poolAvailableSize());
        return info;
    }

    private AtomikosDataSourceBean getTamDataSource() {
        return (AtomikosDataSourceBean) tamDataSource;
    }

    private AtomikosDataSourceBean getPvmDataSource() {
        return (AtomikosDataSourceBean) pvmDataSource;
    }

    private Map<String, Object> getExceptionMap(String message) {
        var map = new HashMap<String, Object>();
        map.put("error", message);
        return map;
    }
}
```


```
package de.consorsbank.core.trauthsc.common.actuator;

import com.atomikos.jdbc.AtomikosDataSourceBean;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component("db")
@RequiredArgsConstructor
public class DatabaseSchemaHealthIndicator implements HealthIndicator {

    private final DataSource tamDataSource;
    private final DataSource pvmDataSource;

    @Override
    public Health health() {
        var tamInfo = getDBInfo((AtomikosDataSourceBean) tamDataSource, "TAM");
        var pvmInfo = getDBInfo((AtomikosDataSourceBean) pvmDataSource, "PVM");

        boolean tamUp = tamInfo.get("status").equals("UP");
        boolean pvmUp = pvmInfo.get("status").equals("UP");

        Health.Builder builder = (tamUp && pvmUp) ? Health.up() : Health.down();

        return builder
                .withDetail("tam", tamInfo)
                .withDetail("pvm", pvmInfo)
                .build();
    }

    private java.util.Map<String, Object> getDBInfo(AtomikosDataSourceBean dataSource, String schema) {
        var info = new java.util.HashMap<String, Object>();
        try {
            info.put("schema", schema);
            info.put("status", "UP");
            info.put("MaxPoolSize", dataSource.getMaxPoolSize());
            info.put("MinPoolSize", dataSource.getMinPoolSize());
            info.put("ConnectionTimeout", dataSource.getBorrowConnectionTimeout());
            info.put("MaxIdleTimeout", dataSource.getMaxIdleTime());
            info.put("ConnectionCount", dataSource.poolTotalSize());
            info.put("AvailableConnectionCount", dataSource.poolAvailableSize());
            info.put("MaxConnectionsInUseCount", dataSource.getMaxPoolSize());
            info.put("InUseConnectionCount", dataSource.poolTotalSize() - dataSource.poolAvailableSize());
        } catch (Exception e) {
            info.put("status", "DOWN");
            info.put("error", e.getMessage());
        }
        return info;
    }
}
```
