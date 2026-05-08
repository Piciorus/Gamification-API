```
openapi: 3.0.0
security:
  - BearerAuthentication: []

info:
  title: Transaction Authorization Manager API
  description: REST API for the Transaction Authorization Microservice (TAM).
    TAM handles the full lifecycle of transaction authorization attempts.
    It supports two flows - INLINE (synchronous) and CHALLENGE_RESPONSE (asynchronous).
  version: 1.0.0

servers:
  - url: "https://tam-x0-de.pi.dev.echonet/svc/tam"
    description: Server url + baseUrl

paths:

  # ─────────────────────────────────────────────────────────────────────────
  # 1. INITIATE TRANSACTION AUTHORIZATION
  # ─────────────────────────────────────────────────────────────────────────
  /v1/authorizations:
    post:
      tags:
        - Transaction Authorization
      summary: Initiate a Transaction Authorization
      description: >
        Called by Business/Domain MS to initiate a new transaction authorization.
        TAM creates a TRANSACTIONS_AUTHORIZATION record and links it to the
        payload already stored in PVM via transactionPayloadId.
        Use Idempotency-Key header to avoid duplicate authorizations.
      operationId: initiateTransactionAuthorization
      parameters:
        - $ref: '../authorization/authorization-headers.yaml#/components/parameters/Authorization'
        - $ref: '../authorization/authorization-headers.yaml#/components/parameters/FeId'
        - $ref: '../authorization/authorization-headers.yaml#/components/parameters/Language'
        - $ref: '../authorization/authorization-headers.yaml#/components/parameters/TraceId'
        - $ref: '../common/common-headers.yaml#/components/parameters/userAgent'
        - $ref: '../common/common-headers.yaml#/components/parameters/x-source-service'
        - $ref: '../common/common-headers.yaml#/components/parameters/x-request-id'
        - $ref: '#/components/parameters/IdempotencyKey'
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/InitiateTransactionAuthorizationRequest'
            examples:
              basic:
                summary: Basic initiation
                value:
                  transactionId: "550e8400-e29b-41d4-a716-446655440000"
                  transactionType: "PAYMENT"
                  transactionPayloadId: "7f3e4567-e89b-12d3-a456-426614174abc"
                  expiresAt: "2026-05-07T10:00:00Z"
      responses:
        '201':
          description: Transaction authorization successfully initiated
          headers:
            x-correlation-id:
              $ref: '../common/common-headers.yaml#/components/parameters/x-correlation-id'
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/InitiateTransactionAuthorizationResponse'
              example:
                transactionAuthorizationId: "7f3e4567-e89b-12d3-a456-426614174000"
        '400':
          description: Bad request
          headers:
            x-correlation-id:
              $ref: '../common/common-headers.yaml#/components/parameters/x-correlation-id'
          content:
            application/json:
              schema:
                $ref: '../common/schemas.yaml#/components/schemas/ApiError'
        '401':
          description: Unauthorized
          headers:
            x-correlation-id:
              $ref: '../common/common-headers.yaml#/components/parameters/x-correlation-id'
          content:
            application/json:
              schema:
                $ref: '../common/schemas.yaml#/components/schemas/ApiError'
        '403':
          description: Forbidden
          headers:
            x-correlation-id:
              $ref: '../common/common-headers.yaml#/components/parameters/x-correlation-id'
          content:
            application/json:
              schema:
                $ref: '../common/schemas.yaml#/components/schemas/ApiError'
        '409':
          description: Transaction already has an active authorization
          headers:
            x-correlation-id:
              $ref: '../common/common-headers.yaml#/components/parameters/x-correlation-id'
          content:
            application/json:
              schema:
                $ref: '../common/schemas.yaml#/components/schemas/ApiError'
        '422':
          description: Unprocessable entity - business rule violation
          headers:
            x-correlation-id:
              $ref: '../common/common-headers.yaml#/components/parameters/x-correlation-id'
          content:
            application/json:
              schema:
                $ref: '../common/schemas.yaml#/components/schemas/ApiError'
        '500':
          description: Internal server error
          headers:
            x-correlation-id:
              $ref: '../common/common-headers.yaml#/components/parameters/x-correlation-id'
          content:
            application/json:
              schema:
                $ref: '../common/schemas.yaml#/components/schemas/ApiError'

  # ─────────────────────────────────────────────────────────────────────────
  # 2. SUBMIT AUTHORIZATION METHOD
  # ─────────────────────────────────────────────────────────────────────────
  /v1/authorizations/attempts:
    post:
      tags:
        - Transaction Authorization
      summary: Submit Authorization Method
      description: >
        Called by Business/Domain MS to submit the chosen authorization method
        and start a new authorization attempt.
        TAM creates a new AUTHORIZATION_ATTEMPTS record (status PENDING),
        determines the methodFlowType (INLINE or CHALLENGE_RESPONSE),
        and for CHALLENGE_RESPONSE sends the challenge asynchronously to the user.
      operationId: submitAuthorizationMethod
      parameters:
        - $ref: '../authorization/authorization-headers.yaml#/components/parameters/Authorization'
        - $ref: '../authorization/authorization-headers.yaml#/components/parameters/FeId'
        - $ref: '../authorization/authorization-headers.yaml#/components/parameters/Language'
        - $ref: '../authorization/authorization-headers.yaml#/components/parameters/TraceId'
        - $ref: '../common/common-headers.yaml#/components/parameters/userAgent'
        - $ref: '../common/common-headers.yaml#/components/parameters/x-source-service'
        - $ref: '../common/common-headers.yaml#/components/parameters/x-request-id'
        - $ref: '#/components/parameters/IdempotencyKey'
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/SubmitAuthorizationMethodRequest'
            examples:
              sms_otp:
                summary: Submit SMS OTP method
                value:
                  transactionAuthorizationId: "7f3e4567-e89b-12d3-a456-426614174000"
                  authorizationMethod: "SMS_OTP"
              pin:
                summary: Submit PIN method
                value:
                  transactionAuthorizationId: "7f3e4567-e89b-12d3-a456-426614174000"
                  authorizationMethod: "PIN"
      responses:
        '201':
          description: Authorization method submitted successfully
          headers:
            x-correlation-id:
              $ref: '../common/common-headers.yaml#/components/parameters/x-correlation-id'
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/SubmitAuthorizationMethodResponse'
              example:
                message: "Authorization method submitted successfully"
        '400':
          description: Bad request
          headers:
            x-correlation-id:
              $ref: '../common/common-headers.yaml#/components/parameters/x-correlation-id'
          content:
            application/json:
              schema:
                $ref: '../common/schemas.yaml#/components/schemas/ApiError'
        '401':
          description: Unauthorized
          headers:
            x-correlation-id:
              $ref: '../common/common-headers.yaml#/components/parameters/x-correlation-id'
          content:
            application/json:
              schema:
                $ref: '../common/schemas.yaml#/components/schemas/ApiError'
        '403':
          description: Forbidden
          headers:
            x-correlation-id:
              $ref: '../common/common-headers.yaml#/components/parameters/x-correlation-id'
          content:
            application/json:
              schema:
                $ref: '../common/schemas.yaml#/components/schemas/ApiError'
        '404':
          description: Transaction authorization not found
          headers:
            x-correlation-id:
              $ref: '../common/common-headers.yaml#/components/parameters/x-correlation-id'
          content:
            application/json:
              schema:
                $ref: '../common/schemas.yaml#/components/schemas/ApiError'
        '409':
          description: Authorization already finalized - cannot create new attempt
          headers:
            x-correlation-id:
              $ref: '../common/common-headers.yaml#/components/parameters/x-correlation-id'
          content:
            application/json:
              schema:
                $ref: '../common/schemas.yaml#/components/schemas/ApiError'
        '422':
          description: Unprocessable entity - business rule violation
          headers:
            x-correlation-id:
              $ref: '../common/common-headers.yaml#/components/parameters/x-correlation-id'
          content:
            application/json:
              schema:
                $ref: '../common/schemas.yaml#/components/schemas/ApiError'
        '500':
          description: Internal server error
          headers:
            x-correlation-id:
              $ref: '../common/common-headers.yaml#/components/parameters/x-correlation-id'
          content:
            application/json:
              schema:
                $ref: '../common/schemas.yaml#/components/schemas/ApiError'

  # ─────────────────────────────────────────────────────────────────────────
  # 3. SUBMIT AUTHORIZATION CREDENTIAL
  # ─────────────────────────────────────────────────────────────────────────
  /v1/authorizations/attempts/credential:
    post:
      tags:
        - Transaction Authorization
      summary: Submit Authorization Credential
      description: >
        Called by Business/Domain MS to submit the user's credential for a
        specific authorization attempt. TAM validates the credential and updates
        the attempt status to AUTHORIZED or FAILED.
        Business/Domain MS never validates credentials directly.
        Works for both INLINE (e.g. PIN) and CHALLENGE_RESPONSE (e.g. SMS OTP) flows.
      operationId: submitAuthorizationCredential
      parameters:
        - $ref: '../authorization/authorization-headers.yaml#/components/parameters/Authorization'
        - $ref: '../authorization/authorization-headers.yaml#/components/parameters/FeId'
        - $ref: '../authorization/authorization-headers.yaml#/components/parameters/Language'
        - $ref: '../authorization/authorization-headers.yaml#/components/parameters/TraceId'
        - $ref: '../common/common-headers.yaml#/components/parameters/userAgent'
        - $ref: '../common/common-headers.yaml#/components/parameters/x-source-service'
        - $ref: '../common/common-headers.yaml#/components/parameters/x-request-id'
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/SubmitAuthorizationCredentialRequest'
            examples:
              pin:
                summary: PIN credential
                value:
                  transactionAuthorizationId: "7f3e4567-e89b-12d3-a456-426614174000"
                  authorizationAttemptId: "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d"
                  authorizationCredential: "1234"
              otp:
                summary: SMS OTP credential
                value:
                  transactionAuthorizationId: "7f3e4567-e89b-12d3-a456-426614174000"
                  authorizationAttemptId: "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d"
                  authorizationCredential: "847291"
      responses:
        '200':
          description: >
            Credential processed successfully.
            Check the status field - AUTHORIZED means success, FAILED means invalid credential.
          headers:
            x-correlation-id:
              $ref: '../common/common-headers.yaml#/components/parameters/x-correlation-id'
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/SubmitAuthorizationCredentialResponse'
              examples:
                authorized:
                  summary: Credential valid - authorized
                  value:
                    transactionAuthorizationId: "7f3e4567-e89b-12d3-a456-426614174000"
                    authorizationAttemptId: "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d"
                    status: "AUTHORIZED"
                failed:
                  summary: Credential invalid - failed
                  value:
                    transactionAuthorizationId: "7f3e4567-e89b-12d3-a456-426614174000"
                    authorizationAttemptId: "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d"
                    status: "FAILED"
                    errorCode: "INVALID_CREDENTIAL"
        '400':
          description: Bad request - missing credential
          headers:
            x-correlation-id:
              $ref: '../common/common-headers.yaml#/components/parameters/x-correlation-id'
          content:
            application/json:
              schema:
                $ref: '../common/schemas.yaml#/components/schemas/ApiError'
        '401':
          description: Unauthorized
          headers:
            x-correlation-id:
              $ref: '../common/common-headers.yaml#/components/parameters/x-correlation-id'
          content:
            application/json:
              schema:
                $ref: '../common/schemas.yaml#/components/schemas/ApiError'
        '403':
          description: Forbidden
          headers:
            x-correlation-id:
              $ref: '../common/common-headers.yaml#/components/parameters/x-correlation-id'
          content:
            application/json:
              schema:
                $ref: '../common/schemas.yaml#/components/schemas/ApiError'
        '404':
          description: Authorization or attempt not found
          headers:
            x-correlation-id:
              $ref: '../common/common-headers.yaml#/components/parameters/x-correlation-id'
          content:
            application/json:
              schema:
                $ref: '../common/schemas.yaml#/components/schemas/ApiError'
        '409':
          description: Attempt already completed
          headers:
            x-correlation-id:
              $ref: '../common/common-headers.yaml#/components/parameters/x-correlation-id'
          content:
            application/json:
              schema:
                $ref: '../common/schemas.yaml#/components/schemas/ApiError'
        '422':
          description: Unprocessable entity - business rule violation
          headers:
            x-correlation-id:
              $ref: '../common/common-headers.yaml#/components/parameters/x-correlation-id'
          content:
            application/json:
              schema:
                $ref: '../common/schemas.yaml#/components/schemas/ApiError'
        '500':
          description: Internal server error
          headers:
            x-correlation-id:
              $ref: '../common/common-headers.yaml#/components/parameters/x-correlation-id'
          content:
            application/json:
              schema:
                $ref: '../common/schemas.yaml#/components/schemas/ApiError'

# ─────────────────────────────────────────────────────────────────────────
# COMPONENTS
# ─────────────────────────────────────────────────────────────────────────
components:

  securitySchemes:
    BearerAuthentication:
      type: http
      scheme: bearer
      bearerFormat: JWT

  parameters:
    IdempotencyKey:
      name: Idempotency-Key
      in: header
      required: false
      description: >
        Unique key to ensure idempotency.
        If the same key is reused, the cached response is returned.
      schema:
        type: string
        example: "a1b2c3d4-e5f6-7890-abcd-ef1234567890"

  schemas:

    # ── REQUESTS ────────────────────────────────────────────────────────────

    InitiateTransactionAuthorizationRequest:
      type: object
      required:
        - transactionId
        - transactionType
        - transactionPayloadId
      properties:
        transactionId:
          type: string
          format: uuid
          description: ID of the transaction managed by Business/Domain MS
          example: "550e8400-e29b-41d4-a716-446655440000"
        transactionType:
          type: string
          description: >
            Type of transaction defined by Business/Domain MS (free-form string).
            TAM does not validate this value.
          example: "PAYMENT"
        transactionPayloadId:
          type: string
          format: uuid
          description: >
            Reference ID of the payload already stored in PVM (Payload Vault Manager).
            TAM uses this ID to retrieve the payload when needed for credential validation.
          example: "7f3e4567-e89b-12d3-a456-426614174abc"
        expiresAt:
          type: string
          format: date-time
          description: Expiration timestamp received from Business/Domain MS (ISO 8601)
          example: "2026-05-07T10:00:00Z"

    SubmitAuthorizationMethodRequest:
      type: object
      required:
        - transactionAuthorizationId
        - authorizationMethod
      properties:
        transactionAuthorizationId:
          type: string
          format: uuid
          description: ID of the transaction authorization returned from the initiation step
          example: "7f3e4567-e89b-12d3-a456-426614174000"
        authorizationMethod:
          $ref: '#/components/schemas/AuthorizationMethod'

    SubmitAuthorizationCredentialRequest:
      type: object
      required:
        - transactionAuthorizationId
        - authorizationAttemptId
        - authorizationCredential
      properties:
        transactionAuthorizationId:
          type: string
          format: uuid
          description: ID of the transaction authorization
          example: "7f3e4567-e89b-12d3-a456-426614174000"
        authorizationAttemptId:
          type: string
          format: uuid
          description: ID of the authorization attempt returned from the submit method step
          example: "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d"
        authorizationCredential:
          type: string
          description: >
            The credential provided by the user.
            Mandatory for both INLINE (e.g. PIN) and CHALLENGE_RESPONSE (e.g. SMS OTP) flows.
          example: "847291"

    # ── RESPONSES ───────────────────────────────────────────────────────────

    InitiateTransactionAuthorizationResponse:
      type: object
      properties:
        transactionAuthorizationId:
          type: string
          format: uuid
          description: UUID of the created transaction authorization
          example: "7f3e4567-e89b-12d3-a456-426614174000"

    SubmitAuthorizationMethodResponse:
      type: object
      properties:
        message:
          type: string
          description: Confirmation message
          example: "Authorization method submitted successfully"

    SubmitAuthorizationCredentialResponse:
      type: object
      properties:
        transactionAuthorizationId:
          type: string
          format: uuid
          description: ID of the transaction authorization
          example: "7f3e4567-e89b-12d3-a456-426614174000"
        authorizationAttemptId:
          type: string
          format: uuid
          description: ID of the authorization attempt
          example: "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d"
        status:
          $ref: '#/components/schemas/AuthorizationAttemptStatus'
        errorCode:
          type: string
          description: Present only when status is FAILED
          example: "INVALID_CREDENTIAL"

    # ── ENUMS ───────────────────────────────────────────────────────────────

    AuthorizationMethod:
      type: string
      description: The authorization method chosen by the user
      enum:
        - SMS_OTP
        - PUSH
        - PIN
        - BIOMETRIC
        - EMAIL_OTP
      example: "SMS_OTP"

    AuthorizationAttemptStatus:
      type: string
      description: >
        Status of the authorization attempt.
        AUTHORIZED - credential valid, transaction authorized.
        FAILED - credential invalid or expired.
      enum:
        - AUTHORIZED
        - FAILED
      example: "AUTHORIZED"

```
