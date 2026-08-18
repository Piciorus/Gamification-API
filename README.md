```
openapi: 3.0.0
security:
  - BearerAuthentication: [ ]
info:
  title: Cash Transfer Restrictions Inquiry API
  description: REST API endpoint to inquire about cash transfer restrictions like online transfer limits and reference accounts.
  version: 1.0.0

servers:
  - url: ""
    description: Server url + baseUrl

paths:
  /v1/cash-transfer-restrictions/validate:
    post:
      tags:
        - Cash Transfer Restrictions Inquiry
      summary: Validates the cash transfer restrictions inquiry
      description: Validates the cash transfer restrictions inquiry.
      operationId: validateCashTransResInq
      parameters:
        - $ref: "../authorization/authorization-headers.yaml#/components/parameters/Authorization"
        - $ref: "../authorization/authorization-headers.yaml#/components/parameters/FeId"
        - $ref: "../authorization/authorization-headers.yaml#/components/parameters/Language"
        - $ref: "../authorization/authorization-headers.yaml#/components/parameters/TraceId"
        - $ref: "../common/common-headers.yaml#/components/parameters/userAgent"
        - $ref: "../common/common-headers.yaml#/components/parameters/x-source-service"
        - $ref: "../common/common-headers.yaml#/components/parameters/x-request-id"
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: "#/components/schemas/ValidateCashTransResInqRequest"
      responses:
        '204':
          description: Successful Validation.
          headers:
            x-correlation-id:
              $ref: "../common/common-headers.yaml#/components/parameters/x-correlation-id"
        "401":
          description: Unauthorized
          headers:
            x-correlation-id:
              $ref: "../common/common-headers.yaml#/components/parameters/x-correlation-id"
          content:
            application/json:
              schema:
                $ref: "../common/schemas.yaml#/components/schemas/ApiError"
        "403":
          description: Forbidden
          headers:
            x-correlation-id:
              $ref: "../common/common-headers.yaml#/components/parameters/x-correlation-id"
          content:
            application/json:
              schema:
                $ref: "../common/schemas.yaml#/components/schemas/ApiError"
        "404":
          description: Not found
          headers:
            x-correlation-id:
              $ref: "../common/common-headers.yaml#/components/parameters/x-correlation-id"
          content:
            application/json:
              schema:
                $ref: "../common/schemas.yaml#/components/schemas/ApiError"
        "500":
          description: Internal server error
          headers:
            x-correlation-id:
              $ref: "../common/common-headers.yaml#/components/parameters/x-correlation-id"
          content:
            application/json:
              schema:
                $ref: "../common/schemas.yaml#/components/schemas/ApiError"

  /v1/cash-transfer-restrictions/inquire:
    post:
      tags:
        - Cash Transfer Restrictions Inquiry
      summary: Initiates the cash transfer restrictions inquiry
      description: Initiates the cash transfer restrictions inquiry and returns transfer limits and reference accounts.
      operationId: initiateCashTransResInq
      parameters:
        - $ref: "../authorization/authorization-headers.yaml#/components/parameters/Authorization"
        - $ref: "../authorization/authorization-headers.yaml#/components/parameters/FeId"
        - $ref: "../authorization/authorization-headers.yaml#/components/parameters/Language"
        - $ref: "../authorization/authorization-headers.yaml#/components/parameters/TraceId"
        - $ref: "../common/common-headers.yaml#/components/parameters/userAgent"
        - $ref: "../common/common-headers.yaml#/components/parameters/x-source-service"
        - $ref: "../common/common-headers.yaml#/components/parameters/x-request-id"
        - $ref: "../common/common-headers.yaml#/components/parameters/idempotency-key"
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: "#/components/schemas/InitiateCashTransResInqRequest"
      responses:
        "200":
          description: Returns the cash transfer restrictions for the given account
          headers:
            x-correlation-id:
              $ref: "../common/common-headers.yaml#/components/parameters/x-correlation-id"
          content:
            application/json:
              schema:
                $ref: "../common/schemas.yaml#/components/schemas/CashTransResInqResponse"
        "401":
          description: Unauthorized
          headers:
            x-correlation-id:
              $ref: "../common/common-headers.yaml#/components/parameters/x-correlation-id"
          content:
            application/json:
              schema:
                $ref: "../common/schemas.yaml#/components/schemas/ApiError"
        "403":
          description: Forbidden
          headers:
            x-correlation-id:
              $ref: "../common/common-headers.yaml#/components/parameters/x-correlation-id"
          content:
            application/json:
              schema:
                $ref: "../common/schemas.yaml#/components/schemas/ApiError"
        "404":
          description: Not found
          headers:
            x-correlation-id:
              $ref: "../common/common-headers.yaml#/components/parameters/x-correlation-id"
          content:
            application/json:
              schema:
                $ref: "../common/schemas.yaml#/components/schemas/ApiError"
        "500":
          description: Internal server error
          headers:
            x-correlation-id:
              $ref: "../common/common-headers.yaml#/components/parameters/x-correlation-id"
          content:
            application/json:
              schema:
                $ref: "../common/schemas.yaml#/components/schemas/ApiError"

components:
  securitySchemes:
    BearerAuthentication:
      $ref: '../authorization/authorization-headers.yaml#/components/securitySchemes/BearerAuthentication'

  schemas:
    # ── Validate request ──────────────────────────────────────────────────────
    ValidateCashTransResInqRequest:
      type: object
      properties:
        transactionPayload:
          $ref: "#/components/schemas/CashTransResInqRequest"
      required:
        - transactionPayload

    # ── Initiate request ──────────────────────────────────────────────────────
    InitiateCashTransResInqRequest:
      type: object
      properties:
        transactionIntentId:
          type: string
          format: uuid
          example: "11019087-5800-4000-8000-000000000000"
          description: The transaction intent id
        authorization:
          $ref: "../common/schemas.yaml#/components/schemas/AuthorizationCredentials"
        transactionPayload:
          $ref: "#/components/schemas/CashTransResInqRequest"
      required:
        - transactionIntentId
        - transactionPayload

    # ── Core inquiry request payload ──────────────────────────────────────────
    CashTransResInqRequest:
      type: object
      description: "The cash transfer restrictions inquiry request payload."
      properties:
        crmCustomerNumber:
          type: string
          description: "The CRM customer number of the customer to inquire restrictions for."
        accountNumber:
          $ref: '../common/schemas.yaml#/components/schemas/ClearingAccountNumber'
          description: "The account number to inquire restrictions for. If not specified, all accounts accessible in this session are returned."

    # ── Reference account (nested in response) ────────────────────────────────
    ReferenceAccount:
      type: object
      description: "A predefined reference account for cash transfers."
      properties:
        ibanOpponent:
          $ref: '../common/schemas.yaml#/components/schemas/Iban'
          description: "Account number of the external bank the transaction uses."
        bicOpponent:
          $ref: '../common/schemas.yaml#/components/schemas/Bic'
          description: "BLZ of the external bank."
        bankNameOpponent:
          type: string
          description: "Name of an external bank."
        accountingOpponent:
          $ref: '../common/schemas.yaml#/components/schemas/ReceiverSepa'
          description: "Name of the external receiver."
        addressOpponent:
          type: string
          description: "The address of the opponent."
        countryCodeOpponent:
          $ref: '../common/schemas.yaml#/components/schemas/CountryCode'
          description: "The country code of the opponents address."
        createdAt:
          type: string
          format: date
          description: "The day when this was created (no time). Note: for older entries this info may not be available."
      required:
        - ibanOpponent
        - bicOpponent
        - accountingOpponent

    # ── Per-account restrictions block ────────────────────────────────────────
    CashTransResInqAccount:
      type: object
      description: "Cash transfer restriction details for a single account."
      properties:
        accountno:
          type: string
          description: "Account number."
        accountTypeDescription:
          type: string
          description: "A technical account type."
        dailyTransferLimit:
          type: number
          format: bigdecimal
          minimum: -9999999.99
          maximum: 9999999.99
          description: "A daily limit for online cash transfers."
        usedTransferLimit:
          type: number
          format: bigdecimal
          minimum: -9999999.99
          maximum: 9999999.99
          description: "An amount of the daily cash transfer limit which was already used today."
        availableSingleTransferAmount:
          type: number
          format: bigdecimal
          minimum: -9999999.99
          maximum: 9999999.99
          description: "The currently available amount for a single cash transfer. NOTE: this amount is only derived from the limit settings of the user and has nothing to do with the cash on the user's account."
        currentLimitIsUserLimit:
          type: boolean
          description: "Indicates whether the current limit is a user-defined limit."
        currentLimitType:
          type: string
          description: "The type of the current limit."
          enum:
            - NONE
            - LIMIT_UNSECURE_ACCOUNT
            - GLOBAL_LIMIT
            - SINGLE_LIMIT
            - DAILY_LIMIT
            - DAILY_LIMIT_REDUCED
        referenceAccounts:
          type: array
          items:
            $ref: "#/components/schemas/ReferenceAccount"
          description: "List of predefined reference accounts."
        instantDailyTransferLimit:
          type: number
          format: bigdecimal
          minimum: -9999999.99
          maximum: 9999999.99
          description: "An instant daily limit for online cash transfers."
        instantSingleTransferLimit:
          type: number
          format: bigdecimal
          minimum: -9999999.99
          maximum: 9999999.99
          description: "An instant single transfer limit (pro transaction) for online cash transfers."
        usedInstantTransferLimit:
          type: number
          format: bigdecimal
          minimum: -9999999.99
          maximum: 9999999.99
          description: "An amount of the instant daily cash transfer limit which was already used today."
        availableInstantTransferAmount:
          type: number
          format: bigdecimal
          minimum: -9999999.99
          maximum: 9999999.99
          description: "An available amount of the instant cash transfer."
      required:
        - accountno
        - accountTypeDescription
        - availableSingleTransferAmount
        - currentLimitIsUserLimit

    # ── Top-level response ────────────────────────────────────────────────────
    CashTransResInqResponse:
      type: object
      description: "Response containing cash transfer restriction details per account."
      properties:
        account:
          type: array
          items:
            $ref: "#/components/schemas/CashTransResInqAccount"
          description: "List of account restriction details. Empty if no restrictions or accounts are found."

```
