```
@Override
public Map<String, Object> handleCallWithResult(ExemptionOrderCreateDTO request) {
    return Optional.ofNullable(createExemptionOrder(request))
            .map(CreateExemptionOrderRespDTO::exemptionOrderNumber)
            .<Map<String, Object>>map(number -> Map.of(EXEMPTION_ORDER_NUMBER, number))
            .orElse(Map.of());
}

/**
 * Required by {@link BusinessBaseHandler}. trauth-lib calls {@link #handleCallWithResult},
 * so this is only used by callers that don't need the execution data.
 */
@Override
public void handleCall(ExemptionOrderCreateDTO request) {
    createExemptionOrder(request);
}

private CreateExemptionOrderRespDTO createExemptionOrder(ExemptionOrderCreateDTO request) {
    return exemptionOrders.createExemptionOrder(request);
}

```
