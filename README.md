```
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TamApiErrorCodes {
    TamExceptionCode[] value();
}
```


```
@Component
@RequiredArgsConstructor
public class TamErrorCodesCustomizer implements OperationCustomizer {

    @Override
    public Operation customize(Operation operation, HandlerMethod handlerMethod) {
        TamApiErrorCodes annotation = handlerMethod
            .getMethodAnnotation(TamApiErrorCodes.class);

        if (annotation == null) return operation;

        // Group error codes by HTTP status
        Map<HttpStatus, List<TamExceptionCode>> byStatus = Arrays.stream(annotation.value())
            .collect(Collectors.groupingBy(TamExceptionCode::getHttpStatus));

        byStatus.forEach((status, codes) -> {
            String description = codes.stream()
                .map(c -> "- **%s** (`%s`): %s"
                    .formatted(c.name(), c.getErrorCode(), c.getMessage()))
                .collect(Collectors.joining("\n"));

            ApiResponse apiResponse = new ApiResponse()
                .description(description)
                .content(new Content().addMediaType(
                    "application/problem+json",
                    new MediaType().schema(new Schema<>().$ref(
                        "#/components/schemas/ProblemDetail"))
                ));

            operation.getResponses()
                .addApiResponse(String.valueOf(status.value()), apiResponse);
        });

        return operation;
    }
}

```


```
@Bean
public GroupedOpenApi applicationApiTransactionAuthorizationManager(
        TamErrorCodesCustomizer errorCodesCustomizer) {   // inject it
    return GroupedOpenApi.builder()
        .group("2-Transaction Authorization Manager")
        .displayName("Transaction Authorization Manager")
        .addOpenApiCustomizer(openApi -> readHtmlFile(openApi,
            "documentation/documentation-tam.html"))
        .addOperationCustomizer(errorCodesCustomizer)     // add here
        .packagesToScan("de.consorsbank.core.trauthsc.tam.controller")
        .build();
}
```


```
@TamApiErrorCodes({
    TamExceptionCode.AUTHORIZATION_ALREADY_APPROVED,  // 131 CONFLICT
    TamExceptionCode.INVALID_LANGUAGE                 // 135 BAD_REQUEST
})
@Override
public ResponseEntity<InitiateTransactionAuthorizationResponse> 
    initiateTransactionAuthorization(...) { ... }

```


```
@Bean
public OpenApiCustomizer problemDetailSchema() {
    return openApi -> openApi.getComponents()
        .addSchemas("ProblemDetail", new Schema<>()
            .type("object")
            .addProperty("type", new Schema<>().type("string"))
            .addProperty("title", new Schema<>().type("string"))
            .addProperty("status", new Schema<>().type("integer"))
            .addProperty("detail", new Schema<>().type("string"))
            .addProperty("errorCode", new Schema<>().type("string"))
        );
}

```
