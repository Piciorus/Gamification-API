```
package de.consorsbank.core.trauthsc.integration.wiremock;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class WireMockConfig {

    @Bean(initMethod = "start", destroyMethod = "stop")
    public WireMockServer mockAuthorizationService() {
        return new WireMockServer(9680);
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public WireMockServer mockDraasService() {
        return new WireMockServer(9681); // separate port for Draas
    }
}
```
```
spring:
  cloud:
    openfeign:
      client:
        config:
          draas-client:
            url: http://localhost:9681/rest/api  # ← WireMock port
```

```
package de.consorsbank.core.trauthsc.integration.wiremock;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class DraasWireMockStubs {

    public static void stubValidTan(WireMockServer server) {
        server.stubFor(
            post(urlPathMatching("/neosec/.*/v1/secureplus/tan/validation"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("""
                        {
                            "valid": true,
                            "sessionId": "test-session-id"
                        }
                    """))
        );
    }

    public static void stubInvalidTan(WireMockServer server) {
        server.stubFor(
            post(urlPathMatching("/neosec/.*/v1/secureplus/tan/validation"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("""
                        {
                            "valid": false,
                            "sessionId": null
                        }
                    """))
        );
    }
}
```


```
@Autowired
protected WireMockServer mockDraasService;

@BeforeEach
void setupDraasStub() {
    mockDraasService.resetAll();
    DraasWireMockStubs.stubValidTan(mockDraasService); // default — valid TAN
}
```
```
@Test
void should_return_422_or_200_when_wrong_tan_is_submitted() {
    // Override default stub with invalid TAN response
    mockDraasService.resetAll();
    DraasWireMockStubs.stubInvalidTan(mockDraasService);

    var response = testRestTemplate.exchange(
        attemptsUrl(authorizationId),
        HttpMethod.PATCH,
        new HttpEntity<>(
            TestUtils.buildSubmitAuthorizationCredentialRequest("000000000"),
            TestUtils.getTestHttpHeaders()
        ),
        Object.class
    );

    assertThat(response.getStatusCode()).isIn(
        HttpStatus.OK,
        HttpStatus.UNPROCESSABLE_ENTITY
    );
}
```
