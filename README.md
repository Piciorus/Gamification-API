```
package com.consorsbank.custpm.kyc.rest.adapter.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record KYCRestApiError(
    String code,
    String message,
    String origin
) {}
```


```
package com.consorsbank.custpm.kyc.rest.adapter.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;

import java.io.InputStream;

public class KYCRestErrorDecoder {

    private static final Logger LOG = LoggerFactory.getLogger(KYCRestErrorDecoder.class);
    private static final String SERVICE_NAME = "kyc-client";

    @Bean
    public ErrorDecoder kycErrorDecoder(ObjectMapper objectMapper) {
        return (methodKey, response) -> {
            int status = response.status();
            LOG.error("Received HTTP response code {} from {}", status, SERVICE_NAME);

            KYCRestApiError apiError = extractError(response, objectMapper);

            // Mirror the old ErrorInterceptor pattern — throw RestCommonException
            throw new RestCommonException(
                new Result(
                    apiError != null ? apiError.code() : KycRestClientExceptionCode.DEFAULT.getCode(),
                    apiError != null ? apiError.origin() : KycRestClientExceptionCode.DEFAULT.getOrigin(),
                    apiError != null ? apiError.message() : "KYC service error (status=" + status + ")",
                    ResultSeverity.ERROR,
                    status
                )
            );
        };
    }

    private KYCRestApiError extractError(Response response, ObjectMapper objectMapper) {
        try {
            if (response.body() == null) {
                return null;
            }
            try (InputStream is = response.body().asInputStream()) {
                return objectMapper.readValue(is, KYCRestApiError.class);
            }
        } catch (Exception e) {
            // Non-JSON response (HTML on 401/403, empty body, etc.)
            LOG.warn("Could not parse KYC error response body: {}", e.getMessage());
            return null;
        }
    }
}
```
