```

2:46 PM
Claude responded: Based on the documentation, let me first explain the concepts simply, then give you the API design.
Based on the documentation, let me first explain the concepts simply, then give you the API design.

Simple Explanation
What is this system? A user wants to authorize a transaction (like a bank payment). The system needs to verify it's really them.

Two flows:

INLINE — credential sent immediately (e.g. PIN/password) → instant AUTHORIZED/FAILED
CHALLENGE_RESPONSE — system sends a challenge first (e.g. SMS code) → user responds later
Key components:

Business/Domain MS — the main microservice that orchestrates everything
TAM (Transaction Authorization Manager) — your microservice, handles authorization logic
PVM (Payload Vault Manager) — stores sensitive payload securely
TAM Schema tables (Image 7):

TRANSACTIONS_AUTHORIZATION — one per transaction
AUTHORIZATION_ATTEMPTS — multiple attempts per transaction
SERVICE_AUTHORIZATION_METHOD — available methods
PVM Schema tables:

PAYLOAD_VAULT — stores encrypted payloads
API Design — Submit Authorization Method
POST /api/v1/authorization/submit-method
Request
json
{
  "transactionalIntentId": "uuid",
  "transactionId": "uuid",
  "method": "SMS_OTP | PUSH | PIN | BIOMETRIC",
  "methodFlowType": "INLINE | CHALLENGE_RESPONSE",
  "credential": "123456",
  "payloadVaultRef": "uuid"
}
Response — INLINE AUTHORIZED
json
{
  "transactionId": "uuid",
  "authorizationAttemptId": "uuid",
  "status": "AUTHORIZED",
  "methodFlowType": "INLINE"
}
Response — INLINE FAILED
json
{
  "transactionId": "uuid",
  "authorizationAttemptId": "uuid",
  "status": "FAILED",
  "methodFlowType": "INLINE",
  "errorCode": "INVALID_CREDENTIAL"
}
Response — CHALLENGE_RESPONSE
json
{
  "transactionId": "uuid",
  "authorizationAttemptId": "uuid",
  "status": "WAITING_FOR_CREDENTIAL",
  "methodFlowType": "CHALLENGE_RESPONSE",
  "challengeSentAsync": true
}
HTTP Status Codes
Status	Scenario
200	AUTHORIZED or FAILED (both valid outcomes)
202	CHALLENGE_RESPONSE initiated async
400	Invalid request / missing credential for INLINE
409	Transaction already finalized
422	Invalid transactionIntentId / Invalid method
TAM Internal Flow (what happens inside TAM)
1. Receive request
2. Validate:
   - transactionIntentId exists
   - transaction not finalized
   - method is valid
   - credential present if INLINE
3. Create AuthorizationAttempt (status = PENDING)
4. Store payload in PVM (payloadVaultRef)
5. if INLINE:
   - Validate credential
   - Update AuthorizationAttempt (AUTHORIZED | FAILED)
   - Return result synchronously
6. if CHALLENGE_RESPONSE:
   - Generate challenge
   - Send challenge to user (ASYNC)
   - Update AuthorizationAttempt (WAITING_FOR_CREDENTIAL)
   - Return 202 immediately
Failure Scenarios & Error Codes
json
{
  "errorCode": "INVALID_TRANSACTION_INTENT_ID",
  "message": "TransactionIntentId not found"
}
{
  "errorCode": "TRANSACTION_ALREADY_FINALIZED", 
  "message": "Transaction is already in final state"
}
{
  "errorCode": "MISSING_CREDENTIAL",
  "message": "Credential is required for INLINE flow"
}
{
  "errorCode": "INVALID_METHOD",
  "message": "Method not supported or not available"
}


```
