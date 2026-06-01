```
@Override
@Transactional
public SubmitAuthorizationCredentialResponse submitAuthorizationCredential(
        String authorizationHeader,
        String authorizationId,
        SubmitAuthorizationCredentialRequest submitAuthorizationCredentialRequest) {

    AuthorizationAttemptStatus status;

    // Build requestSpecificData
    RequestSpecificData requestSpecificDataManuallyComputed = new RequestSpecificData();
    requestSpecificDataManuallyComputed.setAuthorization(authorizationHeader);
    requestSpecificDataManuallyComputed.setLanguage("DE");
    requestSpecificDataManuallyComputed.setFeId("WEB");
    requestSpecificDataManuallyComputed.setTraceId("aTraceId");
    requestSpecificDataManuallyComputed.setRequestLanguage("DE");
    requestSpecificDataManuallyComputed.setCrmCustomerno(
            submitAuthorizationCredentialRequest.getCrmCustomerNumber());
    requestSpecificDataManuallyComputed.setBackendOwnerNumber("1505396878");

    // Template per method
    String template = "";
    if (submitAuthorizationCredentialRequest.getAuthorizationMethod()
            .equals(AuthorizationMethod.NEO_SECURE_SIGNATURE_BOUND)) {
        template = "{\"metadata\":{\"supportedLanguages\":[\"de\"],\"defaultLanguage\":\"de\","
                + "\"frontendId\":\"WEB\",\"credentialFlow\":\"SIGNATURE\",\"transactionTempl...";
    }
    if (submitAuthorizationCredentialRequest.getAuthorizationMethod()
            .equals(AuthorizationMethod.PUSH_NOTIFICATION_FORM_NEO_APP)) {
        template = "{\"metadata\":{\"supportedLanguages\":[\"de\"],\"defaultLanguage\":\"de\","
                + "\"frontendId\":\"\",\"credentialFlow\":\"SIGNATURE\",\"transactionTemplate...";
    }

    Map<String, String> dataMap = new HashMap<>();
    dataMap.put("ACCOUNT_NUMBER_IBAN", "DE09701204008402740008");
    dataMap.put("AMOUNT_VALUE", "1");
    TamOrderTypeEnum orderType = TamOrderTypeEnum.SEPA_PAYMENT_2;
    TamLanguageEnum language = TamLanguageEnum.DE;

    // Step 1: Check if authorizationId & attemptId exist in database
    AuthorizationEntity authorizationEntity = authorizationRepository
            .findByIdWithServiceAndMethods(UUID.fromString(authorizationId))
            .orElseThrow(() -> new RuntimeException(
                    "AuthorizationEntity not found for id: " + authorizationId));

    AuthorizationAttemptEntity authorizationAttemptEntity = authorizationAttemptRepository
            .findByAuthorizationEntityIdAndAuthorizationMethodEntityName(
                    authorizationEntity.getId(),
                    submitAuthorizationCredentialRequest.getAuthorizationMethod().name())
            .orElseThrow(() -> new RuntimeException(
                    "No active attempt found for authorizationId: " + authorizationId
                    + " and method: "
                    + submitAuthorizationCredentialRequest.getAuthorizationMethod()));

    // Step 2: Check if status of AuthorizationEntity and AuthorizationAttemptEntity
    //         permits credential submission
    if (authorizationEntity.getStatus() != AuthorizationStatusEnum.INITIATED
            && authorizationEntity.getStatus() != AuthorizationStatusEnum.PENDING) {
        throw new RuntimeException(
                "AuthorizationEntity status does not permit credential submission: "
                        + authorizationEntity.getStatus());
    }
    if (authorizationAttemptEntity.getStatus() != AuthorizationAttemptStatusEnum.INITIATED
            && authorizationAttemptEntity.getStatus() != AuthorizationAttemptStatusEnum.PENDING) {
        throw new RuntimeException(
                "AuthorizationAttemptEntity status does not permit credential submission: "
                        + authorizationAttemptEntity.getStatus());
    }

    // Step 3: Call authorization engine with credentials
    AuthorizationRequest authorizationRequest = submitAuthorizationCredentialMapper
            .submitAuthorizationCredentialRequestToAuthorizationRequest(
                    submitAuthorizationCredentialRequest,
                    requestSpecificDataManuallyComputed,
                    template,
                    authorizationEntity.getTransactionId().toString(),
                    dataMap,
                    orderType,
                    language
            );
    AuthorizationResponse authorizationResponse =
            authorizationEngineService.submitAuthorization(authorizationRequest);

    // Step 4: Update AuthorizationEntity and AuthorizationAttemptEntity based on response
    if (submitAuthorizationCredentialRequest.getAuthorizationMethod()
            .equals(AuthorizationMethod.QRCODE_FROM_GENERATOR)) {
        if (authorizationResponse.valid()) {
            status = AuthorizationAttemptStatus.AUTHORIZED;
        } else {
            status = AuthorizationAttemptStatus.FAILED;
        }
    } else if (submitAuthorizationCredentialRequest.getAuthorizationMethod()
            .equals(AuthorizationMethod.PUSH_NOTIFICATION_FORM_NEO_APP)) {
        if (authorizationResponse.valid()) {
            status = AuthorizationAttemptStatus.PENDING;
        } else {
            status = AuthorizationAttemptStatus.FAILED;
        }
    } else {
        if (authorizationResponse.valid()) {
            status = AuthorizationAttemptStatus.AUTHORIZED;
        } else {
            status = AuthorizationAttemptStatus.FAILED;
        }
    }

    // Persist updated statuses
    authorizationAttemptEntity.setStatus(
            AuthorizationAttemptStatusEnum.valueOf(status.name()));
    authorizationAttemptRepository.save(authorizationAttemptEntity);

    if (status == AuthorizationAttemptStatus.AUTHORIZED) {
        authorizationEntity.setStatus(AuthorizationStatusEnum.AUTHORIZED);
    } else if (status == AuthorizationAttemptStatus.FAILED) {
        authorizationEntity.setStatus(AuthorizationStatusEnum.CANCELED);
    }
    authorizationRepository.save(authorizationEntity);

    // Step 5: Send authorization status on queue to domain MS
    // TODO: wire in messaging/queue once infrastructure is known
    log.info("Authorization status to publish to domain MS: authorizationId={}, status={}",
            authorizationId, status);

    // Step 6: This method doesn't make sense for PUSH_NOTIFICATION (result is async)
    if (submitAuthorizationCredentialRequest.getAuthorizationMethod()
            .equals(AuthorizationMethod.PUSH_NOTIFICATION_FORM_NEO_APP)) {
        log.warn("submitAuthorizationCredential called for PUSH_NOTIFICATION — "
                + "result is async, returning PENDING status.");
    }

    return new SubmitAuthorizationCredentialResponse(status);
}
```
