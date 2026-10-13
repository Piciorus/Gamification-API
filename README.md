```
openapi: 3.0.3
info:
  title: Risk Strategy Update API
  description: API for updating the risk strategy of a managed account
  version: 1.0.0

paths:
  /v1/risk-strategy/validate:
    post:
      summary: Validate risk strategy update
      description: |
        Validates the risk strategy update for a managed account.
        Returns 204 No Content on successful validation.
      operationId: validateRiskStrategyUpdate
      tags:
        - Risk Strategy Update
      parameters:
        - $ref: '../authorization/authorization-headers.yaml#/components/parameters/Authorization'
        - $ref: '../authorization/authorization-headers.yaml#/components/parameters/FeId'
        - $ref: '../authorization/authorization-headers.yaml#/components/parameters/Language'
        - $ref: '../authorization/authorization-headers.yaml#/components/parameters/TraceId'
        - $ref: '../authorization/authorization-headers.yaml#/components/parameters/UserAgent'
        - $ref: '../common/common-headers.yaml#/components/parameters/XSourceService'
        - $ref: '../common/common-headers.yaml#/components/parameters/XRequestId'
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/ValidateRiskStrategyUpdateRequest'
            example:
              transactionPayload:
                accountno: "1234567"
                portfolioId: "P-987654"
                portfolioDescription: "Conservative Portfolio"
                suggestedRiskStrategy: "LOW"
                selectedRiskStrategy: "MEDIUM"
                investmentDuration: "5Y"
                investmentAmount: 50000
      responses:
        '204':
          description: Validation successful
          headers:
            x-correlation-id:
              description: Correlation ID echoed from x-request-id
              schema:
                type: string
        '400':
          description: Bad Request - missing or invalid required fields
          headers:
            x-correlation-id:
              description: Correlation ID echoed from x-request-id
              schema:
                type: string
          content:
            application/json:
              schema:
                $ref: '../common/schemas.yaml#/components/schemas/ErrorResponse'

  /v1/risk-strategy/initiate-transaction:
    post:
      summary: Initiate risk strategy update transaction
      description: |
        Initiates the risk strategy update transaction for a managed account.
        Returns 200 OK with the updated risk strategy data.
      operationId: initiateRiskStrategyUpdate
      tags:
        - Risk Strategy Update
      parameters:
        - $ref: '../authorization/authorization-headers.yaml#/components/parameters/Authorization'
        - $ref: '../authorization/authorization-headers.yaml#/components/parameters/FeId'
        - $ref: '../authorization/authorization-headers.yaml#/components/parameters/Language'
        - $ref: '../authorization/authorization-headers.yaml#/components/parameters/TraceId'
        - $ref: '../authorization/authorization-headers.yaml#/components/parameters/UserAgent'
        - $ref: '../common/common-headers.yaml#/components/parameters/XSourceService'
        - $ref: '../common/common-headers.yaml#/components/parameters/XRequestId'
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/InitiateRiskStrategyUpdateRequest'
            example:
              validateOnly: false
              tan: "123456"
              txId: "TX-abc123"
              authorizationType: "TAN"
              serviceOperationMode: "REAL"
              tanAuthType: "SMS_TAN"
              transactionPayload:
                accountno: "1234567"
                portfolioId: "P-987654"
                portfolioDescription: "Conservative Portfolio"
                suggestedRiskStrategy: "LOW"
                selectedRiskStrategy: "MEDIUM"
                investmentDuration: "5Y"
                investmentAmount: 50000
      responses:
        '200':
          description: Risk strategy update transaction initiated successfully
          headers:
            x-correlation-id:
              description: Correlation ID echoed from x-request-id
              schema:
                type: string
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/RiskStrategyUpdateResponse'
              example:
                portfolioId: "P-987654"
                portfolioDescription: "Conservative Portfolio"
                suggestedRiskStrategy: "LOW"
                selectedRiskStrategy: "MEDIUM"
                investmentDuration: "5Y"
                investmentAmount: 50000
        '400':
          description: Bad Request - missing or invalid required fields
          headers:
            x-correlation-id:
              description: Correlation ID echoed from x-request-id
              schema:
                type: string
          content:
            application/json:
              schema:
                $ref: '../common/schemas.yaml#/components/schemas/ErrorResponse'

components:
  schemas:
    RiskStrategyUpdateTransactionPayload:
      type: object
      required:
        - portfolioId
        - portfolioDescription
      properties:
        accountno:
          type: string
          description: Account number of the managed account
          example: "1234567"
        portfolioId:
          type: string
          description: Portfolio identifier
          example: "P-987654"
        portfolioDescription:
          type: string
          description: Portfolio description
          example: "Conservative Portfolio"
        suggestedRiskStrategy:
          type: string
          description: Suggested risk strategy
          example: "LOW"
        selectedRiskStrategy:
          type: string
          description: Selected risk strategy chosen by the customer
          example: "MEDIUM"
        investmentDuration:
          type: string
          description: Investment duration
          example: "5Y"
        investmentAmount:
          type: integer
          description: Investment amount
          example: 50000

    ValidateRiskStrategyUpdateRequest:
      type: object
      required:
        - transactionPayload
      properties:
        transactionPayload:
          $ref: '#/components/schemas/RiskStrategyUpdateTransactionPayload'

    InitiateRiskStrategyUpdateRequest:
      type: object
      required:
        - transactionPayload
      properties:
        validateOnly:
          type: string
          description: Flag indicating validate-only mode
          example: "false"
        tan:
          type: string
          description: Transaction authentication number
          example: "123456"
        txId:
          type: string
          description: Transaction ID from previous validation step
          example: "TX-abc123"
        authorizationType:
          type: string
          description: Authorization type
          example: "TAN"
        serviceOperationMode:
          type: string
          description: Service operation mode
          example: "REAL"
        tanAuthType:
          type: string
          description: TAN authorization type
          example: "SMS_TAN"
        transactionPayload:
          $ref: '#/components/schemas/RiskStrategyUpdateTransactionPayload'

    RiskStrategyUpdateResponse:
      type: object
      required:
        - portfolioId
        - portfolioDescription
        - suggestedRiskStrategy
        - selectedRiskStrategy
        - investmentDuration
        - investmentAmount
      properties:
        portfolioId:
          type: string
          description: Portfolio identifier
          example: "P-987654"
        portfolioDescription:
          type: string
          description: Portfolio description
          example: "Conservative Portfolio"
        suggestedRiskStrategy:
          type: string
          description: Suggested risk strategy
          example: "LOW"
        selectedRiskStrategy:
          type: string
          description: Selected risk strategy
          example: "MEDIUM"
        investmentDuration:
          type: string
          description: Investment duration
          example: "5Y"
        investmentAmount:
          type: integer
          description: Investment amount
          example: 50000

```


```
import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "should return 204 when validating risk strategy update with all parameters"
    request {
        method POST()
        url "/v1/risk-strategy/validate"
        headers {
            header("Authorization", consumer(regex('.+')), producer("Bearer eyJhbGciOiJSUzI1NiJ9"))
            header("fe-id", consumer(regex('.+')), producer("fe-001"))
            header("language", consumer(regex('.+')), producer("DE"))
            header("trace-id", consumer(regex('.+')), producer("trace-abc123"))
            header("User-Agent", consumer(regex('.+')), producer("Mozilla/5.0"))
            header("x-source-service", consumer(regex('.+')), producer("risk-strategy-service"))
            header("x-request-id", consumer(regex('[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}')), producer("a1b2c3d4-e5f6-7890-abcd-ef1234567890"))
            contentType(applicationJson())
        }
        body([
            transactionPayload: [
                accountno           : "1234567",
                portfolioId         : "P-987654",
                portfolioDescription: "Conservative Portfolio",
                suggestedRiskStrategy: "LOW",
                selectedRiskStrategy : "MEDIUM",
                investmentDuration  : "5Y",
                investmentAmount    : 50000
            ]
        ])
    }
    response {
        status 204
        headers {
            header("x-correlation-id", fromRequest().header("x-request-id"))
        }
    }
}  
```


```
import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "should return 204 when validating risk strategy update with only required parameters"
    request {
        method POST()
        url "/v1/risk-strategy/validate"
        headers {
            header("Authorization", consumer(regex('.+')), producer("Bearer eyJhbGciOiJSUzI1NiJ9"))
            header("fe-id", consumer(regex('.+')), producer("fe-001"))
            header("language", consumer(regex('.+')), producer("DE"))
            header("trace-id", consumer(regex('.+')), producer("trace-abc123"))
            header("User-Agent", consumer(regex('.+')), producer("Mozilla/5.0"))
            header("x-source-service", consumer(regex('.+')), producer("risk-strategy-service"))
            header("x-request-id", consumer(regex('[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}')), producer("a1b2c3d4-e5f6-7890-abcd-ef1234567890"))
            contentType(applicationJson())
        }
        body([
            transactionPayload: [
                portfolioId         : "P-111222",
                portfolioDescription: "Growth Portfolio"
            ]
        ])
    }
    response {
        status 204
        headers {
            header("x-correlation-id", fromRequest().header("x-request-id"))
        }
    }
}

```


```
import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "should return 400 when required header x-source-service is missing"
    request {
        method POST()
        url "/v1/risk-strategy/validate"
        headers {
            header("Authorization", consumer(regex('.+')), producer("Bearer eyJhbGciOiJSUzI1NiJ9"))
            header("fe-id", consumer(regex('.+')), producer("fe-001"))
            header("language", consumer(regex('.+')), producer("DE"))
            header("trace-id", consumer(regex('.+')), producer("trace-abc123"))
            header("User-Agent", consumer(regex('.+')), producer("Mozilla/5.0"))
            header("x-request-id", consumer(regex('[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}')), producer("a1b2c3d4-e5f6-7890-abcd-ef1234567890"))
            contentType(applicationJson())
        }
        body([
            transactionPayload: [
                portfolioId         : "P-987654",
                portfolioDescription: "Conservative Portfolio"
            ]
        ])
    }
    response {
        status 400
        headers {
            header("x-correlation-id", fromRequest().header("x-request-id"))
            contentType(applicationJson())
        }
    }
}

```

```
import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "should return 200 with full response when initiating risk strategy update transaction with all parameters"
    request {
        method POST()
        url "/v1/risk-strategy/initiate-transaction"
        headers {
            header("Authorization", consumer(regex('.+')), producer("Bearer eyJhbGciOiJSUzI1NiJ9"))
            header("fe-id", consumer(regex('.+')), producer("fe-001"))
            header("language", consumer(regex('.+')), producer("DE"))
            header("trace-id", consumer(regex('.+')), producer("trace-abc123"))
            header("User-Agent", consumer(regex('.+')), producer("Mozilla/5.0"))
            header("x-source-service", consumer(regex('.+')), producer("risk-strategy-service"))
            header("x-request-id", consumer(regex('[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}')), producer("a1b2c3d4-e5f6-7890-abcd-ef1234567890"))
            contentType(applicationJson())
        }
        body([
            validateOnly          : "false",
            tan                   : "123456",
            txId                  : "TX-abc123",
            authorizationType     : "TAN",
            serviceOperationMode  : "REAL",
            tanAuthType           : "SMS_TAN",
            transactionPayload    : [
                accountno           : "1234567",
                portfolioId         : "P-987654",
                portfolioDescription: "Conservative Portfolio",
                suggestedRiskStrategy: "LOW",
                selectedRiskStrategy : "MEDIUM",
                investmentDuration  : "5Y",
                investmentAmount    : 50000
            ]
        ])
    }
    response {
        status 200
        headers {
            header("x-correlation-id", fromRequest().header("x-request-id"))
            contentType(applicationJson())
        }
        body([
            portfolioId          : "P-987654",
            portfolioDescription : "Conservative Portfolio",
            suggestedRiskStrategy: "LOW",
            selectedRiskStrategy : "MEDIUM",
            investmentDuration   : "5Y",
            investmentAmount     : 50000
        ])
    }
}

```

```
import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "should return 200 with full response when initiating risk strategy update transaction with only required parameters"
    request {
        method POST()
        url "/v1/risk-strategy/initiate-transaction"
        headers {
            header("Authorization", consumer(regex('.+')), producer("Bearer eyJhbGciOiJSUzI1NiJ9"))
            header("fe-id", consumer(regex('.+')), producer("fe-001"))
            header("language", consumer(regex('.+')), producer("DE"))
            header("trace-id", consumer(regex('.+')), producer("trace-abc123"))
            header("User-Agent", consumer(regex('.+')), producer("Mozilla/5.0"))
            header("x-source-service", consumer(regex('.+')), producer("risk-strategy-service"))
            header("x-request-id", consumer(regex('[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}')), producer("a1b2c3d4-e5f6-7890-abcd-ef1234567890"))
            contentType(applicationJson())
        }
        body([
            transactionPayload: [
                portfolioId         : "P-111222",
                portfolioDescription: "Growth Portfolio"
            ]
        ])
    }
    response {
        status 200
        headers {
            header("x-correlation-id", fromRequest().header("x-request-id"))
            contentType(applicationJson())
        }
        body([
            portfolioId          : "P-111222",
            portfolioDescription : "Growth Portfolio",
            suggestedRiskStrategy: "HIGH",
            selectedRiskStrategy : "HIGH",
            investmentDuration   : "10Y",
            investmentAmount     : 100000
        ])
    }
}
```
```
import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "should return 400 when required header x-source-service is missing on initiate transaction"
    request {
        method POST()
        url "/v1/risk-strategy/initiate-transaction"
        headers {
            header("Authorization", consumer(regex('.+')), producer("Bearer eyJhbGciOiJSUzI1NiJ9"))
            header("fe-id", consumer(regex('.+')), producer("fe-001"))
            header("language", consumer(regex('.+')), producer("DE"))
            header("trace-id", consumer(regex('.+')), producer("trace-abc123"))
            header("User-Agent", consumer(regex('.+')), producer("Mozilla/5.0"))
            header("x-request-id", consumer(regex('[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}')), producer("a1b2c3d4-e5f6-7890-abcd-ef1234567890"))
            contentType(applicationJson())
        }
        body([
            transactionPayload: [
                portfolioId         : "P-987654",
                portfolioDescription: "Conservative Portfolio"
            ]
        ])
    }
    response {
        status 400
        headers {
            header("x-correlation-id", fromRequest().header("x-request-id"))
            contentType(applicationJson())
        }
    }
}

```


```
package de.consorsbank.banking.payments.rest.adapter.controller.riskstrategy.model;

public class RiskStrategyUpdateUtils {

    private RiskStrategyUpdateUtils() {}

    public static ValidateRiskStrategyUpdateRequest buildValidateRequest() {
        var payload = new RiskStrategyUpdateTransactionPayload();
        payload.setAccountno("1234567");
        payload.setPortfolioId("P-987654");
        payload.setPortfolioDescription("Conservative Portfolio");
        payload.setSuggestedRiskStrategy("LOW");
        payload.setSelectedRiskStrategy("MEDIUM");
        payload.setInvestmentDuration("5Y");
        payload.setInvestmentAmount(50000);

        var request = new ValidateRiskStrategyUpdateRequest();
        request.setTransactionPayload(payload);
        return request;
    }

    public static ValidateRiskStrategyUpdateRequest buildValidateRequestRequiredOnly() {
        var payload = new RiskStrategyUpdateTransactionPayload();
        payload.setPortfolioId("P-111222");
        payload.setPortfolioDescription("Growth Portfolio");

        var request = new ValidateRiskStrategyUpdateRequest();
        request.setTransactionPayload(payload);
        return request;
    }

    public static InitiateRiskStrategyUpdateRequest buildInitiateRequest() {
        var payload = new RiskStrategyUpdateTransactionPayload();
        payload.setAccountno("1234567");
        payload.setPortfolioId("P-987654");
        payload.setPortfolioDescription("Conservative Portfolio");
        payload.setSuggestedRiskStrategy("LOW");
        payload.setSelectedRiskStrategy("MEDIUM");
        payload.setInvestmentDuration("5Y");
        payload.setInvestmentAmount(50000);

        var request = new InitiateRiskStrategyUpdateRequest();
        request.setValidateOnly("false");
        request.setTan("123456");
        request.setTxId("TX-abc123");
        request.setAuthorizationType("TAN");
        request.setServiceOperationMode("REAL");
        request.setTanAuthType("SMS_TAN");
        request.setTransactionPayload(payload);
        return request;
    }

    public static InitiateRiskStrategyUpdateRequest buildInitiateRequestRequiredOnly() {
        var payload = new RiskStrategyUpdateTransactionPayload();
        payload.setPortfolioId("P-111222");
        payload.setPortfolioDescription("Growth Portfolio");

        var request = new InitiateRiskStrategyUpdateRequest();
        request.setTransactionPayload(payload);
        return request;
    }

    public static RiskStrategyUpdateResponse buildRiskStrategyUpdateResponse() {
        return RiskStrategyUpdateResponse.builder()
                .portfolioId("P-987654")
                .portfolioDescription("Conservative Portfolio")
                .suggestedRiskStrategy("LOW")
                .selectedRiskStrategy("MEDIUM")
                .investmentDuration("5Y")
                .investmentAmount(50000)
                .build();
    }

    public static String buildValidateRequestAsJson() {
        return """
                {
                    "transactionPayload": {
                        "accountno": "1234567",
                        "portfolioId": "P-987654",
                        "portfolioDescription": "Conservative Portfolio",
                        "suggestedRiskStrategy": "LOW",
                        "selectedRiskStrategy": "MEDIUM",
                        "investmentDuration": "5Y",
                        "investmentAmount": 50000
                    }
                }
                """;
    }

    public static String buildValidateRequestRequiredOnlyAsJson() {
        return """
                {
                    "transactionPayload": {
                        "portfolioId": "P-111222",
                        "portfolioDescription": "Growth Portfolio"
                    }
                }
                """;
    }

    public static String buildInitiateRequestAsJson() {
        return """
                {
                    "validateOnly": "false",
                    "tan": "123456",
                    "txId": "TX-abc123",
                    "authorizationType": "TAN",
                    "serviceOperationMode": "REAL",
                    "tanAuthType": "SMS_TAN",
                    "transactionPayload": {
                        "accountno": "1234567",
                        "portfolioId": "P-987654",
                        "portfolioDescription": "Conservative Portfolio",
                        "suggestedRiskStrategy": "LOW",
                        "selectedRiskStrategy": "MEDIUM",
                        "investmentDuration": "5Y",
                        "investmentAmount": 50000
                    }
                }
                """;
    }

    public static String buildInitiateRequestRequiredOnlyAsJson() {
        return """
                {
                    "transactionPayload": {
                        "portfolioId": "P-111222",
                        "portfolioDescription": "Growth Portfolio"
                    }
                }
                """;
    }
}

```

```
package de.consorsbank.banking.payments.rest.adapter.controller.riskstrategy;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import de.consorsbank.banking.payments.rest.adapter.controller.riskstrategy.model.InitiateRiskStrategyUpdateRequest;
import de.consorsbank.banking.payments.rest.adapter.controller.riskstrategy.model.RiskStrategyUpdateUtils;
import de.consorsbank.banking.payments.rest.adapter.controller.riskstrategy.model.ValidateRiskStrategyUpdateRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(controllers = RiskStrategyUpdateController.class)
@Import({SecurityConfiguration.class})
@ActiveProfiles("test")
class RiskStrategyUpdateControllerTest extends ControllerUnitTestConfig {

    private static final String VALIDATE_URL = "/v1/risk-strategy/validate";
    private static final String INITIATE_URL = "/v1/risk-strategy/initiate-transaction";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RiskStrategyUpdateApi riskStrategyUpdateApi;

    @Test
    void should_ReturnNoContent_When_ValidatingRiskStrategyUpdateWithAllParameters() throws Exception {
        // given
        when(riskStrategyUpdateApi.validateRiskStrategyUpdate(
                any(), any(), any(), any(), any(), any(), any(), any(ValidateRiskStrategyUpdateRequest.class)))
                .thenReturn(ResponseEntity.noContent().build());

        // when
        mockMvc.perform(
                MockMvcRequestBuilders.post(VALIDATE_URL)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .headers(TestUtils.getAllHttpHeadersWithoutOwner("risk-strategy-validate"))
                        .content(RiskStrategyUpdateUtils.buildValidateRequestAsJson()))

                // then
                .andExpect(status().isNoContent());
    }

    @Test
    void should_ReturnNoContent_When_ValidatingRiskStrategyUpdateWithRequiredParametersOnly() throws Exception {
        // given
        when(riskStrategyUpdateApi.validateRiskStrategyUpdate(
                any(), any(), any(), any(), any(), any(), any(), any(ValidateRiskStrategyUpdateRequest.class)))
                .thenReturn(ResponseEntity.noContent().build());

        // when
        mockMvc.perform(
                MockMvcRequestBuilders.post(VALIDATE_URL)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .headers(TestUtils.getAllHttpHeadersWithoutOwner("risk-strategy-validate"))
                        .content(RiskStrategyUpdateUtils.buildValidateRequestRequiredOnlyAsJson()))

                // then
                .andExpect(status().isNoContent());
    }

    @Test
    void should_ReturnBadRequest_When_RequiredHeadersAreMissingOnValidate() throws Exception {
        // when
        mockMvc.perform(
                MockMvcRequestBuilders.post(VALIDATE_URL)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .headers(TestUtils.getMissingHttpHeadersWithoutOwnerAndAuthorization())
                        .content(RiskStrategyUpdateUtils.buildValidateRequestAsJson()))

                // then
                .andExpect(status().isBadRequest());
    }

    @Test
    void should_ReturnOk_When_InitiatingRiskStrategyUpdateTransactionWithAllParameters() throws Exception {
        // given
        when(riskStrategyUpdateApi.initiateRiskStrategyUpdate(
                any(), any(), any(), any(), any(), any(), any(), any(InitiateRiskStrategyUpdateRequest.class)))
                .thenReturn(ResponseEntity.ok(RiskStrategyUpdateUtils.buildRiskStrategyUpdateResponse()));

        // when
        mockMvc.perform(
                MockMvcRequestBuilders.post(INITIATE_URL)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .headers(TestUtils.getAllHttpHeadersWithoutOwner("risk-strategy-initiate"))
                        .content(RiskStrategyUpdateUtils.buildInitiateRequestAsJson()))

                // then
                .andExpect(status().isOk());
    }

    @Test
    void should_ReturnOk_When_InitiatingRiskStrategyUpdateTransactionWithRequiredParametersOnly() throws Exception {
        // given
        when(riskStrategyUpdateApi.initiateRiskStrategyUpdate(
                any(), any(), any(), any(), any(), any(), any(), any(InitiateRiskStrategyUpdateRequest.class)))
                .thenReturn(ResponseEntity.ok(RiskStrategyUpdateUtils.buildRiskStrategyUpdateResponse()));

        // when
        mockMvc.perform(
                MockMvcRequestBuilders.post(INITIATE_URL)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .headers(TestUtils.getAllHttpHeadersWithoutOwner("risk-strategy-initiate"))
                        .content(RiskStrategyUpdateUtils.buildInitiateRequestRequiredOnlyAsJson()))

                // then
                .andExpect(status().isOk());
    }

    @Test
    void should_ReturnBadRequest_When_RequiredHeadersAreMissingOnInitiateTransaction() throws Exception {
        // when
        mockMvc.perform(
                MockMvcRequestBuilders.post(INITIATE_URL)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .headers(TestUtils.getMissingHttpHeadersWithoutOwnerAndAuthorization())
                        .content(RiskStrategyUpdateUtils.buildInitiateRequestAsJson()))

                // then
                .andExpect(status().isBadRequest());
    }
}
```
