```
// Base class - generic over any ExceptionCode type
public abstract class BaseErrorCodesCustomizer<T extends ExceptionCode> 
        implements OperationCustomizer {

    @Override
    public Operation customize(Operation operation, HandlerMethod handlerMethod) {
        T[] codes = getAnnotationCodes(handlerMethod);
        if (codes == null) return operation;

        Arrays.stream(codes)
            .collect(Collectors.groupingBy(this::getHttpStatus))
            .forEach((status, groupedCodes) -> {
                String description = groupedCodes.stream()
                    .map(c -> "- **%s** (`%s`): %s"
                        .formatted(getName(c), getErrorCode(c), getMessage(c)))
                    .collect(Collectors.joining("\n"));

                operation.getResponses()
                    .addApiResponse(String.valueOf(status.value()),
                        new ApiResponse()
                            .description(description)
                            .content(new Content().addMediaType(
                                "application/json",
                                new MediaType().schema(buildSchema(groupedCodes))
                            )));
            });

        return operation;
    }

    protected abstract T[] getAnnotationCodes(HandlerMethod handlerMethod);
    protected abstract HttpStatus getHttpStatus(T code);
    protected abstract String getErrorCode(T code);
    protected abstract String getMessage(T code);
    protected abstract String getName(T code);

    private Schema<?> buildSchema(List<T> codes) {
        T first = codes.get(0);
        return new ObjectSchema()
            .addProperty("code",    new StringSchema().example(getErrorCode(first)))
            .addProperty("detail",  new StringSchema().example(getMessage(first)))
            .addProperty("errors",  new ArraySchema().example(List.of()))
            .addProperty("status",  new StringSchema()
                .example(String.valueOf(getHttpStatus(first).value())))
            .addProperty("title",   new StringSchema().example((Object) null))
            .addProperty("traceId", new StringSchema().example(""));
    }
}
```


```
@Component
public class TamErrorCodesCustomizer 
        extends BaseErrorCodesCustomizer<TamExceptionCode> {

    @Override
    protected TamExceptionCode[] getAnnotationCodes(HandlerMethod handlerMethod) {
        TamApiErrorCodes ann = handlerMethod.getMethodAnnotation(TamApiErrorCodes.class);
        return ann != null ? ann.value() : null;
    }

    @Override protected HttpStatus getHttpStatus(TamExceptionCode c) { return c.getHttpStatus(); }
    @Override protected String getErrorCode(TamExceptionCode c)       { return c.getErrorCode(); }
    @Override protected String getMessage(TamExceptionCode c)         { return c.getMessage(); }
    @Override protected String getName(TamExceptionCode c)            { return c.name(); }
}
```


```
@Component
public class PvmErrorCodesCustomizer 
        extends BaseErrorCodesCustomizer<PvmExceptionCode> {

    @Override
    protected PvmExceptionCode[] getAnnotationCodes(HandlerMethod handlerMethod) {
        PvmApiErrorCodes ann = handlerMethod.getMethodAnnotation(PvmApiErrorCodes.class);
        return ann != null ? ann.value() : null;
    }

    @Override protected HttpStatus getHttpStatus(PvmExceptionCode c) { return c.getHttpStatus(); }
    @Override protected String getErrorCode(PvmExceptionCode c)      { return c.getErrorCode(); }
    @Override protected String getMessage(PvmExceptionCode c)        { return c.getMessage(); }
    @Override protected String getName(PvmExceptionCode c)           { return c.name(); }
}

```


```
public interface ExceptionCode {
    String getErrorCode();
    String getMessage();
    HttpStatus getHttpStatus();
    String name(); // enums have this for free
}

```
