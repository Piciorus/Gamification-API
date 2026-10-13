```
import org.springframework.cloud.contract.spec.Contract

Contract.make {
    priority(priority: 1)
    description(description: """
        Represents a successful scenario for validate risk strategy update.

        when:
            api request to validate risk strategy update with all parameters.

        then:
            return 204 with successful validation
    """)

    request {
        method method: 'POST'
        urlPath($(consumer(regex('/v1/risk-strategy/validate')), producer(serverValue: '/v1/risk-strategy/validate')))
        headers {
            contentType applicationJson()
            header 'Authorization': value(consumer(regex('.+')), producer(serverValue: 'aSessionId'))
            header 'FeId': value(consumer(regex('.+')), producer(serverValue: 'WEB'))
            header 'Language': value(consumer(regex('.+')), producer(serverValue: 'DE'))
            header 'TraceId': value(consumer(regex('.+')), producer(serverValue: 'traceId'))
            header 'User-Agent': value(consumer(regex('.*')), producer(serverValue: 'User-Agent'))
            header 'x-source-service': value(consumer(regex('.+')), producer(serverValue: 'xSourceService'))
            header 'x-request-id': value(consumer(regex('.+')), producer(serverValue: 'b0589506-65eb-4fb9-9747-f023b5example'))
        }
        body([
            "transactionPayload": [
                "accountNumber"       : value(consumer(regex('[0-9]{7,10}')), producer(serverValue: '1234567')),
                "portfolioId"         : value(consumer(regex('.+')), producer(serverValue: '1234567')),
                "portfolioDescription": value(consumer(regex('.+')), producer(serverValue: 'MA (defensiv kurzfristig)')),
                "suggestedRiskStrategy": value(consumer(regex('[A-E]')), producer(serverValue: 'A')),
                "selectedRiskStrategy" : value(consumer(regex('[A-E]')), producer(serverValue: 'B')),
                "investmentDuration"  : value(consumer(regex('^SHORT_TERM$|^MIDDLE_TERM$|^LONG_TERM$')), producer(serverValue: 'SHORT_TERM')),
                "investmentAmount"    : value(consumer(regex('[0-9]+')), producer(serverValue: 50000))
            ]
        ])
    }

    response {
        status 204
        headers {
            header 'x-correlation-id': fromRequest().header('x-request-id')
        }
    }
}
```
```
import org.springframework.cloud.contract.spec.Contract

Contract.make {
    priority(priority: 1)
    description(description: """
        Represents a successful scenario for validate risk strategy update with only required parameters.

        when:
            api request to validate risk strategy update with required parameters only.

        then:
            return 204 with successful validation
    """)

    request {
        method method: 'POST'
        urlPath($(consumer(regex('/v1/risk-strategy/validate')), producer(serverValue: '/v1/risk-strategy/validate')))
        headers {
            contentType applicationJson()
            header 'Authorization': value(consumer(regex('.+')), producer(serverValue: 'aSessionId'))
            header 'FeId': value(consumer(regex('.+')), producer(serverValue: 'WEB'))
            header 'Language': value(consumer(regex('.+')), producer(serverValue: 'DE'))
            header 'TraceId': value(consumer(regex('.+')), producer(serverValue: 'traceId'))
            header 'User-Agent': value(consumer(regex('.*')), producer(serverValue: 'User-Agent'))
            header 'x-source-service': value(consumer(regex('.+')), producer(serverValue: 'xSourceService'))
            header 'x-request-id': value(consumer(regex('.+')), producer(serverValue: 'b0589506-65eb-4fb9-9747-f023b5example'))
        }
        body([
            "transactionPayload": [
                "portfolioId"         : value(consumer(regex('.+')), producer(serverValue: '1112222')),
                "portfolioDescription": value(consumer(regex('.+')), producer(serverValue: 'MA (solide mittelfristig)'))
            ]
        ])
    }

    response {
        status 204
        headers {
            header 'x-correlation-id': fromRequest().header('x-request-id')
        }
    }
}
```


```
import org.springframework.cloud.contract.spec.Contract

Contract.make {
    priority(priority: 1)
    description(description: """
        Represents a failed scenario for validate risk strategy update when required header is missing.

        when:
            api request to validate risk strategy update without x-source-service header.

        then:
            return 400 bad request
    """)

    request {
        method method: 'POST'
        urlPath($(consumer(regex('/v1/risk-strategy/validate')), producer(serverValue: '/v1/risk-strategy/validate')))
        headers {
            contentType applicationJson()
            header 'Authorization': value(consumer(regex('.+')), producer(serverValue: 'aSessionId'))
            header 'FeId': value(consumer(regex('.+')), producer(serverValue: 'WEB'))
            header 'Language': value(consumer(regex('.+')), producer(serverValue: 'DE'))
            header 'TraceId': value(consumer(regex('.+')), producer(serverValue: 'traceId'))
            header 'User-Agent': value(consumer(regex('.*')), producer(serverValue: 'User-Agent'))
            header 'x-request-id': value(consumer(regex('.+')), producer(serverValue: 'b0589506-65eb-4fb9-9747-f023b5example'))
        }
        body([
            "transactionPayload": [
                "portfolioId"         : value(consumer(regex('.+')), producer(serverValue: '1234567')),
                "portfolioDescription": value(consumer(regex('.+')), producer(serverValue: 'MA (defensiv kurzfristig)'))
            ]
        ])
    }

    response {
        status 400
        headers {
            contentType applicationJson()
            header 'x-correlation-id': fromRequest().header('x-request-id')
        }
    }
}
```

```
import org.springframework.cloud.contract.spec.Contract

Contract.make {
    priority(priority: 1)
    description(description: """
        Represents a successful scenario for initiate risk strategy update transaction with all parameters.

        when:
            api request to initiate risk strategy update transaction.

        then:
            return 200 with successful transaction
    """)

    request {
        method method: 'POST'
        urlPath($(consumer(regex('/v1/risk-strategy/initiate-transaction')), producer(serverValue: '/v1/risk-strategy/initiate-transaction')))
        headers {
            contentType applicationJson()
            header 'Authorization': value(consumer(regex('.+')), producer(serverValue: 'aSessionId'))
            header 'FeId': value(consumer(regex('.+')), producer(serverValue: 'WEB'))
            header 'Language': value(consumer(regex('.+')), producer(serverValue: 'DE'))
            header 'TraceId': value(consumer(regex('.+')), producer(serverValue: 'traceId'))
            header 'User-Agent': value(consumer(regex('.*')), producer(serverValue: 'User-Agent'))
            header 'x-source-service': value(consumer(regex('.+')), producer(serverValue: 'xSourceService'))
            header 'x-request-id': value(consumer(regex('.+')), producer(serverValue: 'b0589506-65eb-4fb9-9747-f023b5example'))
            header 'idempotency-key': value(consumer(regex('.+')), producer(serverValue: 'b0589506-65eb-4fb9-9747-f023b5example'))
        }
        body([
            "transactionIntentId": value(consumer(regex('.+')), producer(serverValue: '4866044f-c630-4678-9a17-faf0example')),
            "transactionPayload" : [
                "accountNumber"       : value(consumer(regex('[0-9]{7,10}')), producer(serverValue: '1234567')),
                "portfolioId"         : value(consumer(regex('.+')), producer(serverValue: '1234567')),
                "portfolioDescription": value(consumer(regex('.+')), producer(serverValue: 'MA (defensiv kurzfristig)')),
                "suggestedRiskStrategy": value(consumer(regex('[A-E]')), producer(serverValue: 'A')),
                "selectedRiskStrategy" : value(consumer(regex('[A-E]')), producer(serverValue: 'B')),
                "investmentDuration"  : value(consumer(regex('^SHORT_TERM$|^MIDDLE_TERM$|^LONG_TERM$')), producer(serverValue: 'SHORT_TERM')),
                "investmentAmount"    : value(consumer(regex('[0-9]+')), producer(serverValue: 50000))
            ]
        ])
    }

    response {
        status 200
        headers {
            contentType applicationJson()
            header 'x-correlation-id': fromRequest().header('x-request-id')
        }
        body([
            "portfolioId"          : value(producer(regex('.+')), consumer(serverValue: '1234567')),
            "portfolioDescription" : value(producer(regex('.+')), consumer(serverValue: 'MA (defensiv kurzfristig)')),
            "suggestedRiskStrategy": value(producer(regex('[A-E]')), consumer(serverValue: 'A')),
            "selectedRiskStrategy" : value(producer(regex('[A-E]')), consumer(serverValue: 'B')),
            "investmentDuration"   : value(producer(regex('.+')), consumer(serverValue: 'SHORT_TERM')),
            "investmentAmount"     : value(producer(regex('[0-9]+')), consumer(serverValue: 50000))
        ])
    }
}

```


```
import org.springframework.cloud.contract.spec.Contract

Contract.make {
    priority(priority: 1)
    description(description: """
        Represents a successful scenario for initiate risk strategy update transaction with required parameters only.

        when:
            api request to initiate risk strategy update transaction with required parameters only.

        then:
            return 200 with successful transaction
    """)

    request {
        method method: 'POST'
        urlPath($(consumer(regex('/v1/risk-strategy/initiate-transaction')), producer(serverValue: '/v1/risk-strategy/initiate-transaction')))
        headers {
            contentType applicationJson()
            header 'Authorization': value(consumer(regex('.+')), producer(serverValue: 'aSessionId'))
            header 'FeId': value(consumer(regex('.+')), producer(serverValue: 'WEB'))
            header 'Language': value(consumer(regex('.+')), producer(serverValue: 'DE'))
            header 'TraceId': value(consumer(regex('.+')), producer(serverValue: 'traceId'))
            header 'User-Agent': value(consumer(regex('.*')), producer(serverValue: 'User-Agent'))
            header 'x-source-service': value(consumer(regex('.+')), producer(serverValue: 'xSourceService'))
            header 'x-request-id': value(consumer(regex('.+')), producer(serverValue: 'b0589506-65eb-4fb9-9747-f023b5example'))
            header 'idempotency-key': value(consumer(regex('.+')), producer(serverValue: 'b0589506-65eb-4fb9-9747-f023b5example'))
        }
        body([
            "transactionIntentId": value(consumer(regex('.+')), producer(serverValue: '4866044f-c630-4678-9a17-faf0example')),
            "transactionPayload" : [
                "portfolioId"         : value(consumer(regex('.+')), producer(serverValue: '1112222')),
                "portfolioDescription": value(consumer(regex('.+')), producer(serverValue: 'MA (solide mittelfristig)'))
            ]
        ])
    }

    response {
        status 200
        headers {
            contentType applicationJson()
            header 'x-correlation-id': fromRequest().header('x-request-id')
        }
        body([
            "portfolioId"          : value(producer(regex('.+')), consumer(serverValue: '1112222')),
            "portfolioDescription" : value(producer(regex('.+')), consumer(serverValue: 'MA (solide mittelfristig)')),
            "suggestedRiskStrategy": value(producer(regex('[A-E]')), consumer(serverValue: 'C')),
            "selectedRiskStrategy" : value(producer(regex('[A-E]')), consumer(serverValue: 'C')),
            "investmentDuration"   : value(producer(regex('.+')), consumer(serverValue: 'LONG_TERM')),
            "investmentAmount"     : value(producer(regex('[0-9]+')), consumer(serverValue: 100000))
        ])
    }
}

```


```
import org.springframework.cloud.contract.spec.Contract

Contract.make {
    priority(priority: 1)
    description(description: """
        Represents a failed scenario for initiate risk strategy update transaction when required header is missing.

        when:
            api request to initiate risk strategy update transaction without x-source-service header.

        then:
            return 400 bad request
    """)

    request {
        method method: 'POST'
        urlPath($(consumer(regex('/v1/risk-strategy/initiate-transaction')), producer(serverValue: '/v1/risk-strategy/initiate-transaction')))
        headers {
            contentType applicationJson()
            header 'Authorization': value(consumer(regex('.+')), producer(serverValue: 'aSessionId'))
            header 'FeId': value(consumer(regex('.+')), producer(serverValue: 'WEB'))
            header 'Language': value(consumer(regex('.+')), producer(serverValue: 'DE'))
            header 'TraceId': value(consumer(regex('.+')), producer(serverValue: 'traceId'))
            header 'User-Agent': value(consumer(regex('.*')), producer(serverValue: 'User-Agent'))
            header 'x-request-id': value(consumer(regex('.+')), producer(serverValue: 'b0589506-65eb-4fb9-9747-f023b5example'))
            header 'idempotency-key': value(consumer(regex('.+')), producer(serverValue: 'b0589506-65eb-4fb9-9747-f023b5example'))
        }
        body([
            "transactionIntentId": value(consumer(regex('.+')), producer(serverValue: '4866044f-c630-4678-9a17-faf0example')),
            "transactionPayload" : [
                "portfolioId"         : value(consumer(regex('.+')), producer(serverValue: '1234567')),
                "portfolioDescription": value(consumer(regex('.+')), producer(serverValue: 'MA (defensiv kurzfristig)'))
            ]
        ])
    }

    response {
        status 400
        headers {
            contentType applicationJson()
            header 'x-correlation-id': fromRequest().header('x-request-id')
        }
    }
}

```





```
package de.consorsbank.banking.payments.rest.adapter.controller.riskstrategy.model;

public class RiskStrategyUpdateUtils {

    private RiskStrategyUpdateUtils() {}

    public static ValidateRiskStrategyUpdateRequest buildValidateRequest() {
        var payload = new RiskStrategyUpdateTransactionRequest();
        payload.setAccountNumber("1234567");
        payload.setPortfolioId("1234567");
        payload.setPortfolioDescription("MA (defensiv kurzfristig)");
        payload.setSuggestedRiskStrategy("A");
        payload.setSelectedRiskStrategy("B");
        payload.setInvestmentDuration("SHORT_TERM");
        payload.setInvestmentAmount(50000);

        var request = new ValidateRiskStrategyUpdateRequest();
        request.setTransactionPayload(payload);
        return request;
    }

    public static ValidateRiskStrategyUpdateRequest buildValidateRequestRequiredOnly() {
        var payload = new RiskStrategyUpdateTransactionRequest();
        payload.setPortfolioId("1112222");
        payload.setPortfolioDescription("MA (solide mittelfristig)");

        var request = new ValidateRiskStrategyUpdateRequest();
        request.setTransactionPayload(payload);
        return request;
    }

    public static InitiateRiskStrategyUpdateRequest buildInitiateRequest() {
        var payload = new RiskStrategyUpdateTransactionRequest();
        payload.setAccountNumber("1234567");
        payload.setPortfolioId("1234567");
        payload.setPortfolioDescription("MA (defensiv kurzfristig)");
        payload.setSuggestedRiskStrategy("A");
        payload.setSelectedRiskStrategy("B");
        payload.setInvestmentDuration("SHORT_TERM");
        payload.setInvestmentAmount(50000);

        var request = new InitiateRiskStrategyUpdateRequest();
        request.setTransactionIntentId("4866044f-c630-4678-9a17-faf0example");
        request.setTransactionPayload(payload);
        return request;
    }

    public static InitiateRiskStrategyUpdateRequest buildInitiateRequestRequiredOnly() {
        var payload = new RiskStrategyUpdateTransactionRequest();
        payload.setPortfolioId("1112222");
        payload.setPortfolioDescription("MA (solide mittelfristig)");

        var request = new InitiateRiskStrategyUpdateRequest();
        request.setTransactionIntentId("4866044f-c630-4678-9a17-faf0example");
        request.setTransactionPayload(payload);
        return request;
    }

    public static RiskStrategyUpdateResponse buildRiskStrategyUpdateResponse() {
        return RiskStrategyUpdateResponse.builder()
                .portfolioId("1234567")
                .portfolioDescription("MA (defensiv kurzfristig)")
                .suggestedRiskStrategy("A")
                .selectedRiskStrategy("B")
                .investmentDuration("SHORT_TERM")
                .investmentAmount(50000)
                .build();
    }

    public static String buildValidateRequestAsJson() {
        return """
                {
                    "transactionPayload": {
                        "accountNumber": "1234567",
                        "portfolioId": "1234567",
                        "portfolioDescription": "MA (defensiv kurzfristig)",
                        "suggestedRiskStrategy": "A",
                        "selectedRiskStrategy": "B",
                        "investmentDuration": "SHORT_TERM",
                        "investmentAmount": 50000
                    }
                }
                """;
    }

    public static String buildValidateRequestRequiredOnlyAsJson() {
        return """
                {
                    "transactionPayload": {
                        "portfolioId": "1112222",
                        "portfolioDescription": "MA (solide mittelfristig)"
                    }
                }
                """;
    }

    public static String buildInitiateRequestAsJson() {
        return """
                {
                    "transactionIntentId": "4866044f-c630-4678-9a17-faf0example",
                    "transactionPayload": {
                        "accountNumber": "1234567",
                        "portfolioId": "1234567",
                        "portfolioDescription": "MA (defensiv kurzfristig)",
                        "suggestedRiskStrategy": "A",
                        "selectedRiskStrategy": "B",
                        "investmentDuration": "SHORT_TERM",
                        "investmentAmount": 50000
                    }
                }
                """;
    }

    public static String buildInitiateRequestRequiredOnlyAsJson() {
        return """
                {
                    "transactionIntentId": "4866044f-c630-4678-9a17-faf0example",
                    "transactionPayload": {
                        "portfolioId": "1112222",
                        "portfolioDescription": "MA (solide mittelfristig)"
                    }
                }
                """;
    }
}

```


```
package de.consorsbank.banking.payments.rest.adapter.controller.riskstrategy;

import de.consorsbank.banking.payments.rest.adapter.controller.riskstrategy.model.InitiateRiskStrategyUpdateRequest;
import de.consorsbank.banking.payments.rest.adapter.controller.riskstrategy.model.RiskStrategyUpdateResponse;
import de.consorsbank.banking.payments.rest.adapter.controller.riskstrategy.model.ValidateRiskStrategyUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RiskStrategyUpdateController implements RiskStrategyUpdateApi {

    private final RiskStrategyUpdateService riskStrategyUpdateService;

    @Override
    public ResponseEntity<Void> validateRiskStrategyUpdate(
            String authorization,
            String feId,
            String language,
            String traceId,
            String userAgent,
            String xSourceService,
            String xRequestId,
            ValidateRiskStrategyUpdateRequest validateRiskStrategyUpdateRequest) {

        riskStrategyUpdateService.validateRiskStrategyUpdate(validateRiskStrategyUpdateRequest);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<RiskStrategyUpdateResponse> initiateRiskStrategyUpdate(
            String authorization,
            String feId,
            String language,
            String traceId,
            String userAgent,
            String xSourceService,
            String xRequestId,
            String idempotencyKey,
            InitiateRiskStrategyUpdateRequest initiateRiskStrategyUpdateRequest) {

        return ResponseEntity.ok()
                .body(riskStrategyUpdateService.initiateRiskStrategyUpdate(initiateRiskStrategyUpdateRequest));
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
                any(), any(), any(), any(), any(), any(), any(), any(), any(InitiateRiskStrategyUpdateRequest.class)))
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
                any(), any(), any(), any(), any(), any(), any(), any(), any(InitiateRiskStrategyUpdateRequest.class)))
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
