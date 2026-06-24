
package de.consorsbank.core.trauthsc.authorizationengine.service;

import de.consorsbank.core.trauthsc.authorizationengine.dto.AuthorizationMethodEnum;
import de.consorsbank.core.trauthsc.authorizationengine.dto.AuthorizationRequest;
import de.consorsbank.core.trauthsc.authorizationengine.dto.AuthorizationResponse;
import de.consorsbank.core.trauthsc.authorizationengine.dto.PreliminaryAuthorizationRequest;
import de.consorsbank.core.trauthsc.authorizationengine.dto.PreliminaryAuthorizationResponse;
import de.consorsbank.core.trauthsc.authorizationengine.mapper.base.AuthorizationMapper;
import de.consorsbank.core.trauthsc.authorizationengine.mapper.base.MultiStepAuthorizationMapper;
import de.consorsbank.core.trauthsc.authorizationengine.provider.base.AuthorizationBaseRequest;
import de.consorsbank.core.trauthsc.authorizationengine.provider.base.AuthorizationBaseResponse;
import de.consorsbank.core.trauthsc.authorizationengine.provider.base.AuthorizationInitBaseRequest;
import de.consorsbank.core.trauthsc.authorizationengine.provider.base.AuthorizationInitBaseResponse;
import de.consorsbank.core.trauthsc.authorizationengine.provider.base.AuthorizationProvider;
import de.consorsbank.core.trauthsc.authorizationengine.provider.base.MultiStepAuthorizationProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.lang.reflect.Field;
import java.util.EnumMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AuthorizationEngineServiceImpl}.
 *
 * <p>NOTE ON RECONSTRUCTION: this class, the {@code AuthorizationProvider} /
 * {@code AuthorizationMapper} interfaces, and {@code MultiStepAuthorizationProvider} were
 * reconstructed from photos of an IDE screen, not from the actual source file. The
 * non-multi-step path ({@code submitAuthorization}, {@code getAuthorizationProvider}) is backed
 * by clearly-photographed source and should be reliable. The multi-step path
 * ({@code preliminaryAuthorizationSubmission}) relies on a {@code MultiStepAuthorizationMapper}
 * interface whose declaration was never fully visible — only its 4-type-parameter usage. If your
 * actual interface differs (different method names/generics), the multi-step tests below will
 * need adjusting; everything else should compile and pass as-is.
 *
 * <p>Dependencies assumed on the test classpath: JUnit 5, Mockito (mockito-core /
 * mockito-junit-jupiter), AssertJ.
 */
@DisplayName("AuthorizationEngineServiceImpl")
class AuthorizationEngineServiceImplTest {

    private AuthorizationProvider<AuthorizationBaseRequest, AuthorizationBaseResponse> simpleProvider;
    private AuthorizationMapper<AuthorizationBaseRequest, AuthorizationBaseResponse> simpleMapper;

    private MultiStepAuthorizationProvider<AuthorizationBaseRequest, AuthorizationBaseResponse,
            AuthorizationInitBaseRequest, AuthorizationInitBaseResponse> multiStepProvider;
    private MultiStepAuthorizationMapper<AuthorizationBaseRequest, AuthorizationBaseResponse,
            AuthorizationInitBaseRequest, AuthorizationInitBaseResponse> multiStepMapper;

    private Map<AuthorizationMethodEnum, AuthorizationProvider<?, ?>> providerMap;

    private AuthorizationEngineServiceImpl sut;

    @BeforeEach
    void setUp() {
        simpleProvider = mock(AuthorizationProvider.class);
        simpleMapper = mock(AuthorizationMapper.class);

        multiStepProvider = mock(MultiStepAuthorizationProvider.class);
        multiStepMapper = mock(MultiStepAuthorizationMapper.class);

        providerMap = new EnumMap<>(AuthorizationMethodEnum.class);

        sut = new AuthorizationEngineServiceImpl(providerMap);
    }

    // ------------------------------------------------------------------
    // shouldPerformPreliminaryAuthorization
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("shouldPerformPreliminaryAuthorization")
    class ShouldPerformPreliminaryAuthorization {

        @Test
        @DisplayName("returns true when the resolved provider is a MultiStepAuthorizationProvider")
        void returnsTrue_whenProviderIsMultiStep() {
            providerMap.put(AuthorizationMethodEnum.QRCODE_FROM_GENERATOR, multiStepProvider);

            boolean result = sut.shouldPerformPreliminaryAuthorization(AuthorizationMethodEnum.QRCODE_FROM_GENERATOR);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("returns false when the resolved provider is a plain AuthorizationProvider")
        void returnsFalse_whenProviderIsNotMultiStep() {
            providerMap.put(AuthorizationMethodEnum.GENERIC_TAN, simpleProvider);

            boolean result = sut.shouldPerformPreliminaryAuthorization(AuthorizationMethodEnum.GENERIC_TAN);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("throws when no provider is registered for the method")
        void throws_whenNoProviderRegistered() {
            assertThatThrownBy(() ->
                    sut.shouldPerformPreliminaryAuthorization(AuthorizationMethodEnum.TAN_FROM_NEOAPP))
                    .isInstanceOf(NullPointerException.class); // adjust if impl null-checks differently
        }
    }

    // ------------------------------------------------------------------
    // shouldSubmitAuthorizationEarlier
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("shouldSubmitAuthorizationEarlier")
    class ShouldSubmitAuthorizationEarlier {

        @Test
        @DisplayName("returns true only for PUSH_NOTIFICATION_FORM_NEO_APP")
        void returnsTrue_forPushNotificationFormNeoApp() {
            boolean result = sut.shouldSubmitAuthorizationEarlier(
                    AuthorizationMethodEnum.PUSH_NOTIFICATION_FORM_NEO_APP);

            assertThat(result).isTrue();
        }

        @ParameterizedTest(name = "returns false for {0}")
        @EnumSource(value = AuthorizationMethodEnum.class, names = "PUSH_NOTIFICATION_FORM_NEO_APP", mode = EnumSource.Mode.EXCLUDE)
        @DisplayName("returns false for every other enum value")
        void returnsFalse_forAllOtherMethods(AuthorizationMethodEnum methodEnum) {
            boolean result = sut.shouldSubmitAuthorizationEarlier(methodEnum);

            assertThat(result).isFalse();
        }
    }

    // ------------------------------------------------------------------
    // submitAuthorization
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("submitAuthorization")
    class SubmitAuthorization {

        @Test
        @DisplayName("resolves provider + mapper, maps request, authorizes, maps response")
        void happyPath_delegatesToProviderAndMapper() {
            AuthorizationRequest request = mock(AuthorizationRequest.class);
            when(request.authorizationMethodEnum()).thenReturn(AuthorizationMethodEnum.GENERIC_TAN);
            providerMap.put(AuthorizationMethodEnum.GENERIC_TAN, simpleProvider);

            AuthorizationBaseRequest baseRequest = mock(AuthorizationBaseRequest.class);
            AuthorizationBaseResponse baseResponse = mock(AuthorizationBaseResponse.class);
            AuthorizationResponse expectedResponse = mock(AuthorizationResponse.class);

            when(simpleProvider.getAuthorizationMapper()).thenReturn((AuthorizationMapper) simpleMapper);
            when(simpleMapper.authorizationRequestToReq(request)).thenReturn(baseRequest);
            when(simpleProvider.authorize(baseRequest)).thenReturn(baseResponse);
            when(simpleMapper.respToAuthorizationResponse(baseResponse)).thenReturn(expectedResponse);

            AuthorizationResponse actual = sut.submitAuthorization(request);

            assertThat(actual).isSameAs(expectedResponse);

            verify(simpleMapper).authorizationRequestToReq(request);
            verify(simpleProvider).authorize(baseRequest);
            verify(simpleMapper).respToAuthorizationResponse(baseResponse);
        }

        @Test
        @DisplayName("looks up the provider using the request's authorizationMethodEnum")
        void looksUpProvider_usingRequestMethodEnum() {
            AuthorizationRequest request = mock(AuthorizationRequest.class);
            when(request.authorizationMethodEnum()).thenReturn(AuthorizationMethodEnum.NEO_SECURE_SIGNATURE_BOUND);
            providerMap.put(AuthorizationMethodEnum.NEO_SECURE_SIGNATURE_BOUND, simpleProvider);

            when(simpleProvider.getAuthorizationMapper()).thenReturn((AuthorizationMapper) simpleMapper);
            when(simpleMapper.authorizationRequestToReq(any())).thenReturn(mock(AuthorizationBaseRequest.class));
            when(simpleProvider.authorize(any())).thenReturn(mock(AuthorizationBaseResponse.class));
            when(simpleMapper.respToAuthorizationResponse(any())).thenReturn(mock(AuthorizationResponse.class));

            sut.submitAuthorization(request);

            verify(request).authorizationMethodEnum();
            verify(simpleProvider).authorize(any());
        }

        @Test
        @DisplayName("throws when no provider is registered for the request's method")
        void throws_whenProviderNotRegistered() {
            AuthorizationRequest request = mock(AuthorizationRequest.class);
            when(request.authorizationMethodEnum()).thenReturn(AuthorizationMethodEnum.TAN_FROM_GENERATOR);
            // providerMap intentionally left empty for this method

            assertThatThrownBy(() -> sut.submitAuthorization(request))
                    .isInstanceOf(NullPointerException.class); // adjust if impl null-checks differently
        }

        @Test
        @DisplayName("does not call the provider/mapper more than once per invocation")
        void doesNotOverInvokeCollaborators() {
            AuthorizationRequest request = mock(AuthorizationRequest.class);
            when(request.authorizationMethodEnum()).thenReturn(AuthorizationMethodEnum.GENERIC_TAN);
            providerMap.put(AuthorizationMethodEnum.GENERIC_TAN, simpleProvider);

            when(simpleProvider.getAuthorizationMapper()).thenReturn((AuthorizationMapper) simpleMapper);
            when(simpleMapper.authorizationRequestToReq(any())).thenReturn(mock(AuthorizationBaseRequest.class));
            when(simpleProvider.authorize(any())).thenReturn(mock(AuthorizationBaseResponse.class));
            when(simpleMapper.respToAuthorizationResponse(any())).thenReturn(mock(AuthorizationResponse.class));

            sut.submitAuthorization(request);

            verify(simpleProvider).getAuthorizationMapper();
            verify(simpleMapper).authorizationRequestToReq(request);
            verify(simpleProvider).authorize(any());
            verify(simpleMapper).respToAuthorizationResponse(any());
            verifyNoMoreInteractions(simpleProvider, simpleMapper);
        }
    }

    // ------------------------------------------------------------------
    // preliminaryAuthorizationSubmission
    // (multi-step path — see class-level NOTE ON RECONSTRUCTION)
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("preliminaryAuthorizationSubmission")
    class PreliminaryAuthorizationSubmission {

        @Test
        @DisplayName("maps preliminary request to init request, initiates, maps init response back")
        void happyPath_delegatesToMultiStepProviderAndMapper() {
            PreliminaryAuthorizationRequest request = mock(PreliminaryAuthorizationRequest.class);
            when(request.authorizationMethodEnum()).thenReturn(AuthorizationMethodEnum.QRCODE_FROM_GENERATOR);
            providerMap.put(AuthorizationMethodEnum.QRCODE_FROM_GENERATOR, multiStepProvider);

            AuthorizationInitBaseRequest initRequest = mock(AuthorizationInitBaseRequest.class);
            AuthorizationInitBaseResponse initResponse = mock(AuthorizationInitBaseResponse.class);
            PreliminaryAuthorizationResponse expectedResponse = mock(PreliminaryAuthorizationResponse.class);

            when(multiStepProvider.getAuthorizationMapper()).thenReturn((AuthorizationMapper) multiStepMapper);
            when(multiStepMapper.preliminaryAuthorizationRequestToInitReq(request)).thenReturn(initRequest);
            when(multiStepProvider.initiate(initRequest)).thenReturn(initResponse);
            when(multiStepMapper.initRespToPreliminaryAuthorizationResponse(initResponse)).thenReturn(expectedResponse);

            PreliminaryAuthorizationResponse actual = sut.preliminaryAuthorizationSubmission(request);

            assertThat(actual).isSameAs(expectedResponse);

            verify(multiStepMapper).preliminaryAuthorizationRequestToInitReq(request);
            verify(multiStepProvider).initiate(initRequest);
            verify(multiStepMapper).initRespToPreliminaryAuthorizationResponse(initResponse);
        }

        @Test
        @DisplayName("throws (ClassCastException) when the resolved provider is not multi-step")
        void throws_whenResolvedProviderIsNotMultiStep() {
            PreliminaryAuthorizationRequest request = mock(PreliminaryAuthorizationRequest.class);
            when(request.authorizationMethodEnum()).thenReturn(AuthorizationMethodEnum.GENERIC_TAN);
            providerMap.put(AuthorizationMethodEnum.GENERIC_TAN, simpleProvider);

            assertThatThrownBy(() -> sut.preliminaryAuthorizationSubmission(request))
                    .isInstanceOf(ClassCastException.class);
        }

        @Test
        @DisplayName("throws when no provider is registered for the request's method")
        void throws_whenProviderNotRegistered() {
            PreliminaryAuthorizationRequest request = mock(PreliminaryAuthorizationRequest.class);
            when(request.authorizationMethodEnum()).thenReturn(AuthorizationMethodEnum.TAN_FROM_NEOAPP);

            assertThatThrownBy(() -> sut.preliminaryAuthorizationSubmission(request))
                    .isInstanceOf(Exception.class); // NPE or CCE depending on exact cast order in impl
        }
    }

    // ------------------------------------------------------------------
    // Helper: only needed if AuthorizationEngineServiceImpl has no
    // package-visible/constructor-injected access to authorizationProviderMap
    // in your real source. If @RequiredArgsConstructor (Lombok) generates the
    // constructor as seen in the screenshots, the constructor call in setUp()
    // above is sufficient and this reflection helper is unused/removable.
    // ------------------------------------------------------------------
    private static void setProviderMapViaReflection(AuthorizationEngineServiceImpl target,
                                                      Map<AuthorizationMethodEnum, AuthorizationProvider<?, ?>> map)
            throws NoSuchFieldException, IllegalAccessException {
        Field field = AuthorizationEngineServiceImpl.class.getDeclaredField("authorizationProviderMap");
        field.setAccessible(true);
        field.set(target, map);
    }
}


```
package de.consorsbank.core.trauthsc.common.actuator;

import org.springframework.boot.health.contributor.AbstractHealthIndicator;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.Status;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
public class KobilHealthCheckIndicator extends AbstractHealthIndicator {

    private static final int TIMEOUT_SECONDS = 3;
    private static final int CONNECT_TIMEOUT_MILLIS = 2000;
    private static final String KOBIL = "kobil";
    private static final String HOST = "host";
    private static final String PORT = "port";

    private final String host;
    private final int port;

    public KobilHealthCheckIndicator(FeignClientUrlProperties feignClientUrlProperties) {
        super("Kobil health check failed");
        URI uri = URI.create(feignClientUrlProperties.transauthKobilScClient().url());
        this.host = uri.getHost();
        this.port = uri.getPort() != -1 ? uri.getPort() : defaultPortFor(uri.getScheme());
    }

    private static int defaultPortFor(String scheme) {
        return "https".equalsIgnoreCase(scheme) ? 443 : 80;
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) {
        var future = checkTcp();
        boolean up = getResult(future);

        builder.status(up ? Status.UP : Status.DOWN)
                .withDetail(KOBIL, up ? "reachable" : "unreachable")
                .withDetail(HOST, host)
                .withDetail(PORT, port);
    }

    private CompletableFuture<Boolean> checkTcp() {
        return CompletableFuture.supplyAsync(() -> {
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress(host, port), CONNECT_TIMEOUT_MILLIS);
                return true;
            } catch (IOException e) {
                return false;
            }
        });
    }

    private boolean getResult(CompletableFuture<Boolean> future) {
        try {
            return future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            future.cancel(true);
            return false;
        } catch (ExecutionException | CancellationException e) {
            return false;
        }
    }
}
```


```
package de.consorsbank.core.trauthsc.common.actuator;

import org.springframework.boot.health.contributor.AbstractHealthIndicator;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.Status;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
public class DraasHealthCheckIndicator extends AbstractHealthIndicator {

    private static final int TIMEOUT_SECONDS = 3;
    private static final int CONNECT_TIMEOUT_MILLIS = 2000;
    private static final String DRAAS = "draas";
    private static final String HOST = "host";
    private static final String PORT = "port";

    private final String host;
    private final int port;

    public DraasHealthCheckIndicator(FeignClientUrlProperties feignClientUrlProperties) {
        super("Draas health check failed");
        URI uri = URI.create(feignClientUrlProperties.draasClient().url());
        this.host = uri.getHost();
        this.port = uri.getPort() != -1 ? uri.getPort() : defaultPortFor(uri.getScheme());
    }

    private static int defaultPortFor(String scheme) {
        return "https".equalsIgnoreCase(scheme) ? 443 : 80;
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) {
        var future = checkTcp();
        boolean up = getResult(future);

        builder.status(up ? Status.UP : Status.DOWN)
                .withDetail(DRAAS, up ? "reachable" : "unreachable")
                .withDetail(HOST, host)
                .withDetail(PORT, port);
    }

    private CompletableFuture<Boolean> checkTcp() {
        return CompletableFuture.supplyAsync(() -> {
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress(host, port), CONNECT_TIMEOUT_MILLIS);
                return true;
            } catch (IOException e) {
                return false;
            }
        });
    }

    private boolean getResult(CompletableFuture<Boolean> future) {
        try {
            return future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            future.cancel(true);
            return false;
        } catch (ExecutionException | CancellationException e) {
            return false;
        }
    }
}
```

```
package de.consorsbank.core.trauthsc.common.actuator;

import org.springframework.boot.health.contributor.AbstractHealthIndicator;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.Status;
import org.springframework.stereotype.Component;

import jakarta.jms.Connection;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.CancellationException;

@Component
public class ArtemisHealthCheckIndicator extends AbstractHealthIndicator {

    private static final int TIMEOUT = 3;
    private static final String BROKER = "broker";

    private final ConnectionFactory connectionFactory;

    public ArtemisHealthCheckIndicator(ConnectionFactory connectionFactory) {
        super("Artemis health check failed");
        this.connectionFactory = connectionFactory;
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) {
        var future = checkBroker();
        boolean up = getResult(future);

        builder.status(up ? Status.UP : Status.DOWN)
                .withDetail(BROKER, up ? "reachable" : "unreachable");
    }

    private CompletableFuture<Boolean> checkBroker() {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = connectionFactory.createConnection()) {
                connection.start();
                return true;
            } catch (JMSException e) {
                return false;
            }
        });
    }

    private boolean getResult(CompletableFuture<Boolean> future) {
        try {
            return future.get(TIMEOUT, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            future.cancel(true);
            return false;
        } catch (ExecutionException | CancellationException e) {
            return false;
        }
    }
}
```


```
package de.consorsbank.core.trauthsc.common.actuator;

import org.springframework.boot.health.contributor.AbstractHealthIndicator;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.Status;
import org.springframework.stereotype.Component;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultHealth;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.CancellationException;

@Component
public class VaultHealthCheckIndicator extends AbstractHealthIndicator {

    private static final int TIMEOUT = 3;
    private static final String VAULT = "vault";
    private static final String SEALED = "sealed";
    private static final String INITIALIZED = "initialized";

    private final VaultTemplate vaultTemplate;

    public VaultHealthCheckIndicator(VaultTemplate vaultLocalTemplate) {
        super("Vault health check failed");
        this.vaultTemplate = vaultLocalTemplate;
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) {
        var future = checkVault();
        VaultHealth health = getResult(future);

        boolean up = health != null && health.isInitialized() && !health.isSealed();

        builder.status(up ? Status.UP : Status.DOWN)
                .withDetail(VAULT, "transit");

        if (health != null) {
            builder.withDetail(SEALED, health.isSealed())
                   .withDetail(INITIALIZED, health.isInitialized());
        }
    }

    private CompletableFuture<VaultHealth> checkVault() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return vaultTemplate.opsForSys().health();
            } catch (Exception e) {
                return null;
            }
        });
    }

    private VaultHealth getResult(CompletableFuture<VaultHealth> future) {
        try {
            return future.get(TIMEOUT, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            return null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            future.cancel(true);
            return null;
        } catch (ExecutionException | CancellationException e) {
            return null;
        }
    }
}

```


```
package de.consorsbank.core.trauthsc.common.actuator;

import org.springframework.boot.health.contributor.AbstractHealthIndicator;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.Status;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.CancellationException;

@Component
public class KobilHealthCheckIndicator extends AbstractHealthIndicator {

    private static final int TIMEOUT = 3;
    private static final String KOBIL = "kobil";

    private final RestClient restClient;
    private final String healthUrl;

    public KobilHealthCheckIndicator(RestClient.Builder restClientBuilder,
                                      HealthCheckProperties healthCheckProperties) {
        super("Kobil health check failed");
        this.restClient = restClientBuilder.build();
        this.healthUrl = healthCheckProperties.kobilHealthUrl();
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) {
        var future = pingKobil();
        boolean up = getResult(future);

        builder.status(up ? Status.UP : Status.DOWN)
                .withDetail(KOBIL, up ? "reachable" : "unreachable");
    }

    private CompletableFuture<Boolean> pingKobil() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                restClient.get()
                        .uri(healthUrl)
                        .retrieve()
                        .toBodilessEntity();
                return true;
            } catch (Exception e) {
                return false;
            }
        });
    }

    private boolean getResult(CompletableFuture<Boolean> future) {
        try {
            return future.get(TIMEOUT, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            future.cancel(true);
            return false;
        } catch (ExecutionException | CancellationException e) {
            return false;
        }
    }
}

```


```

package de.consorsbank.core.trauthsc.common.actuator;

import org.springframework.boot.health.contributor.AbstractHealthIndicator;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.Status;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.CancellationException;

@Component
public class DraasHealthCheckIndicator extends AbstractHealthIndicator {

    private static final int TIMEOUT = 3;
    private static final String DRAAS = "draas";

    private final RestClient restClient;
    private final String healthUrl;

    public DraasHealthCheckIndicator(RestClient.Builder restClientBuilder,
                                      HealthCheckProperties healthCheckProperties) {
        super("Draas health check failed");
        this.restClient = restClientBuilder.build();
        this.healthUrl = healthCheckProperties.draasHealthUrl();
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) {
        var future = pingDraas();
        boolean up = getResult(future);

        builder.status(up ? Status.UP : Status.DOWN)
                .withDetail(DRAAS, up ? "reachable" : "unreachable");
    }

    private CompletableFuture<Boolean> pingDraas() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                restClient.get()
                        .uri(healthUrl)
                        .retrieve()
                        .toBodilessEntity();
                return true;
            } catch (Exception e) {
                return false;
            }
        });
    }

    private boolean getResult(CompletableFuture<Boolean> future) {
        try {
            return future.get(TIMEOUT, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            future.cancel(true);
            return false;
        } catch (ExecutionException | CancellationException e) {
            return false;
        }
    }
}
```


```
package de.consorsbank.core.trauthsc.common.actuator;

import org.springframework.boot.health.contributor.AbstractHealthIndicator;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.Status;
import org.springframework.stereotype.Component;

@Component
public class TamHealthCheckIndicator extends AbstractHealthIndicator {

    private static final String DB = "db";
    private static final String ARTEMIS = "artemis";
    private static final String VAULT = "hvault";
    private static final String KOBIL = "kobil";
    private static final String DRAAS = "draas";

    private final DbHealthCheckIndicator dbHealthCheckIndicator;
    private final ArtemisHealthCheckIndicator artemisHealthCheckIndicator;
    private final VaultHealthCheckIndicator vaultHealthCheckIndicator;
    private final KobilHealthCheckIndicator kobilHealthCheckIndicator;
    private final DraasHealthCheckIndicator draasHealthCheckIndicator;

    public TamHealthCheckIndicator(DbHealthCheckIndicator dbHealthCheckIndicator,
                                    ArtemisHealthCheckIndicator artemisHealthCheckIndicator,
                                    VaultHealthCheckIndicator vaultHealthCheckIndicator,
                                    KobilHealthCheckIndicator kobilHealthCheckIndicator,
                                    DraasHealthCheckIndicator draasHealthCheckIndicator) {
        super("TAM dependency health check failed");
        this.dbHealthCheckIndicator = dbHealthCheckIndicator;
        this.artemisHealthCheckIndicator = artemisHealthCheckIndicator;
        this.vaultHealthCheckIndicator = vaultHealthCheckIndicator;
        this.kobilHealthCheckIndicator = kobilHealthCheckIndicator;
        this.draasHealthCheckIndicator = draasHealthCheckIndicator;
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) {
        Health dbHealth = dbHealthCheckIndicator.health();
        Health artemisHealth = artemisHealthCheckIndicator.health();
        Health vaultHealth = vaultHealthCheckIndicator.health();
        Health kobilHealth = kobilHealthCheckIndicator.health();
        Health draasHealth = draasHealthCheckIndicator.health();

        boolean allUp = dbHealth.getStatus() == Status.UP
                && artemisHealth.getStatus() == Status.UP
                && vaultHealth.getStatus() == Status.UP
                && kobilHealth.getStatus() == Status.UP
                && draasHealth.getStatus() == Status.UP;

        builder.status(allUp ? Status.UP : Status.DOWN)
                .withDetail(DB, dbHealth)
                .withDetail(ARTEMIS, artemisHealth)
                .withDetail(VAULT, vaultHealth)
                .withDetail(KOBIL, kobilHealth)
                .withDetail(DRAAS, draasHealth);
    }
}

```
