```
@Bean
public ErrorDecoder kycErrorDecoder() {
    return (String methodKey, Response response) -> {
        var status = response.status();
        LOG.error("Received HTTP response code {} from {}", status, kycRestProperties.getLoggingServiceName());

        var apiError = extractError(response);
        var errorMessage = apiError != null && apiError.message() != null
                ? apiError.message()
                : "KYC service error (HTTP " + status + ")";

        var httpStatus = HttpStatus.resolve(status);
        if (httpStatus == null) {
            return new CommonException(CommonExceptionCode.SERVER_ERROR, List.of(errorMessage));
        }

        return switch (httpStatus.series()) {
            case CLIENT_ERROR -> switch (status) {
                case 404 -> new CommonException(CustpmExceptionCode.KYC_PERSON_NOT_FOUND, List.of(errorMessage));
                case 400 -> new CommonException(CustpmExceptionCode.KWS_INVALID_REQ_BODY, List.of(errorMessage));
                case 401, 403 -> new CommonException(CommonExceptionCode.UNAUTHORIZED, List.of(errorMessage));
                default -> new CommonException(CommonExceptionCode.SERVER_ERROR, List.of(errorMessage));
            };
            case SERVER_ERROR -> new CommonException(CommonExceptionCode.SERVER_ERROR, List.of(errorMessage));
            default -> new CommonException(CommonExceptionCode.SERVER_ERROR, List.of(errorMessage));
        };
    };
}
```
