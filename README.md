```
private void logDuration(MessageContext messageContext) {
    if (messageContext == null) {
        return;
    }
    var requestStartTime = (Long) messageContext.getProperty(REQUEST_START_TIME);
    if (requestStartTime != null) {
        var ms = System.currentTimeMillis() - requestStartTime;
        log.info(TECH_STAT, "Stopwatch 'SOAP {}': running time (millis) = {}",
                extractAction(messageContext), ms);
    }
}

private static String extractAction(MessageContext messageContext) {
    var request = messageContext.getRequest();
    if (request == null) {
        return "";
    }
    if (!(request.getPayloadSource() instanceof DOMSource domSource)) {
        return "";
    }
    var node = domSource.getNode();
    if (node instanceof Document document) {
        node = document.getDocumentElement();
    }
    if (node == null) {
        return "";
    }
    var name = node.getLocalName() != null ? node.getLocalName() : node.getNodeName();
    return name != null ? name : "";
}

The new import is org.w3c.dom
```
