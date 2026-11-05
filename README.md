```
private void logDuration(MessageContext ctx) {
    Long startTime = (Long) ctx.getProperty(REQUEST_START_TIME);
    if (startTime != null) {
        String action = ((SoapMessage) ctx.getRequest()).getSoapAction();
        log.info("Response for SOAP {} duration(millis): {}",
                action, System.currentTimeMillis() - startTime);
    }
}
```

```
@Override
protected void doFilterInternal(final HttpServletRequest request,
                                final HttpServletResponse response,
                                final FilterChain filterChain) throws ServletException, IOException {
    final var requestWrapper = new CachingRequestWrapper(request, cacheWrapperProperties.contentCacheLimit());
    final var responseWrapper = new ContentCachingResponseWrapper(response);
    logRequest(requestWrapper);
    long startTime = System.currentTimeMillis();
    filterChain.doFilter(requestWrapper, responseWrapper);
    long duration = System.currentTimeMillis() - startTime;
    logResponse(requestWrapper, responseWrapper);          // ca înainte, fără durată
    logDuration(requestWrapper, duration);                 // log nou, separat
    responseWrapper.copyBodyToResponse();
}

// readus la forma originală
private void logResponse(final CachingRequestWrapper requestWrapper,
                         final ContentCachingResponseWrapper responseWrapper) {
    final String responseBody = new String(responseWrapper.getContentAsByteArray(),
            getResponseCharset(responseWrapper));
    log.info(FUNC_AUDIT, "Response for {} {} Status: {} Body: {}",
            requestWrapper.getMethod(), requestWrapper.getRequestURI(),
            responseWrapper.getStatus(), responseBody);
}

private void logDuration(final CachingRequestWrapper requestWrapper, final long duration) {
    log.info("Response for REST call {} {}: running time (millis) = {}",
            requestWrapper.getMethod(), requestWrapper.getRequestURI(), duration);
}

```
```
log.info("Response for SOAP call {}: running time (millis) = {}",
        soapAction, System.currentTimeMillis() - startTime);

```
