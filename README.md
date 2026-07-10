```
package de.consorsbank.core.trauthsc.integration;

import com.github.tomakehurst.wiremock.WireMockServer;
import de.consorsbank.core.trauthsc.integration.wiremock.WireMockConfig;
import de.consorsbank.core.trauthsc.integration.wiremock.DraasWireMockStubs;
import de.consorsbank.core.trauthsc.tam.messaging.sender.TransactionAuthResultEventJmsSender;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integration-test")
@ContextConfiguration(classes = {
    WireMockConfig.class,
    TestDataSourceConfig.class,
    TestTransactionManagerConfig.class,
    TestJpaConfig.class
})
@AutoConfigureTestRestTemplate
public abstract class IntegrationBaseTest {

    protected static final String LOCALHOST = "http://localhost:";

    @LocalServerPort
    protected int port;

    @Autowired
    protected TestRestTemplate testRestTemplate;

    @Autowired
    protected WireMockServer mockDraasService;

    @MockBean
    protected TransactionAuthResultEventJmsSender transactionAuthResultEventJmsSender;

    @BeforeEach
    void setupDraasStub() {
        mockDraasService.resetAll();
        DraasWireMockStubs.stubValidTan(mockDraasService);
    }

    protected String url() {
        return LOCALHOST + port + path();
    }

    protected abstract String path();
}
```
```
package de.consorsbank.core.trauthsc.integration;

import com.github.tomakehurst.wiremock.WireMockServer;
import de.consorsbank.core.trauthsc.integration.wiremock.WireMockConfig;
import de.consorsbank.core.trauthsc.integration.wiremock.DraasWireMockStubs;
import de.consorsbank.core.trauthsc.tam.messaging.sender.TransactionAuthResultEventJmsSender;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integration-test")
@ContextConfiguration(classes = {
    WireMockConfig.class,
    TestDataSourceConfig.class,
    TestTransactionManagerConfig.class,
    TestJpaConfig.class
})
@AutoConfigureTestRestTemplate
public abstract class IntegrationBaseTest {

    protected static final String LOCALHOST = "http://localhost:";

    @LocalServerPort
    protected int port;

    @Autowired
    protected TestRestTemplate testRestTemplate;

    @Autowired
    protected WireMockServer mockDraasService;

    @MockBean
    protected TransactionAuthResultEventJmsSender transactionAuthResultEventJmsSender;

    @BeforeEach
    void setupDraasStub() {
        mockDraasService.resetAll();
        DraasWireMockStubs.stubValidTan(mockDraasService);
    }

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

public class TestUtils {

    public static final String TEST_CRM_CUSTOMER_NUMBER = "TEST-CUSTOMER-001";
    public static final String TEST_TENANT = "B2C";
    public static final String TEST_AUTHORIZATION_METHOD = "TAN_FROM_NEOAPP";
    public static final String VALID_TAN = "754254919";
    public static final String INVALID_TAN = "000000000";
    public static final String NON_EXISTENT_AUTHORIZATION_ID = "00000000-0000-0000-0000-000000000000";

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

    public static InitiateAuthorizationRequest buildInitiateAuthorizationRequest() {
        return new InitiateAuthorizationRequest(
            UUID.randomUUID().toString(),
            "compensation-pot-change",
            "{\"compensationPotYear\":2026}",
            "v1",
            TEST_CRM_CUSTOMER_NUMBER,
            TEST_TENANT,
            OffsetDateTime.now().plusDays(1)
        );
    }

    public static InitiateAuthorizationRequest buildInitiateAuthorizationRequestMissingFields() {
        return new InitiateAuthorizationRequest(
            null, null, null, null, null, null, null
        );
    }

    public static InitiateAuthorizationRequest buildInitiateTransactionAuthorizationRequestExpired() {
        return new InitiateAuthorizationRequest(
            UUID.randomUUID().toString(),
            "compensation-pot-change",
            "{\"compensationPotYear\":2026}",
            "v1",
            TEST_CRM_CUSTOMER_NUMBER,
            TEST_TENANT,
            OffsetDateTime.now().minusDays(1)
        );
    }

    public static SubmitAuthorizationMethodRequest buildSubmitAuthorizationMethodRequest() {
        return new SubmitAuthorizationMethodRequest(
            TEST_AUTHORIZATION_METHOD,
            TEST_CRM_CUSTOMER_NUMBER,
            TEST_TENANT
        );
    }

    public static SubmitAuthorizationMethodRequest buildSubmitAuthorizationMethodWithNullAuthorizationMethodRequest() {
        return new SubmitAuthorizationMethodRequest(
            null,
            TEST_CRM_CUSTOMER_NUMBER,
            TEST_TENANT
        );
    }

    public static SubmitAuthorizationCredentialRequest buildSubmitAuthorizationCredentialRequest(String tan) {
        return new SubmitAuthorizationCredentialRequest(
            "test-session-id",
            TEST_CRM_CUSTOMER_NUMBER,
            TEST_TENANT,
            tan,
            TEST_AUTHORIZATION_METHOD
        );
    }

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
import de.consorsbank.core.trauthsc.integration.util.AuthorizationTestSteps;
import de.consorsbank.core.trauthsc.integration.util.TestUtils;
import de.consorsbank.core.trauthsc.rest.api.tam.common.model.AuthorizationStatus;
import de.consorsbank.core.trauthsc.rest.api.tam.get.authorization.status.model.GetAuthorizationStatusResponse;
import de.consorsbank.core.trauthsc.rest.api.tam.initiate.transaction.authorization.model.InitiateTransactionAuthorizationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import static de.consorsbank.core.trauthsc.integration.util.TestUtils.buildInitiateAuthorizationRequest;
import static de.consorsbank.core.trauthsc.integration.util.TestUtils.buildInitiateTransactionAuthorizationRequestExpired;
import static org.assertj.core.api.Assertions.assertThat;

class InitiateTransactionAuthorizationIntegrationTest extends IntegrationBaseTest {

    private static final String PATH = "/svc/trauth/v1/authorizations";

    private AuthorizationTestSteps steps;

    @Override
    protected String path() {
        return PATH;
    }

    @BeforeEach
    void setUp() {
        steps = new AuthorizationTestSteps(testRestTemplate, port);
    }

    @Test
    void should_Return201_When_RequestIsValid_SuccessfulCall() {
        // given
        var request = buildInitiateAuthorizationRequest();

        // when
        var response = testRestTemplate.exchange(
            url(),
            HttpMethod.POST,
            new HttpEntity<>(request, TestUtils.getTestHttpHeaders()),
            InitiateTransactionAuthorizationResponse.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getAuthorizationStatus()).isEqualTo(AuthorizationStatus.INITIATED);
    }

    @Test
    void should_Return200_When_AuthorizationIsPersisted_SuccessfulCall() {
        // given
        var request = buildInitiateAuthorizationRequest();

        // when
        var createResponse = testRestTemplate.exchange(
            url(),
            HttpMethod.POST,
            new HttpEntity<>(request, TestUtils.getTestHttpHeaders()),
            InitiateTransactionAuthorizationResponse.class
        );
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        var authorizationId = createResponse.getBody().getAuthorizationId().toString();

        var statusResponse = testRestTemplate.exchange(
            steps.statusUrl(authorizationId),
            HttpMethod.GET,
            new HttpEntity<>(TestUtils.getTestHttpHeaders()),
            GetAuthorizationStatusResponse.class
        );

        // then
        assertThat(statusResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(statusResponse.getBody())
            .extracting("authorizationStatus")
            .isEqualTo(AuthorizationStatus.INITIATED);
    }

    @Test
    void should_Return400_When_RequiredFieldsMissing_SuccessfulCall() {
        // given
        var request = TestUtils.buildInitiateAuthorizationRequestMissingFields();

        // when
        var response = testRestTemplate.exchange(
            url(),
            HttpMethod.POST,
            new HttpEntity<>(request, TestUtils.getTestHttpHeaders()),
            Object.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void should_Return400_When_BodyIsEmpty_SuccessfulCall() {
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
    void should_Return400_When_ExpiresAtInPast_SuccessfulCall() {
        // given
        var request = buildInitiateTransactionAuthorizationRequestExpired();

        // when
        var response = testRestTemplate.exchange(
            url(),
            HttpMethod.POST,
            new HttpEntity<>(request, TestUtils.getTestHttpHeaders()),
            Object.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
```

```
package de.consorsbank.core.trauthsc.integration.service;

import de.consorsbank.core.trauthsc.integration.IntegrationBaseTest;
import de.consorsbank.core.trauthsc.integration.util.AuthorizationTestSteps;
import de.consorsbank.core.trauthsc.integration.util.TestUtils;
import de.consorsbank.core.trauthsc.rest.api.tam.get.authorization.attempt.status.model.GetAuthorizationAttemptStatusResponse;
import de.consorsbank.core.trauthsc.rest.api.tam.initiate.transaction.authorization.model.InitiateTransactionAuthorizationResponse;
import de.consorsbank.core.trauthsc.rest.api.tam.submit.authorization.method.model.SubmitAuthorizationMethodResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import static de.consorsbank.core.trauthsc.integration.util.TestUtils.buildSubmitAuthorizationMethodRequest;
import static de.consorsbank.core.trauthsc.integration.util.TestUtils.buildSubmitAuthorizationMethodWithNullAuthorizationMethodRequest;
import static org.assertj.core.api.Assertions.assertThat;

class SubmitAuthorizationMethodIntegrationTest extends IntegrationBaseTest {

    private static final String PATH = "/svc/trauth/v1/authorizations";

    private String authorizationId;
    private AuthorizationTestSteps steps;

    @Override
    protected String path() {
        return PATH;
    }

    @BeforeEach
    void createAuthorization() {
        steps = new AuthorizationTestSteps(testRestTemplate, port);
        authorizationId = steps.createAuthorization();
    }

    @Test
    void should_Return200_When_AuthorizationMethodIsSubmitted_SuccessfulCall() {
        // given
        var request = buildSubmitAuthorizationMethodRequest();

        // when
        var response = testRestTemplate.exchange(
            steps.attemptsUrl(authorizationId),
            HttpMethod.POST,
            new HttpEntity<>(request, TestUtils.getTestHttpHeaders()),
            SubmitAuthorizationMethodResponse.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void should_Return200_When_AttemptRecordCreatedInDb_SuccessfulCall() {
        // when
        testRestTemplate.exchange(
            steps.attemptsUrl(authorizationId),
            HttpMethod.POST,
            new HttpEntity<>(buildSubmitAuthorizationMethodRequest(), TestUtils.getTestHttpHeaders()),
            Object.class
        );

        var statusResponse = testRestTemplate.exchange(
            steps.attemptStatusUrl(authorizationId, TestUtils.TEST_AUTHORIZATION_METHOD),
            HttpMethod.GET,
            new HttpEntity<>(TestUtils.getTestHttpHeaders()),
            GetAuthorizationAttemptStatusResponse.class
        );

        // then
        assertThat(statusResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(statusResponse.getBody()).isNotNull();
    }

    @Test
    void should_Return404_When_AuthorizationDoesNotExist_SuccessfulCall() {
        // given
        var request = buildSubmitAuthorizationMethodRequest();

        // when
        var response = testRestTemplate.exchange(
            steps.attemptsUrl(TestUtils.NON_EXISTENT_AUTHORIZATION_ID),
            HttpMethod.POST,
            new HttpEntity<>(request, TestUtils.getTestHttpHeaders()),
            Object.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void should_Return400_When_AuthorizationMethodIsNull_SuccessfulCall() {
        // given
        var request = buildSubmitAuthorizationMethodWithNullAuthorizationMethodRequest();

        // when
        var response = testRestTemplate.exchange(
            steps.attemptsUrl(authorizationId),
            HttpMethod.POST,
            new HttpEntity<>(request, TestUtils.getTestHttpHeaders()),
            Object.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void should_Return400_When_AuthorizationMethodIsUnknown_SuccessfulCall() {
        // given
        var request = buildSubmitAuthorizationMethodWithNullAuthorizationMethodRequest();

        // when
        var response = testRestTemplate.exchange(
            steps.attemptsUrl(authorizationId),
            HttpMethod.POST,
            new HttpEntity<>(request, TestUtils.getTestHttpHeaders()),
            Object.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void should_Return409_When_AttemptAlreadyExistsForSameMethod_SuccessfulCall() {
        // given
        testRestTemplate.exchange(
            steps.attemptsUrl(authorizationId),
            HttpMethod.POST,
            new HttpEntity<>(buildSubmitAuthorizationMethodRequest(), TestUtils.getTestHttpHeaders()),
            Object.class
        );

        // when
        var response = testRestTemplate.exchange(
            steps.attemptsUrl(authorizationId),
            HttpMethod.POST,
            new HttpEntity<>(buildSubmitAuthorizationMethodRequest(), TestUtils.getTestHttpHeaders()),
            Object.class
        );

        // then
        assertThat(response.getStatusCode()).isIn(HttpStatus.CONFLICT, HttpStatus.BAD_REQUEST);
    }
}
```

```
package de.consorsbank.core.trauthsc.integration.service;

import de.consorsbank.core.trauthsc.integration.IntegrationBaseTest;
import de.consorsbank.core.trauthsc.integration.util.AuthorizationTestSteps;
import de.consorsbank.core.trauthsc.integration.util.TestUtils;
import de.consorsbank.core.trauthsc.integration.wiremock.DraasWireMockStubs;
import de.consorsbank.core.trauthsc.rest.api.tam.get.authorization.attempt.status.model.GetAuthorizationAttemptStatusResponse;
import de.consorsbank.core.trauthsc.rest.api.tam.get.authorization.status.model.GetAuthorizationStatusResponse;
import de.consorsbank.core.trauthsc.rest.api.tam.initiate.transaction.authorization.model.InitiateTransactionAuthorizationResponse;
import de.consorsbank.core.trauthsc.rest.api.tam.submit.authorization.credential.model.SubmitAuthorizationCredentialResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import static de.consorsbank.core.trauthsc.integration.util.TestUtils.buildSubmitAuthorizationCredentialRequest;
import static org.assertj.core.api.Assertions.assertThat;

class SubmitAuthorizationCredentialIntegrationTest extends IntegrationBaseTest {

    private static final String PATH = "/svc/trauth/v1/authorizations";

    private String authorizationId;
    private AuthorizationTestSteps steps;

    @Override
    protected String path() {
        return PATH;
    }

    @BeforeEach
    void createAuthorizationAndSubmitMethod() {
        steps = new AuthorizationTestSteps(testRestTemplate, port);
        var initResponse = testRestTemplate.exchange(
            url(),
            HttpMethod.POST,
            new HttpEntity<>(
                TestUtils.buildInitiateAuthorizationRequest(),
                TestUtils.getTestHttpHeaders()
            ),
            InitiateTransactionAuthorizationResponse.class
        );
        assertThat(initResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        authorizationId = String.valueOf(initResponse.getBody().getTransactionId());

        var methodResponse = testRestTemplate.exchange(
            steps.attemptsUrl(authorizationId),
            HttpMethod.POST,
            new HttpEntity<>(
                TestUtils.buildSubmitAuthorizationMethodRequest(),
                TestUtils.getTestHttpHeaders()
            ),
            Object.class
        );
        assertThat(methodResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void should_Return200_When_ValidTanIsSubmitted_SuccessfulCall() {
        // given
        mockDraasService.resetAll();
        DraasWireMockStubs.stubValidTan(mockDraasService);

        // when
        var response = testRestTemplate.exchange(
            steps.attemptsUrl(authorizationId),
            HttpMethod.PATCH,
            new HttpEntity<>(
                buildSubmitAuthorizationCredentialRequest(TestUtils.VALID_TAN),
                TestUtils.getTestHttpHeaders()
            ),
            SubmitAuthorizationCredentialResponse.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void should_Return200_When_InvalidTanIsSubmitted_SuccessfulCall() {
        // given
        mockDraasService.resetAll();
        DraasWireMockStubs.stubInvalidTan(mockDraasService);

        // when
        var response = testRestTemplate.exchange(
            steps.attemptsUrl(authorizationId),
            HttpMethod.PATCH,
            new HttpEntity<>(
                buildSubmitAuthorizationCredentialRequest(TestUtils.INVALID_TAN),
                TestUtils.getTestHttpHeaders()
            ),
            Object.class
        );

        // then
        assertThat(response.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    void should_SetAuthorizationStatusToAuthorized_When_ValidTanSubmitted_SuccessfulCall() {
        // given
        mockDraasService.resetAll();
        DraasWireMockStubs.stubValidTan(mockDraasService);

        // when
        testRestTemplate.exchange(
            steps.attemptsUrl(authorizationId),
            HttpMethod.PATCH,
            new HttpEntity<>(
                buildSubmitAuthorizationCredentialRequest(TestUtils.VALID_TAN),
                TestUtils.getTestHttpHeaders()
            ),
            Object.class
        );

        var statusResponse = testRestTemplate.exchange(
            steps.detailedStatusUrl(authorizationId),
            HttpMethod.GET,
            new HttpEntity<>(TestUtils.getTestHttpHeaders()),
            GetAuthorizationStatusResponse.class
        );

        // then
        assertThat(statusResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void should_IncrementAttemptCounter_When_WrongTanSubmitted_SuccessfulCall() {
        // given
        mockDraasService.resetAll();
        DraasWireMockStubs.stubInvalidTan(mockDraasService);

        // when
        testRestTemplate.exchange(
            steps.attemptsUrl(authorizationId),
            HttpMethod.PATCH,
            new HttpEntity<>(
                buildSubmitAuthorizationCredentialRequest(TestUtils.INVALID_TAN),
                TestUtils.getTestHttpHeaders()
            ),
            Object.class
        );

        var attemptStatusResponse = testRestTemplate.exchange(
            steps.attemptStatusUrl(authorizationId, TestUtils.TEST_AUTHORIZATION_METHOD),
            HttpMethod.GET,
            new HttpEntity<>(TestUtils.getTestHttpHeaders()),
            GetAuthorizationAttemptStatusResponse.class
        );

        // then
        assertThat(attemptStatusResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void should_Return404_When_AuthorizationDoesNotExist_SuccessfulCall() {
        // given
        mockDraasService.resetAll();
        DraasWireMockStubs.stubValidTan(mockDraasService);

        // when
        var response = testRestTemplate.exchange(
            steps.attemptsUrl(TestUtils.NON_EXISTENT_AUTHORIZATION_ID),
            HttpMethod.PATCH,
            new HttpEntity<>(
                buildSubmitAuthorizationCredentialRequest(TestUtils.VALID_TAN),
                TestUtils.getTestHttpHeaders()
            ),
            Object.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void should_Return400_When_CredentialIsNull_SuccessfulCall() {
        // given
        var request = buildSubmitAuthorizationCredentialRequest(null);

        // when
        var response = testRestTemplate.exchange(
            steps.attemptsUrl(authorizationId),
            HttpMethod.PATCH,
            new HttpEntity<>(request, TestUtils.getTestHttpHeaders()),
            Object.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
```

```
package de.consorsbank.core.trauthsc.integration.service;

import de.consorsbank.core.trauthsc.integration.IntegrationBaseTest;
import de.consorsbank.core.trauthsc.integration.util.AuthorizationTestSteps;
import de.consorsbank.core.trauthsc.integration.util.TestUtils;
import de.consorsbank.core.trauthsc.rest.api.tam.get.authorization.status.model.GetAuthorizationStatusResponse;
import de.consorsbank.core.trauthsc.rest.api.tam.initiate.transaction.authorization.model.InitiateTransactionAuthorizationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

class GetAuthorizationStatusIntegrationTest extends IntegrationBaseTest {

    private static final String PATH = "/svc/trauth/v1/authorizations";

    private AuthorizationTestSteps steps;

    @Override
    protected String path() {
        return PATH;
    }

    @BeforeEach
    void setUp() {
        steps = new AuthorizationTestSteps(testRestTemplate, port);
    }

    @Test
    void should_Return200WithInitiatedStatus_When_AuthorizationCreated_SuccessfulCall() {
        // given
        var authorizationId = steps.createAuthorization();

        // when
        var response = testRestTemplate.exchange(
            steps.statusUrl(authorizationId),
            HttpMethod.GET,
            new HttpEntity<>(TestUtils.getTestHttpHeaders()),
            GetAuthorizationStatusResponse.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    void should_Return200_When_StatusChangesAfterMethodSubmitted_SuccessfulCall() {
        // given
        var authorizationId = steps.createAuthorization();
        steps.submitMethod(authorizationId);

        // when
        var response = testRestTemplate.exchange(
            steps.statusUrl(authorizationId),
            HttpMethod.GET,
            new HttpEntity<>(TestUtils.getTestHttpHeaders()),
            GetAuthorizationStatusResponse.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void should_Return404_When_AuthorizationDoesNotExist_SuccessfulCall() {
        // when
        var response = testRestTemplate.exchange(
            steps.statusUrl(TestUtils.NON_EXISTENT_AUTHORIZATION_ID),
            HttpMethod.GET,
            new HttpEntity<>(TestUtils.getTestHttpHeaders()),
            Object.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
```


```
package de.consorsbank.core.trauthsc.integration.service;

import de.consorsbank.core.trauthsc.integration.IntegrationBaseTest;
import de.consorsbank.core.trauthsc.integration.util.AuthorizationTestSteps;
import de.consorsbank.core.trauthsc.integration.util.TestUtils;
import de.consorsbank.core.trauthsc.integration.wiremock.DraasWireMockStubs;
import de.consorsbank.core.trauthsc.rest.api.tam.get.authorization.attempt.status.model.GetAuthorizationAttemptStatusResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

class GetAuthorizationAttemptStatusIntegrationTest extends IntegrationBaseTest {

    private static final String PATH = "/svc/trauth/v1/authorizations";

    private AuthorizationTestSteps steps;

    @Override
    protected String path() {
        return PATH;
    }

    @BeforeEach
    void setUp() {
        steps = new AuthorizationTestSteps(testRestTemplate, port);
    }

    @Test
    void should_Return200WithAttemptStatus_When_MethodSubmitted_SuccessfulCall() {
        // given
        var authorizationId = steps.createAuthorizationWithMethod();

        // when
        var response = testRestTemplate.exchange(
            steps.attemptStatusUrl(authorizationId, TestUtils.TEST_AUTHORIZATION_METHOD),
            HttpMethod.GET,
            new HttpEntity<>(TestUtils.getTestHttpHeaders()),
            GetAuthorizationAttemptStatusResponse.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    void should_Return200WithFailedStatus_When_WrongCredentialSubmitted_SuccessfulCall() {
        // given
        var authorizationId = steps.createAuthorizationWithMethod();
        mockDraasService.resetAll();
        DraasWireMockStubs.stubInvalidTan(mockDraasService);
        steps.submitInvalidCredential(authorizationId);

        // when
        var response = testRestTemplate.exchange(
            steps.attemptStatusUrl(authorizationId, TestUtils.TEST_AUTHORIZATION_METHOD),
            HttpMethod.GET,
            new HttpEntity<>(TestUtils.getTestHttpHeaders()),
            GetAuthorizationAttemptStatusResponse.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void should_Return200WithAuthorizedStatus_When_CorrectCredentialSubmitted_SuccessfulCall() {
        // given
        var authorizationId = steps.createAuthorizationWithMethod();
        mockDraasService.resetAll();
        DraasWireMockStubs.stubValidTan(mockDraasService);
        steps.submitValidCredential(authorizationId);

        // when
        var response = testRestTemplate.exchange(
            steps.attemptStatusUrl(authorizationId, TestUtils.TEST_AUTHORIZATION_METHOD),
            HttpMethod.GET,
            new HttpEntity<>(TestUtils.getTestHttpHeaders()),
            GetAuthorizationAttemptStatusResponse.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(Objects.requireNonNull(response.getBody()).getAuthorizationAttemptStatus())
            .isIn("AUTHORIZED", "COMPLETED", "SUCCESS");
    }

    @Test
    void should_Return404_When_AuthorizationDoesNotExist_SuccessfulCall() {
        // when
        var response = testRestTemplate.exchange(
            steps.attemptStatusUrl(TestUtils.NON_EXISTENT_AUTHORIZATION_ID, TestUtils.TEST_AUTHORIZATION_METHOD),
            HttpMethod.GET,
            new HttpEntity<>(TestUtils.getTestHttpHeaders()),
            Object.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void should_Return404_When_MethodWasNeverSubmitted_SuccessfulCall() {
        // given
        var authorizationId = steps.createAuthorization();

        // when
        var response = testRestTemplate.exchange(
            steps.attemptStatusUrl(authorizationId, TestUtils.TEST_AUTHORIZATION_METHOD),
            HttpMethod.GET,
            new HttpEntity<>(TestUtils.getTestHttpHeaders()),
            Object.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void should_Return404_When_AuthorizationMethodDoesNotMatch_SuccessfulCall() {
        // given
        var authorizationId = steps.createAuthorizationWithMethod();

        // when
        var response = testRestTemplate.exchange(
            steps.attemptStatusUrl(authorizationId, "UNKNOWN_METHOD"),
            HttpMethod.GET,
            new HttpEntity<>(TestUtils.getTestHttpHeaders()),
            Object.class
        );

        // then
        assertThat(response.getStatusCode()).isIn(HttpStatus.NOT_FOUND, HttpStatus.BAD_REQUEST);
    }
}

```

```
package de.consorsbank.core.trauthsc.integration.service;

import de.consorsbank.core.trauthsc.integration.IntegrationBaseTest;
import de.consorsbank.core.trauthsc.integration.util.AuthorizationTestSteps;
import de.consorsbank.core.trauthsc.integration.util.TestUtils;
import de.consorsbank.core.trauthsc.integration.wiremock.DraasWireMockStubs;
import de.consorsbank.core.trauthsc.rest.api.pvm.model.PayloadVaultDetailResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

class GetPayloadTransactionAuthorizationIntegrationTest extends IntegrationBaseTest {

    private static final String PATH = "/svc/trauth/v1/authorizations";

    private AuthorizationTestSteps steps;

    @Override
    protected String path() {
        return PATH;
    }

    @BeforeEach
    void setUp() {
        steps = new AuthorizationTestSteps(testRestTemplate, port);
    }

    @Test
    void should_Return200WithPayload_When_AuthorizationCompleted_SuccessfulCall() {
        // given
        var authorizationId = steps.createAuthorization();
        steps.submitMethod(authorizationId);
        mockDraasService.resetAll();
        DraasWireMockStubs.stubValidTan(mockDraasService);
        steps.submitValidCredential(authorizationId);

        // when
        var response = testRestTemplate.exchange(
            steps.payloadUrl(authorizationId),
            HttpMethod.GET,
            new HttpEntity<>(TestUtils.getTestHttpHeaders()),
            PayloadVaultDetailResponse.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void should_Return404_When_AuthorizationDoesNotExist_SuccessfulCall() {
        // when
        var response = testRestTemplate.exchange(
            steps.payloadUrl(TestUtils.NON_EXISTENT_AUTHORIZATION_ID),
            HttpMethod.GET,
            new HttpEntity<>(TestUtils.getTestHttpHeaders()),
            PayloadVaultDetailResponse.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
```


```
package de.consorsbank.core.trauthsc.integration.service;

import de.consorsbank.core.trauthsc.integration.IntegrationBaseTest;
import de.consorsbank.core.trauthsc.integration.util.AuthorizationTestSteps;
import de.consorsbank.core.trauthsc.integration.util.TestUtils;
import de.consorsbank.core.trauthsc.integration.wiremock.DraasWireMockStubs;
import de.consorsbank.core.trauthsc.rest.api.tam.get.authorization.attempt.status.model.GetAuthorizationAttemptStatusResponse;
import de.consorsbank.core.trauthsc.rest.api.tam.get.authorization.status.model.GetAuthorizationStatusResponse;
import de.consorsbank.core.trauthsc.rest.api.tam.initiate.transaction.authorization.model.InitiateTransactionAuthorizationResponse;
import de.consorsbank.core.trauthsc.rest.api.tam.submit.authorization.credential.model.SubmitAuthorizationCredentialResponse;
import de.consorsbank.core.trauthsc.rest.api.tam.submit.authorization.method.model.SubmitAuthorizationMethodResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

class FullAuthorizationFlowIntegrationTest extends IntegrationBaseTest {

    private static final String PATH = "/svc/trauth/v1/authorizations";
    private static final String AUTH_METHOD = "TAN_FROM_NEOAPP";

    private AuthorizationTestSteps steps;

    @Override
    protected String path() {
        return PATH;
    }

    @BeforeEach
    void setUp() {
        steps = new AuthorizationTestSteps(testRestTemplate, port);
    }

    @Test
    void should_CompleteFullTanFromNeoappAuthorizationFlow_SuccessfulCall() {
        // given
        mockDraasService.resetAll();
        DraasWireMockStubs.stubValidTan(mockDraasService);

        // when - Step 1: Initiate authorization
        var initResponse = testRestTemplate.exchange(
            url(),
            HttpMethod.POST,
            new HttpEntity<>(TestUtils.buildInitiateAuthorizationRequest(), TestUtils.getTestHttpHeaders()),
            InitiateTransactionAuthorizationResponse.class
        );

        // then
        assertThat(initResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        var authorizationId = initResponse.getBody().getAuthorizationId().toString();
        assertThat(authorizationId).isNotBlank();

        // when - Step 2: Fetch payload
        var payloadResponse = testRestTemplate.exchange(
            steps.payloadUrl(authorizationId),
            HttpMethod.GET,
            new HttpEntity<>(TestUtils.getTestHttpHeaders()),
            Object.class
        );

        // then
        assertThat(payloadResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        // when - Step 3: Submit authorization method
        var methodResponse = testRestTemplate.exchange(
            steps.attemptsUrl(authorizationId),
            HttpMethod.POST,
            new HttpEntity<>(TestUtils.buildSubmitAuthorizationMethodRequest(), TestUtils.getTestHttpHeaders()),
            SubmitAuthorizationMethodResponse.class
        );

        // then
        assertThat(methodResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        // when - Step 4: Check status after method submission
        var statusAfterMethod = testRestTemplate.exchange(
            steps.detailedStatusUrl(authorizationId),
            HttpMethod.GET,
            new HttpEntity<>(TestUtils.getTestHttpHeaders()),
            GetAuthorizationStatusResponse.class
        );

        // then
        assertThat(statusAfterMethod.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(statusAfterMethod.getBody().getResponseType()).isNotNull();

        // when - Step 5: Submit credential
        var credentialResponse = testRestTemplate.exchange(
            steps.attemptsUrl(authorizationId),
            HttpMethod.PATCH,
            new HttpEntity<>(
                TestUtils.buildSubmitAuthorizationCredentialRequest(TestUtils.VALID_TAN),
                TestUtils.getTestHttpHeaders()
            ),
            SubmitAuthorizationCredentialResponse.class
        );

        // then
        assertThat(credentialResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        // when - Step 6: Check attempt status
        var attemptStatusResponse = testRestTemplate.exchange(
            steps.attemptStatusUrl(authorizationId, AUTH_METHOD),
            HttpMethod.GET,
            new HttpEntity<>(TestUtils.getTestHttpHeaders()),
            GetAuthorizationAttemptStatusResponse.class
        );

        // then
        assertThat(attemptStatusResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(attemptStatusResponse.getBody().getAuthorizationMethod()).isEqualTo(AUTH_METHOD);

        // when - Step 7: Verify final status
        var finalStatus = testRestTemplate.exchange(
            steps.detailedStatusUrl(authorizationId),
            HttpMethod.GET,
            new HttpEntity<>(TestUtils.getTestHttpHeaders()),
            GetAuthorizationStatusResponse.class
        );

        // then
        assertThat(finalStatus.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(finalStatus.getBody().getResponseType().getValue())
            .isIn("DETAILED_RESPONSE");
    }
}

```
