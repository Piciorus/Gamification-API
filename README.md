```
package de.consorsbank.trading.brkprcsc.config;

import com.atomikos.jdbc.AtomikosNonXADataSourceBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.web.WebEndpointResponse;
import org.springframework.stereotype.Component;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.sql.DataSource;
import java.lang.management.ManagementFactory;
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

        // Config properties — still available in 6.x
        int maxPoolSize = dataSource.getMaxPoolSize();
        monitorInfos.put("MaxPoolSize",        maxPoolSize);
        monitorInfos.put("MinPoolSize",        dataSource.getMinPoolSize());
        monitorInfos.put("ConnectionTimeout",  dataSource.getBorrowConnectionTimeout());
        monitorInfos.put("MaxIdleTimeout",     dataSource.getMaxIdleTime());

        // Pool runtime stats — moved to JMX in Atomikos 6.x
        try {
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            String name = dataSource.getUniqueResourceName();
            ObjectName on = new ObjectName(
                "com.atomikos:type=ConnectionPool,name=" + name);

            int total     = ((Number) mbs.getAttribute(on, "PoolSize")).intValue();
            int available = ((Number) mbs.getAttribute(on, "AvailableSize")).intValue();
            int inUse     = total - available;

            monitorInfos.put("ConnectionCount",          total);
            monitorInfos.put("AvailableConnectionCount", available);
            monitorInfos.put("MaxConnectionsInUseCount", maxPoolSize);
            monitorInfos.put("InUseConnectionCount",     inUse);
        } catch (Exception e) {
            // JMX not yet registered (pool not yet initialized) or name mismatch
            monitorInfos.put("ConnectionCount",          "unavailable");
            monitorInfos.put("AvailableConnectionCount", "unavailable");
            monitorInfos.put("MaxConnectionsInUseCount", maxPoolSize);
            monitorInfos.put("InUseConnectionCount",     "unavailable");
        }

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
