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
