```
openapi: 3.0.0
security:
  - BearerAuthentication: [ ]
info:
  title: Cash Transfer Restrictions Inquiry API
  description: >
    REST API to inquire about cash transfer restrictions like online transfer
    limits and reference accounts. If the query parameter crmCustomerNo is
    specified, only the information for that customer is returned; otherwise
    all restrictions accessible in this session are returned.
    If no restrictions are available an empty account list is returned.
    If no single or daily limit is specified those fields are empty.
    If no reference accounts are specified the referenceAccounts list is empty.
  version: 1.0.0

servers:
  - url: ""
    description: Server url + baseUrl

paths:
  /v1/cash-transfer-restrictions:
    get:
      tags:
        - Cash Transfer Restrictions Inquiry
      summary: Inquires cash transfer restrictions
      description: >
        Inquires cash transfer restrictions like online transfer limit and
        reference accounts for the given customer or account.
      operationId: getCashTransferRestrictions
      parameters:
        - $ref: "../authorization/authorization-headers.yaml#/components/parameters/Authorization"
        - $ref: "../authorization/authorization-headers.yaml#/components/parameters/FeId"
        - $ref: "../authorization/authorization-headers.yaml#/components/parameters/Language"
        - $ref: "../authorization/authorization-headers.yaml#/components/parameters/TraceId"
        - $ref: "../common/common-headers.yaml#/components/parameters/userAgent"
        - $ref: "../common/common-headers.yaml#/components/parameters/x-source-service"
        - $ref: "../common/common-headers.yaml#/components/parameters/x-request-id"
        - name: crmCustomerNo
          in: query
          required: false
          description: >
            The CRM customer number of the customer to inquire restrictions for.
            If not specified, all accounts accessible in this session are returned.
          schema:
            type: string
        - name: accountNo
          in: query
          required: false
          description: >
            The account number to inquire restrictions for.
            If not specified, all accounts accessible in this session are returned.
          schema:
            type: string
      responses:
        "200":
          description: Returns the cash transfer restrictions for the given customer or account.
          headers:
            x-correlation-id:
              $ref: "../common/common-headers.yaml#/components/parameters/x-correlation-id"
          content:
            application/json:
              schema:
                $ref: "#/components/schemas/CashTransResInqResponse"
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

    # ── Reference account ─────────────────────────────────────────────────────
    ReferenceAccount:
      type: object
      description: "A predefined reference account for cash transfers."
      properties:
        ibanOpponent:
          $ref: '../common/schemas.yaml#/components/schemas/Iban'
          description: "Account number (IBAN) of the external bank the transaction uses."
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
          description: >
            The day when this reference account was created (no time).
            Note: for older entries this info may not be available.
      required:
        - ibanOpponent
        - bicOpponent
        - accountingOpponent

    # ── Per-account restriction details ───────────────────────────────────────
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
          description: >
            The currently available amount for a single cash transfer.
            NOTE: this amount is only derived from the limit settings of the
            user and has nothing to do with the cash on the user's account.
        currentLimitIsUserLimit:
          type: boolean
          description: "Indicates whether the current limit is a user-defined limit."
        currentLimitType:
          type: string
          description: "The type of the current active limit."
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
          description: "List of predefined reference accounts. Empty if none are specified."
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
          description: "An instant single transfer limit (per transaction) for online cash transfers."
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
