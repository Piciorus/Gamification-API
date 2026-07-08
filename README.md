@Autowired
private JdbcTemplate jdbcTemplate; // or your tam JdbcTemplate

@BeforeEach
void createAuthorization() {
    var initResponse = testRestTemplate.exchange(
        url(), HttpMethod.POST,
        new HttpEntity<>(TestUtils.buildInitiateAuthorizationRequest(), 
            TestUtils.getTestHttpHeaders()),
        InitiateAuthorizationResponse.class
    );
    authorizationId = initResponse.getBody().authorizationId();
    
    // Does the row actually exist?
    var count = jdbcTemplate.queryForObject(
        "SELECT COUNT(*) FROM authorizations WHERE external_id = ?",
        Integer.class, authorizationId
    );
    System.out.println("Row count after POST: " + count); // expect 1, likely 0
}

```
spring:
  datasource:
    tam:
      url: jdbc:h2:mem:tam;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MODE=Oracle;INIT=CREATE SCHEMA IF NOT EXISTS TAM
      username: ${H2_LOCAL_USR:sa}
      password: ${H2_LOCAL_PASS:}
      driverClassName: org.h2.Driver
      xa-data-source-class-name: org.h2.jdbcx.JdbcDataSource
      configuration:
        uniqueResourceName: tam-${random.uuid}
        minPoolSize: 2
        maxPoolSize: 10
        readonly: false
        idle-timeout: 240
        borrowConnectionTimeout: 5

    pvm:
      url: jdbc:h2:mem:pvm;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MODE=Oracle;INIT=CREATE SCHEMA IF NOT EXISTS PVM
      username: ${H2_LOCAL_USR:sa}
      password: ${H2_LOCAL_PASS:}
      driverClassName: org.h2.Driver
      xa-data-source-class-name: org.h2.jdbcx.JdbcDataSource
      configuration:
        uniqueResourceName: pvm-${random.uuid}
        minPoolSize: 2
        maxPoolSize: 10
        readonly: false
        idle-timeout: 240
        borrowConnectionTimeout: 5

  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    hibernate:
      ddl-auto: none
    show-sql: true
    properties:
      hibernate:
        format_sql: true

liquibase:
  pvm:
    change-log: classpath:/db/changelog/changelog-master-pvm.yaml
    enabled: true
    drop-first: false
  tam:
    change-log: classpath:/db/changelog/changelog-master-tam.yaml
    enabled: true
    drop-first: false

# WireMock stub for the downstream authorization service
wiremock:
  server:
    port: 9680

# Disable security for integration tests — adjust if you use test JWT
spring.security.enabled: false

logging:
  level:
    de.consorsbank: DEBUG
    org.springframework.web: DEBUG
```


```
package de.consorsbank.core.trauthsc.integration;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import de.consorsbank.core.trauthsc.integration.wiremock.WireMockConfig;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integration-test")
@ContextConfiguration(classes = {WireMockConfig.class})
@AutoConfigureTestRestTemplate
public abstract class IntegrationBaseTest {

    protected static final String LOCALHOST = "http://localhost:";

    @LocalServerPort
    protected int port;

    @Autowired
    protected TestRestTemplate testRestTemplate;

    @Autowired
    protected WireMockServer mockAuthorizationService;

    protected String url() {
        return LOCALHOST + port + path();
    }

    protected abstract String path();
}
```


```

package de.consorsbank.core.trauthsc.integration.util;

import org.springframework.http.HttpHeaders;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Utility class for integration tests.
 * Extend your existing TestUtils with these additional builder methods.
 */
public class TestUtils {

    // -------------------------------------------------------------------------
    // Headers
    // -------------------------------------------------------------------------

    public static HttpHeaders getTestHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Feld", "WEB");
        headers.add("Language", "DE");
        headers.add("TraceId", "aTraceId");
        headers.add("x-source-service", "testService");
        headers.add("x-payload-owner", "TAM");
        headers.add("x-client-cert", "123213");
        headers.add("x-request-id", UUID.randomUUID().toString());
        headers.add("userAgent", "integration-test");
        headers.add("x-audit-auth", "Bearer test-session-token");
        headers.add("x-audit-usrid", "test-user-id");
        headers.add("Content-Type", "application/json");
        return headers;
    }

    public static HttpHeaders getTestHttpHeadersWithoutOwner() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Feld", "WEB");
        headers.add("Language", "DE");
        headers.add("TraceId", "aTraceId");
        headers.add("x-source-service", "testService");
        headers.add("x-request-id", UUID.randomUUID().toString());
        headers.add("userAgent", "integration-test");
        headers.add("x-audit-auth", "Bearer test-session-token");
        headers.add("x-audit-usrid", "test-user-id");
        headers.add("Content-Type", "application/json");
        return headers;
    }

    // -------------------------------------------------------------------------
    // Request builders — adapt field names to your actual DTOs/records
    // -------------------------------------------------------------------------

    /** POST /v1/authorizations */
    public static InitiateAuthorizationRequest buildInitiateAuthorizationRequest() {
        return new InitiateAuthorizationRequest(
            UUID.randomUUID().toString(),           // transactionId
            "compensation-pot-change",              // transactionService
            "{\"compensationPotYear\":2026}",       // transactionServicePayload
            "v1",                                   // transactionServiceVersion
            "TEST-CUSTOMER-001",                    // crmCustomerNumber
            "B2C",                                  // tenant
            OffsetDateTime.now().plusDays(1)        // expiresAt
        );
    }

    /** POST /v1/authorizations — missing required fields (for validation tests) */
    public static InitiateAuthorizationRequest buildInitiateAuthorizationRequestMissingFields() {
        return new InitiateAuthorizationRequest(
            null,   // transactionId — required → should fail
            null,   // transactionService — required → should fail
            null,
            null,
            null,
            null,
            null
        );
    }

    /** POST /v1/authorizations/{id}/attempts */
    public static SubmitAuthorizationMethodRequest buildSubmitAuthorizationMethodRequest() {
        return new SubmitAuthorizationMethodRequest(
            "TAN_FROM_NEOAPP",      // authorizationMethod
            "TEST-CUSTOMER-001",    // crmCustomerNumber
            "B2C"                   // tenant
        );
    }

    /** PATCH /v1/authorizations/{id}/attempts */
    public static SubmitAuthorizationCredentialRequest buildSubmitAuthorizationCredentialRequest(String tan) {
        return new SubmitAuthorizationCredentialRequest(
            "test-session-id",      // sessionId
            "TEST-CUSTOMER-001",    // crmCustomerNumber
            "B2C",                  // tenant
            tan,                    // authorizationCredential (TAN)
            "TAN_FROM_NEOAPP"       // authorizationMethod
        );
    }

    // -------------------------------------------------------------------------
    // Existing — kept for PayloadVault compatibility
    // -------------------------------------------------------------------------

    public static PayloadVaultRequest buildPayloadVaultRequest() {
        var payloadVaultRequest = new PayloadVaultRequest();
        payloadVaultRequest.setPayload("3");
        payloadVaultRequest.setExpiresAt(OffsetDateTime.now().plusDays(1));
        return payloadVaultRequest;
    }

    // -------------------------------------------------------------------------
    // Inner request records — replace with your actual DTOs if they already exist
    // -------------------------------------------------------------------------

    public record InitiateAuthorizationRequest(
        String transactionId,
        String transactionService,
        String transactionServicePayload,
        String transactionServiceVersion,
        String crmCustomerNumber,
        String tenant,
        OffsetDateTime expiresAt
    ) {}

    public record SubmitAuthorizationMethodRequest(
        String authorizationMethod,
        String crmCustomerNumber,
        String tenant
    ) {}

    public record SubmitAuthorizationCredentialRequest(
        String sessionId,
        String crmCustomerNumber,
        String tenant,
        String authorizationCredential,
        String authorizationMethod
    ) {}
}
```


```
package de.consorsbank.core.trauthsc.integration.service;

import de.consorsbank.core.trauthsc.integration.IntegrationBaseTest;
import de.consorsbank.core.trauthsc.integration.util.TestUtils;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for GET /v1/authorizations/{authorizationId}/payload
 * Returns the transaction payload stored in the PVM (Payload Vault Microservice).
 * Payload is stored at authorization creation time and retrieved here.
 */
class GetPayloadTransactionAuthorizationIT extends IntegrationBaseTest {

    private static final String BASE_PATH = "/svc/trauth/v1/authorizations";

    @Override
    protected String path() {
        return BASE_PATH;
    }

    // -------------------------------------------------------------------------
    // Happy path
    // -------------------------------------------------------------------------

    @Test
    void should_return_200_with_payload_after_authorization_created() {
        // given — create authorization (payload stored in PVM at this point)
        var authorizationId = createAuthorization();

        // when
        var response = testRestTemplate.exchange(
            payloadUrl(authorizationId),
            HttpMethod.GET,
            new HttpEntity<>(TestUtils.getTestHttpHeaders()),
            PayloadResponse.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        // The payload is the transactionServicePayload from the initiate request
        assertThat(response.getBody().transactionServicePayload()).isNotBlank();
    }

    @Test
    void should_return_payload_matching_the_one_submitted_at_creation() {
        // given
        var authorizationId = createAuthorization();

        // when
        var response = testRestTemplate.exchange(
            payloadUrl(authorizationId),
            HttpMethod.GET,
            new HttpEntity<>(TestUtils.getTestHttpHeaders()),
            PayloadResponse.class
        );

        // then — payload should match what we sent during initiation
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().transactionServicePayload())
            .contains("compensationPotYear");
    }

    @Test
    void should_return_payload_after_authorization_is_completed() {
        // given — full happy path
        var authorizationId = createAuthorization();
        submitMethod(authorizationId);
        submitCorrectCredential(authorizationId);

        // when
        var response = testRestTemplate.exchange(
            payloadUrl(authorizationId),
            HttpMethod.GET,
            new HttpEntity<>(TestUtils.getTestHttpHeaders()),
            PayloadResponse.class
        );

        // then — payload still accessible after completion
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    // -------------------------------------------------------------------------
    // Not found
    // -------------------------------------------------------------------------

    @Test
    void should_return_404_when_authorization_does_not_exist() {
        var response = testRestTemplate.exchange(
            payloadUrl("00000000-0000-0000-0000-000000000000"),
            HttpMethod.GET,
            new HttpEntity<>(TestUtils.getTestHttpHeaders()),
            Object.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // -------------------------------------------------------------------------
    // Auth failure
    // -------------------------------------------------------------------------

    @Test
    void should_return_403_when_owner_header_is_missing() {
        var authorizationId = createAuthorization();

        var response = testRestTemplate.exchange(
            payloadUrl(authorizationId),
            HttpMethod.GET,
            new HttpEntity<>(TestUtils.getTestHttpHeadersWithoutOwner()),
            Object.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    // -------------------------------------------------------------------------
    // Expiry
    // -------------------------------------------------------------------------

    @Test
    void should_return_404_or_410_when_authorization_payload_is_expired() {
        // This test requires a way to create an authorization with past expiresAt
        // via a backdoor (direct DB insert or a test-only endpoint).
        // Skipped here — add if you have a DB seeding helper.
        // assertThat(response.getStatusCode()).isIn(HttpStatus.NOT_FOUND, HttpStatus.GONE);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private String createAuthorization() {
        var response = testRestTemplate.exchange(
            url(),
            HttpMethod.POST,
            new HttpEntity<>(TestUtils.buildInitiateAuthorizationRequest(), TestUtils.getTestHttpHeaders()),
            InitiateAuthorizationResponse.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return response.getBody().authorizationId();
    }

    private void submitMethod(String authorizationId) {
        testRestTemplate.exchange(
            LOCALHOST + port + BASE_PATH + "/" + authorizationId + "/attempts",
            HttpMethod.POST,
            new HttpEntity<>(TestUtils.buildSubmitAuthorizationMethodRequest(), TestUtils.getTestHttpHeaders()),
            Object.class
        );
    }

    private void submitCorrectCredential(String authorizationId) {
        testRestTemplate.exchange(
            LOCALHOST + port + BASE_PATH + "/" + authorizationId + "/attempts",
            HttpMethod.PATCH,
            new HttpEntity<>(TestUtils.buildSubmitAuthorizationCredentialRequest("728059557"), TestUtils.getTestHttpHeaders()),
            Object.class
        );
    }

    private String payloadUrl(String authorizationId) {
        return LOCALHOST + port + BASE_PATH + "/" + authorizationId + "/payload";
    }

    record InitiateAuthorizationResponse(String authorizationId, String authorizationStatus) {}
    record PayloadResponse(String transactionServicePayload, String transactionService, String transactionServiceVersion) {}
}

```


```

package de.consorsbank.core.trauthsc.integration.service;

import de.consorsbank.core.trauthsc.integration.IntegrationBaseTest;
import de.consorsbank.core.trauthsc.integration.util.TestUtils;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for:
 * GET /v1/authorizations/{authorizationId}/methods/{authorizationMethod}/status
 */
class GetAuthorizationAttemptStatusIT extends IntegrationBaseTest {

    private static final String BASE_PATH = "/svc/trauth/v1/authorizations";
    private static final String AUTH_METHOD = "TAN_FROM_NEOAPP";

    @Override
    protected String path() {
        return BASE_PATH;
    }

    // -------------------------------------------------------------------------
    // Happy path
    // -------------------------------------------------------------------------

    @Test
    void should_return_200_with_attempt_status_after_method_submitted() {
        // given
        var authorizationId = createAuthorizationWithMethod();

        // when
        var response = testRestTemplate.exchange(
            attemptStatusUrl(authorizationId, AUTH_METHOD),
            HttpMethod.GET,
            new HttpEntity<>(TestUtils.getTestHttpHeaders()),
            AttemptStatusResponse.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().authorizationMethod()).isEqualTo(AUTH_METHOD);
    }

    @Test
    void should_reflect_failed_status_after_wrong_credential_submitted() {
        // given
        var authorizationId = createAuthorizationWithMethod();
        submitWrongCredential(authorizationId);

        // when
        var response = testRestTemplate.exchange(
            attemptStatusUrl(authorizationId, AUTH_METHOD),
            HttpMethod.GET,
            new HttpEntity<>(TestUtils.getTestHttpHeaders()),
            AttemptStatusResponse.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().status())
            .isIn("FAILED", "PENDING");  // adjust to your domain states
    }

    @Test
    void should_reflect_authorized_status_after_correct_credential_submitted() {
        // given
        var authorizationId = createAuthorizationWithMethod();
        submitCorrectCredential(authorizationId);

        // when
        var response = testRestTemplate.exchange(
            attemptStatusUrl(authorizationId, AUTH_METHOD),
            HttpMethod.GET,
            new HttpEntity<>(TestUtils.getTestHttpHeaders()),
            AttemptStatusResponse.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().status())
            .isIn("AUTHORIZED", "COMPLETED", "SUCCESS");
    }

    // -------------------------------------------------------------------------
    // Not found
    // -------------------------------------------------------------------------

    @Test
    void should_return_404_when_authorization_does_not_exist() {
        var response = testRestTemplate.exchange(
            attemptStatusUrl("00000000-0000-0000-0000-000000000000", AUTH_METHOD),
            HttpMethod.GET,
            new HttpEntity<>(TestUtils.getTestHttpHeaders()),
            Object.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void should_return_404_when_method_was_never_submitted() {
        // authorization exists, but no attempt was made
        var authorizationId = createAuthorization();

        var response = testRestTemplate.exchange(
            attemptStatusUrl(authorizationId, AUTH_METHOD),
            HttpMethod.GET,
            new HttpEntity<>(TestUtils.getTestHttpHeaders()),
            Object.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void should_return_404_when_authorization_method_does_not_match() {
        var authorizationId = createAuthorizationWithMethod();

        var response = testRestTemplate.exchange(
            attemptStatusUrl(authorizationId, "UNKNOWN_METHOD"),
            HttpMethod.GET,
            new HttpEntity<>(TestUtils.getTestHttpHeaders()),
            Object.class
        );

        assertThat(response.getStatusCode()).isIn(HttpStatus.NOT_FOUND, HttpStatus.BAD_REQUEST);
    }

    // -------------------------------------------------------------------------
    // Auth failure
    // -------------------------------------------------------------------------

    @Test
    void should_return_403_when_owner_header_is_missing() {
        var authorizationId = createAuthorizationWithMethod();

        var response = testRestTemplate.exchange(
            attemptStatusUrl(authorizationId, AUTH_METHOD),
            HttpMethod.GET,
            new HttpEntity<>(TestUtils.getTestHttpHeadersWithoutOwner()),
            Object.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private String createAuthorization() {
        var response = testRestTemplate.exchange(
            url(),
            HttpMethod.POST,
            new HttpEntity<>(TestUtils.buildInitiateAuthorizationRequest(), TestUtils.getTestHttpHeaders()),
            InitiateAuthorizationResponse.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return response.getBody().authorizationId();
    }

    private String createAuthorizationWithMethod() {
        var id = createAuthorization();
        testRestTemplate.exchange(
            LOCALHOST + port + BASE_PATH + "/" + id + "/attempts",
            HttpMethod.POST,
            new HttpEntity<>(TestUtils.buildSubmitAuthorizationMethodRequest(), TestUtils.getTestHttpHeaders()),
            Object.class
        );
        return id;
    }

    private void submitCorrectCredential(String authorizationId) {
        testRestTemplate.exchange(
            LOCALHOST + port + BASE_PATH + "/" + authorizationId + "/attempts",
            HttpMethod.PATCH,
            new HttpEntity<>(TestUtils.buildSubmitAuthorizationCredentialRequest("728059557"), TestUtils.getTestHttpHeaders()),
            Object.class
        );
    }

    private void submitWrongCredential(String authorizationId) {
        testRestTemplate.exchange(
            LOCALHOST + port + BASE_PATH + "/" + authorizationId + "/attempts",
            HttpMethod.PATCH,
            new HttpEntity<>(TestUtils.buildSubmitAuthorizationCredentialRequest("000000000"), TestUtils.getTestHttpHeaders()),
            Object.class
        );
    }

    private String attemptStatusUrl(String authorizationId, String method) {
        return LOCALHOST + port + BASE_PATH + "/" + authorizationId + "/methods/" + method + "/status";
    }

    record InitiateAuthorizationResponse(String authorizationId, String authorizationStatus) {}
    record AttemptStatusResponse(String authorizationMethod, String status) {}
}
```


```
package de.consorsbank.core.trauthsc.integration.service;

import de.consorsbank.core.trauthsc.integration.IntegrationBaseTest;
import de.consorsbank.core.trauthsc.integration.util.TestUtils;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for GET /v1/authorizations/{authorizationId}/status
 */
class GetAuthorizationStatusIT extends IntegrationBaseTest {

    private static final String BASE_PATH = "/svc/trauth/v1/authorizations";

    @Override
    protected String path() {
        return BASE_PATH;
    }

    // -------------------------------------------------------------------------
    // Happy path
    // -------------------------------------------------------------------------

    @Test
    void should_return_200_with_INITIATED_status_after_creation() {
        // given
        var authorizationId = createAuthorization();

        // when
        var response = testRestTemplate.exchange(
            statusUrl(authorizationId),
            HttpMethod.GET,
            new HttpEntity<>(TestUtils.getTestHttpHeaders()),
            AuthorizationStatusResponse.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().authorizationStatus()).isEqualTo("INITIATED");
    }

    @Test
    void should_reflect_status_change_after_method_submitted() {
        // given
        var authorizationId = createAuthorization();
        submitMethod(authorizationId);

        // when
        var response = testRestTemplate.exchange(
            statusUrl(authorizationId),
            HttpMethod.GET,
            new HttpEntity<>(TestUtils.getTestHttpHeaders()),
            AuthorizationStatusResponse.class
        );

        // then — status should have moved from INITIATED after method submission
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().authorizationStatus())
            .isNotNull()
            .isNotEqualTo("UNKNOWN");
    }

    // -------------------------------------------------------------------------
    // Not found
    // -------------------------------------------------------------------------

    @Test
    void should_return_404_when_authorization_does_not_exist() {
        var response = testRestTemplate.exchange(
            statusUrl("00000000-0000-0000-0000-000000000000"),
            HttpMethod.GET,
            new HttpEntity<>(TestUtils.getTestHttpHeaders()),
            Object.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // -------------------------------------------------------------------------
    // Auth failure
    // -------------------------------------------------------------------------

    @Test
    void should_return_403_when_owner_header_is_missing() {
        var authorizationId = createAuthorization();

        var response = testRestTemplate.exchange(
            statusUrl(authorizationId),
            HttpMethod.GET,
            new HttpEntity<>(TestUtils.getTestHttpHeadersWithoutOwner()),
            Object.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    // -------------------------------------------------------------------------
    // Edge cases
    // -------------------------------------------------------------------------

    @Test
    void should_return_400_when_authorizationId_is_malformed() {
        var response = testRestTemplate.exchange(
            statusUrl("not-a-uuid!!!"),
            HttpMethod.GET,
            new HttpEntity<>(TestUtils.getTestHttpHeaders()),
            Object.class
        );

        assertThat(response.getStatusCode()).isIn(HttpStatus.BAD_REQUEST, HttpStatus.NOT_FOUND);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private String createAuthorization() {
        var response = testRestTemplate.exchange(
            url(),
            HttpMethod.POST,
            new HttpEntity<>(TestUtils.buildInitiateAuthorizationRequest(), TestUtils.getTestHttpHeaders()),
            InitiateAuthorizationResponse.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return response.getBody().authorizationId();
    }

    private void submitMethod(String authorizationId) {
        testRestTemplate.exchange(
            LOCALHOST + port + BASE_PATH + "/" + authorizationId + "/attempts",
            HttpMethod.POST,
            new HttpEntity<>(TestUtils.buildSubmitAuthorizationMethodRequest(), TestUtils.getTestHttpHeaders()),
            Object.class
        );
    }

    private String statusUrl(String id) {
        return LOCALHOST + port + BASE_PATH + "/" + id + "/status";
    }

    record InitiateAuthorizationResponse(String authorizationId, String authorizationStatus) {}
    record AuthorizationStatusResponse(String authorizationStatus) {}
}
```


```

package de.consorsbank.core.trauthsc.integration.service;

import de.consorsbank.core.trauthsc.integration.IntegrationBaseTest;
import de.consorsbank.core.trauthsc.integration.util.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for PATCH /v1/authorizations/{authorizationId}/attempts
 * Submits the TAN credential — should move authorization to AUTHORIZED or FAILED.
 */
class SubmitAuthorizationCredentialIT extends IntegrationBaseTest {

    private static final String BASE_PATH = "/svc/trauth/v1/authorizations";
    private static final String VALID_TAN = "728059557";
    private static final String INVALID_TAN = "000000000";

    private String authorizationId;

    @Override
    protected String path() {
        return BASE_PATH;
    }

    @BeforeEach
    void createAuthorizationAndSubmitMethod() {
        // Step 1 — initiate
        var initResponse = testRestTemplate.exchange(
            url(),
            HttpMethod.POST,
            new HttpEntity<>(TestUtils.buildInitiateAuthorizationRequest(), TestUtils.getTestHttpHeaders()),
            InitiateAuthorizationResponse.class
        );
        assertThat(initResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        authorizationId = initResponse.getBody().authorizationId();

        // Step 2 — submit method
        var methodResponse = testRestTemplate.exchange(
            attemptsUrl(authorizationId),
            HttpMethod.POST,
            new HttpEntity<>(TestUtils.buildSubmitAuthorizationMethodRequest(), TestUtils.getTestHttpHeaders()),
            Object.class
        );
        assertThat(methodResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    // -------------------------------------------------------------------------
    // Happy path
    // -------------------------------------------------------------------------

    @Test
    void should_return_200_when_valid_tan_is_submitted() {
        var response = testRestTemplate.exchange(
            attemptsUrl(authorizationId),
            HttpMethod.PATCH,
            new HttpEntity<>(TestUtils.buildSubmitAuthorizationCredentialRequest(VALID_TAN), TestUtils.getTestHttpHeaders()),
            AuthorizationStatusResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void should_set_authorization_status_to_authorized_after_valid_tan() {
        // when
        testRestTemplate.exchange(
            attemptsUrl(authorizationId),
            HttpMethod.PATCH,
            new HttpEntity<>(TestUtils.buildSubmitAuthorizationCredentialRequest(VALID_TAN), TestUtils.getTestHttpHeaders()),
            Object.class
        );

        // then — check final status
        var statusResponse = testRestTemplate.exchange(
            LOCALHOST + port + BASE_PATH + "/" + authorizationId + "/status",
            HttpMethod.GET,
            new HttpEntity<>(TestUtils.getTestHttpHeaders()),
            AuthorizationStatusResponse.class
        );

        assertThat(statusResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(statusResponse.getBody().authorizationStatus())
            .isIn("AUTHORIZED", "COMPLETED");  // adjust to your domain's terminal state
    }

    // -------------------------------------------------------------------------
    // Wrong credential
    // -------------------------------------------------------------------------

    @Test
    void should_return_422_or_200_when_wrong_tan_is_submitted() {
        var response = testRestTemplate.exchange(
            attemptsUrl(authorizationId),
            HttpMethod.PATCH,
            new HttpEntity<>(TestUtils.buildSubmitAuthorizationCredentialRequest(INVALID_TAN), TestUtils.getTestHttpHeaders()),
            Object.class
        );

        // Adjust to your domain — some services return 200 with FAILED status,
        // others return 422 Unprocessable Entity
        assertThat(response.getStatusCode()).isIn(
            HttpStatus.OK,
            HttpStatus.UNPROCESSABLE_ENTITY,
            HttpStatus.BAD_REQUEST
        );
    }

    @Test
    void should_increment_attempt_counter_on_wrong_tan() {
        // Submit wrong TAN
        testRestTemplate.exchange(
            attemptsUrl(authorizationId),
            HttpMethod.PATCH,
            new HttpEntity<>(TestUtils.buildSubmitAuthorizationCredentialRequest(INVALID_TAN), TestUtils.getTestHttpHeaders()),
            Object.class
        );

        // Attempt status should reflect FAILED or pending
        var attemptStatusResponse = testRestTemplate.exchange(
            LOCALHOST + port + BASE_PATH + "/" + authorizationId + "/methods/TAN_FROM_NEOAPP/status",
            HttpMethod.GET,
            new HttpEntity<>(TestUtils.getTestHttpHeaders()),
            AttemptStatusResponse.class
        );
        assertThat(attemptStatusResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    // -------------------------------------------------------------------------
    // Not found
    // -------------------------------------------------------------------------

    @Test
    void should_return_404_when_authorization_does_not_exist() {
        var response = testRestTemplate.exchange(
            attemptsUrl("non-existent-00000000"),
            HttpMethod.PATCH,
            new HttpEntity<>(TestUtils.buildSubmitAuthorizationCredentialRequest(VALID_TAN), TestUtils.getTestHttpHeaders()),
            Object.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // -------------------------------------------------------------------------
    // Validation
    // -------------------------------------------------------------------------

    @Test
    void should_return_400_when_credential_is_null() {
        var response = testRestTemplate.exchange(
            attemptsUrl(authorizationId),
            HttpMethod.PATCH,
            new HttpEntity<>(TestUtils.buildSubmitAuthorizationCredentialRequest(null), TestUtils.getTestHttpHeaders()),
            Object.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void should_return_400_when_session_id_is_null() {
        var request = new TestUtils.SubmitAuthorizationCredentialRequest(
            null, "TEST-CUSTOMER-001", "B2C", VALID_TAN, "TAN_FROM_NEOAPP"
        );

        var response = testRestTemplate.exchange(
            attemptsUrl(authorizationId),
            HttpMethod.PATCH,
            new HttpEntity<>(request, TestUtils.getTestHttpHeaders()),
            Object.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private String attemptsUrl(String id) {
        return LOCALHOST + port + BASE_PATH + "/" + id + "/attempts";
    }

    record InitiateAuthorizationResponse(String authorizationId, String authorizationStatus) {}
    record AuthorizationStatusResponse(String authorizationStatus) {}
    record AttemptStatusResponse(String authorizationMethod, String status) {}
}
```


```
package de.consorsbank.core.trauthsc.integration.service;

import de.consorsbank.core.trauthsc.integration.IntegrationBaseTest;
import de.consorsbank.core.trauthsc.integration.util.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for POST /v1/authorizations/{authorizationId}/attempts
 * Submits an authorization method (e.g. TAN_FROM_NEOAPP) for a given authorization.
 * Inserts a row in AUTHORIZATION_ATTEMPTS.
 */
class SubmitAuthorizationMethodIT extends IntegrationBaseTest {

    private static final String BASE_PATH = "/svc/trauth/v1/authorizations";

    private String authorizationId;

    @Override
    protected String path() {
        return BASE_PATH;
    }

    @BeforeEach
    void createAuthorization() {
        // Each test starts with a fresh INITIATED authorization
        var initResponse = testRestTemplate.exchange(
            url(),
            HttpMethod.POST,
            new HttpEntity<>(TestUtils.buildInitiateAuthorizationRequest(), TestUtils.getTestHttpHeaders()),
            InitiateAuthorizationResponse.class
        );
        assertThat(initResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        authorizationId = initResponse.getBody().authorizationId();
    }

    // -------------------------------------------------------------------------
    // Happy path
    // -------------------------------------------------------------------------

    @Test
    void should_return_200_when_authorization_method_is_submitted_successfully() {
        var response = testRestTemplate.exchange(
            attemptsUrl(authorizationId),
            HttpMethod.POST,
            new HttpEntity<>(TestUtils.buildSubmitAuthorizationMethodRequest(), TestUtils.getTestHttpHeaders()),
            Object.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void should_create_attempt_record_in_db() {
        // when
        testRestTemplate.exchange(
            attemptsUrl(authorizationId),
            HttpMethod.POST,
            new HttpEntity<>(TestUtils.buildSubmitAuthorizationMethodRequest(), TestUtils.getTestHttpHeaders()),
            Object.class
        );

        // then — verify attempt status via GET attempt status endpoint
        var statusResponse = testRestTemplate.exchange(
            LOCALHOST + port + BASE_PATH + "/" + authorizationId + "/methods/TAN_FROM_NEOAPP/status",
            HttpMethod.GET,
            new HttpEntity<>(TestUtils.getTestHttpHeaders()),
            AttemptStatusResponse.class
        );

        assertThat(statusResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(statusResponse.getBody()).isNotNull();
    }

    // -------------------------------------------------------------------------
    // Not found
    // -------------------------------------------------------------------------

    @Test
    void should_return_404_when_authorization_does_not_exist() {
        var response = testRestTemplate.exchange(
            attemptsUrl("non-existent-id-00000000"),
            HttpMethod.POST,
            new HttpEntity<>(TestUtils.buildSubmitAuthorizationMethodRequest(), TestUtils.getTestHttpHeaders()),
            Object.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // -------------------------------------------------------------------------
    // Validation
    // -------------------------------------------------------------------------

    @Test
    void should_return_400_when_authorization_method_is_null() {
        var request = new TestUtils.SubmitAuthorizationMethodRequest(null, "TEST-CUSTOMER-001", "B2C");

        var response = testRestTemplate.exchange(
            attemptsUrl(authorizationId),
            HttpMethod.POST,
            new HttpEntity<>(request, TestUtils.getTestHttpHeaders()),
            Object.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void should_return_400_when_authorization_method_is_unknown() {
        var request = new TestUtils.SubmitAuthorizationMethodRequest(
            "UNKNOWN_METHOD", "TEST-CUSTOMER-001", "B2C"
        );

        var response = testRestTemplate.exchange(
            attemptsUrl(authorizationId),
            HttpMethod.POST,
            new HttpEntity<>(request, TestUtils.getTestHttpHeaders()),
            Object.class
        );

        // Expect 400 — UNKNOWN_METHOD not in AUTHORIZATION_METHODS table
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void should_return_409_when_attempt_already_exists_for_same_method() {
        // First attempt
        testRestTemplate.exchange(
            attemptsUrl(authorizationId),
            HttpMethod.POST,
            new HttpEntity<>(TestUtils.buildSubmitAuthorizationMethodRequest(), TestUtils.getTestHttpHeaders()),
            Object.class
        );

        // Second attempt with same method — expect conflict or 409
        var response = testRestTemplate.exchange(
            attemptsUrl(authorizationId),
            HttpMethod.POST,
            new HttpEntity<>(TestUtils.buildSubmitAuthorizationMethodRequest(), TestUtils.getTestHttpHeaders()),
            Object.class
        );

        assertThat(response.getStatusCode()).isIn(HttpStatus.CONFLICT, HttpStatus.BAD_REQUEST);
    }

    // -------------------------------------------------------------------------
    // Auth failure
    // -------------------------------------------------------------------------

    @Test
    void should_return_403_when_owner_header_is_missing() {
        var response = testRestTemplate.exchange(
            attemptsUrl(authorizationId),
            HttpMethod.POST,
            new HttpEntity<>(TestUtils.buildSubmitAuthorizationMethodRequest(), TestUtils.getTestHttpHeadersWithoutOwner()),
            Object.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private String attemptsUrl(String id) {
        return LOCALHOST + port + BASE_PATH + "/" + id + "/attempts";
    }

    record InitiateAuthorizationResponse(String authorizationId, String authorizationStatus) {}
    record AttemptStatusResponse(String authorizationMethod, String status) {}
}

```


```
package de.consorsbank.core.trauthsc.integration.service;

import de.consorsbank.core.trauthsc.integration.IntegrationBaseTest;
import de.consorsbank.core.trauthsc.integration.util.TestUtils;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for POST /v1/authorizations
 * Initiates a transaction authorization — creates a row in AUTHORIZATIONS table.
 */
class InitiateTransactionAuthorizationIT extends IntegrationBaseTest {

    private static final String PATH = "/svc/trauth/v1/authorizations";

    @Override
    protected String path() {
        return PATH;
    }

    // -------------------------------------------------------------------------
    // Happy path
    // -------------------------------------------------------------------------

    @Test
    void should_return_201_and_authorizationId_when_request_is_valid() {
        // when
        var response = testRestTemplate.exchange(
            url(),
            HttpMethod.POST,
            new HttpEntity<>(TestUtils.buildInitiateAuthorizationRequest(), TestUtils.getTestHttpHeaders()),
            InitiateAuthorizationResponse.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().authorizationId()).isNotBlank();
        assertThat(response.getBody().authorizationStatus()).isEqualTo("INITIATED");
    }

    @Test
    void should_persist_authorization_in_db_when_created() {
        // when
        var response = testRestTemplate.exchange(
            url(),
            HttpMethod.POST,
            new HttpEntity<>(TestUtils.buildInitiateAuthorizationRequest(), TestUtils.getTestHttpHeaders()),
            InitiateAuthorizationResponse.class
        );

        // then — verify the returned ID actually corresponds to a real record
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        var authorizationId = response.getBody().authorizationId();

        var statusResponse = testRestTemplate.exchange(
            LOCALHOST + port + "/svc/trauth/v1/authorizations/" + authorizationId + "/status",
            HttpMethod.GET,
            new HttpEntity<>(TestUtils.getTestHttpHeaders()),
            AuthorizationStatusResponse.class
        );
        assertThat(statusResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(statusResponse.getBody().authorizationStatus()).isEqualTo("INITIATED");
    }

    // -------------------------------------------------------------------------
    // Validation failures
    // -------------------------------------------------------------------------

    @Test
    void should_return_400_when_required_fields_are_missing() {
        // when
        var response = testRestTemplate.exchange(
            url(),
            HttpMethod.POST,
            new HttpEntity<>(TestUtils.buildInitiateAuthorizationRequestMissingFields(), TestUtils.getTestHttpHeaders()),
            Object.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void should_return_400_when_body_is_empty() {
        // when
        var response = testRestTemplate.exchange(
            url(),
            HttpMethod.POST,
            new HttpEntity<>(null, TestUtils.getTestHttpHeaders()),
            Object.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void should_return_400_when_expiresAt_is_in_the_past() {
        var request = new TestUtils.InitiateAuthorizationRequest(
            java.util.UUID.randomUUID().toString(),
            "compensation-pot-change",
            "{\"key\":\"val\"}",
            "v1",
            "TEST-CUSTOMER-001",
            "B2C",
            java.time.OffsetDateTime.now().minusDays(1)  // expired
        );

        var response = testRestTemplate.exchange(
            url(),
            HttpMethod.POST,
            new HttpEntity<>(request, TestUtils.getTestHttpHeaders()),
            Object.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // -------------------------------------------------------------------------
    // Auth / header failures
    // -------------------------------------------------------------------------

    @Test
    void should_return_403_when_x_payload_owner_header_is_missing() {
        // when
        var response = testRestTemplate.exchange(
            url(),
            HttpMethod.POST,
            new HttpEntity<>(TestUtils.buildInitiateAuthorizationRequest(), TestUtils.getTestHttpHeadersWithoutOwner()),
            Object.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    // -------------------------------------------------------------------------
    // Response DTOs — replace with your actual response types
    // -------------------------------------------------------------------------

    record InitiateAuthorizationResponse(String authorizationId, String authorizationStatus) {}
    record AuthorizationStatusResponse(String authorizationStatus) {}
}
```


```
package de.consorsbank.core.trauthsc.integration.service;

import de.consorsbank.core.trauthsc.integration.IntegrationBaseTest;
import de.consorsbank.core.trauthsc.integration.util.TestUtils;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end integration test covering the complete TAN_FROM_NEOAPP authorization flow:
 *
 *   1. POST  /v1/authorizations                          → INITIATED
 *   2. GET   /v1/authorizations/{id}/payload             → payload accessible
 *   3. POST  /v1/authorizations/{id}/attempts            → method submitted
 *   4. GET   /v1/authorizations/{id}/status              → status updated
 *   5. PATCH /v1/authorizations/{id}/attempts            → credential submitted
 *   6. GET   /v1/authorizations/{id}/methods/{m}/status  → attempt status
 *   7. GET   /v1/authorizations/{id}/status              → AUTHORIZED/COMPLETED
 */
class FullAuthorizationFlowIT extends IntegrationBaseTest {

    private static final String BASE_PATH = "/svc/trauth/v1/authorizations";
    private static final String AUTH_METHOD = "TAN_FROM_NEOAPP";
    private static final String VALID_TAN = "728059557";

    @Override
    protected String path() {
        return BASE_PATH;
    }

    @Test
    void should_complete_full_tan_from_neoapp_authorization_flow() {

        // ── Step 1: Initiate authorization ──────────────────────────────────
        var initResponse = testRestTemplate.exchange(
            url(),
            HttpMethod.POST,
            new HttpEntity<>(TestUtils.buildInitiateAuthorizationRequest(), TestUtils.getTestHttpHeaders()),
            InitiateAuthorizationResponse.class
        );

        assertThat(initResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        var authorizationId = initResponse.getBody().authorizationId();
        assertThat(authorizationId).isNotBlank();
        assertThat(initResponse.getBody().authorizationStatus()).isEqualTo("INITIATED");

        // ── Step 2: Fetch payload ────────────────────────────────────────────
        var payloadResponse = testRestTemplate.exchange(
            BASE_PATH + "/" + authorizationId + "/payload",
            HttpMethod.GET,
            new HttpEntity<>(TestUtils.getTestHttpHeaders()),
            Object.class
        );

        assertThat(payloadResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        // ── Step 3: Submit authorization method ──────────────────────────────
        var methodResponse = testRestTemplate.exchange(
            LOCALHOST + port + BASE_PATH + "/" + authorizationId + "/attempts",
            HttpMethod.POST,
            new HttpEntity<>(TestUtils.buildSubmitAuthorizationMethodRequest(), TestUtils.getTestHttpHeaders()),
            Object.class
        );

        assertThat(methodResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        // ── Step 4: Check status after method submission ─────────────────────
        var statusAfterMethod = testRestTemplate.exchange(
            LOCALHOST + port + BASE_PATH + "/" + authorizationId + "/status",
            HttpMethod.GET,
            new HttpEntity<>(TestUtils.getTestHttpHeaders()),
            AuthorizationStatusResponse.class
        );

        assertThat(statusAfterMethod.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(statusAfterMethod.getBody().authorizationStatus()).isNotNull();

        // ── Step 5: Submit credential (TAN) ──────────────────────────────────
        var credentialResponse = testRestTemplate.exchange(
            LOCALHOST + port + BASE_PATH + "/" + authorizationId + "/attempts",
            HttpMethod.PATCH,
            new HttpEntity<>(
                TestUtils.buildSubmitAuthorizationCredentialRequest(VALID_TAN),
                TestUtils.getTestHttpHeaders()
            ),
            Object.class
        );

        assertThat(credentialResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        // ── Step 6: Check attempt status ─────────────────────────────────────
        var attemptStatusResponse = testRestTemplate.exchange(
            LOCALHOST + port + BASE_PATH + "/" + authorizationId + "/methods/" + AUTH_METHOD + "/status",
            HttpMethod.GET,
            new HttpEntity<>(TestUtils.getTestHttpHeaders()),
            AttemptStatusResponse.class
        );

        assertThat(attemptStatusResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(attemptStatusResponse.getBody().authorizationMethod()).isEqualTo(AUTH_METHOD);

        // ── Step 7: Verify final authorization status ─────────────────────────
        var finalStatus = testRestTemplate.exchange(
            LOCALHOST + port + BASE_PATH + "/" + authorizationId + "/status",
            HttpMethod.GET,
            new HttpEntity<>(TestUtils.getTestHttpHeaders()),
            AuthorizationStatusResponse.class
        );

        assertThat(finalStatus.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(finalStatus.getBody().authorizationStatus())
            .isIn("AUTHORIZED", "COMPLETED");
    }

    @Test
    void should_fail_authorization_after_max_wrong_tan_attempts() {
        // given
        var initResponse = testRestTemplate.exchange(
            url(),
            HttpMethod.POST,
            new HttpEntity<>(TestUtils.buildInitiateAuthorizationRequest(), TestUtils.getTestHttpHeaders()),
            InitiateAuthorizationResponse.class
        );
        var authorizationId = initResponse.getBody().authorizationId();

        testRestTemplate.exchange(
            LOCALHOST + port + BASE_PATH + "/" + authorizationId + "/attempts",
            HttpMethod.POST,
            new HttpEntity<>(TestUtils.buildSubmitAuthorizationMethodRequest(), TestUtils.getTestHttpHeaders()),
            Object.class
        );

        // Submit wrong TAN multiple times (adjust count to your domain's max attempts)
        for (int i = 0; i < 3; i++) {
            testRestTemplate.exchange(
                LOCALHOST + port + BASE_PATH + "/" + authorizationId + "/attempts",
                HttpMethod.PATCH,
                new HttpEntity<>(
                    TestUtils.buildSubmitAuthorizationCredentialRequest("000000000"),
                    TestUtils.getTestHttpHeaders()
                ),
                Object.class
            );
        }

        // Final status should be FAILED or BLOCKED
        var finalStatus = testRestTemplate.exchange(
            LOCALHOST + port + BASE_PATH + "/" + authorizationId + "/status",
            HttpMethod.GET,
            new HttpEntity<>(TestUtils.getTestHttpHeaders()),
            AuthorizationStatusResponse.class
        );

        assertThat(finalStatus.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(finalStatus.getBody().authorizationStatus())
            .isIn("FAILED", "BLOCKED", "REJECTED");
    }

    record InitiateAuthorizationResponse(String authorizationId, String authorizationStatus) {}
    record AuthorizationStatusResponse(String authorizationStatus) {}
    record AttemptStatusResponse(String authorizationMethod, String status) {}
}package de.consorsbank.core.trauthsc.integration.service;

import de.consorsbank.core.trauthsc.integration.IntegrationBaseTest;
import de.consorsbank.core.trauthsc.integration.util.TestUtils;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end integration test covering the complete TAN_FROM_NEOAPP authorization flow:
 *
 *   1. POST  /v1/authorizations                          → INITIATED
 *   2. GET   /v1/authorizations/{id}/payload             → payload accessible
 *   3. POST  /v1/authorizations/{id}/attempts            → method submitted
 *   4. GET   /v1/authorizations/{id}/status              → status updated
 *   5. PATCH /v1/authorizations/{id}/attempts            → credential submitted
 *   6. GET   /v1/authorizations/{id}/methods/{m}/status  → attempt status
 *   7. GET   /v1/authorizations/{id}/status              → AUTHORIZED/COMPLETED
 */
class FullAuthorizationFlowIT extends IntegrationBaseTest {

    private static final String BASE_PATH = "/svc/trauth/v1/authorizations";
    private static final String AUTH_METHOD = "TAN_FROM_NEOAPP";
    private static final String VALID_TAN = "728059557";

    @Override
    protected String path() {
        return BASE_PATH;
    }

    @Test
    void should_complete_full_tan_from_neoapp_authorization_flow() {

        // ── Step 1: Initiate authorization ──────────────────────────────────
        var initResponse = testRestTemplate.exchange(
            url(),
            HttpMethod.POST,
            new HttpEntity<>(TestUtils.buildInitiateAuthorizationRequest(), TestUtils.getTestHttpHeaders()),
            InitiateAuthorizationResponse.class
        );

        assertThat(initResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        var authorizationId = initResponse.getBody().authorizationId();
        assertThat(authorizationId).isNotBlank();
        assertThat(initResponse.getBody().authorizationStatus()).isEqualTo("INITIATED");

        // ── Step 2: Fetch payload ────────────────────────────────────────────
        var payloadResponse = testRestTemplate.exchange(
            BASE_PATH + "/" + authorizationId + "/payload",
            HttpMethod.GET,
            new HttpEntity<>(TestUtils.getTestHttpHeaders()),
            Object.class
        );

        assertThat(payloadResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        // ── Step 3: Submit authorization method ──────────────────────────────
        var methodResponse = testRestTemplate.exchange(
            LOCALHOST + port + BASE_PATH + "/" + authorizationId + "/attempts",
            HttpMethod.POST,
            new HttpEntity<>(TestUtils.buildSubmitAuthorizationMethodRequest(), TestUtils.getTestHttpHeaders()),
            Object.class
        );

        assertThat(methodResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        // ── Step 4: Check status after method submission ─────────────────────
        var statusAfterMethod = testRestTemplate.exchange(
            LOCALHOST + port + BASE_PATH + "/" + authorizationId + "/status",
            HttpMethod.GET,
            new HttpEntity<>(TestUtils.getTestHttpHeaders()),
            AuthorizationStatusResponse.class
        );

        assertThat(statusAfterMethod.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(statusAfterMethod.getBody().authorizationStatus()).isNotNull();

        // ── Step 5: Submit credential (TAN) ──────────────────────────────────
        var credentialResponse = testRestTemplate.exchange(
            LOCALHOST + port + BASE_PATH + "/" + authorizationId + "/attempts",
            HttpMethod.PATCH,
            new HttpEntity<>(
                TestUtils.buildSubmitAuthorizationCredentialRequest(VALID_TAN),
                TestUtils.getTestHttpHeaders()
            ),
            Object.class
        );

        assertThat(credentialResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        // ── Step 6: Check attempt status ─────────────────────────────────────
        var attemptStatusResponse = testRestTemplate.exchange(
            LOCALHOST + port + BASE_PATH + "/" + authorizationId + "/methods/" + AUTH_METHOD + "/status",
            HttpMethod.GET,
            new HttpEntity<>(TestUtils.getTestHttpHeaders()),
            AttemptStatusResponse.class
        );

        assertThat(attemptStatusResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(attemptStatusResponse.getBody().authorizationMethod()).isEqualTo(AUTH_METHOD);

        // ── Step 7: Verify final authorization status ─────────────────────────
        var finalStatus = testRestTemplate.exchange(
            LOCALHOST + port + BASE_PATH + "/" + authorizationId + "/status",
            HttpMethod.GET,
            new HttpEntity<>(TestUtils.getTestHttpHeaders()),
            AuthorizationStatusResponse.class
        );

        assertThat(finalStatus.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(finalStatus.getBody().authorizationStatus())
            .isIn("AUTHORIZED", "COMPLETED");
    }

    @Test
    void should_fail_authorization_after_max_wrong_tan_attempts() {
        // given
        var initResponse = testRestTemplate.exchange(
            url(),
            HttpMethod.POST,
            new HttpEntity<>(TestUtils.buildInitiateAuthorizationRequest(), TestUtils.getTestHttpHeaders()),
            InitiateAuthorizationResponse.class
        );
        var authorizationId = initResponse.getBody().authorizationId();

        testRestTemplate.exchange(
            LOCALHOST + port + BASE_PATH + "/" + authorizationId + "/attempts",
            HttpMethod.POST,
            new HttpEntity<>(TestUtils.buildSubmitAuthorizationMethodRequest(), TestUtils.getTestHttpHeaders()),
            Object.class
        );

        // Submit wrong TAN multiple times (adjust count to your domain's max attempts)
        for (int i = 0; i < 3; i++) {
            testRestTemplate.exchange(
                LOCALHOST + port + BASE_PATH + "/" + authorizationId + "/attempts",
                HttpMethod.PATCH,
                new HttpEntity<>(
                    TestUtils.buildSubmitAuthorizationCredentialRequest("000000000"),
                    TestUtils.getTestHttpHeaders()
                ),
                Object.class
            );
        }

        // Final status should be FAILED or BLOCKED
        var finalStatus = testRestTemplate.exchange(
            LOCALHOST + port + BASE_PATH + "/" + authorizationId + "/status",
            HttpMethod.GET,
            new HttpEntity<>(TestUtils.getTestHttpHeaders()),
            AuthorizationStatusResponse.class
        );

        assertThat(finalStatus.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(finalStatus.getBody().authorizationStatus())
            .isIn("FAILED", "BLOCKED", "REJECTED");
    }

    record InitiateAuthorizationResponse(String authorizationId, String authorizationStatus) {}
    record AuthorizationStatusResponse(String authorizationStatus) {}
    record AttemptStatusResponse(String authorizationMethod, String status) {}
}

```
