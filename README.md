// Constants to add at top
private static final String UNBOUND_SIGNATURE = "unbound-sig-xyz";
private static final ECPrivateKey EC_PRIVATE_KEY_LV9 = mock(ECPrivateKey.class);

@Test
void givenValidContext_whenGenerateUnboundSignature_thenReturnExpectedSignature() {
    // given
    when(deviceMapperMock.buildCustomerAuthInfo(
            CUSTOMER_ID, SESSION_ID, DEVICE_AUTH_TOKEN_JWT, APP_INSTANCE_ID))
        .thenReturn(CUSTOMER_AUTH_INFO);
    when(virtualDeviceDataServiceMock.getVirtualDeviceWithUserByAppInstanceId(APP_INSTANCE_ID))
        .thenReturn(VIRTUAL_DEVICE);
    when(encryptionKeysServiceMock.extractClientPrivateKeyLvl9ForDecryption(VIRTUAL_DEVICE))
        .thenReturn(EC_PRIVATE_KEY_LV9);
    when(encryptionServiceMock.generateUnboundSignature(
            CUSTOMER_AUTH_INFO.getAppInstanceId(),
            CUSTOMER_AUTH_INFO.getCustomerId(),
            VIRTUAL_DEVICE.getUserDataEntity().getAppPin(),
            EC_PRIVATE_KEY_LV9))
        .thenReturn(UNBOUND_SIGNATURE);

    // when
    final String result = underTest.generateUnboundSignature();

    // then
    assertThat(result).isEqualTo(UNBOUND_SIGNATURE);
}

@Test
void givenVirtualDeviceServiceThrowsException_whenGenerateUnboundSignature_thenThrowException() {
    // given
    when(deviceMapperMock.buildCustomerAuthInfo(
            CUSTOMER_ID, SESSION_ID, DEVICE_AUTH_TOKEN_JWT, APP_INSTANCE_ID))
        .thenReturn(CUSTOMER_AUTH_INFO);
    when(virtualDeviceDataServiceMock.getVirtualDeviceWithUserByAppInstanceId(APP_INSTANCE_ID))
        .thenThrow(new RuntimeException(ERROR_MESSAGE));

    // when
    final RuntimeException exception = assertThrows(RuntimeException.class,
        () -> underTest.generateUnboundSignature());

    // then
    assertThat(exception.getMessage()).isEqualTo(ERROR_MESSAGE);
    verifyNoInteractions(encryptionServiceMock);
}

@Test
void givenEncryptionServiceThrowsException_whenGenerateUnboundSignature_thenThrowException() {
    // given
    when(deviceMapperMock.buildCustomerAuthInfo(
            CUSTOMER_ID, SESSION_ID, DEVICE_AUTH_TOKEN_JWT, APP_INSTANCE_ID))
        .thenReturn(CUSTOMER_AUTH_INFO);
    when(virtualDeviceDataServiceMock.getVirtualDeviceWithUserByAppInstanceId(APP_INSTANCE_ID))
        .thenReturn(VIRTUAL_DEVICE);
    when(encryptionKeysServiceMock.extractClientPrivateKeyLvl9ForDecryption(VIRTUAL_DEVICE))
        .thenReturn(EC_PRIVATE_KEY_LV9);
    when(encryptionServiceMock.generateUnboundSignature(any(), any(), any(), any()))
        .thenThrow(new RuntimeException(ERROR_MESSAGE));

    // when
    final RuntimeException exception = assertThrows(RuntimeException.class,
        () -> underTest.generateUnboundSignature());

    // then
    assertThat(exception.getMessage()).isEqualTo(ERROR_MESSAGE);
}

@Test
void givenBuildCustomerAuthInfoThrowsException_whenGenerateUnboundSignature_thenThrowException() {
    // given
    when(deviceMapperMock.buildCustomerAuthInfo(
            CUSTOMER_ID, SESSION_ID, DEVICE_AUTH_TOKEN_JWT, APP_INSTANCE_ID))
        .thenThrow(new RuntimeException(ERROR_MESSAGE));

    // when
    final RuntimeException exception = assertThrows(RuntimeException.class,
        () -> underTest.generateUnboundSignature());

    // then
    assertThat(exception.getMessage()).isEqualTo(ERROR_MESSAGE);
    verifyNoInteractions(virtualDeviceDataServiceMock, encryptionServiceMock);
}



@Named("parseBirthDateToXmlGregorianCalendar")
default XMLGregorianCalendar parseBirthDateToXmlGregorianCalendar(String birthDate) {
    if (birthDate == null) return null;
    try {
        // Handle both "...Z", "...+00:00", and "...00" (no timezone)
        OffsetDateTime odt;
        try {
            odt = OffsetDateTime.parse(birthDate);
        } catch (DateTimeParseException e) {
            // No timezone info — assume UTC
            LocalDateTime ldt = LocalDateTime.parse(birthDate);
            odt = ldt.atOffset(ZoneOffset.UTC);
        }
        
        GregorianCalendar gc = GregorianCalendar.from(
            odt.atZoneSameInstant(ZoneOffset.UTC)
        );
        return DatatypeFactory.newInstance()
                              .newXMLGregorianCalendar(gc);
    } catch (DatatypeConfigurationException e) {
        throw new CommonException(
            CustpmExceptionCode.CREATE_LEADS_INVALID_DATE_TIME
        );
    }
}




@Named("parseBirthDateToXmlGregorianCalendar")
default XMLGregorianCalendar parseBirthDateToXmlGregorianCalendar(String birthDate) {
    if (birthDate == null) return null;
    try {
        OffsetDateTime odt = OffsetDateTime.parse(birthDate);
        GregorianCalendar gc = GregorianCalendar.from(
            odt.atZoneSameInstant(ZoneOffset.UTC)
        );
        return DatatypeFactory.newInstance()
                              .newXMLGregorianCalendar(gc);
    } catch (DatatypeConfigurationException e) {
        throw new CommonException(CustpmExceptionCode.CREATE_LEADS_INVALID_DATE_TIME);
    }
}
parseBirthDateToXmlGregorianCalendar




XMLGregorianCalendar offsetDateTimeToXmlGregorianCalendar(OffsetDateTime offsetDateTime) {
    if (offsetDateTime == null) {
        return null;
    }

    var gregorianCalendar = GregorianCalendar.from(
        offsetDateTime.atZoneSameInstant(ZoneOffset.UTC)
    );

    try {
        XMLGregorianCalendar xmlCal = DatatypeFactory.newInstance()
            .newXMLGregorianCalendar(gregorianCalendar);

        xmlCal.setTimezone(DatatypeConstants.FIELD_UNDEFINED); // ← key line

        return xmlCal;
    } catch (DatatypeConfigurationException e) {
        throw new CommonException(CustpmExceptionCode.CREATE_LEADS_INVALID_DATE_TIME);
    }
}
```
openapi: 3.0.0
security:
  - BearerAuthentication: [ ]
info:
  title: Cash Transfer Reference Account Delete API
  description: >
    REST API endpoint to delete a reference account from the internal list of accounts.
  version: 1.0.0

servers:
  - url: ""
    description: Server url + baseUrl

paths:
  /v1/cash-transfer-account-references/delete/validate:
    post:
      tags:
        - Cash Transfer Reference Account Delete
      summary: Validates the cash transfer reference account delete
      description: Validates the cash transfer reference account delete.
      operationId: validateDeleteCashTransferReferenceAccount
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
              $ref: "#/components/schemas/ValidateDeleteCashTransferReferenceAccountRequest"
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

  /v1/cash-transfer-account-references/delete/initiate-transaction:
    post:
      tags:
        - Cash Transfer Reference Account Delete
      summary: Initiates the cash transfer reference account delete
      description: Initiates the cash transfer reference account delete.
      operationId: initiateDeleteCashTransferReferenceAccount
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
              $ref: "#/components/schemas/InitiateDeleteCashTransferReferenceAccountRequest"
      responses:
        "200":
          description: Returns status of the cash transfer reference account delete
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
    ValidateDeleteCashTransferReferenceAccountRequest:
      type: object
      properties:
        transactionPayload:
          $ref: "#/components/schemas/DeleteCashTransferReferenceAccountRequest"
      required:
        - transactionPayload

    # ── Initiate request ──────────────────────────────────────────────────────
    InitiateDeleteCashTransferReferenceAccountRequest:
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
          $ref: "#/components/schemas/DeleteCashTransferReferenceAccountRequest"
      required:
        - transactionIntentId
        - transactionPayload

    # ── Core payload ──────────────────────────────────────────────────────────
    DeleteCashTransferReferenceAccountRequest:
      type: object
      description: "The delete cash transfer reference account request payload."
      properties:
        crmCustomerNumber:
          type: string
          description: "The CRM customer number of the customer for whom to delete this reference account."
        accountno:
          $ref: '../common/schemas.yaml#/components/schemas/ClearingAccountNumber'
          description: "Account number of the source account."
        ibanOpponent:
          $ref: '../common/schemas.yaml#/components/schemas/Iban'
          description: "IBAN of the reference account to delete."
        bicOpponent:
          $ref: '../common/schemas.yaml#/components/schemas/Bic'
          description: "International Bank code of the receiver's bank."
        bankNameOpponent:
          type: string
          description: "Name of the external bank."
        accountingOpponent:
          $ref: '../common/schemas.yaml#/components/schemas/ReceiverSepa'
          description: "Receiver name."
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
    description("""
        Represents a successful scenario for validate delete cash transfer reference account.

        when:
            api request to validate delete cash transfer reference account.
        then:
            return 204 with Successful Validation
    """)

    request {
        method 'POST'
        urlPath($(consumer('/v1/cash-transfer-account-references/delete/validate'), producer('/v1/cash-transfer-account-references/delete/validate')))
        headers {
            contentType applicationJson()
            header 'Authorization': value(consumer(regex('.+')), producer('aSessionId'))
            header 'FeId': value(consumer(regex('.+')), producer('WEB'))
            header 'Language': value(consumer(regex('.+')), producer('DE'))
            header 'TraceId': value(consumer(regex('.+')), producer('traceId'))
            header 'User-Agent': value(consumer(regex('.*')), producer('User-Agent'))
            header 'x-source-service': value(consumer(regex('.+')), producer('xSourceService'))
            header 'x-request-id': value(consumer(regex('.+')), producer('12345678'))
        }
        body([
            "transactionPayload": [
                "crmCustomerNumber"  : value(consumer(regex('.+')), producer('1234567')),
                "accountno"          : value(consumer(regex('[0-9]{7,10}')), producer('1234567')),
                "ibanOpponent"       : value(consumer(regex('[A-Z]{2}[0-9]{2}[0-9A-Z]{1,30}')), producer('DE89370400440532013000')),
                "bicOpponent"        : value(consumer(regex('[0-9A-Z]{4}[A-Z]{2}[0-9A-Z]{2}$|^[0-9A-Z]{4}[A-Z]{2}[0-9A-Z]{5}')), producer('COBADEFFXXX')),
                "bankNameOpponent"   : value(consumer(regex('.+')), producer('Deutsche Bank AG')),
                "accountingOpponent" : value(consumer(regex('[a-zA-Z0-9 /\\|\\-?:().,\'\\+]{0,35}')), producer('Test Receiver'))
            ]
        ])
    }

    response {
        status NO_CONTENT()
        headers {
            header 'x-correlation-id': fromRequest().header("x-request-id")
        }
    }
}]

```


```
package contracts

import org.springframework.cloud.contract.spec.Contract

[Contract.make {
    priority(2)
    description("""
        Represents a successful scenario for validate delete cash transfer reference account with only required parameters.

        when:
            api request to validate delete cash transfer reference account with only required parameters.
        then:
            return 204 with Successful Validation
    """)

    request {
        method 'POST'
        urlPath($(consumer('/v1/cash-transfer-account-references/delete/validate'), producer('/v1/cash-transfer-account-references/delete/validate')))
        headers {
            contentType applicationJson()
            header 'Authorization': value(consumer(regex('.+')), producer('aSessionId'))
            header 'FeId': value(consumer(regex('.+')), producer('WEB'))
            header 'Language': value(consumer(regex('.+')), producer('DE'))
            header 'TraceId': value(consumer(regex('.+')), producer('traceId'))
            header 'User-Agent': value(consumer(regex('.*')), producer('User-Agent'))
            header 'x-source-service': value(consumer(regex('.+')), producer('xSourceService'))
            header 'x-request-id': value(consumer(regex('.+')), producer('12345678'))
        }
        body([
            "transactionPayload": [
                "accountno"         : value(consumer(regex('[0-9]{7,10}')), producer('1234567')),
                "ibanOpponent"      : value(consumer(regex('[A-Z]{2}[0-9]{2}[0-9A-Z]{1,30}')), producer('DE89370400440532013000')),
                "accountingOpponent": value(consumer(regex('[a-zA-Z0-9 /\\|\\-?:().,\'\\+]{0,35}')), producer('Test Receiver'))
            ]
        ])
    }

    response {
        status NO_CONTENT()
        headers {
            header 'x-correlation-id': fromRequest().header("x-request-id")
        }
    }
}]

```



```

package contracts

import org.springframework.cloud.contract.spec.Contract

[Contract.make {
    priority(10)
    description("""
        Represents an unsuccessful scenario for validate delete cash transfer reference account.

        when:
            api request to validate delete cash transfer reference account.
        then:
            return 400 with Bad Request
    """)

    request {
        method 'POST'
        urlPath($(consumer('/v1/cash-transfer-account-references/delete/validate'), producer('/v1/cash-transfer-account-references/delete/validate')))
        headers {
            contentType applicationJson()
            header 'Authorization': value(consumer(regex('.+')), producer('aSessionId'))
            header 'FeId': value(consumer(regex('.+')), producer('WEB'))
            header 'Language': value(consumer(regex('.+')), producer('DE'))
            header 'TraceId': value(consumer(regex('.+')), producer('traceId'))
            header 'User-Agent': value(consumer(regex('.*')), producer('User-Agent'))
            header 'x-request-id': value(consumer(regex('.+')), producer('12345678'))
        }
        body([
            "transactionPayload": [
                "crmCustomerNumber"  : value(consumer(regex('.+')), producer('1234567')),
                "accountno"          : value(consumer(regex('[0-9]{7,10}')), producer('1234567')),
                "ibanOpponent"       : value(consumer(regex('[A-Z]{2}[0-9]{2}[0-9A-Z]{1,30}')), producer('DE89370400440532013000')),
                "bicOpponent"        : value(consumer(regex('[0-9A-Z]{4}[A-Z]{2}[0-9A-Z]{2}$|^[0-9A-Z]{4}[A-Z]{2}[0-9A-Z]{5}')), producer('COBADEFFXXX')),
                "bankNameOpponent"   : value(consumer(regex('.+')), producer('Deutsche Bank AG')),
                "accountingOpponent" : value(consumer(regex('[a-zA-Z0-9 /\\|\\-?:().,\'\\+]{0,35}')), producer('Test Receiver'))
            ]
        ])
    }

    response {
        status BAD_REQUEST()
        headers {
            contentType applicationJson()
            header 'x-correlation-id': fromRequest().header("x-request-id")
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
    description("""
        Represents a successful scenario for initiate delete cash transfer reference account transaction.

        when:
            api request to initiate delete cash transfer reference account transaction.
        then:
            return 200 with successful transaction
    """)

    request {
        method 'POST'
        urlPath($(consumer('/v1/cash-transfer-account-references/delete/initiate-transaction'), producer('/v1/cash-transfer-account-references/delete/initiate-transaction')))
        headers {
            contentType applicationJson()
            header 'Authorization': value(consumer(regex('.+')), producer('aSessionId'))
            header 'FeId': value(consumer(regex('.+')), producer('WEB'))
            header 'Language': value(consumer(regex('.+')), producer('DE'))
            header 'TraceId': value(consumer(regex('.+')), producer('traceId'))
            header 'User-Agent': value(consumer(regex('.*')), producer('User-Agent'))
            header 'x-source-service': value(consumer(regex('.+')), producer('xSourceService'))
            header 'x-request-id': value(consumer(regex('.+')), producer('12345678'))
            header 'idempotency-key': value(consumer(regex('.+')), producer('b0589506-65eb-4fb9-9747-f023b5101b5a'))
        }
        body([
            "transactionIntentId": value(consumer(regex('.+')), producer('11111111')),
            "authorization"      : [
                "tan"        : value(consumer(regex('.+')), producer('123456')),
                "tanAuthType": value(consumer(regex('.+')), producer('SMS_TAN'))
            ],
            "transactionPayload" : [
                "crmCustomerNumber"  : value(consumer(regex('.+')), producer('1234567')),
                "accountno"          : value(consumer(regex('[0-9]{7,10}')), producer('1234567')),
                "ibanOpponent"       : value(consumer(regex('[A-Z]{2}[0-9]{2}[0-9A-Z]{1,30}')), producer('DE89370400440532013000')),
                "bicOpponent"        : value(consumer(regex('[0-9A-Z]{4}[A-Z]{2}[0-9A-Z]{2}$|^[0-9A-Z]{4}[A-Z]{2}[0-9A-Z]{5}')), producer('COBADEFFXXX')),
                "bankNameOpponent"   : value(consumer(regex('.+')), producer('Deutsche Bank AG')),
                "accountingOpponent" : value(consumer(regex('[a-zA-Z0-9 /\\|\\-?:().,\'\\+]{0,35}')), producer('Test Receiver'))
            ]
        ])
    }

    response {
        status OK()
        headers {
            contentType applicationJson()
            header 'x-correlation-id': fromRequest().header("x-request-id")
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
    description("""
        Represents a successful scenario for initiate delete cash transfer reference account transaction with only required parameters.

        when:
            api request to initiate delete cash transfer reference account transaction with only required parameters.
        then:
            return 200 with successful transaction
    """)

    request {
        method 'POST'
        urlPath($(consumer('/v1/cash-transfer-account-references/delete/initiate-transaction'), producer('/v1/cash-transfer-account-references/delete/initiate-transaction')))
        headers {
            contentType applicationJson()
            header 'Authorization': value(consumer(regex('.+')), producer('aSessionId'))
            header 'FeId': value(consumer(regex('.+')), producer('WEB'))
            header 'Language': value(consumer(regex('.+')), producer('DE'))
            header 'TraceId': value(consumer(regex('.+')), producer('traceId'))
            header 'User-Agent': value(consumer(regex('.*')), producer('User-Agent'))
            header 'x-source-service': value(consumer(regex('.+')), producer('xSourceService'))
            header 'x-request-id': value(consumer(regex('.+')), producer('12345678'))
            header 'idempotency-key': value(consumer(regex('.+')), producer('b0589506-65eb-4fb9-9747-f023b5101b5a'))
        }
        body([
            "transactionIntentId": value(consumer(regex('.+')), producer('11111111')),
            "authorization"      : [
                "tan"        : value(consumer(regex('.+')), producer('123456')),
                "tanAuthType": value(consumer(regex('.+')), producer('SMS_TAN'))
            ],
            "transactionPayload" : [
                "accountno"         : value(consumer(regex('[0-9]{7,10}')), producer('1234567')),
                "ibanOpponent"      : value(consumer(regex('[A-Z]{2}[0-9]{2}[0-9A-Z]{1,30}')), producer('DE89370400440532013000')),
                "accountingOpponent": value(consumer(regex('[a-zA-Z0-9 /\\|\\-?:().,\'\\+]{0,35}')), producer('Test Receiver'))
            ]
        ])
    }

    response {
        status OK()
        headers {
            contentType applicationJson()
            header 'x-correlation-id': fromRequest().header("x-request-id")
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
    description("""
        Represents an unsuccessful scenario for initiate delete cash transfer reference account transaction.

        when:
            api request to initiate delete cash transfer reference account transaction.
        then:
            return 400 with Bad Request
    """)

    request {
        method 'POST'
        urlPath($(consumer('/v1/cash-transfer-account-references/delete/initiate-transaction'), producer('/v1/cash-transfer-account-references/delete/initiate-transaction')))
        headers {
            contentType applicationJson()
            header 'Authorization': value(consumer(regex('.+')), producer('aSessionId'))
            header 'FeId': value(consumer(regex('.+')), producer('WEB'))
            header 'Language': value(consumer(regex('.+')), producer('DE'))
            header 'TraceId': value(consumer(regex('.+')), producer('traceId'))
            header 'User-Agent': value(consumer(regex('.*')), producer('User-Agent'))
            header 'x-request-id': value(consumer(regex('.+')), producer('12345678'))
            header 'idempotency-key': value(consumer(regex('.+')), producer('b0589506-65eb-4fb9-9747-f023b5101b5a'))
        }
        body([
            "transactionIntentId": value(consumer(regex('.+')), producer('11111111')),
            "authorization"      : [
                "tan"        : value(consumer(regex('.+')), producer('123456')),
                "tanAuthType": value(consumer(regex('.+')), producer('SMS_TAN'))
            ],
            "transactionPayload" : [
                "crmCustomerNumber"  : value(consumer(regex('.+')), producer('1234567')),
                "accountno"          : value(consumer(regex('[0-9]{7,10}')), producer('1234567')),
                "ibanOpponent"       : value(consumer(regex('[A-Z]{2}[0-9]{2}[0-9A-Z]{1,30}')), producer('DE89370400440532013000')),
                "bicOpponent"        : value(consumer(regex('[0-9A-Z]{4}[A-Z]{2}[0-9A-Z]{2}$|^[0-9A-Z]{4}[A-Z]{2}[0-9A-Z]{5}')), producer('COBADEFFXXX')),
                "bankNameOpponent"   : value(consumer(regex('.+')), producer('Deutsche Bank AG')),
                "accountingOpponent" : value(consumer(regex('[a-zA-Z0-9 /\\|\\-?:().,\'\\+]{0,35}')), producer('Test Receiver'))
            ]
        ])
    }

    response {
        status BAD_REQUEST()
        headers {
            contentType applicationJson()
            header 'x-correlation-id': fromRequest().header("x-request-id")
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

package de.consorsbank.banking.payments.rest.adapter.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import de.consorsbank.banking.payments.rest.adapter.controller.model.DeleteCashTransferReferenceAccountRequest;
import de.consorsbank.banking.payments.rest.adapter.controller.service.DeleteCashTransferReferenceAccountService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(controllers = DeleteCashTransferReferenceAccountController.class)
@Import({SecurityConfiguration.class})
@ActiveProfiles("test")
class DeleteCashTransferReferenceAccountControllerTest extends ControllerUnitTestConfig {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DeleteCashTransferReferenceAccountService deleteService;

    @Test
    void should_ReturnNoContent_When_ValidateDeleteReferenceAccountWithAllParameters() throws Exception {
        // when
        mockMvc.perform(
                MockMvcRequestBuilders.post("/v1/cash-transfer-account-references/delete/validate")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .headers(TestUtils.getAllHttpHeadersWithoutOwner("delete-cash-transfer-reference-account"))
                        .content(CashTransferReferenceAccountUtils.buildValidateDeleteRequestAsJson()))

                // then
                .andExpect(status().isNoContent());
    }

    @Test
    void should_ReturnNoContent_When_ValidateDeleteReferenceAccountWithRequiredParametersOnly() throws Exception {
        // when
        mockMvc.perform(
                MockMvcRequestBuilders.post("/v1/cash-transfer-account-references/delete/validate")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .headers(TestUtils.getAllHttpHeadersWithoutOwner("delete-cash-transfer-reference-account"))
                        .content(CashTransferReferenceAccountUtils.buildValidateDeleteRequestRequiredParamsAsJson()))

                // then
                .andExpect(status().isNoContent());
    }

    @Test
    void should_ReturnBadRequest_When_ValidateDeleteReferenceAccountRequiredHeadersNotPassed() throws Exception {
        // when
        mockMvc.perform(
                MockMvcRequestBuilders.post("/v1/cash-transfer-account-references/delete/validate")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .headers(TestUtils.getMissingHttpHeadersWithoutOwnerAndAuthorization())
                        .content(CashTransferReferenceAccountUtils.buildValidateDeleteRequestAsJson()))

                // then
                .andExpect(status().isBadRequest());
    }

    @Test
    void should_ReturnOk_When_InitiateDeleteReferenceAccountWithAllParameters() throws Exception {
        // given
        when(deleteService.initiateDeleteCashTransferReferenceAccount(any()))
                .thenReturn(CashTransferReferenceAccountUtils.buildInitiateTransactionResponse());

        // when
        mockMvc.perform(
                MockMvcRequestBuilders.post("/v1/cash-transfer-account-references/delete/initiate-transaction")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .headers(TestUtils.getAllHttpHeadersWithoutOwner("delete-cash-transfer-reference-account"))
                        .content(CashTransferReferenceAccountUtils.buildInitiateDeleteRequestAsJson()))

                // then
                .andExpect(status().isOk());
    }

    @Test
    void should_ReturnOk_When_InitiateDeleteReferenceAccountWithRequiredParametersOnly() throws Exception {
        // given
        when(deleteService.initiateDeleteCashTransferReferenceAccount(any()))
                .thenReturn(CashTransferReferenceAccountUtils.buildInitiateTransactionResponse());

        // when
        mockMvc.perform(
                MockMvcRequestBuilders.post("/v1/cash-transfer-account-references/delete/initiate-transaction")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .headers(TestUtils.getAllHttpHeadersWithoutOwner("delete-cash-transfer-reference-account"))
                        .content(CashTransferReferenceAccountUtils.buildInitiateDeleteRequestRequiredParamsAsJson()))

                // then
                .andExpect(status().isOk());
    }

    @Test
    void should_ReturnBadRequest_When_InitiateDeleteReferenceAccountRequiredHeadersNotPassed() throws Exception {
        // when
        mockMvc.perform(
                MockMvcRequestBuilders.post("/v1/cash-transfer-account-references/delete/initiate-transaction")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .headers(TestUtils.getMissingHttpHeadersWithoutOwnerAndAuthorization())
                        .content(CashTransferReferenceAccountUtils.buildInitiateDeleteRequestAsJson()))

                // then
                .andExpect(status().isBadRequest());
    }
}

```


```
package de.consorsbank.banking.payments.rest.adapter.controller;

import de.consorsbank.banking.payments.rest.adapter.controller.model.DeleteCashTransferReferenceAccountRequest;
import de.consorsbank.banking.payments.rest.adapter.controller.model.InitiateDeleteCashTransferReferenceAccountRequest;
import de.consorsbank.banking.payments.rest.adapter.controller.model.InitiateTransactionResponse;
import de.consorsbank.banking.payments.rest.adapter.controller.model.ValidateDeleteCashTransferReferenceAccountRequest;

public class CashTransferReferenceAccountUtils {

    public static ValidateDeleteCashTransferReferenceAccountRequest buildValidateDeleteRequest() {
        return ValidateDeleteCashTransferReferenceAccountRequest.builder()
                .transactionPayload(buildDeletePayloadAllParams())
                .build();
    }

    public static ValidateDeleteCashTransferReferenceAccountRequest buildValidateDeleteRequestRequiredParams() {
        return ValidateDeleteCashTransferReferenceAccountRequest.builder()
                .transactionPayload(buildDeletePayloadRequiredParams())
                .build();
    }

    public static InitiateDeleteCashTransferReferenceAccountRequest buildInitiateDeleteRequest() {
        return InitiateDeleteCashTransferReferenceAccountRequest.builder()
                .transactionIntentId("11019087-5800-4000-8000-000000000000")
                .transactionPayload(buildDeletePayloadAllParams())
                .build();
    }

    public static InitiateDeleteCashTransferReferenceAccountRequest buildInitiateDeleteRequestRequiredParams() {
        return InitiateDeleteCashTransferReferenceAccountRequest.builder()
                .transactionIntentId("11019087-5800-4000-8000-000000000000")
                .transactionPayload(buildDeletePayloadRequiredParams())
                .build();
    }

    public static InitiateTransactionResponse buildInitiateTransactionResponse() {
        return InitiateTransactionResponse.builder()
                .transactionId("b0589506-65eb-4fb9-9747-f023b5101b5a")
                .status("TX_EXEC_SUCCESS")
                .build();
    }

    private static DeleteCashTransferReferenceAccountRequest buildDeletePayloadAllParams() {
        return DeleteCashTransferReferenceAccountRequest.builder()
                .crmCustomerNumber("1234567")
                .accountno("1234567")
                .ibanOpponent("DE89370400440532013000")
                .bicOpponent("COBADEFFXXX")
                .bankNameOpponent("Deutsche Bank AG")
                .accountingOpponent("Test Receiver")
                .build();
    }

    private static DeleteCashTransferReferenceAccountRequest buildDeletePayloadRequiredParams() {
        return DeleteCashTransferReferenceAccountRequest.builder()
                .accountno("1234567")
                .ibanOpponent("DE89370400440532013000")
                .accountingOpponent("Test Receiver")
                .build();
    }
}

```


```



```
