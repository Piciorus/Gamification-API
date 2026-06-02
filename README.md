```
@Slf4j
public class FeignCustomLogger extends feign.Logger {

    @Override
    protected void log(String configKey, String format, Object... args) {
        // suppress default feign logging - we handle it ourselves
    }

    @Override
    protected void logRequest(String configKey, Level logLevel, Request request) {
        log.info("[FEIGN REQUEST] {} {}", request.httpMethod(), request.url());
        if (request.body() != null) {
            log.info("[FEIGN REQUEST BODY] {}", new String(request.body()));
        }
    }

    @Override
    protected Response logAndRebufferResponse(
            String configKey, Level logLevel,
            Response response, long elapsedTime) throws IOException {

        byte[] bodyBytes = response.body() != null
                ? Util.toByteArray(response.body().asInputStream())
                : new byte[0];

        log.info("[FEIGN RESPONSE] {} {} ({}ms)",
                response.status(),
                response.request().url(),
                elapsedTime);

        if (bodyBytes.length > 0) {
            log.info("[FEIGN RESPONSE BODY] {}", new String(bodyBytes));
        }

        return response.toBuilder()
                .body(bodyBytes)
                .build();
    }
}
```



```

@Configuration
public class FeignLoggingConfiguration {

    @Bean
    public FeignCustomLogger feignCustomLogger() {
        return new FeignCustomLogger();
    }

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC; // needed to trigger logRequest/logAndRebufferResponse
    }
}
```


```
logging:
  level:
    de.consorsbank.core.trauthsc: INFO
    feign: INFO  # suppress feign's own debug noise
```


```
@Slf4j
public class FeignCustomLogger extends feign.Logger {

    @Override
    protected void log(String configKey, String format, Object... args) {
        // suppress default feign logging
    }

    @Override
    protected void logRequest(String configKey, Level logLevel, Request request) {
        String clientName = extractClientName(configKey);
        log.info("[{} REQUEST] {} {}", clientName, request.httpMethod(), request.url());
        if (request.body() != null) {
            log.info("[{} REQUEST BODY] {}", clientName, new String(request.body()));
        }
    }

    @Override
    protected Response logAndRebufferResponse(
            String configKey, Level logLevel,
            Response response, long elapsedTime) throws IOException {

        byte[] bodyBytes = response.body() != null
                ? Util.toByteArray(response.body().asInputStream())
                : new byte[0];

        String clientName = extractClientName(configKey);
        log.info("[{} RESPONSE] {} {} ({}ms)",
                clientName,
                response.status(),
                response.request().url(),
                elapsedTime);

        if (bodyBytes.length > 0) {
            log.info("[{} RESPONSE BODY] {}", clientName, new String(bodyBytes));
        }

        return response.toBuilder()
                .body(bodyBytes)
                .build();
    }

    private String extractClientName(String configKey) {
        // configKey format: "DraasFeignClient#validateTan(String,...)"
        if (configKey.contains("Draas")) {
            return "DRAAS";
        } else if (configKey.contains("Kobil")) {
            return "KOBIL";
        }
        // fallback: extract class name before #
        return configKey.contains("#")
                ? configKey.substring(0, configKey.indexOf("#"))
                : configKey;
    }
}
```
