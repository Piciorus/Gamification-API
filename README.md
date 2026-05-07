```
1. INITIATE TRANSACTION AUTHORIZATION
POST /authorizations
Request:
json{
  "transactionId": "uuid",
  "transactionType": "string",
  "transactionPayload": "object",
  "authorizationMethod": "SMS_OTP | PUSH | PIN | BIOMETRIC",
  "authorizationCredential": "string (optional)"
}
Response:
json{
  "transactionAuthorizationUuid": "uuid",
  "authorizationAttemptUuid": "uuid"
}

2. SUBMIT AUTHORIZATION CREDENTIAL
PATCH /authorizations/{transaction_authorization_uuid}
Request:
json{
  "attemptId": "uuid",
  "authorizationMethod": "SMS_OTP | PUSH | PIN | BIOMETRIC",
  "authorizationCredential": "string (mandatory)"
}
Response:
json{
  "status": "AUTHORIZED | FAILED"
}

3. GET TRANSACTION AUTHORIZATION STATUS (simple)
GET /authorizations/{authorization_uuid}/status
Response:
json{
  "status": "PENDING | AUTHORIZED | FAILED | WAITING_FOR_CREDENTIAL"
}

4. GET TRANSACTION AUTHORIZATION STATUS (detailed)
GET /authorizations/{authorization_uuid}/status?detailed=true
Response:
json{
  "transactionAuthorizationUuid": "uuid",
  "status": "PENDING | AUTHORIZED | FAILED | WAITING_FOR_CREDENTIAL",
  "attempts": [
    {
      "authorizationAttemptUuid": "uuid",
      "status": "AUTHORIZED | FAILED | PENDING"
    }
  ]
}

5. CANCEL TRANSACTION AUTHORIZATION
DELETE /authorizations/{transaction_authorization_id}
(or POST if preferred)
Response:
json{
  "status": "CANCELLED"
}

Full Flow Summary
Business/Domain MS → TAM:
  1. POST /authorizations          → Initiate
  2. PATCH /authorizations/{uuid}  → Submit credential
  3. GET /authorizations/{uuid}/status → Check status
  4. GET /authorizations/{uuid}/status?detailed=true → Full details

TAM → PVM:
  - STORE payload on initiate
  - RETRIEVE payload when needed

TAM internally:
  - Creates TRANSACTIONS_AUTHORIZATION
  - Creates AUTHORIZATION_ATTEMPTS
  - Updates status on each attempt

Key Notes from diagram

TRANSACTIONS_AUTHORIZATION has 1→N AUTHORIZATION_ATTEMPTS — one transaction can have multiple attempts
PAYLOAD_VAULT has logical relation (no FK) to AUTHORIZATION_ATTEMPTS — stored separately in PVM schema
SERVICE_AUTHORIZATION_METHOD is TBD — needs check with Yomy DB vs Config
For PUSH notifications — check in DRAAS what happens with multiple attempts with same method

```
