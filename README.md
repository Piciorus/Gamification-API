```
openapi: 3.0.3
info:
  title: Transaction Authorization Manager (TAM) API
  description: |
    API for the Transaction Authorization Microservice (TAM).
    
    TAM handles the full lifecycle of transaction authorization attempts.
    It supports two authorization flows:
    - **INLINE**: Credential is provided immediately → synchronous AUTHORIZED/FAILED response
    - **CHALLENGE_RESPONSE**: TAM generates a challenge (e.g. SMS OTP) → user responds asynchronously
    
    ## Flow Overview
    1. `POST /authorizations` — Business/Domain MS initiates a transaction authorization
    2. `POST /authorizations/{uuid}/attempts` — Business/Domain MS submits the chosen authorization method
    3. `PATCH /authorizations/{uuid}/attempts/{attemptUuid}/credential` — Business/Domain MS submits the credential
  version: 1.0.0
  contact:
    name: Transaction Authorization Team

servers:
  - url: https://api.example.com/tam/v1
    description: Production
  - url: https://api.staging.example.com/tam/v1
    description: Staging
  - url: http://localhost:8080/tam/v1
    description: Local development

tags:
  - name: Transaction Authorization
    description: Endpoints for managing transaction authorization lifecycle

paths:

  # ─────────────────────────────────────────────
  # 1. INITIATE TRANSACTION AUTHORIZATION
  # ─────────────────────────────────────────────
  /authorizations:
    post:
      tags:
        - Transaction Authorization
      operationId: initiateTransactionAuthorization
      summary: Initiate a Transaction Authorization
      description: |
        Called by Business/Domain MS to initiate a new transaction authorization.
        
        TAM will:
        - Create a new `TRANSACTIONS_AUTHORIZATION` record
        - Store the payload securely in PVM (Payload Vault)
        - Return the `transactionAuthorizationUuid` for subsequent calls
        
        **Idempotency**: Pass `Idempotency-Key` header to avoid duplicate authorizations.
        If the same key is used, the cached response is returned.
      parameters:
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
                  transactionOwner: "business-service-A"
                  transactionPayload:
                    amount: 1000.00
                    currency: "EUR"
                    recipient: "DE89370400440532013000"
                  expiresAt: "2026-05-07T10:00:00Z"
      responses:
        '201':
          description: Transaction authorization successfully initiated
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/InitiateTransactionAuthorizationResponse'
              example:
                transactionAuthorizationUuid: "7f3e4567-e89b-12d3-a456-426614174000"
                status: "PENDING"
        '400':
          $ref: '#/components/responses/BadRequest'
        '409':
          description: Transaction already has an active authorization
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
              example:
                errorCode: "TRANSACTION_ALREADY_AUTHORIZED"
                message: "An active authorization already exists for this transaction"
        '422':
          $ref: '#/components/responses/UnprocessableEntity'
        '500':
          $ref: '#/components/responses/InternalServerError'

  # ─────────────────────────────────────────────
  # 2. SUBMIT AUTHORIZATION METHOD
  # ─────────────────────────────────────────────
  /authorizations/{transactionAuthorizationUuid}/attempts:
    post:
      tags:
        - Transaction Authorization
      operationId: submitAuthorizationMethod
      summary: Submit Authorization Method
      description: |
        Called by Business/Domain MS to submit the chosen authorization method
        and start a new authorization attempt.
        
        TAM will:
        - Create a new `AUTHORIZATION_ATTEMPTS` record (status → PENDING)
        - Determine `methodFlowType` (INLINE or CHALLENGE_RESPONSE)
        
        **INLINE flow**: TAM expects credential to be submitted via `PATCH .../credential`
        
        **CHALLENGE_RESPONSE flow**: TAM generates and sends a challenge to the user (async).
        Business MS status → WAITING_FOR_CREDENTIAL.
        
        **Note**: Multiple attempts per authorization are possible (controlled by TAM policy).
        Check with DRAAS what happens when trying the same method multiple times (e.g. PUSH).
      parameters:
        - $ref: '#/components/parameters/TransactionAuthorizationUuid'
        - $ref: '#/components/parameters/IdempotencyKey'
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/SubmitAuthorizationMethodRequest'
            examples:
              inline:
                summary: INLINE flow - submit method
                value:
                  authorizationMethod: "PIN"
              challenge:
                summary: CHALLENGE_RESPONSE flow - submit method
                value:
                  authorizationMethod: "SMS_OTP"
      responses:
        '201':
          description: Authorization attempt created successfully
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/SubmitAuthorizationMethodResponse'
              examples:
                inline:
                  summary: INLINE flow response
                  value:
                    authorizationAttemptUuid: "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d"
                    methodFlowType: "INLINE"
                    status: "PENDING"
                challenge_response:
                  summary: CHALLENGE_RESPONSE flow response
                  value:
                    authorizationAttemptUuid: "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d"
                    methodFlowType: "CHALLENGE_RESPONSE"
                    status: "WAITING_FOR_CREDENTIAL"
                    challengeSentAsync: true
        '400':
          $ref: '#/components/responses/BadRequest'
        '404':
          $ref: '#/components/responses/NotFound'
        '409':
          description: Transaction authorization already finalized
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
              example:
                errorCode: "TRANSACTION_AUTHORIZATION_FINALIZED"
                message: "Cannot create new attempt - authorization is already finalized"
        '422':
          $ref: '#/components/responses/UnprocessableEntity'
        '500':
          $ref: '#/components/responses/InternalServerError'

  # ─────────────────────────────────────────────
  # 3. SUBMIT AUTHORIZATION CREDENTIAL
  # ─────────────────────────────────────────────
  /authorizations/{transactionAuthorizationUuid}/attempts/{authorizationAttemptUuid}/credential:
    patch:
      tags:
        - Transaction Authorization
      operationId: submitAuthorizationCredential
      summary: Submit Authorization Credential
      description: |
        Called by Business/Domain MS to submit the user's credential for a specific attempt.
        
        TAM will:
        - Validate the credential against the authorization method
        - Update `AUTHORIZATION_ATTEMPTS` status → AUTHORIZED or FAILED
        - If AUTHORIZED: update `TRANSACTIONS_AUTHORIZATION` status → AUTHORIZED
        - If FAILED: status → FAILED (Business MS can create a new attempt if policy allows)
        
        **INLINE flow**: credential was known upfront (e.g. PIN typed by user)
        
        **CHALLENGE_RESPONSE flow**: credential is the user's response to the challenge (e.g. SMS OTP code)
        
        **Key constraint**: Payload is sent to TAM only in this use case.
        Business/Domain MS never validates credentials directly.
      parameters:
        - $ref: '#/components/parameters/TransactionAuthorizationUuid'
        - $ref: '#/components/parameters/AuthorizationAttemptUuid'
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
                  authorizationCredential: "1234"
              otp:
                summary: SMS OTP credential
                value:
                  authorizationCredential: "847291"
      responses:
        '200':
          description: |
            Credential processed. Check `status` field:
            - `AUTHORIZED` — credential valid, transaction authorized
            - `FAILED` — credential invalid
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/SubmitAuthorizationCredentialResponse'
              examples:
                authorized:
                  summary: Successfully authorized
                  value:
                    authorizationAttemptUuid: "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d"
                    transactionAuthorizationUuid: "7f3e4567-e89b-12d3-a456-426614174000"
                    status: "AUTHORIZED"
                failed:
                  summary: Authorization failed
                  value:
                    authorizationAttemptUuid: "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d"
                    transactionAuthorizationUuid: "7f3e4567-e89b-12d3-a456-426614174000"
                    status: "FAILED"
                    errorCode: "INVALID_CREDENTIAL"
        '400':
          $ref: '#/components/responses/BadRequest'
        '404':
          $ref: '#/components/responses/NotFound'
        '409':
          description: Attempt already completed
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
              example:
                errorCode: "ATTEMPT_ALREADY_COMPLETED"
                message: "This authorization attempt has already been completed"
        '422':
          $ref: '#/components/responses/UnprocessableEntity'
        '500':
          $ref: '#/components/responses/InternalServerError'

  # ─────────────────────────────────────────────
  # 4. GET AUTHORIZATION STATUS (simple)
  # ─────────────────────────────────────────────
  /authorizations/{transactionAuthorizationUuid}/status:
    get:
      tags:
        - Transaction Authorization
      operationId: getTransactionAuthorizationStatus
      summary: Get Transaction Authorization Status
      description: |
        Returns the current status of a transaction authorization.
        Use `?detailed=true` to get the full list of attempts.
      parameters:
        - $ref: '#/components/parameters/TransactionAuthorizationUuid'
        - name: detailed
          in: query
          required: false
          schema:
            type: boolean
            default: false
          description: If true, returns full details including all authorization attempts
      responses:
        '200':
          description: Authorization status
          content:
            application/json:
              schema:
                oneOf:
                  - $ref: '#/components/schemas/AuthorizationStatusSimpleResponse'
                  - $ref: '#/components/schemas/AuthorizationStatusDetailedResponse'
              examples:
                simple:
                  summary: Simple status
                  value:
                    status: "AUTHORIZED"
                detailed:
                  summary: Detailed status
                  value:
                    transactionAuthorizationUuid: "7f3e4567-e89b-12d3-a456-426614174000"
                    status: "AUTHORIZED"
                    attempts:
                      - authorizationAttemptUuid: "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d"
                        status: "FAILED"
                      - authorizationAttemptUuid: "1c3a5e7b-9f2d-4c6e-8a0b-2d4f6e8a0b2d"
                        status: "AUTHORIZED"
        '404':
          $ref: '#/components/responses/NotFound'
        '500':
          $ref: '#/components/responses/InternalServerError'

  # ─────────────────────────────────────────────
  # 5. CANCEL AUTHORIZATION
  # ─────────────────────────────────────────────
  /authorizations/{transactionAuthorizationUuid}:
    delete:
      tags:
        - Transaction Authorization
      operationId: cancelTransactionAuthorization
      summary: Cancel Transaction Authorization
      description: |
        Cancels an active transaction authorization.
        Only possible if authorization is not yet in a final state (AUTHORIZED/FAILED).
      parameters:
        - $ref: '#/components/parameters/TransactionAuthorizationUuid'
      responses:
        '200':
          description: Authorization cancelled
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/CancelAuthorizationResponse'
              example:
                transactionAuthorizationUuid: "7f3e4567-e89b-12d3-a456-426614174000"
                status: "CANCELLED"
        '404':
          $ref: '#/components/responses/NotFound'
        '409':
          description: Cannot cancel - authorization already finalized
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
              example:
                errorCode: "AUTHORIZATION_ALREADY_FINALIZED"
                message: "Cannot cancel an already finalized authorization"
        '500':
          $ref: '#/components/responses/InternalServerError'

# ─────────────────────────────────────────────
# COMPONENTS
# ─────────────────────────────────────────────
components:

  parameters:
    TransactionAuthorizationUuid:
      name: transactionAuthorizationUuid
      in: path
      required: true
      description: UUID of the transaction authorization
      schema:
        type: string
        format: uuid
        example: "7f3e4567-e89b-12d3-a456-426614174000"

    AuthorizationAttemptUuid:
      name: authorizationAttemptUuid
      in: path
      required: true
      description: UUID of the authorization attempt
      schema:
        type: string
        format: uuid
        example: "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d"

    IdempotencyKey:
      name: Idempotency-Key
      in: header
      required: false
      description: |
        Unique key to ensure idempotency.
        If the same key is used, returns the cached response.
      schema:
        type: string
        example: "a1b2c3d4-e5f6-7890-abcd-ef1234567890"

  schemas:

    # ── REQUESTS ──────────────────────────────

    InitiateTransactionAuthorizationRequest:
      type: object
      required:
        - transactionId
        - transactionType
        - transactionOwner
      properties:
        transactionId:
          type: string
          format: uuid
          description: ID of the transaction managed by Business/Domain MS
          example: "550e8400-e29b-41d4-a716-446655440000"
        transactionType:
          type: string
          description: |
            Type of transaction - defined by Business/Domain MS (free-form string).
            Examples: PAYMENT, SEPA_TRANSFER, CARD_PAYMENT
          example: "PAYMENT"
        transactionOwner:
          type: string
          description: Identifier of the Business/Domain MS that owns this transaction
          example: "business-service-A"
        transactionPayload:
          type: object
          description: |
            The transaction payload (optional for new authorization attempt).
            Stored encrypted in PVM (Payload Vault).
          additionalProperties: true
          example:
            amount: 1000.00
            currency: "EUR"
            recipient: "DE89370400440532013000"
        expiresAt:
          type: string
          format: date-time
          description: Expiration timestamp received from Business/Domain MS
          example: "2026-05-07T10:00:00Z"

    SubmitAuthorizationMethodRequest:
      type: object
      required:
        - authorizationMethod
      properties:
        authorizationMethod:
          $ref: '#/components/schemas/AuthorizationMethod'

    SubmitAuthorizationCredentialRequest:
      type: object
      required:
        - authorizationCredential
      properties:
        authorizationCredential:
          type: string
          description: |
            The credential provided by the user.
            Mandatory for both INLINE and CHALLENGE_RESPONSE flows.
          example: "847291"

    # ── RESPONSES ─────────────────────────────

    InitiateTransactionAuthorizationResponse:
      type: object
      properties:
        transactionAuthorizationUuid:
          type: string
          format: uuid
          description: UUID of the created transaction authorization
          example: "7f3e4567-e89b-12d3-a456-426614174000"
        status:
          $ref: '#/components/schemas/TransactionAuthorizationStatus'

    SubmitAuthorizationMethodResponse:
      type: object
      properties:
        authorizationAttemptUuid:
          type: string
          format: uuid
          description: UUID of the created authorization attempt
          example: "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d"
        methodFlowType:
          $ref: '#/components/schemas/MethodFlowType'
        status:
          $ref: '#/components/schemas/AuthorizationAttemptStatus'
        challengeSentAsync:
          type: boolean
          description: Present only for CHALLENGE_RESPONSE flow
          example: true

    SubmitAuthorizationCredentialResponse:
      type: object
      properties:
        authorizationAttemptUuid:
          type: string
          format: uuid
          example: "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d"
        transactionAuthorizationUuid:
          type: string
          format: uuid
          example: "7f3e4567-e89b-12d3-a456-426614174000"
        status:
          $ref: '#/components/schemas/AuthorizationAttemptStatus'
        errorCode:
          type: string
          description: Present only when status is FAILED
          example: "INVALID_CREDENTIAL"

    AuthorizationStatusSimpleResponse:
      type: object
      properties:
        status:
          $ref: '#/components/schemas/TransactionAuthorizationStatus'

    AuthorizationStatusDetailedResponse:
      type: object
      properties:
        transactionAuthorizationUuid:
          type: string
          format: uuid
          example: "7f3e4567-e89b-12d3-a456-426614174000"
        status:
          $ref: '#/components/schemas/TransactionAuthorizationStatus'
        attempts:
          type: array
          items:
            $ref: '#/components/schemas/AuthorizationAttemptSummary'

    AuthorizationAttemptSummary:
      type: object
      properties:
        authorizationAttemptUuid:
          type: string
          format: uuid
          example: "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d"
        status:
          $ref: '#/components/schemas/AuthorizationAttemptStatus'

    CancelAuthorizationResponse:
      type: object
      properties:
        transactionAuthorizationUuid:
          type: string
          format: uuid
          example: "7f3e4567-e89b-12d3-a456-426614174000"
        status:
          type: string
          example: "CANCELLED"

    ErrorResponse:
      type: object
      required:
        - errorCode
        - message
      properties:
        errorCode:
          type: string
          description: Machine-readable error code
          example: "INVALID_TRANSACTION_INTENT_ID"
        message:
          type: string
          description: Human-readable error message
          example: "TransactionIntentId not found"

    # ── ENUMS ─────────────────────────────────

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

    MethodFlowType:
      type: string
      description: |
        Determines how the authorization flow proceeds:
        - INLINE: credential provided immediately, synchronous result
        - CHALLENGE_RESPONSE: TAM sends challenge to user, credential submitted later
      enum:
        - INLINE
        - CHALLENGE_RESPONSE
      example: "INLINE"

    TransactionAuthorizationStatus:
      type: string
      description: |
        Status of the overall transaction authorization:
        - PENDING: waiting for method/credential
        - WAITING_FOR_CREDENTIAL: challenge sent, waiting for user response
        - AUTHORIZED: successfully authorized
        - FAILED: authorization failed
        - CANCELLED: cancelled by Business MS
      enum:
        - PENDING
        - WAITING_FOR_CREDENTIAL
        - AUTHORIZED
        - FAILED
        - CANCELLED
      example: "AUTHORIZED"

    AuthorizationAttemptStatus:
      type: string
      description: |
        Status of a single authorization attempt:
        - PENDING: attempt created, credential not yet validated
        - WAITING_FOR_CREDENTIAL: challenge sent (CHALLENGE_RESPONSE flow)
        - AUTHORIZED: credential valid
        - FAILED: credential invalid
      enum:
        - PENDING
        - WAITING_FOR_CREDENTIAL
        - AUTHORIZED
        - FAILED
      example: "AUTHORIZED"

  # ── COMMON RESPONSES ──────────────────────────
  responses:
    BadRequest:
      description: Bad request - invalid input
      content:
        application/json:
          schema:
            $ref: '#/components/schemas/ErrorResponse'
          examples:
            missing_credential:
              value:
                errorCode: "MISSING_CREDENTIAL"
                message: "Credential is required for INLINE flow"
            invalid_method:
              value:
                errorCode: "INVALID_METHOD"
                message: "Method not supported or not available"

    NotFound:
      description: Resource not found
      content:
        application/json:
          schema:
            $ref: '#/components/schemas/ErrorResponse'
          example:
            errorCode: "TRANSACTION_AUTHORIZATION_NOT_FOUND"
            message: "Transaction authorization not found"

    UnprocessableEntity:
      description: Unprocessable entity - business rule violation
      content:
        application/json:
          schema:
            $ref: '#/components/schemas/ErrorResponse'
          examples:
            invalid_transaction_intent:
              value:
                errorCode: "INVALID_TRANSACTION_INTENT_ID"
                message: "TransactionIntentId not found"
            transaction_finalized:
              value:
                errorCode: "TRANSACTION_ALREADY_FINALIZED"
                message: "Transaction is already in a final state"

    InternalServerError:
      description: Internal server error
      content:
        application/json:
          schema:
            $ref: '#/components/schemas/ErrorResponse'
          example:
            errorCode: "INTERNAL_SERVER_ERROR"
            message: "An unexpected error occurred"

```


```

TRANSACTION AUTHORIZATION MANAGER
API Design Document
Version
1.0
Status
Ready for Review
Microservice
Transaction Authorization Manager (TAM)
Date
May 2026
Author
Alexandru Piciorus


1. Overview
The Transaction Authorization Manager (TAM) is responsible for the full lifecycle of transaction authorization. Business/Domain microservices delegate all authorization logic to TAM — they never validate credentials directly.

1.1 Authorization Flows
TAM supports two flows:
	•	INLINE — The user provides their credential immediately (e.g. PIN). TAM validates synchronously and returns AUTHORIZED or FAILED.
	•	CHALLENGE_RESPONSE — TAM generates and sends a challenge to the user (e.g. SMS OTP). The user responds asynchronously. Business MS status transitions to WAITING_FOR_CREDENTIAL.

1.2 Key Constraints
	•	Payload is sent to TAM only once — on initiation.
	•	Authorization Attempt lifecycle is fully managed by TAM.
	•	Business/Domain MS never validates credentials.
	•	One Transaction Authorization can have multiple Authorization Attempts (controlled by TAM policy).
	•	TAM stores payload securely in PVM (Payload Vault Manager).

2. Data Model Summary
TAM Schema
TRANSACTIONS_AUTHORIZATION — one record per authorization request
	•	id, uuid (UI), transaction_id, transaction_type, transaction_owner
	•	status (PENDING, WAITING_FOR_CREDENTIAL, AUTHORIZED, FAILED, CANCELLED)
	•	expires_at, created_at, updated_at, deleted_at

AUTHORIZATION_ATTEMPTS — multiple attempts per authorization (1→N)
	•	id, uuid (UI), transition_authorization_id (FK)
	•	authorization_method_id, authorization_credential_string
	•	status (PENDING, WAITING_FOR_CREDENTIAL, AUTHORIZED, FAILED)

PVM Schema
PAYLOAD_VAULT — stores encrypted transaction payloads
	•	id, uuid (UI), transaction_id, payload (json encrypted at rest)
	•	checksum, owner, expires_at
	•	Logical relation only — no FK to TAM tables

3. Enumerations
Authorization Methods
Method
Flow Type
Description
SMS_OTP
CHALLENGE_RESPONSE
One-time password sent via SMS
PUSH
CHALLENGE_RESPONSE
Push notification to mobile app (check DRAAS for parallel attempt rules)
PIN
INLINE
User enters PIN directly
BIOMETRIC
INLINE
Fingerprint or face recognition
EMAIL_OTP
CHALLENGE_RESPONSE
One-time password sent via email

Transaction Authorization Status
Status
Description
PENDING
Authorization created, waiting for method/credential
WAITING_FOR_CREDENTIAL
Challenge sent (CHALLENGE_RESPONSE flow), waiting for user response
AUTHORIZED
Authorization successful
FAILED
Authorization failed - credential invalid or expired
CANCELLED
Cancelled by Business/Domain MS

4. API Endpoints
4.1 Initiate Transaction Authorization
POST
/authorizations

Called by Business/Domain MS to create a new transaction authorization. TAM stores the payload securely in PVM and returns a transactionAuthorizationUuid for subsequent calls. Use the Idempotency-Key header to prevent duplicate authorizations.

Request Headers
Field
Type
Req?
Description
Idempotency-Key
string
No
Unique key to prevent duplicate requests. Returns cached response if same key reused.
Content-Type
string
Yes
application/json

Request Body
Field
Type
Req?
Description
transactionId
uuid
Yes
ID of the transaction managed by Business/Domain MS
transactionType
string
Yes
Type of transaction (free-form, defined by Business MS). E.g. PAYMENT, SEPA_TRANSFER
transactionOwner
string
Yes
Identifier of the Business/Domain MS that owns this transaction
transactionPayload
object
No
Transaction payload - stored encrypted in PVM. Optional for new authorization attempt.
expiresAt
datetime
No
Expiration timestamp received from Business/Domain MS (ISO 8601)

Response Body — 201 Created
Field
Type
Req?
Description
transactionAuthorizationUuid
uuid
Yes
UUID of the created transaction authorization
status
enum
Yes
Initial status: PENDING

HTTP Status Codes
HTTP Status
Description
201 Created
Transaction authorization successfully initiated
400 Bad Request
Invalid input - missing required fields
409 Conflict
Transaction already has an active authorization
422 Unprocessable Entity
Business rule violation - e.g. invalid transactionId
500 Internal Server Error
Unexpected error

4.2 Submit Authorization Method
POST
/authorizations/{transactionAuthorizationUuid}/attempts

Called by Business/Domain MS to submit the chosen authorization method and start a new attempt. TAM determines the methodFlowType (INLINE or CHALLENGE_RESPONSE). For CHALLENGE_RESPONSE, TAM sends the challenge asynchronously.

Path Parameters
Field
Type
Req?
Description
transactionAuthorizationUuid
uuid
Yes
UUID of the transaction authorization

Request Body
Field
Type
Req?
Description
authorizationMethod
enum
Yes
The chosen authorization method: SMS_OTP, PUSH, PIN, BIOMETRIC, EMAIL_OTP

Response Body — 201 Created
Field
Type
Req?
Description
authorizationAttemptUuid
uuid
Yes
UUID of the created authorization attempt
methodFlowType
enum
Yes
INLINE or CHALLENGE_RESPONSE - determined by TAM based on the method
status
enum
Yes
PENDING (INLINE) or WAITING_FOR_CREDENTIAL (CHALLENGE_RESPONSE)
challengeSentAsync
boolean
No
True only for CHALLENGE_RESPONSE flow - indicates challenge was sent

HTTP Status Codes
HTTP Status
Description
201 Created
Authorization attempt created successfully
400 Bad Request
Invalid or unsupported authorization method
404 Not Found
Transaction authorization not found
409 Conflict
Authorization already finalized - cannot create new attempt
422 Unprocessable Entity
Business rule violation
500 Internal Server Error
Unexpected error

Note: Check with DRAAS what happens when trying the same method multiple times (e.g. PUSH - notification or OBI). Policy on parallel attempts with the same method is TBD.

4.3 Submit Authorization Credential
PATCH
/authorizations/{transactionAuthorizationUuid}/attempts/{authorizationAttemptUuid}/credential

Called by Business/Domain MS to submit the user's credential for a specific attempt. TAM validates the credential and returns AUTHORIZED or FAILED. This is the only endpoint where the payload is directly involved — Business MS never validates credentials directly.

Path Parameters
Field
Type
Req?
Description
transactionAuthorizationUuid
uuid
Yes
UUID of the transaction authorization
authorizationAttemptUuid
uuid
Yes
UUID of the authorization attempt

Request Body
Field
Type
Req?
Description
authorizationCredential
string
Yes
The credential provided by the user. Mandatory for both INLINE and CHALLENGE_RESPONSE flows.

Response Body — 200 OK
Field
Type
Req?
Description
authorizationAttemptUuid
uuid
Yes
UUID of the authorization attempt
transactionAuthorizationUuid
uuid
Yes
UUID of the transaction authorization
status
enum
Yes
AUTHORIZED or FAILED
errorCode
string
No
Present only when status is FAILED. E.g. INVALID_CREDENTIAL, CREDENTIAL_EXPIRED

HTTP Status Codes
HTTP Status
Description
200 OK
Credential processed. Check status field for AUTHORIZED or FAILED result
400 Bad Request
Missing credential
404 Not Found
Authorization or attempt not found
409 Conflict
Attempt already completed
422 Unprocessable Entity
Business rule violation
500 Internal Server Error
Unexpected error

4.4 Additional Endpoints
GET Authorization Status (Simple)
GET
/authorizations/{transactionAuthorizationUuid}/status

Returns the current status of the authorization. Add ?detailed=true to get the full list of attempts.

GET Authorization Status (Detailed) — ?detailed=true
Response includes:
	•	transactionAuthorizationUuid
	•	status
	•	attempts[] — list of all attempts with their UUIDs and statuses

Cancel Authorization
DELETE
/authorizations/{transactionAuthorizationUuid}

Cancels an active authorization. Only possible if not yet in a final state (AUTHORIZED/FAILED).

5. Error Codes Reference
Error Code
HTTP
Description
INVALID_TRANSACTION_INTENT_ID
422
TransactionIntentId not found
TRANSACTION_ALREADY_FINALIZED
422
Transaction is in a final state
TRANSACTION_ALREADY_AUTHORIZED
409
An active authorization already exists
TRANSACTION_AUTHORIZATION_FINALIZED
409
Cannot create attempt - authorization finalized
MISSING_CREDENTIAL
400
Credential is required for INLINE flow
INVALID_CREDENTIAL
200*
Credential is invalid (*returned as 200 with FAILED status)
CREDENTIAL_EXPIRED
200*
Credential has expired (*returned as 200 with FAILED status)
INVALID_METHOD
400
Method not supported or not available
ATTEMPT_ALREADY_COMPLETED
409
Authorization attempt already has a final status
AUTHORIZATION_ALREADY_FINALIZED
409
Cannot cancel a finalized authorization
TRANSACTION_AUTHORIZATION_NOT_FOUND
404
Authorization not found
INTERNAL_SERVER_ERROR
500
Unexpected internal error

6. Failure Scenarios
	•	Invalid transactionIntentId — reject with 422
	•	Transaction already finalized — reject with 422
	•	Missing credential for INLINE flow — reject with 400
	•	Invalid method — reject with 400
	•	Attempt already completed — reject with 409
	•	Authorization already finalized when cancelling — reject with 409

7. Open Questions (TBD)
The following items require alignment with the team before finalizing the API.

	•	SERVICE_AUTHORIZATION_METHOD table — check with Yomy DB vs Config for method storage
	•	PUSH notification — what happens when trying multiple times with same method? Check with DRAAS
	•	Should INLINE flow always split into two steps? Check with Yomy
	•	Authorization method — optional or mandatory on initiation? Check with Yomy
	•	Parallel authorization attempts with the same method — should TAM enforce rejection?
	•	transactionType — strict enumeration or free-form string from Business MS?

```
