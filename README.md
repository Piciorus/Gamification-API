package de.consorsbank.trading.brkprcsc.config;

import com.atomikos.jdbc.AtomikosNonXADataSourceBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.web.WebEndpointResponse;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.boot.actuate.endpoint.web.WebEndpointResponse.STATUS_NOT_FOUND;

@Endpoint(id = "dbMonitor")
@Component
public class DataSourceActuator {

    private final BrkprcDataSourceConfig brkprcDataSourceConfig;

    @Autowired
    public DataSourceActuator(BrkprcDataSourceConfig brkprcDataSourceConfig) {
        this.brkprcDataSourceConfig = brkprcDataSourceConfig;
    }

    @ReadOperation
    public WebEndpointResponse<Map<String, Object>> getDataSourceInfo() {
        DataSource ds = brkprcDataSourceConfig.brkprcDataSource();
        
        if (ds instanceof AtomikosNonXADataSourceBean atomikosDS) {
            return new WebEndpointResponse<>(getDBInfo(atomikosDS));
        }
        
        return new WebEndpointResponse<>(
            getExceptionMap("Atomikos DataSource not found."), 
            STATUS_NOT_FOUND
        );
    }

    private Map<String, Object> getDBInfo(AtomikosNonXADataSourceBean dataSource) {
        Map<String, Object> monitorInfos = new HashMap<>();

        // Atomikos 6.x — access via bean properties, not ConnectionPoolProperties
        monitorInfos.put("MaxPoolSize",            dataSource.getMaxPoolSize());
        monitorInfos.put("MinPoolSize",            dataSource.getMinPoolSize());
        monitorInfos.put("BorrowConnectionTimeout", dataSource.getBorrowConnectionTimeout());
        monitorInfos.put("MaxIdleTimeout",         dataSource.getMaxIdleTime());
        monitorInfos.put("LoginTimeout",           dataSource.getLoginTimeout());

        return monitorInfos;
    }

    private Map<String, Object> getExceptionMap(String message) {
        return Map.of("message", message);  // Java 9+ immutable map
    }
}




package de.consorsbank.trading.brkprcsc.config;

import jakarta.sql.DataSource;           // ← jakarta, not javax
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.util.Assert;

public class DbHealthCheckIndicator 
    extends AbstractHealthIndicator {

    private final DataSource dataSource;
    private final int connectionTimeout = 5;

    public DbHealthCheckIndicator(
        @Qualifier("brkprcDataSource") DataSource dataSource) {
        super("DataSource health check failed");
        Assert.state(dataSource != null, 
            "DataSource for DbHealthCheck must be specified");
        this.dataSource = dataSource;
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            if (!conn.isValid(connectionTimeout)) {
                builder.down()
                    .withDetail("time", LocalDateTime.now())
                    .build();
                return;
            }
            String vendor = conn.getMetaData().getDatabaseProductName();
            builder.up()
                .withDetail("database", vendor)
                .withDetail("time", LocalDateTime.now())
                .build();
        }
    }
}



```
package de.consorsbank.trading.brkprcsc.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.web.WebEndpointResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Component
@Endpoint(id = "dbMonitor")
@RequiredArgsConstructor
public class DataSourceActuator {

    private final DataSource brkprcDataSource;

    @ReadOperation
    public WebEndpointResponse<Map<String, Object>> getDataSourceInfo() {
        if (brkprcDataSource == null) {
            return new WebEndpointResponse<>(
                getExceptionMap(), HttpStatus.NOT_FOUND.value());
        }
        return new WebEndpointResponse<>(getDBInfo());
    }

    private Map<String, Object> getDBInfo() {
        var info = new HashMap<String, Object>();
        int total     = invokeInt("poolTotalSize");
        int available = invokeInt("poolAvailableSize");
        info.put("MaxPoolSize",              invokeInt("getMaxPoolSize"));
        info.put("MinPoolSize",              invokeInt("getMinPoolSize"));
        info.put("ConnectionTimeout",        invokeInt("getBorrowConnectionTimeout"));
        info.put("MaxIdleTimeout",           invokeInt("getMaxIdleTime"));
        info.put("ConnectionCount",          total);
        info.put("AvailableConnectionCount", available);
        info.put("MaxConnectionsInUseCount", invokeInt("getMaxPoolSize"));
        info.put("InUseConnectionCount",     total - available);
        return info;
    }

    private int invokeInt(String methodName) {
        try {
            Method m = brkprcDataSource.getClass().getMethod(methodName);
            m.setAccessible(true);
            return ((Number) m.invoke(brkprcDataSource)).intValue();
        } catch (Exception e) {
            return -1;
        }
    }

    private static Map<String, Object> getExceptionMap() {
        var map = new HashMap<String, Object>();
        map.put("error", "DataSource not found.");
        return map;
    }
}
```
