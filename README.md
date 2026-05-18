```
package de.consorsbank.core.trauthsc.tam.client.draas;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.consorsbank.core.trauthsc.tam.client.draas.exception.DraasClientException;
import de.consorsbank.core.trauthsc.tam.client.dto.ClientErrorResponse;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
@RequiredArgsConstructor
public class DraasErrorDecoder implements ErrorDecoder {

    private final ObjectMapper objectMapper;
    private final ErrorDecoder defaultDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        int status = response.status();
        log.error("DRAAS client error - method: {}, status: {}", methodKey, status);

        ClientErrorResponse errorResponse = parseErrorResponse(response);

        return switch (HttpStatus.valueOf(status)) {
            case BAD_REQUEST -> new DraasClientException(
                status,
                errorResponse.getCode(),
                "DRAAS validation failed: " + errorResponse.getReason()
            );
            case NOT_FOUND -> new DraasClientException(
                status,
                errorResponse.getCode(),
                "DRAAS resource not found: " + errorResponse.getReason()
            );
            case INTERNAL_SERVER_ERROR -> new DraasClientException(
                status,
                errorResponse.getCode(),
                "DRAAS internal server error: " + errorResponse.getReason()
            );
            default -> defaultDecoder.decode(methodKey, response);
        };
    }

    private ClientErrorResponse parseErrorResponse(Response response) {
        try (InputStream body = response.body().asInputStream()) {
            return objectMapper.readValue(body, ClientErrorResponse.class);
        } catch (IOException e) {
            log.error("Failed to parse DRAAS error response", e);
            return new ClientErrorResponse();
        }
    }
}
```
