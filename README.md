private AuthorizationEntity createPendingAuthorizationEntity(
        InitiateTransactionAuthorizationRequest request) {

    var authorizationEntity = initiateTransactionAuthorizationMapper.toEntity(request);

    // Build composite key: OWNER + SERVICE + SERVICE_VERSION
    var serviceId = new ServiceId();
    serviceId.setOwner(request.getTenant().getValue()); // "B2B" or "B2C" → OWNER
    serviceId.setService(request.getTransactionService()); // → SERVICE
    serviceId.setServiceVersion(request.getTransactionTypeVersion()); // → SERVICE_VERSION

    var serviceEntity = serviceRepository.findById(serviceId)
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
