grep -E "2026-06-15 14:(1[5-9]|[23][0-9]|4[0-5]):" custpi-sc-az1-*.log



private boolean getResult(CompletableFuture<Boolean> future) {
    try {
        return future.get(TIMEOUT, TimeUnit.SECONDS);
    } catch (TimeoutException e) {
        future.cancel(true);
        return false;
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt(); // restore interrupt status
        future.cancel(true);
        return false;
    } catch (ExecutionException e) {
        return false;
    }
}


```
SubmitAuthorizationMethodResponse:
  type: object
  required:
    - authorizationMethod
  properties:
    authorizationMethod:
      type: string
      enum: [BASE_FIELDS, QR_DATA]
  oneOf:
    - $ref: '#/components/schemas/SubmitAuthorizationMethodWithBaseFieldsResponse'
    - $ref: '#/components/schemas/SubmitAuthorizationMethodWithQrDataResponse'
  discriminator:
    propertyName: authorizationMethod

SubmitAuthorizationMethodWithBaseFieldsResponse:
  allOf:
    - $ref: '#/components/schemas/SubmitAuthorizationMethodResponse'
    - type: object
      required:
        - authorizationAttemptStatus
      properties:
        authorizationAttemptStatus:
          $ref: '../common/schemas.yaml#/components/schemas/AuthorizationAttemptStatus'

SubmitAuthorizationMethodWithQrDataResponse:
  allOf:
    - $ref: '#/components/schemas/SubmitAuthorizationMethodResponse'
    - type: object
      required:
        - authorizationAttemptStatus
        - qrCode
        - qrCodePayload
      properties:
        authorizationAttemptStatus:
          $ref: '../common/schemas.yaml#/components/schemas/AuthorizationAttemptStatus'
        qrCode:
          type: string
        qrCodePayload:
          type: string

```
