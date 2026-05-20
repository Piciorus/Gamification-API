```
components:
  schemas:
    FetchAuthorizationStatusResponse:
      oneOf:
        - $ref: '#/components/schemas/SimpleAuthorizationStatusResponse'
        - $ref: '#/components/schemas/DetailedAuthorizationStatusResponse'

    SimpleAuthorizationStatusResponse:
      type: object
      required:
        - status
      properties:
        status:
          $ref: '../common/schemas.yaml#/components/schemas/AuthorizationStatus'
        authorizationId:
          type: string
          format: uuid
          description: ID of the authorization

    DetailedAuthorizationStatusResponse:
      type: object
      required:
        - status
      properties:
        status:
          $ref: '../common/schemas.yaml#/components/schemas/AuthorizationStatus'
        authorizationId:
          type: string
          format: uuid
          description: ID of the authorization
        items:
          type: array
          items:
            $ref: '#/components/schemas/AttemptsDetail'


components:
  schemas:
    FetchAuthorizationStatusResponse:
      oneOf:
        - $ref: '#/components/schemas/SimpleAuthorizationStatusResponse'
        - $ref: '#/components/schemas/DetailedAuthorizationStatusResponse'
      discriminator:
        propertyName: responseType
        mapping:
          simple: '#/components/schemas/SimpleAuthorizationStatusResponse'
          detailed: '#/components/schemas/DetailedAuthorizationStatusResponse'

    SimpleAuthorizationStatusResponse:
      type: object
      required:
        - responseType
        - status
      properties:
        responseType:
          type: string
        status:
          $ref: '../common/schemas.yaml#/components/schemas/AuthorizationStatus'
        authorizationId:
          type: string
          format: uuid
          description: ID of the authorization

    DetailedAuthorizationStatusResponse:
      type: object
      required:
        - responseType
        - status
      properties:
        responseType:
          type: string
        status:
          $ref: '../common/schemas.yaml#/components/schemas/AuthorizationStatus'
        authorizationId:
          type: string
          format: uuid
          description: ID of the authorization
        items:
          type: array
          items:
            $ref: '#/components/schemas/AttemptsDetail'

    AttemptsDetail:
      type: object
      properties:
        attemptId:
          type: string
          format: uuid
        statusAttempt:
          $ref: '../common/schemas.yaml#/components/schemas/AuthorizationAttemptStatus'
        authorizationMethod:
          $ref: '../common/schemas.yaml#/components/schemas/AuthorizationMethod'


@Slf4j
@RequiredArgsConstructor
public class DraasErrorDecoder implements ErrorDecoder {

    private final ObjectMapper objectMapper;
    private final ErrorDecoder defaultDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        int status = response.status();
        log.error("DRAAS client error - method: {}, status: {}", methodKey, status);

        var errorResponse = parseErrorResponse(response);
        String errorCode = errorResponse != null ? errorResponse.getCode() : null;

        // Incearca sa gaseasca excepția dupa errorCode
        if (errorCode != null) {
            Optional<TransactionAuthorizationExceptionCode> matchedCode =
                TransactionAuthorizationExceptionCode.findByErrorCode(errorCode);

            if (matchedCode.isPresent()) {
                return new CommonException(
                    matchedCode.get(),
                    Collections.singletonList(errorResponse.getReason())
                );
            }
        }

        // Fallback dupa HTTP status
        return switch (HttpStatus.valueOf(status)) {
            case BAD_REQUEST -> new CommonException(
                TransactionAuthorizationExceptionCode.INVALID_PAYLOAD_RECEIVED,
                Collections.singletonList(errorResponse != null ? errorResponse.getReason() : "Bad request")
            );
            case UNAUTHORIZED -> new CommonException(
                TransactionAuthorizationExceptionCode.DEVICE_TOKEN_MISSING,
                Collections.singletonList(errorResponse != null ? errorResponse.getReason() : "Unauthorized")
            );
            case FORBIDDEN -> new CommonException(
                TransactionAuthorizationExceptionCode.INVALID_PAYLOAD_OWNER_RECIEVED,
                Collections.singletonList(errorResponse != null ? errorResponse.getReason() : "Forbidden")
            );
            case NOT_FOUND -> new CommonException(
                TransactionAuthorizationExceptionCode.ENCRYPTION_KEY_NOT_FOUND,
                Collections.singletonList(errorResponse != null ? errorResponse.getReason() : "Not found")
            );
            case TOO_MANY_REQUESTS -> new CommonException(
                TransactionAuthorizationExceptionCode.MW_MAX_NR_OF_APPS_REACHED,
                Collections.singletonList(errorResponse != null ? errorResponse.getReason() : "Too many requests")
            );
            case CONFLICT -> new CommonException(
                TransactionAuthorizationExceptionCode.MW_KOBIL_LAST_GENERATOR,
                Collections.singletonList(errorResponse != null ? errorResponse.getReason() : "Conflict")
            );
            case INTERNAL_SERVER_ERROR -> new CommonException(
                TransactionAuthorizationExceptionCode.GENERAL_ERROR,
                Collections.singletonList(errorResponse != null ? errorResponse.getReason() : "Internal server error")
            );
            default -> defaultDecoder.decode(methodKey, response);
        };
    }

    private ClientErrorResponse parseErrorResponse(Response response) {
        try (InputStream body = response.body().asInputStream()) {
            return objectMapper.readValue(body, ClientErrorResponse.class);
        } catch (IOException e) {
            log.error("Failed to parse DRAAS error response", e);
            return null;
        }
    }
}
```

```
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClientErrorResponse {
    private String code;      // errorCode din DRAAS
    private String reason;    // mesajul
    private List<String> details;
}
```
