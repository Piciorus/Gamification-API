```
package de.consorsbank.trading.brkprcsc.config;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.health.contributor.AbstractHealthIndicator;
import org.springframework.boot.health.contributor.Health;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.util.Assert;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class DbHealthCheckIndicator
        extends AbstractHealthIndicator
        implements InitializingBean {

    private final DataSource dataSource;
    private final int connectionTimeout;
    private String databaseVendor;

    public DbHealthCheckIndicator(
            @Qualifier("brkprcDataSource") DataSource brkprcDataSource) {
        super("DataSource health check failed");
        this.dataSource = brkprcDataSource;
        this.connectionTimeout = 5;
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception {
        int errorCode = check();
        if (errorCode != 1) {
            builder.down()
                   .withDetail("database", databaseVendor)
                   .withDetail("time", LocalDateTime.now())
                   .build();
        } else {
            builder.up()
                   .withDetail("database", databaseVendor)
                   .withDetail("time", LocalDateTime.now())
                   .build();
        }
    }

    protected int check() {
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            databaseVendor = conn.getMetaData().getDatabaseProductName();
            if (!conn.isValid(this.connectionTimeout)) {
                return 0;
            }
            return 1;
        } catch (SQLException e) {
            return 0;
        } finally {
            JdbcUtils.closeConnection(conn);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.state(this.dataSource != null,
            "DataSource for DbHealthCheck must be specified");
    }
}

```




```
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
        AtomikosNonXADataSourceBean ds = getAtomikosDataSource();
        if (ds != null) {
            return new WebEndpointResponse<>(getDBInfo(ds));
        }
        return new WebEndpointResponse<>(
            getExceptionMap("Atomikos DataSource not found."), STATUS_NOT_FOUND);
    }

    private Map<String, Object> getDBInfo(AtomikosNonXADataSourceBean dataSource) {
        Map<String, Object> monitorInfos = new HashMap<>();
        monitorInfos.put("MaxPoolSize",              dataSource.getMaxPoolSize());
        monitorInfos.put("MinPoolSize",              dataSource.getMinPoolSize());
        monitorInfos.put("ConnectionTimeout",        dataSource.getBorrowConnectionTimeout());
        monitorInfos.put("MaxIdleTimeout",           dataSource.getMaxIdleTime());
        monitorInfos.put("UniqueResourceName",       dataSource.getUniqueResourceName());
        // poolTotalSize() / poolAvailableSize() removed in Atomikos 6.x
        // Add back if you downgrade to 5.x:
        // monitorInfos.put("ConnectionCount",          dataSource.poolTotalSize());
        // monitorInfos.put("AvailableConnectionCount", dataSource.poolAvailableSize());
        // monitorInfos.put("InUseConnectionCount",     dataSource.poolTotalSize() - dataSource.poolAvailableSize());
        return monitorInfos;
    }

    private AtomikosNonXADataSourceBean getAtomikosDataSource() {
        DataSource ds = brkprcDataSourceConfig.brkprcDataSource();
        if (ds instanceof AtomikosNonXADataSourceBean atomikos) {
            return atomikos;
        }
        return null;
    }

    private Map<String, Object> getExceptionMap(String message) {
        Map<String, Object> notFoundMap = new HashMap<>();
        notFoundMap.put("message", message);
        return notFoundMap;
    }
}


```



```
private Map<String, Object> getDBInfo(AtomikosNonXADataSourceBean dataSource) {
    Map<String, Object> monitorInfos = new HashMap<>();
    monitorInfos.put("MaxPoolSize",        dataSource.getMaxPoolSize());
    monitorInfos.put("MinPoolSize",        dataSource.getMinPoolSize());
    monitorInfos.put("ConnectionTimeout",  dataSource.getBorrowConnectionTimeout());
    monitorInfos.put("MaxIdleTimeout",     dataSource.getMaxIdleTime());

    // Pool counts via JMX — Atomikos 6.x moved them here
    try {
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        String resourceName = dataSource.getUniqueResourceName();
        ObjectName on = new ObjectName(
            "com.atomikos:type=ConnectionPool,name=" + resourceName);

        int total     = (int) mbs.getAttribute(on, "PoolSize");
        int available = (int) mbs.getAttribute(on, "AvailableSize");

        monitorInfos.put("ConnectionCount",          total);
        monitorInfos.put("AvailableConnectionCount", available);
        monitorInfos.put("MaxConnectionsInUseCount", dataSource.getMaxPoolSize());
        monitorInfos.put("InUseConnectionCount",     total - available);
    } catch (Exception e) {
        monitorInfos.put("PoolStats", "unavailable: " + e.getMessage());
    }

    return monitorInfos;
}


```
