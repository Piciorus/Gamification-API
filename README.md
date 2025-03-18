
import org.apache.hc.core5.pool.PoolStats;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.web.WebEndpointResponse;
import org.springframework.cloud.openfeign.clientconfig.FeignHttpClientProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Endpoint(id = "authorizeFeignClient")
@Component
public class AuthorizationFeignClientActuator {

    private final FeignHttpClientProperties feignHttpClientProperties;
    private final CloseableHttpClient httpClient;

    public AuthorizationFeignClientActuator(FeignHttpClientProperties feignHttpClientProperties, CloseableHttpClient httpClient) {
        this.feignHttpClientProperties = feignHttpClientProperties;
        this.httpClient = httpClient;
    }

    @ReadOperation
    public WebEndpointResponse<Map<String, Object>> getInfo() {
        Map<String, Object> info = new HashMap<>();

        // Retrieve Feign's connection pool statistics from HttpClient
        if (httpClient instanceof org.apache.hc.client5.http.impl.classic.InternalHttpClient internalHttpClient) {
            PoolingHttpClientConnectionManager connectionManager = (PoolingHttpClientConnectionManager) internalHttpClient.getConnectionManager();
            PoolStats poolStats = connectionManager.getTotalStats();

            info.put("Leased Connections", poolStats.getLeased());
            info.put("Pending Connections", poolStats.getPending());
            info.put("Available Connections", poolStats.getAvailable());
            info.put("Max Connections", poolStats.getMax());
            info.put("Total Allocated", poolStats.getLeased() + poolStats.getAvailable());
        } else {
            info.put("Error", "Feign is not using Apache HttpClient, cannot retrieve connection pool stats.");
        }

        // Add Feign default properties
        info.put("Connection Timeout (ms)", feignHttpClientProperties.getConnectionTimeout());
        info.put("Connection Request Timeout", feignHttpClientProperties.getConnectionTimeout()); // Feign does not have a separate request timeout property

        return new WebEndpointResponse<>(info);
    }
}
