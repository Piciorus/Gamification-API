```
package de.consorsbank.trading.brkprcsc.config;

import com.atomikos.jdbc.AtomikosNonXADataSourceBean;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.web.WebEndpointResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Component
@Endpoint(id = "dbMonitor")
@RequiredArgsConstructor
public class DataSourceActuator {

    private final DataSource brkprcDataSource;

    @ReadOperation
    public WebEndpointResponse<Map<String, Object>> getDataSourceInfo() {
        var ds = getBrkprcDataSource();
        if (ds != null) {
            return new WebEndpointResponse<>(getDBInfo(ds));
        }
        return new WebEndpointResponse<>(
            getExceptionMap(),
            HttpStatus.NOT_FOUND.value());
    }

    private static Map<String, Object> getDBInfo(AtomikosNonXADataSourceBean dataSource) {
        var info = new HashMap<String, Object>();
        info.put("MaxPoolSize",              dataSource.getMaxPoolSize());
        info.put("MinPoolSize",              dataSource.getMinPoolSize());
        info.put("ConnectionTimeout",        dataSource.getBorrowConnectionTimeout());
        info.put("MaxIdleTimeout",           dataSource.getMaxIdleTime());
        info.put("ConnectionCount",          dataSource.poolTotalSize());
        info.put("AvailableConnectionCount", dataSource.poolAvailableSize());
        info.put("MaxConnectionsInUseCount", dataSource.getMaxPoolSize());
        info.put("InUseConnectionCount",     dataSource.poolTotalSize() - dataSource.poolAvailableSize());
        return info;
    }

    private AtomikosNonXADataSourceBean getBrkprcDataSource() {
        if (brkprcDataSource instanceof AtomikosNonXADataSourceBean ds) {
            return ds;
        }
        return null;
    }

    private static Map<String, Object> getExceptionMap() {
        var map = new HashMap<String, Object>();
        map.put("error", "DataSource not found.");
        return map;
    }
}

```
