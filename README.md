@Service
@Slf4j
@RequiredArgsConstructor
public class InitiateTransactionAuthorizationService implements InitiateTransactionAuthorization {

    private final AuthorizationRepository authorizationRepository;
    private final ServiceRepository serviceRepository;
    private final InitiateTransactionAuthorizationMapper initiateTransactionAuthorizationMapper;
    private final InitiateTransactionEventSender initiateTransactionEventSender;

    /**
     * Initiates a new transaction authorization.
     * Validates that no authorization already exists for the given transactionId,
     * creates a new AuthorizationEntity with status PENDING,
     * persists it, and sends a PENDING event to the tx-queue.
     *
     * @param request the initiate transaction authorization request
     * @return InitiateTransactionAuthorizationResponse containing the authorizationId
     * @throws CommonException if authorization already exists or is in terminal state
     */
    @Override
    @Transactional(rollbackFor = CommonException.class)
    public InitiateTransactionAuthorizationResponse initiateTransactionAuthorization(
            InitiateTransactionAuthorizationRequest request) {

        log.info("Initiating authorization for transactionId {}.", request.getTransactionId());

        var authorizationEntity = getOrCreateAuthorizationEntity(request);
        var savedAuthorizationEntity = authorizationRepository.save(authorizationEntity);

        log.info("Authorization having transactionId {} was initiated.", request.getTransactionId());

        var response = initiateTransactionAuthorizationMapper.toResponse(savedAuthorizationEntity);

        initiateTransactionEventSender.sendMessage(
                savedAuthorizationEntity.getTransactionId(),
                AuthorizationStatusEnum.PENDING);

        return response;
    }

    /**
     * Get AuthorizationEntity by transactionId or create a new one if it does not exist.
     *
     * - PENDING exists      → throw already initiated
     * - AUTHORIZED/FAILED/EXPIRED exists → throw terminal state
     * - None exists         → create fresh PENDING entity
     */
    private AuthorizationEntity getOrCreateAuthorizationEntity(
            InitiateTransactionAuthorizationRequest request) {

        UUID transactionId = request.getTransactionId();

        if (transactionId != null) {
            for (AuthorizationEntity entity :
                    authorizationRepository.findAllByTransactionId(transactionId)) {

                if (entity.getStatus().equals(AuthorizationStatusEnum.PENDING)) {
                    log.error("Authorization for transactionId {} was already initiated.",
                            transactionId);
                    throw new CommonException(
                            CommonExceptionCode.SERVER_ERROR,
                            List.of("Transaction " + transactionId + " was already initiated."));
                }

                if (entity.getStatus().equals(AuthorizationStatusEnum.AUTHORIZED)
                        || entity.getStatus().equals(AuthorizationStatusEnum.FAILED)
                        || entity.getStatus().equals(AuthorizationStatusEnum.EXPIRED)) {
                    log.error("Authorization for transactionId {} is already in terminal state {}.",
                            transactionId, entity.getStatus());
                    throw new CommonException(
                            CommonExceptionCode.SERVER_ERROR,
                            List.of(transactionId.toString()));
                }
            }
        }

        return createPendingAuthorizationEntity(request);
    }

    /**
     * Creates a new PENDING AuthorizationEntity from the request.
     * Resolves ServiceEntity via OWNER + SERVICE composite key.
     */
    private AuthorizationEntity createPendingAuthorizationEntity(
            InitiateTransactionAuthorizationRequest request) {

        var authorizationEntity = initiateTransactionAuthorizationMapper.toEntity(request);

        // Resolve ServiceEntity — provides OWNER + SERVICE + SERVICE_VERSION columns
        // OWNER comes from tenant, SERVICE from transactionService
        var serviceEntity = serviceRepository
                .findByIdOwnerAndIdServiceAndIdServiceVersion(
                        request.getTenant().getValue(),
                        request.getTransactionService(),
                        request.getTransactionTypeVersion())
                .orElseThrow(() -> {
                    log.error("No service found for owner {}, service {}, version {}.",
                            request.getTenant(),
                            request.getTransactionService(),
                            request.getTransactionTypeVersion());
                    return new CommonException(
                            CommonExceptionCode.SERVER_ERROR,
                            List.of(request.getTransactionService()));
                });

        authorizationEntity.setServiceEntity(serviceEntity);
        return authorizationEntity;
    }

    /**
     * Validates if the provided transaction id already exists.
     * If it does, then an exception is raised.
     *
     * @param transactionId -- the transaction id to validate
     */
    private void validateAlreadyExistingTransaction(UUID transactionId) {
        if (authorizationRepository.existsByTransactionId(transactionId)) {
            log.error("Authorization already exists for transactionId {}.", transactionId);
            throw new CommonException(
                    CommonExceptionCode.SERVER_ERROR,
                    List.of(transactionId.toString()));
        }
    }
}
