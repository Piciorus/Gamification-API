```
package de.consorsbank.core.trauthsc.integration.util;

public class AuthorizationUrls {

    private static final String BASE_PATH = "/svc/trauth/v1/authorizations";

    private final int port;

    public AuthorizationUrls(int port) {
        this.port = port;
    }

    public String baseUrl() {
        return localhost() + BASE_PATH;
    }

    public String attemptsUrl(String authorizationId) {
        return localhost() + BASE_PATH + "/" + authorizationId + "/attempts";
    }

    public String statusUrl(String authorizationId) {
        return localhost() + BASE_PATH + "/" + authorizationId + "/status?detailed=false";
    }

    public String detailedStatusUrl(String authorizationId) {
        return localhost() + BASE_PATH + "/" + authorizationId + "/status?detailed=true";
    }

    public String attemptStatusUrl(String authorizationId, String method) {
        return localhost() + BASE_PATH + "/" + authorizationId + "/methods/" + method + "/status";
    }

    public String payloadUrl(String authorizationId) {
        return localhost() + BASE_PATH + "/" + authorizationId + "/payload";
    }

    private String localhost() {
        return "http://localhost:" + port;
    }
}
```


```
package de.consorsbank.core.trauthsc.integration.util;

import de.consorsbank.core.trauthsc.rest.api.tam.initiate.transaction.authorization.model.InitiateTransactionAuthorizationResponse;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.CREATED;

public class AuthorizationTestSteps {

    private final TestRestTemplate testRestTemplate;
    private final AuthorizationUrls urls;

    public AuthorizationTestSteps(TestRestTemplate testRestTemplate, int port) {
        this.testRestTemplate = testRestTemplate;
        this.urls = new AuthorizationUrls(port);
    }

    public String createAuthorization() {
        var response = testRestTemplate.exchange(
            urls.baseUrl(),
            HttpMethod.POST,
            new HttpEntity<>(
                TestUtils.buildInitiateAuthorizationRequest(),
                TestUtils.getTestHttpHeaders()
            ),
            InitiateTransactionAuthorizationResponse.class
        );
        assertThat(response.getStatusCode()).isEqualTo(CREATED);
        return response.getBody().getAuthorizationId().toString();
    }

    public void submitMethod(String authorizationId) {
        testRestTemplate.exchange(
            urls.attemptsUrl(authorizationId),
            HttpMethod.POST,
            new HttpEntity<>(
                TestUtils.buildSubmitAuthorizationMethodRequest(),
                TestUtils.getTestHttpHeaders()
            ),
            Object.class
        );
    }

    public String createAuthorizationWithMethod() {
        var id = createAuthorization();
        submitMethod(id);
        return id;
    }

    public void submitValidCredential(String authorizationId) {
        testRestTemplate.exchange(
            urls.attemptsUrl(authorizationId),
            HttpMethod.PATCH,
            new HttpEntity<>(
                TestUtils.buildSubmitAuthorizationCredentialRequest(TestUtils.VALID_TAN),
                TestUtils.getTestHttpHeaders()
            ),
            Object.class
        );
    }

    public void submitInvalidCredential(String authorizationId) {
        testRestTemplate.exchange(
            urls.attemptsUrl(authorizationId),
            HttpMethod.PATCH,
            new HttpEntity<>(
                TestUtils.buildSubmitAuthorizationCredentialRequest(TestUtils.INVALID_TAN),
                TestUtils.getTestHttpHeaders()
            ),
            Object.class
        );
    }
}
```


```
// Before
private AuthorizationTestSteps steps;

// After
private AuthorizationTestSteps steps;
private AuthorizationUrls urls;  // if tests need direct URL access

@BeforeEach
void setUp() {
    steps = new AuthorizationTestSteps(testRestTemplate, port);
    urls = new AuthorizationUrls(port);  // optional, only if needed directly
}
```
