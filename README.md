```
openapi: 3.0.0
security:
  - BearerAuthentication: [ ]
info:
  title: Cash Transfer Reference Account Add API
  description: >
    REST API endpoint to add a reference account to the internal list of accounts.
    The service is restricted to 10 reference accounts when entered by the client.
  version: 1.0.0

servers:
  - url: ""
    description: Server url + baseUrl

paths:
  /v1/cash-transfer-account-references/add/validate:
    post:
      tags:
        - Cash Transfer Reference Account Add
      summary: Validates the cash transfer reference account add
      description: Validates the cash transfer reference account add.
      operationId: validateAddCashTransferReferenceAccount
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
              $ref: "#/components/schemas/ValidateAddCashTransferReferenceAccountRequest"
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

  /v1/cash-transfer-account-references/add/initiate-transaction:
    post:
      tags:
        - Cash Transfer Reference Account Add
      summary: Initiates the cash transfer reference account add
      description: Initiates the cash transfer reference account add.
      operationId: initiateAddCashTransferReferenceAccount
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
              $ref: "#/components/schemas/InitiateAddCashTransferReferenceAccountRequest"
      responses:
        "200":
          description: Returns status of the cash transfer reference account add
          headers:
            x-correlation-id:
              $ref: "../common/common-headers.yaml#/components/parameters/x-correlation-id"
          content:
            application/json:
              schema:
                $ref: "../common/schemas.yaml#/components/schemas/InitiateTransactionResponse"
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
    ValidateAddCashTransferReferenceAccountRequest:
      type: object
      properties:
        transactionPayload:
          $ref: "#/components/schemas/AddCashTransferReferenceAccountRequest"
      required:
        - transactionPayload

    # ── Initiate request ──────────────────────────────────────────────────────
    InitiateAddCashTransferReferenceAccountRequest:
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
          $ref: "#/components/schemas/AddCashTransferReferenceAccountRequest"
      required:
        - transactionIntentId
        - transactionPayload

    # ── Core payload ──────────────────────────────────────────────────────────
    AddCashTransferReferenceAccountRequest:
      type: object
      description: "The add cash transfer reference account request payload."
      properties:
        crmCustomerNumber:
          type: string
          description: "The CRM customer number of the customer for whom to store this reference account."
        accountno:
          $ref: '../common/schemas.yaml#/components/schemas/ClearingAccountNumber'
          description: "Account number of the source account."
        ibanOpponent:
          $ref: '../common/schemas.yaml#/components/schemas/Iban'
          description: "IBAN of the receiver."
        bicOpponent:
          $ref: '../common/schemas.yaml#/components/schemas/Bic'
          description: "International Bank code of the receiver's bank."
        bankNameOpponent:
          type: string
          description: "Name of the external bank."
        accountingOpponent:
          $ref: '../common/schemas.yaml#/components/schemas/ReceiverSepa'
          description: "Receiver name."
        addressOpponent:
          type: string
          description: "The address of the opponent."
        countryCodeOpponent:
          $ref: '../common/schemas.yaml#/components/schemas/CountryCode'
          description: "The country code of the opponents address."
      required:
        - accountno
        - ibanOpponent
        - accountingOpponent

```


```
package contracts

import org.springframework.cloud.contract.spec.Contract

[Contract.make {
    priority(1)
    description(description: """
        ...
        Represents a successful scenario for validate add cash transfer reference account.

        when:
            api request to validate add cash transfer reference account.
        then:
            return 204 with Successful Validation
        ...
    """)

    request {
        method method: 'POST'
        urlPath($(consumer(regex: '/v1/cash-transfer-account-references/add/validate')), producer(serverValue: '/v1/cash-transfer-account-references/add/validate'))
        headers {
            contentType applicationJson()
            header 'Authorization': value(consumer(regex: '.+')), producer(serverValue: 'aSessionId')
            header 'FeId': value(consumer(regex: '.+')), producer(serverValue: 'WEB')
            header 'Language': value(consumer(regex: '.+')), producer(serverValue: 'DE')
            header 'TraceId': value(consumer(regex: '.+')), producer(serverValue: 'traceId')
            header 'User-Agent': value(consumer(regex: '.*')), producer(serverValue: 'User-Agent')
            header 'x-source-service': value(consumer(regex: '.+')), producer(serverValue: 'xSourceService')
            header 'x-request-id': value(consumer(regex: '.+')), producer(serverValue: '12345678')
        }
        body([
            "transactionPayload": [
                "crmCustomerNumber"  : value(consumer(regex: '.+')), producer(serverValue: '1234567'),
                "accountno"          : value(consumer(regex: '[0-9]{7,10}')), producer(serverValue: '1234567'),
                "ibanOpponent"       : value(consumer(regex: '[A-Z]{2}[0-9]{2}[0-9A-Z]{1,30}')), producer(serverValue: 'DE89370400440532013000'),
                "bicOpponent"        : value(consumer(regex: '[0-9A-Z]{4}[A-Z]{2}[0-9A-Z]{2}$|^[0-9A-Z]{4}[A-Z]{2}[0-9A-Z]{5}')), producer(serverValue: 'COBADEFFXXX'),
                "bankNameOpponent"   : value(consumer(regex: '.+')), producer(serverValue: 'Deutsche Bank AG'),
                "accountingOpponent" : value(consumer(regex: '[a-zA-Z0-9 /\\|\\-?:().,\'\\+]{0,35}')), producer(serverValue: 'Test Receiver'),
                "addressOpponent"    : value(consumer(regex: '.+')), producer(serverValue: 'Test address opponent'),
                "countryCodeOpponent": value(consumer(regex: '[^;#]{2,3}')), producer(serverValue: 'DE')
            ]
        ])
    }

    response {
        status NO_CONTENT()
        headers {
            header 'x-correlation-id': fromRequest().header(key: "x-request-id")
        }
    }
}]

```


```
package contracts

import org.springframework.cloud.contract.spec.Contract

[Contract.make {
    priority(1)
    description(description: """
        ...
        Represents a successful scenario for validate add cash transfer reference account.

        when:
            api request to validate add cash transfer reference account.
        then:
            return 204 with Successful Validation
        ...
    """)

    request {
        method method: 'POST'
        urlPath($(consumer(regex: '/v1/cash-transfer-account-references/add/validate')), producer(serverValue: '/v1/cash-transfer-account-references/add/validate'))
        headers {
            contentType applicationJson()
            header 'Authorization': value(consumer(regex: '.+')), producer(serverValue: 'aSessionId')
            header 'FeId': value(consumer(regex: '.+')), producer(serverValue: 'WEB')
            header 'Language': value(consumer(regex: '.+')), producer(serverValue: 'DE')
            header 'TraceId': value(consumer(regex: '.+')), producer(serverValue: 'traceId')
            header 'User-Agent': value(consumer(regex: '.*')), producer(serverValue: 'User-Agent')
            header 'x-source-service': value(consumer(regex: '.+')), producer(serverValue: 'xSourceService')
            header 'x-request-id': value(consumer(regex: '.+')), producer(serverValue: '12345678')
        }
        body([
            "transactionPayload": [
                "crmCustomerNumber"  : value(consumer(regex: '.+')), producer(serverValue: '1234567'),
                "accountno"          : value(consumer(regex: '[0-9]{7,10}')), producer(serverValue: '1234567'),
                "ibanOpponent"       : value(consumer(regex: '[A-Z]{2}[0-9]{2}[0-9A-Z]{1,30}')), producer(serverValue: 'DE89370400440532013000'),
                "bicOpponent"        : value(consumer(regex: '[0-9A-Z]{4}[A-Z]{2}[0-9A-Z]{2}$|^[0-9A-Z]{4}[A-Z]{2}[0-9A-Z]{5}')), producer(serverValue: 'COBADEFFXXX'),
                "bankNameOpponent"   : value(consumer(regex: '.+')), producer(serverValue: 'Deutsche Bank AG'),
                "accountingOpponent" : value(consumer(regex: '[a-zA-Z0-9 /\\|\\-?:().,\'\\+]{0,35}')), producer(serverValue: 'Test Receiver'),
                "addressOpponent"    : value(consumer(regex: '.+')), producer(serverValue: 'Test address opponent'),
                "countryCodeOpponent": value(consumer(regex: '[^;#]{2,3}')), producer(serverValue: 'DE')
            ]
        ])
    }

    response {
        status NO_CONTENT()
        headers {
            header 'x-correlation-id': fromRequest().header(key: "x-request-id")
        }
    }
}]
```


```
package contracts

import org.springframework.cloud.contract.spec.Contract

[Contract.make {
    priority(10)
    description(description: """
        ...
        Represents an unsuccessful scenario for validate add cash transfer reference account.

        when:
            api request to validate add cash transfer reference account.
        then:
            return 400 with Bad Request
        ...
    """)

    request {
        method method: 'POST'
        urlPath($(consumer(regex: '/v1/cash-transfer-account-references/add/validate')), producer(serverValue: '/v1/cash-transfer-account-references/add/validate'))
        headers {
            contentType applicationJson()
            header 'Authorization': value(consumer(regex: '.+')), producer(serverValue: 'aSessionId')
            header 'FeId': value(consumer(regex: '.+')), producer(serverValue: 'WEB')
            header 'Language': value(consumer(regex: '.+')), producer(serverValue: 'DE')
            header 'TraceId': value(consumer(regex: '.+')), producer(serverValue: 'traceId')
            header 'User-Agent': value(consumer(regex: '.*')), producer(serverValue: 'User-Agent')
            header 'x-request-id': value(consumer(regex: '.+')), producer(serverValue: '12345678')
        }
        body([
            "transactionPayload": [
                "crmCustomerNumber"  : value(consumer(regex: '.+')), producer(serverValue: '1234567'),
                "accountno"          : value(consumer(regex: '[0-9]{7,10}')), producer(serverValue: '1234567'),
                "ibanOpponent"       : value(consumer(regex: '[A-Z]{2}[0-9]{2}[0-9A-Z]{1,30}')), producer(serverValue: 'DE89370400440532013000'),
                "bicOpponent"        : value(consumer(regex: '[0-9A-Z]{4}[A-Z]{2}[0-9A-Z]{2}$|^[0-9A-Z]{4}[A-Z]{2}[0-9A-Z]{5}')), producer(serverValue: 'COBADEFFXXX'),
                "bankNameOpponent"   : value(consumer(regex: '.+')), producer(serverValue: 'Deutsche Bank AG'),
                "accountingOpponent" : value(consumer(regex: '[a-zA-Z0-9 /\\|\\-?:().,\'\\+]{0,35}')), producer(serverValue: 'Test Receiver'),
                "addressOpponent"    : value(consumer(regex: '.+')), producer(serverValue: 'Test address opponent'),
                "countryCodeOpponent": value(consumer(regex: '[^;#]{2,3}')), producer(serverValue: 'DE')
            ]
        ])
    }

    response {
        status BAD_REQUEST()
        headers {
            contentType applicationJson()
            header 'x-correlation-id': fromRequest().header(key: "x-request-id")
        }
        body([
            "status"  : "400",
            "traceId" : "",
            "code"    : "",
            "title"   : "Header X-source-service ist erforderlich",
            "detail"  : "Required request header 'x-source-service' for method parameter type String is not present",
            "errors"  : []
        ])
    }
}]

```


```
package contracts

import org.springframework.cloud.contract.spec.Contract

[Contract.make {
    priority(1)
    description(description: """
        ...
        Represents a successful scenario for initiate add cash transfer reference account transaction.

        when:
            api request to initiate add cash transfer reference account transaction.
        then:
            return 200 with successful transaction
        ...
    """)

    request {
        method method: 'POST'
        urlPath($(consumer(regex: '/v1/cash-transfer-account-references/add/initiate-transaction')), producer(serverValue: '/v1/cash-transfer-account-references/add/initiate-transaction'))
        headers {
            contentType applicationJson()
            header 'Authorization': value(consumer(regex: '.+')), producer(serverValue: 'aSessionId')
            header 'FeId': value(consumer(regex: '.+')), producer(serverValue: 'WEB')
            header 'Language': value(consumer(regex: '.+')), producer(serverValue: 'DE')
            header 'TraceId': value(consumer(regex: '.+')), producer(serverValue: 'traceId')
            header 'User-Agent': value(consumer(regex: '.*')), producer(serverValue: 'User-Agent')
            header 'x-source-service': value(consumer(regex: '.+')), producer(serverValue: 'xSourceService')
            header 'x-request-id': value(consumer(regex: '.+')), producer(serverValue: '12345678')
            header 'idempotency-key': value(consumer(regex: '.+')), producer(serverValue: 'b0589506-65eb-4fb9-9747-f023b5101b5a')
        }
        body([
            "transactionIntentId": value(consumer(regex: '.+')), producer(serverValue: '11111111'),
            "authorization"      : [
                "tan"             : value(consumer(regex: '.+')), producer(serverValue: '123456'),
                "tanAuthType"     : value(consumer(regex: '.+')), producer(serverValue: 'SMS_TAN')
            ],
            "transactionPayload" : [
                "crmCustomerNumber"  : value(consumer(regex: '.+')), producer(serverValue: '1234567'),
                "accountno"          : value(consumer(regex: '[0-9]{7,10}')), producer(serverValue: '1234567'),
                "ibanOpponent"       : value(consumer(regex: '[A-Z]{2}[0-9]{2}[0-9A-Z]{1,30}')), producer(serverValue: 'DE89370400440532013000'),
                "bicOpponent"        : value(consumer(regex: '[0-9A-Z]{4}[A-Z]{2}[0-9A-Z]{2}$|^[0-9A-Z]{4}[A-Z]{2}[0-9A-Z]{5}')), producer(serverValue: 'COBADEFFXXX'),
                "bankNameOpponent"   : value(consumer(regex: '.+')), producer(serverValue: 'Deutsche Bank AG'),
                "accountingOpponent" : value(consumer(regex: '[a-zA-Z0-9 /\\|\\-?:().,\'\\+]{0,35}')), producer(serverValue: 'Test Receiver'),
                "addressOpponent"    : value(consumer(regex: '.+')), producer(serverValue: 'Test address opponent'),
                "countryCodeOpponent": value(consumer(regex: '[^;#]{2,3}')), producer(serverValue: 'DE')
            ]
        ])
    }

    response {
        status OK()
        headers {
            contentType applicationJson()
            header 'x-correlation-id': fromRequest().header(key: "x-request-id")
        }
        body([
            "transactionId": "b0589506-65eb-4fb9-9747-f023b5101b5a",
            "status"       : "TX_EXEC_SUCCESS"
        ])
    }
}]

```


```
package contracts

import org.springframework.cloud.contract.spec.Contract

[Contract.make {
    priority(2)
    description(description: """
        ...
        Represents a successful scenario for validate add cash transfer reference account with only required parameters.

        when:
            api request to validate add cash transfer reference account with only required parameters.
        then:
            return 204 with Successful Validation
        ...
    """)

    request {
        method method: 'POST'
        urlPath($(consumer(regex: '/v1/cash-transfer-account-references/add/validate')), producer(serverValue: '/v1/cash-transfer-account-references/add/validate'))
        headers {
            contentType applicationJson()
            header 'Authorization': value(consumer(regex: '.+')), producer(serverValue: 'aSessionId')
            header 'FeId': value(consumer(regex: '.+')), producer(serverValue: 'WEB')
            header 'Language': value(consumer(regex: '.+')), producer(serverValue: 'DE')
            header 'TraceId': value(consumer(regex: '.+')), producer(serverValue: 'traceId')
            header 'User-Agent': value(consumer(regex: '.*')), producer(serverValue: 'User-Agent')
            header 'x-source-service': value(consumer(regex: '.+')), producer(serverValue: 'xSourceService')
            header 'x-request-id': value(consumer(regex: '.+')), producer(serverValue: '12345678')
        }
        body([
            "transactionPayload": [
                "accountno"         : value(consumer(regex: '[0-9]{7,10}')), producer(serverValue: '1234567'),
                "ibanOpponent"      : value(consumer(regex: '[A-Z]{2}[0-9]{2}[0-9A-Z]{1,30}')), producer(serverValue: 'DE89370400440532013000'),
                "accountingOpponent": value(consumer(regex: '[a-zA-Z0-9 /\\|\\-?:().,\'\\+]{0,35}')), producer(serverValue: 'Test Receiver')
            ]
        ])
    }

    response {
        status NO_CONTENT()
        headers {
            header 'x-correlation-id': fromRequest().header(key: "x-request-id")
        }
    }
}]

```


```
package contracts

import org.springframework.cloud.contract.spec.Contract

[Contract.make {
    priority(2)
    description(description: """
        ...
        Represents a successful scenario for initiate add cash transfer reference account transaction with only required parameters.

        when:
            api request to initiate add cash transfer reference account transaction with only required parameters.
        then:
            return 200 with successful transaction
        ...
    """)

    request {
        method method: 'POST'
        urlPath($(consumer(regex: '/v1/cash-transfer-account-references/add/initiate-transaction')), producer(serverValue: '/v1/cash-transfer-account-references/add/initiate-transaction'))
        headers {
            contentType applicationJson()
            header 'Authorization': value(consumer(regex: '.+')), producer(serverValue: 'aSessionId')
            header 'FeId': value(consumer(regex: '.+')), producer(serverValue: 'WEB')
            header 'Language': value(consumer(regex: '.+')), producer(serverValue: 'DE')
            header 'TraceId': value(consumer(regex: '.+')), producer(serverValue: 'traceId')
            header 'User-Agent': value(consumer(regex: '.*')), producer(serverValue: 'User-Agent')
            header 'x-source-service': value(consumer(regex: '.+')), producer(serverValue: 'xSourceService')
            header 'x-request-id': value(consumer(regex: '.+')), producer(serverValue: '12345678')
            header 'idempotency-key': value(consumer(regex: '.+')), producer(serverValue: 'b0589506-65eb-4fb9-9747-f023b5101b5a')
        }
        body([
            "transactionIntentId": value(consumer(regex: '.+')), producer(serverValue: '11111111'),
            "authorization"      : [
                "tan"        : value(consumer(regex: '.+')), producer(serverValue: '123456'),
                "tanAuthType": value(consumer(regex: '.+')), producer(serverValue: 'SMS_TAN')
            ],
            "transactionPayload" : [
                "accountno"         : value(consumer(regex: '[0-9]{7,10}')), producer(serverValue: '1234567'),
                "ibanOpponent"      : value(consumer(regex: '[A-Z]{2}[0-9]{2}[0-9A-Z]{1,30}')), producer(serverValue: 'DE89370400440532013000'),
                "accountingOpponent": value(consumer(regex: '[a-zA-Z0-9 /\\|\\-?:().,\'\\+]{0,35}')), producer(serverValue: 'Test Receiver')
            ]
        ])
    }

    response {
        status OK()
        headers {
            contentType applicationJson()
            header 'x-correlation-id': fromRequest().header(key: "x-request-id")
        }
        body([
            "transactionId": "b0589506-65eb-4fb9-9747-f023b5101b5a",
            "status"       : "TX_EXEC_SUCCESS"
        ])
    }
}]

```



```
package contracts

import org.springframework.cloud.contract.spec.Contract

[Contract.make {
    priority(10)
    description(description: """
        ...
        Represents an unsuccessful scenario for initiate add cash transfer reference account transaction.

        when:
            api request to initiate add cash transfer reference account transaction.
        then:
            return 400 with Bad Request
        ...
    """)

    request {
        method method: 'POST'
        urlPath($(consumer(regex: '/v1/cash-transfer-account-references/add/initiate-transaction')), producer(serverValue: '/v1/cash-transfer-account-references/add/initiate-transaction'))
        headers {
            contentType applicationJson()
            header 'Authorization': value(consumer(regex: '.+')), producer(serverValue: 'aSessionId')
            header 'FeId': value(consumer(regex: '.+')), producer(serverValue: 'WEB')
            header 'Language': value(consumer(regex: '.+')), producer(serverValue: 'DE')
            header 'TraceId': value(consumer(regex: '.+')), producer(serverValue: 'traceId')
            header 'User-Agent': value(consumer(regex: '.*')), producer(serverValue: 'User-Agent')
            header 'x-request-id': value(consumer(regex: '.+')), producer(serverValue: '12345678')
            header 'idempotency-key': value(consumer(regex: '.+')), producer(serverValue: 'b0589506-65eb-4fb9-9747-f023b5101b5a')
        }
        body([
            "transactionIntentId": value(consumer(regex: '.+')), producer(serverValue: '11111111'),
            "authorization"      : [
                "tan"        : value(consumer(regex: '.+')), producer(serverValue: '123456'),
                "tanAuthType": value(consumer(regex: '.+')), producer(serverValue: 'SMS_TAN')
            ],
            "transactionPayload" : [
                "accountno"          : value(consumer(regex: '[0-9]{7,10}')), producer(serverValue: '1234567'),
                "ibanOpponent"       : value(consumer(regex: '[A-Z]{2}[0-9]{2}[0-9A-Z]{1,30}')), producer(serverValue: 'DE89370400440532013000'),
                "bicOpponent"        : value(consumer(regex: '[0-9A-Z]{4}[A-Z]{2}[0-9A-Z]{2}$|^[0-9A-Z]{4}[A-Z]{2}[0-9A-Z]{5}')), producer(serverValue: 'COBADEFFXXX'),
                "bankNameOpponent"   : value(consumer(regex: '.+')), producer(serverValue: 'Deutsche Bank AG'),
                "accountingOpponent" : value(consumer(regex: '[a-zA-Z0-9 /\\|\\-?:().,\'\\+]{0,35}')), producer(serverValue: 'Test Receiver'),
                "addressOpponent"    : value(consumer(regex: '.+')), producer(serverValue: 'Test address opponent'),
                "countryCodeOpponent": value(consumer(regex: '[^;#]{2,3}')), producer(serverValue: 'DE')
            ]
        ])
    }

    response {
        status BAD_REQUEST()
        headers {
            contentType applicationJson()
            header 'x-correlation-id': fromRequest().header(key: "x-request-id")
        }
        body([
            "status"  : "400",
            "traceId" : "",
            "code"    : "",
            "title"   : "Header X-source-service ist erforderlich",
            "detail"  : "Required request header 'x-source-service' for method parameter type String is not present",
            "errors"  : []
        ])
    }
}]

```
