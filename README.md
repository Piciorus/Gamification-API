```
@Service
@RequiredArgsConstructor
@Slf4j
public class SubmitAuthorizationCredentialServiceImpl implements SubmitAuthorizationCredentialService {

    private final AuthorizationEngineService authorizationEngineService;
    private final SubmitAuthorizationCredentialMapper submitAuthorizationCredentialMapper;
    private final AuthorizationRepository authorizationRepository;
    private final AuthorizationAttemptRepository authorizationAttemptRepository;
    private final TransactionEventJmsSender transactionEventJmsSender;

    @Override
    @Transactional(rollbackFor = CommonException.class)
    public SubmitAuthorizationCredentialResponse submitAuthorizationCredential(
            String authorizationHeader,
            String feId,
            String language,
            String traceId,
            String authorizationId,
            SubmitAuthorizationCredentialRequest submitAuthorizationCredentialRequest) {

        // Step 0: Check if GENERIC_TAN — length==6 -> TAN_FROM_GENERATOR, length==9 -> TAN_FROM_NEOAPP
        resolveGenericTanMethod(submitAuthorizationCredentialRequest);

        // Step 1: Check if authorization exists & attempt exists for authorizationId + method
        AuthorizationEntity authorizationEntity = findAuthorization(authorizationId);
        AuthorizationAttemptEntity authorizationAttemptEntity =
                findAttempt(authorizationEntity, submitAuthorizationCredentialRequest);

        // Step 2: Check status of AuthorizationEntity (status + expired + isDeleted)
        validateAuthorizationStatus(authorizationEntity);

        // Step 3: Check tenant & crmCustomerNumber match AuthorizationEntity
        validateTenantAndCrmCustomer(authorizationEntity, submitAuthorizationCredentialRequest);

        // Step 4: Check status of AuthorizationAttemptEntity (status + expired + isDeleted)
        validateAttemptStatus(authorizationAttemptEntity);

        // Step 5: Check there's no other attempt already approved
        validateNoApprovedAttemptExists(authorizationEntity);

        // Step 6: Check if template is required for service+method (TODO: TemplateManagementService)
        String template = resolveTemplate(submitAuthorizationCredentialRequest);

        // Build supporting data
        RequestSpecificData requestSpecificData =
                buildRequestSpecificData(
                        authorizationHeader, feId, language, traceId,
                        submitAuthorizationCredentialRequest);
        Map<String, String> dataMap = buildDataMap(submitAuthorizationCredentialRequest);
        TamOrderTypeEnum orderType = TamOrderTypeEnum.SEPA_PAYMENT_2;
        TamLanguageEnum tamLanguage = TamLanguageEnum.DE;

        // Step 7: Call authorization engine with credentials
        AuthorizationRequest authorizationRequest = submitAuthorizationCredentialMapper
                .submitAuthorizationCredentialRequestToAuthorizationRequest(
                        submitAuthorizationCredentialRequest,
                        requestSpecificData,
                        template,
                        authorizationId,
                        dataMap,
                        orderType,
                        tamLanguage,
                        authorizationHeader);
        AuthorizationResponse authorizationResponse =
                authorizationEngineService.submitAuthorization(authorizationRequest);

        // Step 8: Check there's no other attempt already approved (post-engine check)
        validateNoApprovedAttemptExists(authorizationEntity);

        // Step 9: Update AuthorizationEntity and AuthorizationAttemptEntity
        AuthorizationAttemptStatus status = resolveStatus(
                submitAuthorizationCredentialRequest, authorizationResponse);

        updateAttemptStatus(authorizationAttemptEntity, status);
        updateAuthorizationStatus(authorizationEntity, status);

        // Step 11: In case of success, set other attempts to AUTHORIZED_WITH_ANOTHER_METHOD
        if (status == AuthorizationAttemptStatus.AUTHORIZED) {
            markOtherAttemptsAsAuthorizedWithAnotherMethod(
                    authorizationEntity, authorizationAttemptEntity);
        }

        // Step 10: Send authorization status on queue to domain MS
        transactionEventJmsSender.sendMessage(UUID.fromString(authorizationId), status);

        // Note: This method doesn't make sense for PUSH_NOTIFICATION (result is async)
        if (submitAuthorizationCredentialRequest.getAuthorizationMethod()
                .equals(AuthorizationMethod.PUSH_NOTIFICATION_FORM_NEO_APP)) {
            log.warn("submitAuthorizationCredential called for PUSH_NOTIFICATION — "
                    + "result is async, returning PENDING status.");
        }

        return new SubmitAuthorizationCredentialResponse(status);
    }

    // -------------------------------------------------------------------------
    // Private helper methods
    // -------------------------------------------------------------------------

    private void resolveGenericTanMethod(
            SubmitAuthorizationCredentialRequest request) {
        if (!request.getAuthorizationMethod().equals(AuthorizationMethod.GENERIC_TAN)) {
            return;
        }
        String credential = request.getAuthorizationCredential();
        if (credential == null) {
            throw new CommonException(TamExceptionCode.AUTHORIZATION_CREDENTIAL_MISSING);
        }
        if (credential.length() == 6) {
            request.setAuthorizationMethod(AuthorizationMethod.TAN_FROM_GENERATOR);
        } else if (credential.length() == 9) {
            request.setAuthorizationMethod(AuthorizationMethod.TAN_FROM_NEOAPP);
        } else {
            throw new CommonException(
                    TamExceptionCode.AUTHORIZATION_CREDENTIAL_INVALID_LENGTH,
                    List.of(String.valueOf(credential.length())));
        }
    }

    private AuthorizationEntity findAuthorization(String authorizationId) {
        return authorizationRepository
                .findById(UUID.fromString(authorizationId))
                .orElseThrow(() -> new CommonException(
                        TamExceptionCode.AUTHORIZATION_NOT_FOUND,
                        List.of(authorizationId)));
    }

    private AuthorizationAttemptEntity findAttempt(
            AuthorizationEntity authorizationEntity,
            SubmitAuthorizationCredentialRequest request) {
        return authorizationAttemptRepository
                .findByAuthorizationEntityIdAndAuthorizationMethodEntityName(
                        authorizationEntity.getId(),
                        request.getAuthorizationMethod().name())
                .orElseThrow(() -> new CommonException(
                        TamExceptionCode.NO_ACTIVE_ATTEMPT_FOUND,
                        List.of(authorizationEntity.getId().toString(),
                                String.valueOf(request.getAuthorizationMethod()))));
    }

    private void validateAuthorizationStatus(AuthorizationEntity authorizationEntity) {
        if (authorizationEntity.getIsDeleted()) {
            throw new CommonException(
                    TamExceptionCode.AUTHORIZATION_IS_DELETED,
                    List.of(authorizationEntity.getId().toString()));
        }
        if (OffsetDateTime.now().isAfter(authorizationEntity.getExpiresAt())) {
            throw new CommonException(
                    TamExceptionCode.AUTHORIZATION_EXPIRED,
                    List.of(String.valueOf(authorizationEntity.getExpiresAt())));
        }
        if (authorizationEntity.getStatus() != AuthorizationStatusEnum.INITIATED
                && authorizationEntity.getStatus() != AuthorizationStatusEnum.PENDING) {
            throw new CommonException(
                    TamExceptionCode.AUTHORIZATION_STATUS_DOES_NOT_PERMIT_CREDENTIAL_SUBMISSION,
                    List.of(String.valueOf(authorizationEntity.getStatus())));
        }
    }

    private void validateAttemptStatus(AuthorizationAttemptEntity attemptEntity) {
        if (attemptEntity.getIsDeleted()) {
            throw new CommonException(
                    TamExceptionCode.AUTHORIZATION_IS_DELETED,
                    List.of(attemptEntity.getId().toString()));
        }
        if (attemptEntity.getStatus() != AuthorizationAttemptStatusEnum.INITIATED
                && attemptEntity.getStatus() != AuthorizationAttemptStatusEnum.PENDING) {
            throw new CommonException(
                    TamExceptionCode.AUTHORIZATION_STATUS_DOES_NOT_PERMIT_CREDENTIAL_SUBMISSION,
                    List.of(String.valueOf(attemptEntity.getStatus())));
        }
    }

    private void validateTenantAndCrmCustomer(
            AuthorizationEntity authorizationEntity,
            SubmitAuthorizationCredentialRequest request) {
        if (!authorizationEntity.getTenant().name()
                .equals(request.getTenant().getValue())
                || !authorizationEntity.getCrmCustomerNumber()
                .equals(request.getCrmCustomerNumber())) {
            throw new CommonException(
                    TamExceptionCode.AUTHORIZATION_TENANT_OR_CRM_MISMATCH);
        }
    }

    private void validateNoApprovedAttemptExists(AuthorizationEntity authorizationEntity) {
        boolean hasApprovedAttempt = authorizationAttemptRepository
                .existsByAuthorizationEntityIdAndStatus(
                        authorizationEntity.getId(),
                        AuthorizationAttemptStatusEnum.AUTHORIZED);
        if (hasApprovedAttempt) {
            throw new CommonException(
                    TamExceptionCode.AUTHORIZATION_ALREADY_APPROVED);
        }
    }

    private String resolveTemplate(SubmitAuthorizationCredentialRequest request) {
        // TODO: delegate to TemplateManagementService once available
        if (request.getAuthorizationMethod().equals(AuthorizationMethod.NEO_SECURE_SIGNATURE_BOUND)) {
            return "{\"metadata\":{\"supportedLanguages\":[\"de\"],\"defaultLanguage\":\"de\","
                    + "\"frontendId\":\"WEB\",\"credentialFlow\":\"SIGNATURE\","
                    + "\"transactionTemplate\":\"...\"}";
        }
        if (request.getAuthorizationMethod().equals(AuthorizationMethod.PUSH_NOTIFICATION_FORM_NEO_APP)) {
            return "{\"metadata\":{\"supportedLanguages\":[\"de\"],\"defaultLanguage\":\"de\","
                    + "\"frontendId\":\"\",\"credentialFlow\":\"SIGNATURE\","
                    + "\"transactionTemplate\":\"...\"}";
        }
        return "";
    }

    private RequestSpecificData buildRequestSpecificData(
            String authorizationHeader,
            String feId,
            String language,
            String traceId,
            SubmitAuthorizationCredentialRequest request) {
        RequestSpecificData data = new RequestSpecificData();
        data.setAuthorization(authorizationHeader);
        data.setLanguage(language);
        data.setFeId(feId);
        data.setTraceId(traceId);
        data.setRequestLanguage(language);
        data.setCrmCustomerno(request.getCrmCustomerNumber());
        data.setBackendOwnerNumber("1505396878");
        return data;
    }

    private Map<String, String> buildDataMap(SubmitAuthorizationCredentialRequest request) {
        Map<String, String> dataMap = new HashMap<>();
        dataMap.put("ACCOUNT_NUMBER_IBAN",
                request.getExtendedAuthorizationData().get("ACCOUNT_NUMBER_IBAN"));
        dataMap.put("AMOUNT_VALUE",
                request.getExtendedAuthorizationData().get("AMOUNT_VALUE"));
        return dataMap;
    }

    private AuthorizationAttemptStatus resolveStatus(
            SubmitAuthorizationCredentialRequest request,
            AuthorizationResponse authorizationResponse) {
        if (request.getAuthorizationMethod().equals(AuthorizationMethod.QRCODE_FROM_GENERATOR)) {
            return authorizationResponse.valid()
                    ? AuthorizationAttemptStatus.AUTHORIZED
                    : AuthorizationAttemptStatus.FAILED;
        }
        if (request.getAuthorizationMethod().equals(AuthorizationMethod.PUSH_NOTIFICATION_FORM_NEO_APP)) {
            return authorizationResponse.valid()
                    ? AuthorizationAttemptStatus.PENDING
                    : AuthorizationAttemptStatus.FAILED;
        }
        return authorizationResponse.valid()
                ? AuthorizationAttemptStatus.AUTHORIZED
                : AuthorizationAttemptStatus.FAILED;
    }

    private void updateAttemptStatus(
            AuthorizationAttemptEntity attemptEntity,
            AuthorizationAttemptStatus status) {
        attemptEntity.setStatus(AuthorizationAttemptStatusEnum.valueOf(status.name()));
        authorizationAttemptRepository.save(attemptEntity);
    }

    private void updateAuthorizationStatus(
            AuthorizationEntity authorizationEntity,
            AuthorizationAttemptStatus status) {
        if (status == AuthorizationAttemptStatus.AUTHORIZED) {
            authorizationEntity.setStatus(AuthorizationStatusEnum.AUTHORIZED);
        } else if (status == AuthorizationAttemptStatus.FAILED) {
            authorizationEntity.setStatus(AuthorizationStatusEnum.CANCELED);
        }
        authorizationRepository.save(authorizationEntity);
    }

    private void markOtherAttemptsAsAuthorizedWithAnotherMethod(
            AuthorizationEntity authorizationEntity,
            AuthorizationAttemptEntity currentAttempt) {
        List<AuthorizationAttemptEntity> otherAttempts = authorizationAttemptRepository
                .findByAuthorizationIdWithMethod(authorizationEntity.getId())
                .stream()
                .filter(a -> !a.getId().equals(currentAttempt.getId()))
                .collect(Collectors.toList());

        otherAttempts.forEach(a ->
                a.setStatus(AuthorizationAttemptStatusEnum.AUTHORIZED_WITH_ANOTHER_METHOD));
        authorizationAttemptRepository.saveAll(otherAttempts);
    }
}
```

```
boolean existsByAuthorizationEntityIdAndStatus(
        UUID authorizationId, AuthorizationAttemptStatusEnum status);
```

```
default AuthorizationMethodEntity toAuthorizationMethodEntity(
        SubmitAuthorizationMethodRequest request) {
    AuthorizationMethodEntity entity = new AuthorizationMethodEntity();
    entity.setName(String.valueOf(request.getAuthorizationMethod()));
    entity.setDescription(request.getAuthorizationMethod().getValue());
    return entity;
}
```
```
@Mapping(target = "authorizationMethodEntity", 
        expression = "java(toAuthorizationMethodEntity(request))")
```


```
@Service
@RequiredArgsConstructor
@Slf4j
public class SubmitAuthorizationMethodServiceImpl implements SubmitAuthorizationMethodService {

    private static final String OWNER = "OWNER";

    private final AuthorizationEngineService authorizationEngineService;
    private final SubmitAuthorizationMethodMapper submitAuthorizationMethodMapper;
    private final RequestSpecificData requestSpecificData;
    private final AuthorizationRepository authorizationRepository;
    private final AuthorizationAttemptRepository authorizationAttemptRepository;
    private final AuthorizationMethodRepository authorizationMethodRepository;

    @Override
    @Transactional(rollbackFor = CommonException.class)
    public SubmitAuthorizationMethodResponse submitAuthorization(
            String authorizationId,
            SubmitAuthorizationMethodRequest submitAuthorizationMethodRequest) {

        // Step 1: Check if authorization exists
        AuthorizationEntity authorizationEntity = findAuthorization(authorizationId);

        // Step 2: Check if status permits the authorization attempt
        validateAuthorizationStatus(authorizationEntity);

        AuthorizationMethodEnum requestedMethodEnum = submitAuthorizationMethodMapper
                .authorizationMethodToAuthorizationMethodEnum(
                        submitAuthorizationMethodRequest.getAuthorizationMethod());

        // Step 3: Check if an attempt already exists for the same authorization method
        validateNoExistingAttemptForMethod(authorizationEntity, submitAuthorizationMethodRequest);

        // Step 4: Check if authorization method is allowed for current service
        validateMethodAllowedForService(authorizationEntity, requestedMethodEnum);

        // Step 4': Check if tenant & crmCustomerNumber are the same as the AuthorizationEntity
        validateTenantAndCrmCustomer(authorizationEntity, submitAuthorizationMethodRequest);

        // Step 5: Check if template is required for service+method (will be handled by TemplateManagementService)
        String template = resolveTemplate(submitAuthorizationMethodRequest);

        // Build data for engine call
        Map<String, String> dataMap = buildDataMap(submitAuthorizationMethodRequest);
        TamOrderTypeEnum orderType = TamOrderTypeEnum.SEPA_PAYMENT_2;
        TamLanguageEnum language = TamLanguageEnum.DE;
        int validityDuration = 1200;
        int size = 300;

        // Step 6: Check if 2-step authorization and call engine for preliminary step
        if (authorizationEngineService.shouldPerformPreliminaryAuthorization(requestedMethodEnum)) {
            PreliminaryAuthorizationRequest preliminaryAuthorizationRequest =
                    submitAuthorizationMethodMapper
                            .submitAuthorizationMethodRequestToPreliminaryAuthorizationRequest(
                                    submitAuthorizationMethodRequest,
                                    requestSpecificData,
                                    template,
                                    authorizationEntity.getTransactionId().toString(),
                                    dataMap,
                                    orderType,
                                    language,
                                    validityDuration,
                                    size
                            );

            PreliminaryAuthorizationResponse preliminaryAuthorizationResponse =
                    authorizationEngineService.preliminaryAuthorizationSubmission(
                            preliminaryAuthorizationRequest);

            // Step 7: Save attempt and update AuthorizationEntity status
            AuthorizationAttemptEntity attemptEntity = buildAndSaveAttemptEntity(
                    authorizationEntity,
                    submitAuthorizationMethodRequest);

            updateAuthorizationStatus(authorizationEntity, AuthorizationStatusEnum.PENDING);

            if (submitAuthorizationMethodRequest.getAuthorizationMethod()
                    == AuthorizationMethod.QRCODE_FROM_GENERATOR) {
                return buildQrResponse(preliminaryAuthorizationResponse);
            }
        } else {
            // Step 7: Save attempt and update AuthorizationEntity status (inline flow)
            buildAndSaveAttemptEntity(authorizationEntity, submitAuthorizationMethodRequest);
            updateAuthorizationStatus(authorizationEntity, AuthorizationStatusEnum.PENDING);
        }

        return new SubmitAuthorizationMethodWithBaseFieldsResponse(
                AuthorizationAttemptStatus.PENDING);
    }

    // -------------------------------------------------------------------------
    // Private helper methods
    // -------------------------------------------------------------------------

    private AuthorizationEntity findAuthorization(String authorizationId) {
        return authorizationRepository
                .findById(UUID.fromString(authorizationId))
                .orElseThrow(() -> new CommonException(
                        TamExceptionCode.AUTHORIZATION_NOT_FOUND,
                        List.of(authorizationId)));
    }

    private void validateAuthorizationStatus(AuthorizationEntity authorizationEntity) {
        if (authorizationEntity.getStatus() != AuthorizationStatusEnum.INITIATED
                && authorizationEntity.getStatus() != AuthorizationStatusEnum.PENDING) {
            throw new CommonException(
                    TamExceptionCode.AUTHORIZATION_STATUS_DOES_NOT_PERMIT,
                    List.of(String.valueOf(authorizationEntity.getStatus())));
        }
        // TODO: also check expireAt and isDeleted
    }

    private void validateNoExistingAttemptForMethod(
            AuthorizationEntity authorizationEntity,
            SubmitAuthorizationMethodRequest submitAuthorizationMethodRequest) {

        boolean existsAttempt = authorizationAttemptRepository
                .existsAuthorizationIdWithMethod(
                        authorizationEntity.getId(),
                        submitAuthorizationMethodRequest.getAuthorizationMethod());

        if (existsAttempt) {
            throw new CommonException(
                    TamExceptionCode.ATTEMPT_WITH_SAME_AUTHORIZATION_METHOD_NOT_ALLOWED);
        }
    }

    private void validateMethodAllowedForService(
            AuthorizationEntity authorizationEntity,
            AuthorizationMethodEnum requestedMethodEnum) {

        ServiceEntity serviceEntity = authorizationEntity.getServiceEntity();
        boolean methodAllowed = serviceEntity.getAuthorizationMethodEntities()
                .stream()
                .anyMatch(m -> m.getName().equals(requestedMethodEnum.name()));

        if (!methodAllowed) {
            throw new CommonException(
                    TamExceptionCode.AUTHORIZATION_METHOD_NOT_ALLOWED,
                    List.of(String.valueOf(requestedMethodEnum)));
        }
    }

    private void validateTenantAndCrmCustomer(
            AuthorizationEntity authorizationEntity,
            SubmitAuthorizationMethodRequest submitAuthorizationMethodRequest) {

        if (!authorizationEntity.getTenant().name()
                .equals(submitAuthorizationMethodRequest.getTenant().getValue())
                || !authorizationEntity.getCrmCustomerNumber()
                .equals(submitAuthorizationMethodRequest.getCrmCustomerNumber())) {
            throw new CommonException(
                    TamExceptionCode.AUTHORIZATION_TENANT_OR_CRM_MISMATCH);
        }
    }

    private String resolveTemplate(
            SubmitAuthorizationMethodRequest submitAuthorizationMethodRequest) {
        // TODO: delegate to TemplateManagementService once available
        return "{\"metadata\":{\"supportedLanguages\":[\"de\"],\"defaultLanguage\":\"de\","
                + "\"frontendId\":\"WEB\",\"credentialFlow\":\"SIGNATURE\","
                + "\"transactionTemplate\":\"...\"}";
    }

    private Map<String, String> buildDataMap(
            SubmitAuthorizationMethodRequest submitAuthorizationMethodRequest) {
        Map<String, String> dataMap = new HashMap<>();
        dataMap.put("ACCOUNT_NUMBER_IBAN",
                submitAuthorizationMethodRequest.getExtendedAuthorizationData()
                        .get("ACCOUNT_NUMBER_IBAN"));
        dataMap.put("AMOUNT_VALUE",
                submitAuthorizationMethodRequest.getExtendedAuthorizationData()
                        .get("AMOUNT_VALUE"));
        return dataMap;
    }

    private AuthorizationAttemptEntity buildAndSaveAttemptEntity(
            AuthorizationEntity authorizationEntity,
            SubmitAuthorizationMethodRequest submitAuthorizationMethodRequest) {

        AuthorizationAttemptEntity attemptEntity =
                submitAuthorizationMethodMapper.toAttemptEntity(
                        authorizationEntity,
                        submitAuthorizationMethodRequest);

        return authorizationAttemptRepository.save(attemptEntity);
    }

    private void updateAuthorizationStatus(
            AuthorizationEntity authorizationEntity,
            AuthorizationStatusEnum newStatus) {

        submitAuthorizationMethodMapper.updateAuthorizationEntityStatus(
                authorizationEntity, newStatus);
        authorizationRepository.save(authorizationEntity);
    }

    private SubmitAuthorizationMethodWithQrDataResponse buildQrResponse(
            PreliminaryAuthorizationResponse preliminaryAuthorizationResponse) {

        SubmitAuthorizationMethodWithQrDataResponse response =
                new SubmitAuthorizationMethodWithQrDataResponse();
        response.setAuthorizationAttemptStatus(AuthorizationAttemptStatus.INITIATED);
        response.setQrCode(preliminaryAuthorizationResponse.image());
        response.setQrCodePayload(preliminaryAuthorizationResponse.payload());
        response.setQrCodeCreatedAt(
                OffsetDateTime.parse(preliminaryAuthorizationResponse.createdAt()));
        response.setQrCodeValidTo(
                OffsetDateTime.parse(preliminaryAuthorizationResponse.validBy()));
        return response;
    }
}
```


```
@Mapping(target = "externalId", expression = "java(java.util.UUID.randomUUID())")
@Mapping(target = "authorizationEntity", source = "authorizationEntity")
@Mapping(target = "status", constant = "INITIATED")
@Mapping(target = "authorizationMethodEntity.name",
        expression = "java(String.valueOf(request.getAuthorizationMethod()))")
@Mapping(target = "authorizationMethodEntity.description",
        expression = "java(request.getAuthorizationMethod().getValue())")
@Mapping(target = "createdBy", constant = "OWNER")
@Mapping(target = "updatedBy", constant = "OWNER")
@Mapping(target = "createdAt", expression = "java(java.time.OffsetDateTime.now())")
@Mapping(target = "updatedAt", expression = "java(java.time.OffsetDateTime.now())")
@Mapping(target = "isDeleted", constant = "false")
AuthorizationAttemptEntity toAttemptEntity(
        AuthorizationEntity authorizationEntity,
        SubmitAuthorizationMethodRequest request);

@Mapping(target = "status", source = "newStatus")
void updateAuthorizationEntityStatus(
        @MappingTarget AuthorizationEntity authorizationEntity,
        AuthorizationStatusEnum newStatus);
```


```
private void validateAuthorizationStatus(AuthorizationEntity authorizationEntity) {
    if (authorizationEntity.getIsDeleted()) {
        throw new CommonException(
                TamExceptionCode.AUTHORIZATION_IS_DELETED,
                List.of(String.valueOf(authorizationEntity.getId())));
    }

    if (OffsetDateTime.now().isAfter(authorizationEntity.getExpiresAt())) {
        throw new CommonException(
                TamExceptionCode.AUTHORIZATION_EXPIRED,
                List.of(String.valueOf(authorizationEntity.getExpiresAt())));
    }

    if (authorizationEntity.getStatus() != AuthorizationStatusEnum.INITIATED
            && authorizationEntity.getStatus() != AuthorizationStatusEnum.PENDING) {
        throw new CommonException(
                TamExceptionCode.AUTHORIZATION_STATUS_DOES_NOT_PERMIT,
                List.of(String.valueOf(authorizationEntity.getStatus())));
    }
}
```
