```

openapi: 3.0.3
info:
  title: FTG Change API
  description: API for setting or removing the FTG from the current customer
  version: 1.0.0

paths:
  /v1/ftg-change/validate:
    post:
      summary: Validate FTG change
      description: |
        Validates the FTG change for a customer.
        Returns 204 No Content on successful validation.
      operationId: validateFtgChange
      tags:
        - FTG Change
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
              $ref: '#/components/schemas/ValidateFtgChangeRequest'
      responses:
        '204':
          description: Validation successful
          headers:
            x-correlation-id:
              description: Correlation ID echoed from x-request-id
              schema:
                type: string
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
          description: Not found
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

  /v1/ftg-change/initiate-transaction:
    post:
      summary: Initiate FTG change transaction
      description: |
        Initiates the FTG change transaction for a customer.
        Returns 200 OK with the transaction data.
      operationId: initiateFtgChange
      tags:
        - FTG Change
      parameters:
        - $ref: '../authorization/authorization-headers.yaml#/components/parameters/Authorization'
        - $ref: '../authorization/authorization-headers.yaml#/components/parameters/FeId'
        - $ref: '../authorization/authorization-headers.yaml#/components/parameters/Language'
        - $ref: '../authorization/authorization-headers.yaml#/components/parameters/TraceId'
        - $ref: '../common/common-headers.yaml#/components/parameters/userAgent'
        - $ref: '../common/common-headers.yaml#/components/parameters/x-source-service'
        - $ref: '../common/common-headers.yaml#/components/parameters/x-request-id'
        - $ref: '../common/common-headers.yaml#/components/parameters/idempotency-key'
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/InitiateFtgChangeRequest'
      responses:
        '200':
          description: FTG change transaction initiated successfully
          headers:
            x-correlation-id:
              description: Correlation ID echoed from x-request-id
              schema:
                type: string
          content:
            application/json:
              schema:
                $ref: '../common/schemas.yaml#/components/schemas/InitiateTransactionResponse'
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
          description: Not found
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

components:
  schemas:
    FtgChangeTransactionRequest:
      type: object
      required:
        - action
      properties:
        action:
          type: string
          description: FTG action to perform
          pattern: "^FTG_ADD$|^FTG_REMOVE$"
        crmCustomerNumber:
          type: string
          description: CRM customer number of the user for whom the action will be executed. Required only if the user acts as surrogate.

    ValidateFtgChangeRequest:
      type: object
      required:
        - transactionPayload
      properties:
        transactionPayload:
          $ref: '#/components/schemas/FtgChangeTransactionRequest'

    InitiateFtgChangeRequest:
      type: object
      properties:
        transactionIntentId:
          type: string
          format: uuid
          description: The transaction intent id
        authorization:
          $ref: "../common/schemas.yaml#/components/schemas/AuthorizationCredentials"
        transactionPayload:
          $ref: '#/components/schemas/FtgChangeTransactionRequest'
      required:
        - transactionIntentId
        - transactionPayload
```
```
package contracts

import org.springframework.cloud.contract.spec.Contract

[
    Contract.make {
        priority(priority: 1)
        description(description: """
            Represents a successful scenario for validate FTG change.
            when:
                api request to validate FTG change with all parameters.
            then:
                return 204 with successful validation
        """)
        request {
            method POST()
            urlPath($(consumer(regex(regex: '/v1/ftg-change/validate')), producer(serverValue: '/v1/ftg-change/validate')))
            headers {
                contentType applicationJson()
                header 'Authorization': value(consumer(regex(regex: '.+')), producer(serverValue: 'aSessionId'))
                header 'FeId': value(consumer(regex(regex: '.+')), producer(serverValue: 'WEB'))
                header 'Language': value(consumer(regex(regex: '.+')), producer(serverValue: 'DE'))
                header 'TraceId': value(consumer(regex(regex: '.+')), producer(serverValue: 'traceId'))
                header 'User-Agent': value(consumer(regex(regex: '.*')), producer(serverValue: 'User-Agent'))
                header 'x-source-service': value(consumer(regex(regex: '.+')), producer(serverValue: 'xSourceService'))
                header 'x-request-id': value(consumer(regex(regex: '.+')), producer(serverValue: 'b0589506-65eb-4fb9-9747-f023b5example'))
            }
            body([
                "transactionPayload": [
                    "action"            : value(consumer(regex(regex: '^FTG_ADD$|^FTG_REMOVE$')), producer(serverValue: 'FTG_ADD')),
                    "crmCustomerNumber" : value(consumer(regex(regex: '.+')), producer(serverValue: '1234567890'))
                ]
            ])
        }
        response {
            status NO_CONTENT()
            headers {
                header 'x-correlation-id': fromRequest().header(key: 'x-request-id')
            }
        }
    }
]

```
```
package contracts

import org.springframework.cloud.contract.spec.Contract

[
    Contract.make {
        priority(priority: 5)
        description(description: """
            Represents a successful scenario for validate FTG change with only required parameters.
            when:
                api request to validate FTG change with required parameters only.
            then:
                return 204 with successful validation
        """)
        request {
            method POST()
            urlPath($(consumer(regex(regex: '/v1/ftg-change/validate')), producer(serverValue: '/v1/ftg-change/validate')))
            headers {
                contentType applicationJson()
                header 'Authorization': value(consumer(regex(regex: '.+')), producer(serverValue: 'aSessionId'))
                header 'FeId': value(consumer(regex(regex: '.+')), producer(serverValue: 'WEB'))
                header 'Language': value(consumer(regex(regex: '.+')), producer(serverValue: 'DE'))
                header 'TraceId': value(consumer(regex(regex: '.+')), producer(serverValue: 'traceId'))
                header 'User-Agent': value(consumer(regex(regex: '.*')), producer(serverValue: 'User-Agent'))
                header 'x-source-service': value(consumer(regex(regex: '.+')), producer(serverValue: 'xSourceService'))
                header 'x-request-id': value(consumer(regex(regex: '.+')), producer(serverValue: '123456'))
            }
            body([
                "transactionPayload": [
                    "action": value(consumer(regex(regex: '^FTG_ADD$|^FTG_REMOVE$')), producer(serverValue: 'FTG_REMOVE'))
                ]
            ])
        }
        response {
            status NO_CONTENT()
            headers {
                header 'x-correlation-id': fromRequest().header(key: 'x-request-id')
            }
        }
    }
]

```
```
package contracts

import org.springframework.cloud.contract.spec.Contract

[
    Contract.make {
        priority(priority: 2)
        description(description: """
            Represents a bad request scenario for validate FTG change.
            when:
                api request to validate FTG change with missing required headers.
            then:
                return 400 bad request
        """)
        request {
            method POST()
            urlPath($(consumer(regex(regex: '/v1/ftg-change/validate')), producer(serverValue: '/v1/ftg-change/validate')))
            headers {
                contentType applicationJson()
                header 'Authorization': value(consumer(regex(regex: '.+')), producer(serverValue: 'aSessionId'))
                header 'FeId': value(consumer(regex(regex: '.+')), producer(serverValue: 'WEB'))
                header 'Language': value(consumer(regex(regex: '.+')), producer(serverValue: 'DE'))
                header 'TraceId': value(consumer(regex(regex: '.+')), producer(serverValue: 'traceId'))
                header 'User-Agent': value(consumer(regex(regex: '.*')), producer(serverValue: 'User-Agent'))
                header 'x-request-id': value(consumer(regex(regex: '.+')), producer(serverValue: '123456'))
            }
            body([
                "transactionPayload": [
                    "action": value(consumer(regex(regex: '^FTG_ADD$|^FTG_REMOVE$')), producer(serverValue: 'FTG_ADD'))
                ]
            ])
        }
        response {
            status BAD_REQUEST()
            headers {
                contentType applicationJson()
                header 'x-correlation-id': fromRequest().header(key: 'x-request-id')
            }
        }
    }
]

```
```
package contracts

import org.springframework.cloud.contract.spec.Contract

[
    Contract.make {
        priority(priority: 1)
        description(description: """
            Represents a successful scenario for initiate FTG change transaction with all parameters.
            when:
                api request to initiate FTG change transaction.
            then:
                return 200 with successful transaction
        """)
        request {
            method POST()
            urlPath($(consumer(regex(regex: '/v1/ftg-change/initiate-transaction')), producer(serverValue: '/v1/ftg-change/initiate-transaction')))
            headers {
                contentType applicationJson()
                header 'Authorization': value(consumer(regex(regex: '.+')), producer(serverValue: 'aSessionId'))
                header 'FeId': value(consumer(regex(regex: '.+')), producer(serverValue: 'WEB'))
                header 'Language': value(consumer(regex(regex: '.+')), producer(serverValue: 'DE'))
                header 'TraceId': value(consumer(regex(regex: '.+')), producer(serverValue: 'traceId'))
                header 'User-Agent': value(consumer(regex(regex: '.*')), producer(serverValue: 'User-Agent'))
                header 'x-source-service': value(consumer(regex(regex: '.+')), producer(serverValue: 'xSourceService'))
                header 'x-request-id': value(consumer(regex(regex: '.+')), producer(serverValue: '123456'))
                header 'idempotency-key': value(consumer(regex(regex: '.+')), producer(serverValue: '12345678'))
            }
            body([
                "transactionIntentId": value(consumer(regex(regex: '.+')), producer(serverValue: 'deb81870-08d8-46bb-8b19-3ee77e422860')),
                "transactionPayload" : [
                    "action"            : value(consumer(regex(regex: '^FTG_ADD$|^FTG_REMOVE$')), producer(serverValue: 'FTG_ADD')),
                    "crmCustomerNumber" : value(consumer(regex(regex: '.+')), producer(serverValue: '1234567890'))
                ]
            ])
        }
        response {
            status OK()
            headers {
                contentType applicationJson()
                header 'x-correlation-id': fromRequest().header(key: 'x-request-id')
            }
            body([
                "transactionId": value(producer(regex(regex: '.+')), consumer(serverValue: 'b0589506-65eb-4fb9-9747-f023b5101b5a')),
                "status"        : value(producer(regex(regex: '.+')), consumer(serverValue: 'TX_AUTH_PENDING'))
            ])
        }
    }
]

```
```

package contracts

import org.springframework.cloud.contract.spec.Contract

[
    Contract.make {
        priority(priority: 5)
        description(description: """
            Represents a successful scenario for initiate FTG change transaction with required parameters only.
            when:
                api request to initiate FTG change transaction with required parameters only.
            then:
                return 200 with successful transaction
        """)
        request {
            method POST()
            urlPath($(consumer(regex(regex: '/v1/ftg-change/initiate-transaction')), producer(serverValue: '/v1/ftg-change/initiate-transaction')))
            headers {
                contentType applicationJson()
                header 'Authorization': value(consumer(regex(regex: '.+')), producer(serverValue: 'aSessionId'))
                header 'FeId': value(consumer(regex(regex: '.+')), producer(serverValue: 'WEB'))
                header 'Language': value(consumer(regex(regex: '.+')), producer(serverValue: 'DE'))
                header 'TraceId': value(consumer(regex(regex: '.+')), producer(serverValue: 'traceId'))
                header 'User-Agent': value(consumer(regex(regex: '.*')), producer(serverValue: 'User-Agent'))
                header 'x-source-service': value(consumer(regex(regex: '.+')), producer(serverValue: 'xSourceService'))
                header 'x-request-id': value(consumer(regex(regex: '.+')), producer(serverValue: '123456'))
                header 'idempotency-key': value(consumer(regex(regex: '.+')), producer(serverValue: '12345678'))
            }
            body([
                "transactionIntentId": value(consumer(regex(regex: '.+')), producer(serverValue: 'deb81870-08d8-46bb-8b19-3ee77e422860')),
                "transactionPayload" : [
                    "action": value(consumer(regex(regex: '^FTG_ADD$|^FTG_REMOVE$')), producer(serverValue: 'FTG_REMOVE'))
                ]
            ])
        }
        response {
            status OK()
            headers {
                contentType applicationJson()
                header 'x-correlation-id': fromRequest().header(key: 'x-request-id')
            }
            body([
                "transactionId": value(producer(regex(regex: '.+')), consumer(serverValue: 'b0589506-65eb-4fb9-9747-f023b5101b5a')),
                "status"        : value(producer(regex(regex: '.+')), consumer(serverValue: 'TX_AUTH_PENDING'))
            ])
        }
    }
]
```
```
package contracts

import org.springframework.cloud.contract.spec.Contract

[
    Contract.make {
        priority(priority: 2)
        description(description: """
            Represents a bad request scenario for initiate FTG change transaction.
            when:
                api request to initiate FTG change transaction with missing required headers.
            then:
                return 400 bad request
        """)
        request {
            method POST()
            urlPath($(consumer(regex(regex: '/v1/ftg-change/initiate-transaction')), producer(serverValue: '/v1/ftg-change/initiate-transaction')))
            headers {
                contentType applicationJson()
                header 'Authorization': value(consumer(regex(regex: '.+')), producer(serverValue: 'aSessionId'))
                header 'FeId': value(consumer(regex(regex: '.+')), producer(serverValue: 'WEB'))
                header 'Language': value(consumer(regex(regex: '.+')), producer(serverValue: 'DE'))
                header 'TraceId': value(consumer(regex(regex: '.+')), producer(serverValue: 'traceId'))
                header 'User-Agent': value(consumer(regex(regex: '.*')), producer(serverValue: 'User-Agent'))
                header 'x-request-id': value(consumer(regex(regex: '.+')), producer(serverValue: '123456'))
                header 'idempotency-key': value(consumer(regex(regex: '.+')), producer(serverValue: '12345678'))
            }
            body([
                "transactionIntentId": value(consumer(regex(regex: '.+')), producer(serverValue: 'deb81870-08d8-46bb-8b19-3ee77e422860')),
                "transactionPayload" : [
                    "action": value(consumer(regex(regex: '^FTG_ADD$|^FTG_REMOVE$')), producer(serverValue: 'FTG_ADD'))
                ]
            ])
        }
        response {
            status BAD_REQUEST()
            headers {
                contentType applicationJson()
                header 'x-correlation-id': fromRequest().header(key: 'x-request-id')
            }
        }
    }
]

```
```
package de.consorsbank.banking.payments.rest.adapter.controller.ftgchange.model;

public class FtgChangeUtils {

    private FtgChangeUtils() {}

    public static ValidateFtgChangeRequest buildValidateRequest() {
        var payload = new FtgChangeTransactionRequest();
        payload.setAction("FTG_ADD");
        payload.setCrmCustomerNumber("1234567890");

        var request = new ValidateFtgChangeRequest();
        request.setTransactionPayload(payload);
        return request;
    }

    public static ValidateFtgChangeRequest buildValidateRequestRequiredOnly() {
        var payload = new FtgChangeTransactionRequest();
        payload.setAction("FTG_REMOVE");

        var request = new ValidateFtgChangeRequest();
        request.setTransactionPayload(payload);
        return request;
    }

    public static InitiateFtgChangeRequest buildInitiateRequest() {
        var payload = new FtgChangeTransactionRequest();
        payload.setAction("FTG_ADD");
        payload.setCrmCustomerNumber("1234567890");

        var request = new InitiateFtgChangeRequest();
        request.setTransactionIntentId("deb81870-08d8-46bb-8b19-3ee77e422860");
        request.setTransactionPayload(payload);
        return request;
    }

    public static InitiateFtgChangeRequest buildInitiateRequestRequiredOnly() {
        var payload = new FtgChangeTransactionRequest();
        payload.setAction("FTG_REMOVE");

        var request = new InitiateFtgChangeRequest();
        request.setTransactionIntentId("deb81870-08d8-46bb-8b19-3ee77e422860");
        request.setTransactionPayload(payload);
        return request;
    }

    public static String buildValidateRequestAsJson() {
        return """
                {
                    "transactionPayload": {
                        "action": "FTG_ADD",
                        "crmCustomerNumber": "1234567890"
                    }
                }
                """;
    }

    public static String buildValidateRequestRequiredOnlyAsJson() {
        return """
                {
                    "transactionPayload": {
                        "action": "FTG_REMOVE"
                    }
                }
                """;
    }

    public static String buildInitiateRequestAsJson() {
        return """
                {
                    "transactionIntentId": "deb81870-08d8-46bb-8b19-3ee77e422860",
                    "transactionPayload": {
                        "action": "FTG_ADD",
                        "crmCustomerNumber": "1234567890"
                    }
                }
                """;
    }

    public static String buildInitiateRequestRequiredOnlyAsJson() {
        return """
                {
                    "transactionIntentId": "deb81870-08d8-46bb-8b19-3ee77e422860",
                    "transactionPayload": {
                        "action": "FTG_REMOVE"
                    }
                }
                """;
    }
}

```
```
package de.consorsbank.banking.payments.rest.adapter.controller.ftgchange;

import de.consorsbank.banking.payments.rest.adapter.controller.ftgchange.model.InitiateFtgChangeRequest;
import de.consorsbank.banking.payments.rest.adapter.controller.ftgchange.model.ValidateFtgChangeRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class FtgChangeController implements FtgChangeApi {

    private final FtgChangeService ftgChangeService;

    @Override
    public ResponseEntity<Void> validateFtgChange(
            String authorization,
            String feId,
            String language,
            String traceId,
            String userAgent,
            String xSourceService,
            String xRequestId,
            ValidateFtgChangeRequest validateFtgChangeRequest) {

        ftgChangeService.validateFtgChange(validateFtgChangeRequest);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<InitiateTransactionResponse> initiateFtgChange(
            String authorization,
            String feId,
            String language,
            String traceId,
            String userAgent,
            String xSourceService,
            String xRequestId,
            String idempotencyKey,
            InitiateFtgChangeRequest initiateFtgChangeRequest) {

        return ResponseEntity.ok()
                .body(ftgChangeService.initiateFtgChange(initiateFtgChangeRequest));
    }
}

```
```

package de.consorsbank.banking.payments.rest.adapter.controller.ftgchange;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static com.consorsbank.custpm.utils.FtgChangeUtils.buildInitiateFtgChangeRequest;
import static com.consorsbank.custpm.utils.FtgChangeUtils.buildInitiateFtgChangeRequestRequiredOnly;
import static com.consorsbank.custpm.utils.FtgChangeUtils.buildValidateFtgChangeRequest;
import static com.consorsbank.custpm.utils.FtgChangeUtils.buildValidateFtgChangeRequestRequiredOnly;

import com.fasterxml.jackson.databind.json.JsonMapper;
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
@WebMvcTest(controllers = FtgChangeController.class)
@Import({SecurityConfiguration.class})
@ActiveProfiles("test")
class FtgChangeControllerTest extends ControllerUnitTestConfig {

    private static final String VALIDATE_URL = "/v1/ftg-change/validate";
    private static final String INITIATE_URL = "/v1/ftg-change/initiate-transaction";
    private static final JsonMapper mapper = JsonMapper.builder().build();

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FtgChangeApi ftgChangeApi;

    @Test
    void should_ReturnNoContent_When_ValidatingFtgChangeWithAllParameters() throws Exception {
        // given
        var payLoad = mapper.writeValueAsString(buildValidateFtgChangeRequest());

        // when
        mockMvc.perform(
                MockMvcRequestBuilders.post(VALIDATE_URL)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .headers(TestUtils.getAllHttpHeaders("ftg-change-validate"))
                        .content(payLoad))

                // then
                .andExpect(status().isNoContent());
    }

    @Test
    void should_ReturnNoContent_When_ValidatingFtgChangeWithRequiredParametersOnly() throws Exception {
        // given
        var payLoad = mapper.writeValueAsString(buildValidateFtgChangeRequestRequiredOnly());

        // when
        mockMvc.perform(
                MockMvcRequestBuilders.post(VALIDATE_URL)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .headers(TestUtils.getAllHttpHeaders("ftg-change-validate"))
                        .content(payLoad))

                // then
                .andExpect(status().isNoContent());
    }

    @Test
    void should_ReturnBadRequest_When_RequiredHeadersAreMissingOnValidate() throws Exception {
        // given
        var payLoad = mapper.writeValueAsString(buildValidateFtgChangeRequest());

        // when
        mockMvc.perform(
                MockMvcRequestBuilders.post(VALIDATE_URL)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .headers(TestUtils.getMissingHttpHeaders())
                        .content(payLoad))

                // then
                .andExpect(status().isBadRequest());
    }

    @Test
    void should_ReturnOk_When_InitiatingFtgChangeTransactionWithAllParameters() throws Exception {
        // given
        var payLoad = mapper.writeValueAsString(buildInitiateFtgChangeRequest());

        // when
        mockMvc.perform(
                MockMvcRequestBuilders.post(INITIATE_URL)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .headers(TestUtils.getAllHttpHeaders("ftg-change-initiate"))
                        .content(payLoad))

                // then
                .andExpect(status().isOk());
    }

    @Test
    void should_ReturnOk_When_InitiatingFtgChangeTransactionWithRequiredParametersOnly() throws Exception {
        // given
        var payLoad = mapper.writeValueAsString(buildInitiateFtgChangeRequestRequiredOnly());

        // when
        mockMvc.perform(
                MockMvcRequestBuilders.post(INITIATE_URL)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .headers(TestUtils.getAllHttpHeaders("ftg-change-initiate"))
                        .content(payLoad))

                // then
                .andExpect(status().isOk());
    }

    @Test
    void should_ReturnBadRequest_When_RequiredHeadersAreMissingOnInitiateTransaction() throws Exception {
        // given
        var payLoad = mapper.writeValueAsString(buildInitiateFtgChangeRequest());

        // when
        mockMvc.perform(
                MockMvcRequestBuilders.post(INITIATE_URL)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .headers(TestUtils.getMissingHttpHeaders())
                        .content(payLoad))

                // then
                .andExpect(status().isBadRequest());
    }
}
```
```


```
```


```
