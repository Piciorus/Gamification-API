```
package contracts

import org.springframework.cloud.contract.spec.Contract

[Contract.make {
    priority(1)
    description(description: """
        ...
        Represents a successful scenario for get cash transfer restrictions inquiry.

        when:
            api request to get cash transfer restrictions inquiry.
        then:
            return ok with cash transfer restrictions
        ...
    """)

    request {
        method method: 'GET'
        urlPath($(consumer(regex: '/v1/cash-transfer-restrictions')), producer(serverValue: '/v1/cash-transfer-restrictions')) {
            queryParameters {
                parameter 'crmCustomerNo': value(consumer(regex: '.+')), producer(serverValue: '1234567')
                parameter 'accountNo': value(consumer(regex: '[0-9]{7,10}')), producer(serverValue: '1234567')
            }
        }
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
    }

    response {
        status OK()
        headers {
            contentType applicationJson()
            header 'x-correlation-id': fromRequest().header(key: "x-request-id")
        }
        body([
            "account": [
                [
                    "accountno"                   : "1234567",
                    "accountTypeDescription"      : "DEPOT",
                    "dailyTransferLimit"          : 10000.00,
                    "usedTransferLimit"           : 1250.75,
                    "availableSingleTransferAmount": 8749.25,
                    "currentLimitIsUserLimit"     : false,
                    "currentLimitType"            : "DAILY_LIMIT",
                    "referenceAccounts"           : [
                        [
                            "ibanOpponent"       : "DE89370400440532013000",
                            "bicOpponent"        : "COBADEFFXXX",
                            "bankNameOpponent"   : "Deutsche Bank AG",
                            "accountingOpponent" : "Test Receiver",
                            "addressOpponent"    : "Test address opponent",
                            "countryCodeOpponent": "DE",
                            "createdAt"          : "2026-08-19"
                        ]
                    ],
                    "instantDailyTransferLimit"   : 5000.00,
                    "instantSingleTransferLimit"  : 1000.00,
                    "usedInstantTransferLimit"    : 250.00,
                    "availableInstantTransferAmount": 4750.00
                ]
            ]
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
        Represents an unsuccessful scenario for get cash transfer restrictions inquiry.

        when:
            api request to get cash transfer restrictions inquiry.
        then:
            return 400 with Bad Request
        ...
    """)

    request {
        method method: 'GET'
        urlPath($(consumer(regex: '/v1/cash-transfer-restrictions')), producer(serverValue: '/v1/cash-transfer-restrictions')) {
            queryParameters {
                parameter 'crmCustomerNo': value(consumer(regex: '.+')), producer(serverValue: '1234567')
                parameter 'accountNo': value(consumer(regex: '[0-9]{7,10}')), producer(serverValue: '1234567')
            }
        }
        headers {
            contentType applicationJson()
            header 'Authorization': value(consumer(regex: '.+')), producer(serverValue: 'aSessionId')
            header 'FeId': value(consumer(regex: '.+')), producer(serverValue: 'WEB')
            header 'Language': value(consumer(regex: '.+')), producer(serverValue: 'DE')
            header 'TraceId': value(consumer(regex: '.+')), producer(serverValue: 'traceId')
            header 'User-Agent': value(consumer(regex: '.*')), producer(serverValue: 'User-Agent')
            header 'x-request-id': value(consumer(regex: '.+')), producer(serverValue: '12345678')
        }
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
package de.consorsbank.banking.payments.rest.adapter.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import de.consorsbank.banking.payments.rest.adapter.controller.model.CashTransferRestrictionInqResponse;
import de.consorsbank.banking.payments.rest.adapter.controller.model.CashTransferRestrictionAccount;
import de.consorsbank.banking.payments.rest.adapter.controller.model.ReferenceAccount;
import de.consorsbank.banking.payments.rest.adapter.controller.service.CashTransferRestrictionInqService;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(controllers = CashTransferRestrictionInqController.class)
@Import({SecurityConfiguration.class})
@ActiveProfiles("test")
class CashTransferRestrictionInqControllerTest extends ControllerUnitTestConfig {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CashTransferRestrictionInqService cashTransferRestrictionInqService;

    @Test
    void should_ReturnCashTransferRestrictions_When_CrmCustomerNoIsValid() throws Exception {
        // given
        when(cashTransferRestrictionInqService.getCashTransferRestrictions(
                any(String.class), any(String.class)))
                .thenReturn(TestUtils.buildSimpleCashTransferRestrictionInqResponse());

        // when
        mockMvc.perform(
                MockMvcRequestBuilders.get("/v1/cash-transfer-restrictions")
                        .queryParam("crmCustomerNo", "1234567")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .headers(TestUtils.getAllHttpHeadersWithoutOwner(source: "get-cash-transfer-restrictions")))

                // then
                .andExpect(status().isOk());
    }

    @Test
    void should_ReturnCashTransferRestrictions_When_AccountNoIsValid() throws Exception {
        // given
        when(cashTransferRestrictionInqService.getCashTransferRestrictions(
                any(String.class), any(String.class)))
                .thenReturn(TestUtils.buildSimpleCashTransferRestrictionInqResponse());

        // when
        mockMvc.perform(
                MockMvcRequestBuilders.get("/v1/cash-transfer-restrictions")
                        .queryParam("crmCustomerNo", "1234567")
                        .queryParam("accountNo", "1234567")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .headers(TestUtils.getAllHttpHeadersWithoutOwner(source: "get-cash-transfer-restrictions")))

                // then
                .andExpect(status().isOk());
    }

    @Test
    void should_ReturnBadRequest_When_RequiredHeadersAreNotPassed() throws Exception {
        // given
        when(cashTransferRestrictionInqService.getCashTransferRestrictions(
                any(String.class), any(String.class)))
                .thenReturn(TestUtils.buildSimpleCashTransferRestrictionInqResponse());

        // when
        mockMvc.perform(
                MockMvcRequestBuilders.get("/v1/cash-transfer-restrictions")
                        .queryParam("crmCustomerNo", "1234567")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .headers(TestUtils.getMissingHttpHeadersWithoutOwnerAndAuthorization()))

                // then
                .andExpect(status().isBadRequest());
    }

    @Test
    void should_ReturnEmptyAccountList_When_NoRestrictionsFound() throws Exception {
        // given
        when(cashTransferRestrictionInqService.getCashTransferRestrictions(
                any(String.class), any(String.class)))
                .thenReturn(TestUtils.buildEmptyCashTransferRestrictionInqResponse());

        // when
        mockMvc.perform(
                MockMvcRequestBuilders.get("/v1/cash-transfer-restrictions")
                        .queryParam("crmCustomerNo", "1234567")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .headers(TestUtils.getAllHttpHeadersWithoutOwner(source: "get-cash-transfer-restrictions")))

                // then
                .andExpect(status().isOk());
    }
}

```
