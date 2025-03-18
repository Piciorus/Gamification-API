
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
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.pool.PoolStats;
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
    private final CloseableHttpClient closeableHttpClient;

    public AuthorizationFeignClientActuator(FeignHttpClientProperties feignHttpClientProperties,
                                            CloseableHttpClient closeableHttpClient) {
        this.feignHttpClientProperties = feignHttpClientProperties;
        this.closeableHttpClient = closeableHttpClient;
    }

    @ReadOperation
    public WebEndpointResponse<Map<String, Object>> getInfo() {
        Map<String, Object> info = new HashMap<>();

        // Access connection pool stats from Apache HttpClient's pooling connection manager
        if (closeableHttpClient instanceof org.apache.hc.client5.http.impl.classic.InternalHttpClient internalHttpClient) {
            PoolingHttpClientConnectionManager connectionManager = (PoolingHttpClientConnectionManager) internalHttpClient.getConnectionManager();
            PoolStats poolStats = connectionManager.getTotalStats();

            info.put("Leased Connections", poolStats.getLeased());
            info.put("Pending Connections", poolStats.getPending());
            info.put("Available Connections", poolStats.getAvailable());
            info.put("Max Connections", poolStats.getMax());
            info.put("Total Allocated", poolStats.getLeased() + poolStats.getAvailable());
        } else {
            info.put("Error", "Feign client is not using Apache HttpClient, cannot retrieve connection pool stats.");
        }

        // Retrieve Feign HTTP Client properties (timeouts)
        info.put("Connection Timeout (ms)", feignHttpClientProperties.getConnectionTimeout());
        info.put("Connection Request Timeout", feignHttpClientProperties.getConnectionTimeout()); // Feign does not have a separate "request timeout"

        return new WebEndpointResponse<>(info);
    }
}

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.Collectors;

@Component
@Endpoint(id = "feignClients")
public class FeignClientsActuator {

    @Autowired
    private ApplicationContext applicationContext;

    @ReadOperation
    public Map<String, Object> feignClients() {
        // Get all Feign client beans
        Map<String, Object> feignClients = applicationContext.getBeansWithAnnotation(org.springframework.cloud.openfeign.FeignClient.class);

        // Return a map of Feign client names and their classes
        return feignClients.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getClass().getName()));
    }
}
