```
private static final Pattern TRACE_ID_IN_MESSAGE =
        Pattern.compile("(?i)[{,\\s]traceid=([^,}\\s]+)");
private static final Pattern REQUEST_ID_IN_MESSAGE =
        Pattern.compile("(?i)[{,\\s]x-request-id=([^,}\\s]+)");

/** MdcUtils value -> MDC key -> value found in the log message -> "". */
private static String extractValue(Optional<String> fromMdcUtils,
                                   Map<String, String> mdc,
                                   String mdcKey,
                                   Object event,
                                   Pattern messagePattern) {
    String value = fromMdcUtils.orElse(mdc.get(mdcKey));
    if (value != null && !value.isEmpty()) {
        return value;
    }
    if (event instanceof ILoggingEvent loggingEvent) {
        String message = loggingEvent.getFormattedMessage();
        if (message != null && !message.isEmpty()) {
            Matcher matcher = messagePattern.matcher(message);
            if (matcher.find()) {
                return matcher.group(1);
            }
        }
    }
    return "";
}
```

```
String traceId = extractValue(MdcUtils.getTraceId(), mdc,
        "header.trace_id", deferredProcessingAware, TRACE_ID_IN_MESSAGE);
String requestId = extractValue(MdcUtils.getReqId(), mdc,
        "header.request_id", deferredProcessingAware, REQUEST_ID_IN_MESSAGE);
String threadName = deferredProcessingAware instanceof ILoggingEvent le
        ? le.getThreadName()
        : Thread.currentThread().getName();
String logClass = mdc.getOrDefault("header.log_class",
        LogClassification.UNCLASSIFIED.getValue());

jsonGenerator.writeStringField("trace_id", traceId);
jsonGenerator.writeStringField("request_id", requestId);
jsonGenerator.writeStringField("thread_name", threadName);
jsonGenerator.writeStringField("log_class", logClass);

```
