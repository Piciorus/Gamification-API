```
@Mapper(componentModel = "spring")
public interface AuthorizationMapper {

    @Mapping(target = "id", expression = "java(java.util.UUID.randomUUID())")
    @Mapping(target = "transactionId", source = "transactionId")
    @Mapping(target = "service", source = "transactionService")
    @Mapping(target = "serviceVersion", source = "transactionTypeVersion")
    @Mapping(target = "status", 
             expression = "java(AuthorizationStatus.WAITING_FOR_METHOD)")
    @Mapping(target = "isDeleted", constant = "0")
    @Mapping(target = "createdAt", 
             expression = "java(java.time.OffsetDateTime.now())")
    @Mapping(target = "updatedAt", 
             expression = "java(java.time.OffsetDateTime.now())")
    @Mapping(target = "version", constant = "0L")
    Authorization toEntity(
        InitiateTransactionAuthorizationRequest request);

    @Mapping(target = "authorizationId", source = "id")
    InitiateTransactionAuthorizationResponse toResponse(
        Authorization authorization);
}
```
```
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthorizationService {

    private final AuthorizationRepository authorizationRepository;
    private final AuthorizationMapper authorizationMapper;

    @Transactional
    public InitiateTransactionAuthorizationResponse initiateAuthorization(
            InitiateTransactionAuthorizationRequest request) {

        log.info("Initiating authorization for transactionId: {}", 
                 request.getTransactionId());

        // Check if authorization already exists for this transaction
        Optional<Authorization> existing = authorizationRepository
                .findByTransactionId(request.getTransactionId());

        if (existing.isPresent()) {
            log.info("Authorization already exists for transactionId: {}", 
                     request.getTransactionId());
            return authorizationMapper.toResponse(existing.get());
        }

        // Create new authorization
        Authorization authorization = authorizationMapper.toEntity(request);
        Authorization saved = authorizationRepository.save(authorization);

        log.info("Created authorization with id: {}", saved.getId());

        return authorizationMapper.toResponse(saved);
    }
}
```


```
@RestController
@RequestMapping("/v1/authorizations")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Initiate Transaction Authorization")
public class AuthorizationController {

    private final AuthorizationService authorizationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<InitiateTransactionAuthorizationResponse> 
            initiateAuthorization(
                @RequestBody @Valid 
                InitiateTransactionAuthorizationRequest request,
                @RequestHeader("x-correlation-id") 
                String correlationId) {

        log.info("Received initiate authorization request, " +
                 "correlationId: {}", correlationId);

        InitiateTransactionAuthorizationResponse response =
                authorizationService.initiateAuthorization(request);

        return ResponseEntity
                .created(URI.create(
                    "/v1/authorizations/" + response.getAuthorizationId()))
                .body(response);
    }
}

```
