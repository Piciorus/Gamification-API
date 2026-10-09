```
@RequiredArgsConstructor
public class KycRestErrorDecoder {

    private static final Logger LOG = LoggerFactory.getLogger(KycRestErrorDecoder.class);
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;
    private final KYCRestProperties kycRestProperties;

    @Bean
    public ErrorDecoder kycErrorDecoder() {
        return (String methodKey, Response response) -> {
            int status = response.status();
            LOG.error("Received HTTP response code {} from {}",
                    status, kycRestProperties.getLoggingServiceName());

            KycRestApiError apiError = extractError(response);

            if (apiError != null && apiError.code() != null) {
                throw new CommonException(
                        CommonExceptionCode.SERVER_ERROR,
                        List.of(apiError.message()));
            }

            throw new CommonException(CustpmExceptionCode.KWS_INVALID_REQ_BODY);
        };
    }

    private KycRestApiError extractError(Response response) {
        try {
            if (response.body() == null) {
                return null;
            }
            try (InputStream is = response.body().asInputStream()) {
                return objectMapper.readValue(is, KycRestApiError.class);
            }
        } catch (Exception e) {
            LOG.warn("Could not parse KYC error response body: {}", e.getMessage());
            return null;
        }
    }
}
```
