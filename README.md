
## Problem

The old Calvin service returned the `exemptionOrderNumber` (on success, and on some failures) in the initiate-transaction response. The new flow only returns:

```json
{
  "transactionId": "41241",
  "status": "FAILED"
}
```

Target response:

```json
{
  "transactionId": "41241",
  "status": "FAILED",
  "executionData": {
    "exemptionOrderNumber": "12421"
  }
}
```

## Root cause

The persistence already exists: `UpdateTransactionResult` calls `updateTransactionExecutionDetails`, which writes `executionResultData` into the `execution_result_data` BLOB column.

The result is lost in only two places:

1. The handler (`BusinessBaseHandler.handleCall`) never produces it (`void`).
2. Polling (`PollingTransactionService`) never reads it back, only the status.

## Approach

All changes in `trauth-lib` are **additive**: no existing signature is removed or changed. Current consumers keep working, so this ships as a **minor version bump**.

---

## trauth-lib (only additions)

### 1. `BusinessBaseHandler`: new default method

```java
/**
 * Executes the request and returns data exposed to the client as `executionData`.
 * Default keeps legacy behaviour: no execution data.
 */
default Map<String, Object> handleCallWithResult(T request) {
    handleCall(request);
    return Map.of();
}
```

Existing implementations don't change and keep compiling.

### 2. New interface for data on the failure path

```java
package de.consorsbank.core.trauth.exception;

public interface ExecutionDataAware {
    Map<String, Object> getExecutionData();
}
```

Calvin also returns the number on some failures. A consumer's exception can implement this interface to carry the number out of a failed execution.

### 3. `RequestPayloadExecutor`: keep `execute`, add a sibling

```java
// unchanged signature, now delegates
public void execute(Object request) {
    executeWithResult(request);
}

public Map<String, Object> executeWithResult(Object request) {
    var handler = resolveHandler(request.getClass());
    if (handler == null) {
        log.warn("No BaseHandler registered for request type [{}]; validation failed.",
                 request.getClass().getName());
        return Map.of();
    }
    return Optional.ofNullable(handler.handleCallWithResult(request)).orElse(Map.of());
}
```

> **Why not change `execute` to return a value?**
> Changing the return type changes the method's bytecode signature. Any consumer compiled against the old version would fail at runtime with `NoSuchMethodError`.

### 4. `TransactionPayloadExecution`

```java
private static final JsonMapper JSON = new JsonMapper();

try {
    var result = requestPayloadExecutor.executeWithResult(ctx.getTransactionPayload());
    ctx.setExecutionResultData(toJson(result));
    ctx.setStatus(TransactionStatus.TX_EXEC_SUCCESS);
} catch (CommonException e) {
    var error = getApiErrorV3(e.getErrorCode(),
            String.valueOf(e.getHttpStatus().value()), e.getMessage());
    ctx.setExecutionErrorData(mapErrorData(error));
    if (e instanceof ExecutionDataAware aware) {
        ctx.setExecutionResultData(toJson(aware.getExecutionData()));
    }
    ctx.setStatus(TransactionStatus.TX_EXEC_FAILED);
} catch (Exception e) {
    // unchanged
}

private static String toJson(Map<String, Object> data) {
    if (data == null || data.isEmpty()) {
        return null;
    }
    try {
        return JSON.writeValueAsString(data);
    } catch (JsonProcessingException ex) {
        throw new IllegalStateException("Cannot serialize execution data", ex);
    }
}
```

This also fixes the current `ctx.setExecutionResultData()` call with no argument (compile error).

### 5. `PollingTransactionService`: new method, old one kept

```java
public record TransactionPollingResult(TransactionStatus status, String executionResultData) {}
```

```java
@Retry(name = "pollTransactionDB")
public TransactionStatus pollToGetTransactionStatus(UUID id) {
    return pollFinal(id).getStatus();
}

@Retry(name = "pollTransactionDB")
public TransactionPollingResult pollToGetTransactionResult(UUID id) {
    var dto = pollFinal(id);
    var result = dto.getExecutionResultData() == null
            ? null
            : new String(dto.getExecutionResultData(), StandardCharsets.UTF_8);
    return new TransactionPollingResult(dto.getStatus(), result);
}

private TransactionDTO pollFinal(UUID id) {
    // existing body, returning the DTO instead of the status
}
```

> ⚠️ **Both public methods need their own `@Retry` and must call the private `pollFinal`.**
> If the old method called the new public one, the call would stay inside the same class and bypass the Spring proxy. Resilience4j would not retry, and polling would fail on the first attempt where the status isn't final yet.

### 6. `DatabasePollingForTransactionStatus`

```java
var result = pollingTransactionService.pollToGetTransactionResult(context.getTransactionId());
context.setStatus(result.status());
context.setExecutionResultData(result.executionResultData());
```

### 7. Response contract: optional field in `common/schemas.yaml`

```yaml
InitiateTransactionResponse:
  properties:
    # transactionId, status unchanged
    executionData:
      type: object
      additionalProperties: true
      description: Optional, only for inline transactions. Business-specific execution result.
```

- `executionData` is **not** in `required`, so the generated 2-argument constructor stays the same.
- `@JsonInclude(NON_NULL)` is already on the class, so existing consumers see identical JSON.

### 8. `MapperUtils`: new static helper

```java
public static Map<String, Object> getExecutionData(ProcessingContextDTO<?> ctx) {
    if (ctx.getExecutionResultData() == null) {
        return null;
    }
    try {
        return new JsonMapper().readValue(ctx.getExecutionResultData(), new TypeReference<>() {});
    } catch (JsonProcessingException e) {
        throw new CommonException(AuthorizationExceptionCode.INVALID_REQ_BODY);
    }
}
```

With this helper, `InitiateTransactionResponseDTO` and its `@AllArgsConstructor` don't need to change.

---

## taxsrv-sc

### `ExemptionOrders` / `AllExemptionOrderService`

`ExemptionOrders` is internal to taxsrv-sc, so its signature can change:

```java
String createExemptionOrder(ExemptionOrderCreateDTO exemptionOrderCreate); // was void
```

### `ExemptionOrderCreate`

```java
private static final String EXEMPTION_ORDER_NUMBER = "exemptionOrderNumber";

@Override
public void handleCall(ExemptionOrderCreateDTO request) {
    handleCallWithResult(request);
}

@Override
public Map<String, Object> handleCallWithResult(ExemptionOrderCreateDTO request) {
    var number = exemptionOrders.createExemptionOrder(request);
    return number == null ? Map.of() : Map.of(EXEMPTION_ORDER_NUMBER, number);
}
```

The null guard is needed because `Map.of` throws on null values.

### Failure case (number returned on failure)

```java
public class ExemptionOrderExecutionException extends CommonException implements ExecutionDataAware {

    private final Map<String, Object> executionData;

    // constructor: error code + exemptionOrderNumber from the downstream error response

    @Override
    public Map<String, Object> getExecutionData() {
        return executionData;
    }
}
```

### `initiateCreateExemptionOrderTransaction` and `initiateChangeExemptionOrderTransaction`

```java
initiateTransactionPipeline.execute(processingContext);

return new InitiateTransactionResponse(
        String.valueOf(processingContext.getResponseDTO().getTransactionId()),
        processingContext.getResponseDTO().getResponseStatus().toString())
    .executionData(MapperUtils.getExecutionData(processingContext));
```

---

## Compatibility check

| Change | Existing consumers |
|---|---|
| New default `handleCallWithResult` | Compile and run unchanged |
| `execute(Object)` still `void` | No `NoSuchMethodError` |
| `pollToGetTransactionStatus` kept | Unchanged |
| Optional `executionData` with `NON_NULL` | JSON unchanged unless data exists |
| New interface, record and helper | Purely additive |

The status endpoint (`GET`) is out of scope.

---

## Open questions (needed for the FAILED case)

1. **`handleFailureScenario`:** does it throw when the status isn't `TX_EXEC_SUCCESS`? If it does, a failed transaction ends up as an error response, and the number never reaches a 200 `FAILED` body. Need: the rest of that method, plus what Calvin returned in the failure case (HTTP status and body).
2. **Downstream create response:** what is the field name of the number on success, and what does the failure response look like when it includes the number?
3. **`TransactionDTO`:** does it already expose `getExecutionResultData()` as `byte[]`, like `getExecutionErrorData()`?

---

## Compliance

> ⚠️ **GDPR:** never log `executionResultData` or `executionData`. Consumers can put anything in there, including PII.
