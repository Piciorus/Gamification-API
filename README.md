```
import feign.Logger;
import feign.Request;
import feign.Response;
import org.slf4j.LoggerFactory;
import java.io.IOException;

public class DurationFeignLogger extends Logger {

    private static final org.slf4j.Logger log =
            LoggerFactory.getLogger(DurationFeignLogger.class);

    @Override
    protected void logRequest(String configKey, Level logLevel, Request request) {
        // nothing – we only want the duration line
    }

    @Override
    protected Response logAndRebufferResponse(String configKey, Level logLevel,
                                              Response response, long elapsedTime) {
        Request request = response.request();
        log.info("Response for REST call {} {}: running time (millis) = {}",
                request.httpMethod(), request.url(), elapsedTime);
        return response;   // body is not read, so no rebuffering needed
    }

    @Override
    protected IOException logIOException(String configKey, Level logLevel,
                                         IOException ioe, long elapsedTime) {
        log.info("Response for REST call {} failed: running time (millis) = {}",
                configKey, elapsedTime);
        return ioe;
    }

    @Override
    protected void log(String configKey, String format, Object... args) {
        // not used
    }
}
```


```
@Configuration
public class FeignLoggingConfig {

    @Bean
    public feign.Logger feignLogger() {
        return new DurationFeignLogger();
    }

    @Bean
    public feign.Logger.Level feignLoggerLevel() {
        return feign.Logger.Level.BASIC;
    }
}
```
