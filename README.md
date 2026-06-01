```
@Service
@RequiredArgsConstructor
@Slf4j
public class SubmitAuthorizationMethodServiceImpl implements SubmitAuthorizationMethodService {

    private final AuthorizationEngineService authorizationEngineService;
    private final SubmitAuthorizationMethodMapper submitAuthorizationMethodMapper;
    private final RequestSpecificData requestSpecificData;
    private final Map<UUID, AuthorizationMethodEnum> attemptIdToAuthMethod;
    private final AuthorizationRepository authorizationRepository;
    private final AuthorizationAttemptRepository authorizationAttemptRepository;

    @Override
    public SubmitAuthorizationMethodResponse submitAuthorization(
            String authorizationId,
            SubmitAuthorizationMethodRequest submitAuthorizationMethodRequest) {

        UUID attemptId = UUID.randomUUID();

        switch (submitAuthorizationMethodRequest.getAuthorizationMethod()) {
            case TAN_FROM_GENERATOR:
                attemptIdToAuthMethod.put(attemptId, AuthorizationMethodEnum.TAN_FROM_GENERATOR);
                break;
            case TAN_FROM_NEOAPP:
                attemptIdToAuthMethod.put(attemptId, AuthorizationMethodEnum.TAN_FROM_NEOAPP);
                break;
            case QRCODE_FROM_GENERATOR:
                attemptIdToAuthMethod.put(attemptId, AuthorizationMethodEnum.QRCODE_FROM_GENERATOR);
                break;
            case PUSH_NOTIFICATION_FORM_NEO_APP:
                attemptIdToAuthMethod.put(attemptId, AuthorizationMethodEnum.PUSH_NOTIFICATION_FORM_NEO_APP);
                break;
            case NEO_SECURE_SIGNATURE_UNBOUND:
                attemptIdToAuthMethod.put(attemptId, AuthorizationMethodEnum.NEO_SECURE_SIGNATURE_UNBOUND);
                break;
            case NEO_SECURE_SIGNATURE_BOUND:
                attemptIdToAuthMethod.put(attemptId, AuthorizationMethodEnum.NEO_SECURE_SIGNATURE_BOUND);
                break;
        }

        // Step 1 & 2: Check if AuthorizationEntity exists and method is allowed for service
        AuthorizationEntity authorizationEntity = authorizationRepository
                .findById(UUID.fromString(authorizationId))
                .orElseThrow(() -> new RuntimeException(
                        "AuthorizationEntity not found for id: " + authorizationId));

        AuthorizationMethodEnum requestedMethodEnum = submitAuthorizationMethodMapper
                .authorizationMethodToAuthorizationMethodEnum(
                        submitAuthorizationMethodRequest.getAuthorizationMethod());

        ServiceEntity serviceEntity = authorizationEntity.getServiceEntity();
        boolean methodAllowedForService = serviceEntity.getAuthorizationMethodEntities()
                .stream()
                .anyMatch(m -> m.getName().equals(requestedMethodEnum.name()));

        if (!methodAllowedForService) {
            throw new RuntimeException(
                    "Authorization method not allowed for this service: " + requestedMethodEnum);
        }

        // Step 3: Check if status permits the authorization attempt
        if (authorizationEntity.getStatus() != AuthorizationStatusEnum.INITIATED
                && authorizationEntity.getStatus() != AuthorizationStatusEnum.PENDING) {
            throw new RuntimeException(
                    "Authorization status does not permit a new attempt: "
                            + authorizationEntity.getStatus());
        }

        // Step 4: Check if same method already exists; special case for QR and push notif
        List<AuthorizationAttemptEntity> existingAttempts = authorizationAttemptRepository
                .findByAuthorizationIdWithMethod(authorizationEntity.getId());

        boolean sameMethodExists = existingAttempts.stream()
                .anyMatch(a -> a.getAuthorizationMethodEntity().getName()
                        .equals(requestedMethodEnum.name()));

        if (sameMethodExists) {
            throw new RuntimeException(
                    "An attempt with the same authorization method already exists.");
        }

        boolean isQrOrPush = requestedMethodEnum == AuthorizationMethodEnum.QRCODE_FROM_GENERATOR
                || requestedMethodEnum == AuthorizationMethodEnum.PUSH_NOTIFICATION_FORM_NEO_APP;

        if (isQrOrPush && !existingAttempts.isEmpty()) {
            throw new RuntimeException(
                    "QR/Push notification method cannot be combined with other active attempts.");
        }

        // Step 5: Build preliminary request arguments
        String template = "{\"metadata\":{\"supportedLanguages\":[\"de\"],"
                + "\"defaultLanguage\":\"de\",\"frontendId\":\"WEB\","
                + "\"credentialFlow\":\"SIGNATURE\"}";

        Map<String, String> dataMap = new HashMap<>();
        dataMap.put("ACCOUNT_NUMBER_IBAN", "DE09701204008402740008");
        dataMap.put("AMOUNT_VALUE", "1");

        TamOrderTypeEnum orderType = TamOrderTypeEnum.SEPA_PAYMENT_2;
        TamLanguageEnum language = TamLanguageEnum.DE;
        int validityDuration = 1200;
        int size = 300;

        // Step 5: Call authorization engine (2-step preliminary or inline)
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

            // Step 6: QR case — save attempt and return QR data response
            if (submitAuthorizationMethodRequest.getAuthorizationMethod()
                    == AuthorizationMethod.QRCODE_FROM_GENERATOR) {

                AuthorizationAttemptEntity attemptEntity = new AuthorizationAttemptEntity();
                attemptEntity.setExternalId(attemptId);
                attemptEntity.setAuthorizationEntity(authorizationEntity);
                attemptEntity.setStatus(AuthorizationAttemptStatusEnum.INITIATED);
                authorizationAttemptRepository.save(attemptEntity);

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

        // Step 6: Save attempt and update AuthorizationEntity status for non-QR flows
        AuthorizationAttemptEntity attemptEntity = new AuthorizationAttemptEntity();
        attemptEntity.setExternalId(attemptId);
        attemptEntity.setAuthorizationEntity(authorizationEntity);
        attemptEntity.setStatus(AuthorizationAttemptStatusEnum.INITIATED);
        authorizationAttemptRepository.save(attemptEntity);

        authorizationEntity.setStatus(AuthorizationStatusEnum.PENDING);
        authorizationRepository.save(authorizationEntity);

        return new SubmitAuthorizationMethodWithBaseFieldsResponse(
                AuthorizationAttemptStatus.INITIATED);
    }
}

```
