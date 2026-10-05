```
openapi: 3.0.3
info:
  title: KYC Risk And MDC Inquiry API
  description: API for retrieving KYC risk level and MDC information for a customer
  version: 1.0.0

paths:
  /v1/kyc-risk-and-mdc:
    get:
      summary: Get KYC risk and MDC information
      description: |
        Retrieves KYC risk level and MDC (Market Data Compliance) information
        for a customer identified by CRM customer number.
      operationId: getKycRiskAndMdc
      tags:
        - KYC Risk And MDC Inquiry
      parameters:
        - name: crmCustomerNo
          in: query
          required: true
          description: CRM customer number of the client
          schema:
            type: string
          example: "1234567"
        - $ref: '../authorization/authorization-headers.yaml#/components/parameters/Authorization'
        - $ref: '../authorization/authorization-headers.yaml#/components/parameters/FeId'
        - $ref: '../authorization/authorization-headers.yaml#/components/parameters/Language'
        - $ref: '../authorization/authorization-headers.yaml#/components/parameters/TraceId'
        - $ref: '../authorization/authorization-headers.yaml#/components/parameters/UserAgent'
        - $ref: '../common/common-headers.yaml#/components/parameters/XSourceService'
        - $ref: '../common/common-headers.yaml#/components/parameters/XRequestId'
      responses:
        '200':
          description: KYC risk and MDC information retrieved successfully
          headers:
            x-correlation-id:
              description: Correlation ID echoed from x-request-id
              schema:
                type: string
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/KycRiskAndMdcInqResponse'
              example:
                crmCustomerNo: "1234567"
                sourceSystem: "ADM"
                nextRecertificationDate: "2027-01-15"
                riskLevel: "LOW"
                nextMdcDate: "2026-12-01"
        '400':
          description: Bad Request - missing or invalid required parameters
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
    KycRiskAndMdcInqResponse:
      type: object
      required:
        - crmCustomerNo
        - sourceSystem
      properties:
        crmCustomerNo:
          type: string
          description: CRM customer number of the client
          example: "1234567"
        sourceSystem:
          $ref: '#/components/schemas/SourceSystem'
        nextRecertificationDate:
          type: string
          format: date
          description: Next recertification date
          example: "2027-01-15"
        riskLevel:
          $ref: '#/components/schemas/RiskLevel'
        nextMdcDate:
          type: string
          format: date
          description: Next MDC date
          example: "2026-12-01"

    SourceSystem:
      type: string
      description: Source system
      enum:
        - ADM
        - COBRA
      example: "ADM"

    RiskLevel:
      type: string
      description: Risk level of the customer
      enum:
        - LOW
        - MEDIUM
        - HIGH
      example: "LOW"

```

```
package contracts

import org.springframework.cloud.contract.spec.Contract

[Contract.make {
    priority(1)
    description("""
        Represents a successful scenario for get KYC risk and MDC inquiry.

        when:
            api request to get KYC risk and MDC information for a customer.
        then:
            return 200 with KYC risk and MDC data
    """)

    request {
        method 'GET'
        urlPath($(consumer('/v1/kyc-risk-and-mdc'), producer('/v1/kyc-risk-and-mdc'))) {
            queryParameters {
                parameter 'crmCustomerNo': value(consumer(regex('.+')), producer('1234567'))
            }
        }
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
    }

    response {
        status OK()
        headers {
            contentType applicationJson()
            header 'x-correlation-id': fromRequest().header("x-request-id")
        }
        body([
            "crmCustomerNo"           : "1234567",
            "sourceSystem"            : "ADM",
            "nextRecertificationDate" : "2027-01-15",
            "riskLevel"               : "LOW",
            "nextMdcDate"             : "2026-12-01"
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
        Represents a successful scenario for get KYC risk and MDC inquiry
        when optional fields are not present in the response.

        when:
            api request to get KYC risk and MDC information for a customer.
        then:
            return 200 with only required fields
    """)

    request {
        method 'GET'
        urlPath($(consumer('/v1/kyc-risk-and-mdc'), producer('/v1/kyc-risk-and-mdc'))) {
            queryParameters {
                parameter 'crmCustomerNo': value(consumer(regex('.+')), producer('7654321'))
            }
        }
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
    }

    response {
        status OK()
        headers {
            contentType applicationJson()
            header 'x-correlation-id': fromRequest().header("x-request-id")
        }
        body([
            "crmCustomerNo": "7654321",
            "sourceSystem" : "COBRA"
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
        Represents an unsuccessful scenario for get KYC risk and MDC inquiry.

        when:
            api request to get KYC risk and MDC information without required header x-source-service.
        then:
            return 400 with Bad Request
    """)

    request {
        method 'GET'
        urlPath($(consumer('/v1/kyc-risk-and-mdc'), producer('/v1/kyc-risk-and-mdc'))) {
            queryParameters {
                parameter 'crmCustomerNo': value(consumer(regex('.+')), producer('1234567'))
            }
        }
        headers {
            contentType applicationJson()
            header 'Authorization': value(consumer(regex('.+')), producer('aSessionId'))
            header 'FeId': value(consumer(regex('.+')), producer('WEB'))
            header 'Language': value(consumer(regex('.+')), producer('DE'))
            header 'TraceId': value(consumer(regex('.+')), producer('traceId'))
            header 'User-Agent': value(consumer(regex('.*')), producer('User-Agent'))
            header 'x-request-id': value(consumer(regex('.+')), producer('12345678'))
        }
    }

    response {
        status BAD_REQUEST()
        headers {
            contentType applicationJson()
            header 'x-correlation-id': fromRequest().header("x-request-id")
        }
        body([
            "status" : "400",
            "traceId": "",
            "code"   : "",
            "title"  : "Header X-source-service ist erforderlich",
            "detail" : "Required request header 'x-source-service' for method parameter type String is not present",
            "errors" : []
        ])
    }
}]

```


```
package de.consorsbank.banking.payments.rest.adapter.controller.model;

import java.time.LocalDate;

public class KycRiskAndMdcInqUtils {

    private KycRiskAndMdcInqUtils() {}

    public static KycRiskAndMdcInqResponse buildKycRiskAndMdcInqResponse() {
        return KycRiskAndMdcInqResponse.builder()
                .crmCustomerNo("1234567")
                .sourceSystem(SourceSystem.ADM)
                .nextRecertificationDate(LocalDate.of(2027, 1, 15))
                .riskLevel(RiskLevel.LOW)
                .nextMdcDate(LocalDate.of(2026, 12, 1))
                .build();
    }

    public static KycRiskAndMdcInqResponse buildKycRiskAndMdcInqResponseRequiredOnly() {
        return KycRiskAndMdcInqResponse.builder()
                .crmCustomerNo("7654321")
                .sourceSystem(SourceSystem.COBRA)
                .build();
    }
}

```

```
package de.consorsbank.banking.payments.rest.adapter.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import de.consorsbank.banking.payments.rest.adapter.controller.model.KycRiskAndMdcInqResponse;
import de.consorsbank.banking.payments.rest.adapter.controller.model.KycRiskAndMdcInqUtils;
import de.consorsbank.banking.payments.rest.adapter.controller.service.KycRiskAndMdcInqService;
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
@WebMvcTest(controllers = KycRiskAndMdcInqController.class)
@Import({SecurityConfiguration.class})
@ActiveProfiles("test")
class KycRiskAndMdcInqControllerTest extends ControllerUnitTestConfig {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private KycRiskAndMdcInqService kycRiskAndMdcInqService;

    @Test
    void should_ReturnKycRiskAndMdcData_When_CrmCustomerNoIsValid() throws Exception {
        // given
        when(kycRiskAndMdcInqService.getKycRiskAndMdc(any(String.class)))
                .thenReturn(KycRiskAndMdcInqUtils.buildKycRiskAndMdcInqResponse());

        // when
        mockMvc.perform(
                MockMvcRequestBuilders.get("/v1/kyc-risk-and-mdc")
                        .queryParam("crmCustomerNo", "1234567")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .headers(TestUtils.getAllHttpHeadersWithoutOwner("get-kyc-risk-and-mdc")))

                // then
                .andExpect(status().isOk());
    }

    @Test
    void should_ReturnKycRiskAndMdcData_When_ResponseContainsOnlyRequiredFields() throws Exception {
        // given
        when(kycRiskAndMdcInqService.getKycRiskAndMdc(any(String.class)))
                .thenReturn(KycRiskAndMdcInqUtils.buildKycRiskAndMdcInqResponseRequiredOnly());

        // when
        mockMvc.perform(
                MockMvcRequestBuilders.get("/v1/kyc-risk-and-mdc")
                        .queryParam("crmCustomerNo", "7654321")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .headers(TestUtils.getAllHttpHeadersWithoutOwner("get-kyc-risk-and-mdc")))

                // then
                .andExpect(status().isOk());
    }

    @Test
    void should_ReturnBadRequest_When_RequiredHeadersAreNotPassed() throws Exception {
        // given
        when(kycRiskAndMdcInqService.getKycRiskAndMdc(any(String.class)))
                .thenReturn(KycRiskAndMdcInqUtils.buildKycRiskAndMdcInqResponse());

        // when
        mockMvc.perform(
                MockMvcRequestBuilders.get("/v1/kyc-risk-and-mdc")
                        .queryParam("crmCustomerNo", "1234567")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .headers(TestUtils.getMissingHttpHeadersWithoutOwnerAndAuthorization()))

                // then
                .andExpect(status().isBadRequest());
    }

    @Test
    void should_ReturnBadRequest_When_CrmCustomerNoIsMissing() throws Exception {
        // when
        mockMvc.perform(
                MockMvcRequestBuilders.get("/v1/kyc-risk-and-mdc")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .headers(TestUtils.getAllHttpHeadersWithoutOwner("get-kyc-risk-and-mdc")))

                // then
                .andExpect(status().isBadRequest());
    }
}
```
