```
@Override
public ResponseEntity<GenerateSignaturesResponse> generateSignatures(
        String authorization,
        String xRequestId,
        String appInstanceId,
        String txId) {
    
    // If appInstanceId not provided, get it from UserContext
    String resolvedAppInstanceId = Strings.isEmpty(appInstanceId)
        ? buildCustomerAuthInfoFromUserContext().getAppInstanceId()
        : appInstanceId;
    
    GenerateSignaturesResponse response = deviceRestService
        .generateSignatures(resolvedAppInstanceId, txId);
    
    return ResponseEntity.ok().body(response);
}
```
