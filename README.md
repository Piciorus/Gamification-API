```
@Bean
public ErrorDecoder kycErrorDecoder() {
    return (String methodKey, Response response) -> {
        int status = response.status();
        LOG.error("Received HTTP response code {} from {}", status, kycRestProperties.getLoggingServiceName());

        KycRestApiError apiError = extractError(response);
        String errorMessage = apiError != null && apiError.message() != null
                ? apiError.message()
                : "KYC service error (HTTP " + status + ")";

        return switch (status) {
            case 404 -> new CommonException(CustpmExceptionCode.KYC_PERSON_NOT_FOUND, List.of(errorMessage));
            default -> {
                HttpStatus httpStatus = HttpStatus.resolve(status);
                if (httpStatus != null && httpStatus.is4xxClientError()) {
                    yield new CommonException(CustpmExceptionCode.KWS_INVALID_REQ_BODY, List.of(errorMessage));
                }
                yield new CommonException(CommonExceptionCode.SERVER_ERROR, List.of(errorMessage));
            }
        };
    };
}
```


```
KYC_PERSON_NOT_FOUND(errorCode: 141, NOT_FOUND, message: "Person not found in KYC system: {0}"),
```
