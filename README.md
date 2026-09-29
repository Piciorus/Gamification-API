```
openapi: 3.0.3
info:
  title: Template Materialization API
  description: API for materializing templates by code and language with a parameter map
  version: 1.0.0

paths:
  /v1/templates/code/{code}/language/{language}/materialize:
    post:
      summary: Materialize a template
      description: |
        Materializes a template identified by its code and language (composite natural key),
        substituting the provided parameter map into the template content.
        Returns HTTP 404 if no matching Code + Language record exists.
        Returns HTTP 400 for invalid payload inputs or missing mandatory fields.
      operationId: materializeTemplate
      tags:
        - Template Materialization
      parameters:
        - name: code
          in: path
          required: true
          description: The unique code identifying the template (business key)
          schema:
            type: string
          example: "WELCOME_EMAIL"
        - name: language
          in: path
          required: true
          description: The language of the template (business key, e.g. DE, EN)
          schema:
            type: string
          example: "DE"
        - $ref: '../authorization/authorization-headers.yaml#/components/parameters/Authorization'
        - $ref: '../authorization/authorization-headers.yaml#/components/parameters/FeId'
        - $ref: '../authorization/authorization-headers.yaml#/components/parameters/Language'
        - $ref: '../authorization/authorization-headers.yaml#/components/parameters/TraceId'
        - $ref: '../authorization/authorization-headers.yaml#/components/parameters/UserAgent'
        - $ref: '../common/common-headers.yaml#/components/parameters/XSourceService'
        - $ref: '../common/common-headers.yaml#/components/parameters/XRequestId'
      requestBody:
        required: false
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/TemplateMaterializationRequest'
            example:
              parameters:
                customerName: "Max Mustermann"
                accountNumber: "1234567"
                date: "2026-09-29"
      responses:
        '200':
          description: Template successfully materialized
          headers:
            x-correlation-id:
              description: Correlation ID echoed from x-request-id
              schema:
                type: string
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/TemplateMaterializationResponse'
              example:
                templateCode: "WELCOME_EMAIL"
                language: "DE"
                materializedContent: "Sehr geehrter Herr Max Mustermann, Ihr Konto 1234567 wurde am 2026-09-29 eröffnet."
        '400':
          description: Bad Request - invalid payload or missing mandatory fields
          headers:
            x-correlation-id:
              description: Correlation ID echoed from x-request-id
              schema:
                type: string
          content:
            application/json:
              schema:
                $ref: '../common/schemas.yaml#/components/schemas/ErrorResponse'
        '404':
          description: Template not found for the given code and language
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
    TemplateMaterializationRequest:
      type: object
      properties:
        parameters:
          type: object
          description: Map of placeholder key-value pairs to substitute into the template
          additionalProperties:
            type: string
          example:
            customerName: "Max Mustermann"
            accountNumber: "1234567"

    TemplateMaterializationResponse:
      type: object
      properties:
        templateCode:
          type: string
          description: The code of the materialized template
          example: "WELCOME_EMAIL"
        language:
          type: string
          description: The language of the materialized template
          example: "DE"
        materializedContent:
          type: string
          description: The template content with all placeholders substituted
          example: "Sehr geehrter Herr Max Mustermann, Ihr Konto 1234567 wurde am 2026-09-29 eröffnet."
```


```
package contracts

import org.springframework.cloud.contract.spec.Contract

[Contract.make {
    priority(1)
    description("""
        Represents a successful scenario for template materialization.

        when:
            api request to materialize a template by code and language with parameters.
        then:
            return 200 with materialized template content
    """)

    request {
        method 'POST'
        urlPath($(consumer('/v1/templates/code/WELCOME_EMAIL/language/DE/materialize'), producer('/v1/templates/code/WELCOME_EMAIL/language/DE/materialize')))
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
            "parameters": [
                "customerName" : value(consumer(regex('.+')), producer('Max Mustermann')),
                "accountNumber": value(consumer(regex('.+')), producer('1234567')),
                "date"         : value(consumer(regex('.+')), producer('2026-09-29'))
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
            "templateCode"       : "WELCOME_EMAIL",
            "language"           : "DE",
            "materializedContent": "Sehr geehrter Herr Max Mustermann, Ihr Konto 1234567 wurde am 2026-09-29 eröffnet."
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
        Represents a successful scenario for template materialization without parameters map.

        when:
            api request to materialize a template with code and language only (empty body).
        then:
            return 200 with materialized template content
    """)

    request {
        method 'POST'
        urlPath($(consumer('/v1/templates/code/ACCOUNT_STATEMENT/language/EN/materialize'), producer('/v1/templates/code/ACCOUNT_STATEMENT/language/EN/materialize')))
        headers {
            contentType applicationJson()
            header 'Authorization': value(consumer(regex('.+')), producer('aSessionId'))
            header 'FeId': value(consumer(regex('.+')), producer('WEB'))
            header 'Language': value(consumer(regex('.+')), producer('EN'))
            header 'TraceId': value(consumer(regex('.+')), producer('traceId'))
            header 'User-Agent': value(consumer(regex('.*')), producer('User-Agent'))
            header 'x-source-service': value(consumer(regex('.+')), producer('xSourceService'))
            header 'x-request-id': value(consumer(regex('.+')), producer('12345678'))
        }
        body([])
    }

    response {
        status OK()
        headers {
            contentType applicationJson()
            header 'x-correlation-id': fromRequest().header("x-request-id")
        }
        body([
            "templateCode"       : "ACCOUNT_STATEMENT",
            "language"           : "EN",
            "materializedContent": "Dear Customer, your account statement is ready."
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
        Represents an unsuccessful scenario for template materialization.

        when:
            api request to materialize a template without required header x-source-service.
        then:
            return 400 with Bad Request
    """)

    request {
        method 'POST'
        urlPath($(consumer('/v1/templates/code/WELCOME_EMAIL/language/DE/materialize'), producer('/v1/templates/code/WELCOME_EMAIL/language/DE/materialize')))
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
            "parameters": [
                "customerName": value(consumer(regex('.+')), producer('Max Mustermann'))
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

import java.util.Map;

public class TemplateMaterializationUtils {

    private TemplateMaterializationUtils() {}

    public static TemplateMaterializationRequest buildTemplateMaterializationRequest() {
        return TemplateMaterializationRequest.builder()
                .parameters(Map.of(
                        "customerName", "Max Mustermann",
                        "accountNumber", "1234567",
                        "date", "2026-09-29"
                ))
                .build();
    }

    public static TemplateMaterializationResponse buildTemplateMaterializationResponse() {
        return TemplateMaterializationResponse.builder()
                .templateCode("WELCOME_EMAIL")
                .language("DE")
                .materializedContent(
                        "Sehr geehrter Herr Max Mustermann, Ihr Konto 1234567 wurde am 2026-09-29 eröffnet.")
                .build();
    }

    public static String buildRequestAsJson() {
        return """
                {
                    "parameters": {
                        "customerName": "Max Mustermann",
                        "accountNumber": "1234567",
                        "date": "2026-09-29"
                    }
                }
                """;
    }

    public static String buildEmptyRequestAsJson() {
        return "{}";
    }
}
```

```
package de.consorsbank.banking.payments.rest.adapter.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import de.consorsbank.banking.payments.rest.adapter.controller.model.TemplateMaterializationRequest;
import de.consorsbank.banking.payments.rest.adapter.controller.model.TemplateMaterializationUtils;
import de.consorsbank.banking.payments.rest.adapter.controller.service.TemplateMaterializationService;
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
@WebMvcTest(controllers = TemplateMaterializationController.class)
@Import({SecurityConfiguration.class})
@ActiveProfiles("test")
class TemplateMaterializationControllerTest extends ControllerUnitTestConfig {

    private static final String MATERIALIZE_URL = "/v1/templates/code/{code}/language/{language}/materialize";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TemplateMaterializationService templateMaterializationService;

    @Test
    void should_ReturnMaterializedTemplate_When_TemplateCodeAndLanguageAreValid() throws Exception {
        // given
        when(templateMaterializationService.materializeTemplate(
                any(String.class), any(String.class), any(TemplateMaterializationRequest.class)))
                .thenReturn(TemplateMaterializationUtils.buildTemplateMaterializationResponse());

        // when
        mockMvc.perform(
                MockMvcRequestBuilders.post(MATERIALIZE_URL, "WELCOME_EMAIL", "DE")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .headers(TestUtils.getAllHttpHeadersWithoutOwner("materialize-template"))
                        .content(TemplateMaterializationUtils.buildRequestAsJson()))

                // then
                .andExpect(status().isOk());
    }

    @Test
    void should_ReturnMaterializedTemplate_When_NoParametersProvided() throws Exception {
        // given
        when(templateMaterializationService.materializeTemplate(
                any(String.class), any(String.class), any(TemplateMaterializationRequest.class)))
                .thenReturn(TemplateMaterializationUtils.buildTemplateMaterializationResponse());

        // when
        mockMvc.perform(
                MockMvcRequestBuilders.post(MATERIALIZE_URL, "ACCOUNT_STATEMENT", "EN")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .headers(TestUtils.getAllHttpHeadersWithoutOwner("materialize-template"))
                        .content(TemplateMaterializationUtils.buildEmptyRequestAsJson()))

                // then
                .andExpect(status().isOk());
    }

    @Test
    void should_ReturnBadRequest_When_RequiredHeadersAreNotPassed() throws Exception {
        // given
        when(templateMaterializationService.materializeTemplate(
                any(String.class), any(String.class), any(TemplateMaterializationRequest.class)))
                .thenReturn(TemplateMaterializationUtils.buildTemplateMaterializationResponse());

        // when
        mockMvc.perform(
                MockMvcRequestBuilders.post(MATERIALIZE_URL, "WELCOME_EMAIL", "DE")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .headers(TestUtils.getMissingHttpHeadersWithoutOwnerAndAuthorization())
                        .content(TemplateMaterializationUtils.buildRequestAsJson()))

                // then
                .andExpect(status().isBadRequest());
    }
}

```
