
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

import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(value = "authorization", fallback = AuthorizeFeignApiFallback.class)
public interface AuthorizeFeignApi {

    @Retry(name = "ProductServiceRetry")
    @PostMapping(path = "/v1/authorization/authorize/{serviceId}", consumes = "application/json;charset=utf-8")
    XAuthorizeResponse xauthorize(@RequestHeader("Authorization") String authorization,
                                  @RequestHeader("Language") String language,
                                  @RequestHeader("Feid") String feid,
                                  @RequestHeader("TraceId") String traceid,
                                  @RequestParam("serviceId") String serviceId,
                                  @RequestBody List<String> serviceProducts);

    @Retry(name = "ProductServiceRetry")
    @PostMapping(path = "/v2/authorization/authorize/{serviceId}", consumes = "application/json;charset=utf-8")
    XAuthorizeResponseV2 xauthorizeV2(@RequestHeader("Authorization") String authorization,
                                      @RequestHeader("Language") String language,
                                      @RequestHeader("Feid") String feid,
                                      @RequestHeader("TraceId") String traceid,
                                      @RequestParam("serviceId") String serviceId,
                                      @RequestBody List<String> serviceProducts);
}

import feign.Client;
import feign.httpclient.ApacheHttpClient;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class FeignClientConfig {

    @Bean
    public Client feignClient() {
        return new ApacheHttpClient(httpClient());
    }

    @Bean
    public CloseableHttpClient httpClient() {
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(100); // Maximum total connections
        connectionManager.setDefaultMaxPerRoute(20); // Max per route

        return HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setConnectTimeout(Duration.ofSeconds(5))
                        .setResponseTimeout(Duration.ofSeconds(10))
                        .build())
                .build();
    }
}

