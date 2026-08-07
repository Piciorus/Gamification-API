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
