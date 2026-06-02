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
