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
