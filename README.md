```
package de.consorsbank.core.trauthsc.integration.service;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

public class InitiateTransactionAuthorizationIntegrationTest extends IntegrationBaseTest {

    private static final String PATH = "/svc/trauth/v1/transaction-authorizations";

    @Override
    protected String path() { return PATH; }

    // ── 403 - no auth headers ──────────────────────────────────────────────

    @Test
    void should_return403_when_requestIsValid_butNoAuthHeaders() {
        // when
        var response = testRestTemplate.exchange(
                url(),
                HttpMethod.POST,
                new HttpEntity<>(TestUtils.buildInitiateTransactionAuthorizationRequest()),
                Object.class
        );

        // then
        assertThat(response.getBody())
                .as("Expected the received response is not blank")
                .isNotNull();
        assertThat(response.getStatusCode().value())
                .as("Expected the received response to be 403 Forbidden")
                .isEqualTo(HttpStatus.FORBIDDEN.value());
    }

    // ── 200 - happy path ───────────────────────────────────────────────────

    @Test
    void should_return200_when_requestIsValid() {
        // when
        var response = testRestTemplate.exchange(
                url(),
                HttpMethod.POST,
                new HttpEntity<>(
                        TestUtils.buildInitiateTransactionAuthorizationRequest(),
                        TestUtils.getTestHttpHeaders()
                ),
                InitiateTransactionAuthorizationResponse.class
        );

        // then
        assertThat(response.getBody())
                .as("Expected the received response is not blank")
                .isNotNull();
        assertThat(response.getStatusCode().value())
                .as("Expected the received response to be 200 OK")
                .isEqualTo(HttpStatus.OK.value());
        assertThat(response.getBody().getAuthorizationId())
                .as("Expected authorizationId to be present")
                .isNotNull();
        assertThat(response.getBody().getStatus())
                .as("Expected status to be PENDING")
                .isEqualTo("PENDING");
    }

    // ── 400 - invalid payload (bad JSON) ───────────────────────────────────

    @Test
    void should_return400_when_payloadJsonIsInvalid() {
        // given
        var invalidRequest = TestUtils.buildInitiateTransactionAuthorizationRequest();
        invalidRequest.setTransactionServicePayload("not-valid-json");

        // when
        var response = testRestTemplate.exchange(
                url(),
                HttpMethod.POST,
                new HttpEntity<>(invalidRequest, TestUtils.getTestHttpHeaders()),
                Object.class
        );

        // then
        assertThat(response.getStatusCode().value())
                .as("Expected the received response to be 400 Bad Request")
                .isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    // ── 409 - duplicate transactionId (already PENDING) ───────────────────

    @Test
    void should_return409_when_transactionIdAlreadyExists() {
        // given — first call succeeds
        var request = TestUtils.buildInitiateTransactionAuthorizationRequest();
        request.setTransactionId("duplicate-txn-id");

        testRestTemplate.exchange(
                url(),
                HttpMethod.POST,
                new HttpEntity<>(request, TestUtils.getTestHttpHeaders()),
                InitiateTransactionAuthorizationResponse.class
        );

        // when — second call with same transactionId
        var response = testRestTemplate.exchange(
                url(),
                HttpMethod.POST,
                new HttpEntity<>(request, TestUtils.getTestHttpHeaders()),
                Object.class
        );

        // then
        assertThat(response.getStatusCode().value())
                .as("Expected the received response to be 409 Conflict")
                .isEqualTo(HttpStatus.CONFLICT.value());
    }

    // ── 404 - service not found ────────────────────────────────────────────

    @Test
    void should_return404_when_serviceNotFound() {
        // given
        var request = TestUtils.buildInitiateTransactionAuthorizationRequest();
        request.setTransactionService("non-existent-service");
        request.setTransactionServiceVersion("v999");

        // when
        var response = testRestTemplate.exchange(
                url(),
                HttpMethod.POST,
                new HttpEntity<>(request, TestUtils.getTestHttpHeaders()),
                Object.class
        );

        // then
        assertThat(response.getStatusCode().value())
                .as("Expected the received response to be 404 Not Found")
                .isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    // ── 400 - missing required fields ─────────────────────────────────────

    @Test
    void should_return400_when_requiredFieldsAreMissing() {
        // given — empty request body
        var emptyRequest = new InitiateTransactionAuthorizationRequest();

        // when
        var response = testRestTemplate.exchange(
                url(),
                HttpMethod.POST,
                new HttpEntity<>(emptyRequest, TestUtils.getTestHttpHeaders()),
                Object.class
        );

        // then
        assertThat(response.getStatusCode().value())
                .as("Expected the received response to be 400 Bad Request")
                .isEqualTo(HttpStatus.BAD_REQUEST.value());
    }
}

```
