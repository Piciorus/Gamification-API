```
private static final Pattern TRACE_ID_IN_MESSAGE =
        Pattern.compile("(?i)[{,\\s]traceid=([^,}\\s]+)");
private static final Pattern REQUEST_ID_IN_MESSAGE =
        Pattern.compile("(?i)[{,\\s]x-request-id=([^,}\\s]+)");

private static String extractFromMessage(Object event, Pattern pattern) {
    if (!(event instanceof ILoggingEvent loggingEvent)) {
        return "";
    }
    String message = loggingEvent.getFormattedMessage();
    if (message == null || message.isEmpty()) {
        return "";
    }
    Matcher matcher = pattern.matcher(message);
    return matcher.find() ? matcher.group(1) : "";
}

```


```
String traceId = (String) MdcUtils.getTraceId()
        .orElse((String) mdc.getOrDefault("header.trace_id", ""));
if (traceId.isEmpty()) {
    // fallback, e.g. OPA lib logs written before MDC is populated
    traceId = extractFromMessage(deferredProcessingAware, TRACE_ID_IN_MESSAGE);
}

String requestId = (String) MdcUtils.getReqId()
        .orElse((String) mdc.getOrDefault("header.request_id", ""));
if (requestId.isEmpty()) {
    requestId = extractFromMessage(deferredProcessingAware, REQUEST_ID_IN_MESSAGE);
}

jsonGenerator.writeStringField("trace_id", traceId);
jsonGenerator.writeStringField("request_id", requestId);
```
