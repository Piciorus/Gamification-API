
```
package com.consorsbank.common.logging.feign;

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
        // only the duration line is logged
    }

    @Override
    protected Response logAndRebufferResponse(String configKey, Level logLevel,
                                              Response response, long elapsedTime) {
        Request request = response.request();
        log.info("Response for REST client '{} {}' call: running time (millis) = {}",
                request.httpMethod(), withoutQuery(request.url()), elapsedTime);
        return response;
    }

    @Override
    protected IOException logIOException(String configKey, Level logLevel,
                                         IOException ioe, long elapsedTime) {
        log.info("Response for REST client '{}' call failed: running time (millis) = {}",
                configKey, elapsedTime);
        return ioe;
    }

    @Override
    protected void log(String configKey, String format, Object... args) {
        // not used
    }

    private static String withoutQuery(String url) {
        int i = url.indexOf('?');
        return i < 0 ? url : url.substring(0, i);
    }
}

```


```

package com.consorsbank.common.logging.feign;

import feign.Logger;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass(Logger.class)
@ConditionalOnProperty(name = "commons.logging.feign-duration.enabled",
                       havingValue = "true", matchIfMissing = true)
public class FeignDurationLoggingAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(Logger.class)
    public Logger durationFeignLogger() {
        return new DurationFeignLogger();
    }

    @Bean
    @ConditionalOnMissingBean(Logger.Level.class)
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }
}
```
