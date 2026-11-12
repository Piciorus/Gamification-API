
## Problem

Kordoba returns the exemption order number after creating an exemption order, but taxsrv-sc discards it:

```java
soapCall(kordobaRequest);   // response ignored
```

Every layer above returns `void`, so the number never reaches the handler. Without it, `executionData` in the initiate-transaction response stays empty.

## Target response

```json
{
  "transactionId": "41241",
  "status": "SUCCESS",
  "executionData": {
    "exemptionOrderNumber": "12421"
  }
}
```

## Call chain (all five layers change)

```
handlers/ExemptionOrderCreate.handleCallWithResult         → Map { "exemptionOrderNumber": ... }
  └─ ExemptionOrders / AllExemptionOrderService            → ExemptionOrderCreateResponseDTO
       └─ orders/ExemptionOrderCreate / ExemptionOrderCreateService → ExemptionOrderCreateResponseDTO
            └─ KordobaCreateExemptionOrdersService          → ExemptionOrderCreateResponseDTO
                 └─ soapCall(...)                            → KWSResFreistellungAnlegen001
```

All changes are internal to taxsrv-sc. The only dependency on trauth-lib is the new default method `handleCallWithResult` (see the trauth-lib README).

---

## 1. New DTO

Location: **core** package, next to `ExemptionOrderCreateDTO`.

```java
public record ExemptionOrderCreateResponseDTO(String exemptionOrderNo) {}
```

> **Why core and not the Kordoba adapter?**
> Core classes (`AllExemptionOrderService`, the handler) must not depend on SOAP types like `KWSResFreistellungAnlegen001`. The adapter maps the SOAP response to this DTO, and everything above only sees the DTO.

---

## 2. `KordobaCreateExemptionOrdersService`

**Before**

```java
public void createExemptionOrder(CreateExemptionOrderReqDTO createExemptionOrderRequest) {
    var kordobaRequest = createExemptionOrderRequestMapper.mapRequest(kordobaProperties, createExemptionOrderRequest);
    soapCall(kordobaRequest);
}
```

**After**

```java
public ExemptionOrderCreateResponseDTO createExemptionOrder(CreateExemptionOrderReqDTO createExemptionOrderRequest) {
    var kordobaRequest = createExemptionOrderRequestMapper.mapRequest(kordobaProperties, createExemptionOrderRequest);
    KWSResFreistellungAnlegen001 kordobaResponse = soapCall(kordobaRequest);
    return new ExemptionOrderCreateResponseDTO(extractExemptionOrderNo(kordobaResponse));
}

private static String extractExemptionOrderNo(KWSResFreistellungAnlegen001 response) {
    return Optional.ofNullable(response)
            .map(KWSResFreistellungAnlegen001::getXXX)   // TODO: real getter of the Freistellungsauftrag number
            .orElse(null);
}
```

---

## 3. `orders/ExemptionOrderCreate` + `ExemptionOrderCreateService`

**Interface**

```java
// before
void createExemptionOrder(ExemptionOrderCreateDTO exemptionOrderCreate);

// after
ExemptionOrderCreateResponseDTO createExemptionOrder(ExemptionOrderCreateDTO exemptionOrderCreate);
```

**Implementation**

```java
@Override
public ExemptionOrderCreateResponseDTO createExemptionOrder(ExemptionOrderCreateDTO exemptionOrderCreate) {
    var readExemptionOrderResponse = kordobaReadExemptionOrders.readExemptionOrders(/* unchanged */);
    var createExemptionOrderRequest = createExemptionOrderRequestMapper
            .toCreateExemptionOrderRequest(readExemptionOrderResponse /* unchanged */);
    return kordobaCreateExemptionOrdersService.createExemptionOrder(createExemptionOrderRequest);
}
```

---

## 4. `ExemptionOrders` + `AllExemptionOrderService`

**Interface**

```java
// before
void createExemptionOrder(ExemptionOrderCreateDTO exemptionOrderCreate);

// after
ExemptionOrderCreateResponseDTO createExemptionOrder(ExemptionOrderCreateDTO exemptionOrderCreate);
```

**Implementation**

```java
@Override
public ExemptionOrderCreateResponseDTO createExemptionOrder(ExemptionOrderCreateDTO exemptionOrderCreate) {
    return this.exemptionOrderCreate.createExemptionOrder(exemptionOrderCreate);
}
```

**Building the response** (both `initiateCreateExemptionOrderTransaction` and `initiateChangeExemptionOrderTransaction`)

```java
initiateTransactionPipeline.execute(processingContext);

return new InitiateTransactionResponse(
        String.valueOf(processingContext.getResponseDTO().getTransactionId()),
        processingContext.getResponseDTO().getResponseStatus().toString())
    .executionData(MapperUtils.getExecutionData(processingContext));
```

---

## 5. `handlers/ExemptionOrderCreate`

```java
private static final String EXEMPTION_ORDER_NUMBER = "exemptionOrderNumber";

@Override
public void handleCall(ExemptionOrderCreateDTO request) {
    handleCallWithResult(request);
}

@Override
public Map<String, Object> handleCallWithResult(ExemptionOrderCreateDTO request) {
    return Optional.ofNullable(exemptionOrders.createExemptionOrder(request))
            .map(ExemptionOrderCreateResponseDTO::exemptionOrderNo)
            .<Map<String, Object>>map(no -> Map.of(EXEMPTION_ORDER_NUMBER, no))
            .orElse(Map.of());
}
```

> **Naming:** the Java field is `exemptionOrderNo`. The JSON key stays `exemptionOrderNumber`, which matches the old Calvin response and the existing `ExemptionOrder` schema in `exemptions.yaml`.

> **Null safety:** `Map.of` throws on null values, so the `Optional` chain returns an empty map when Kordoba sends no number.

---

## Summary of signature changes

| Class | Before | After |
|---|---|---|
| `KordobaCreateExemptionOrdersService.createExemptionOrder` | `void` | `ExemptionOrderCreateResponseDTO` |
| `orders/ExemptionOrderCreate` (interface) | `void` | `ExemptionOrderCreateResponseDTO` |
| `ExemptionOrderCreateService` | `void` | `ExemptionOrderCreateResponseDTO` |
| `ExemptionOrders` (interface) | `void` | `ExemptionOrderCreateResponseDTO` |
| `AllExemptionOrderService.createExemptionOrder` | `void` | `ExemptionOrderCreateResponseDTO` |
| `handlers/ExemptionOrderCreate` | `handleCall` only | + `handleCallWithResult` |

---

## Checklist before committing

1. [ ] **`soapCall` return type.** In `KordobaWsBaseService`, confirm it is `protected RES soapCall(REQ request)`. If it returns `void` or `Object`, the base class needs a small additive change.
2. [ ] **Getter on `KWSResFreistellungAnlegen001`.** Find the Freistellungsauftrag number and replace `getXXX`.
3. [ ] **Number format.** If Calvin returned a different format (leading zeros, Neo format), apply `adaptNumberToNeoFormat` inside `extractExemptionOrderNo`, so every layer above gets the final value.
4. [ ] **Failure case.** Check whether `soapCall` throws on a Kordoba business error or returns a response with error messages. If the number is also present on failure, throw `ExemptionOrderExecutionException` (implements `ExecutionDataAware`) at that point, carrying the number.
5. [ ] **Tests.** Replace `doNothing()` mocks of `createExemptionOrder` with:
   ```java
   when(exemptionOrders.createExemptionOrder(any()))
           .thenReturn(new ExemptionOrderCreateResponseDTO("123"));
   ```
   Affected: `KordobaCreateExemptionOrdersService` (3 usages) and `ExemptionOrders` (3 usages).
6. [ ] **Change flow.** Apply the same pattern to the exemption order update handler if Calvin also returned the number on CHANGE.

---

## Compliance

> ⚠️ **GDPR:** never log `kordobaResponse`, `createResponse` or `readExemptionOrderResponse`. They contain names, birth dates and TINs.
