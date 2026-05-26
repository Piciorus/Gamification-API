```
services:
  # ---------------------------------------------------------------------------
  # Oracle XE  (gvenzl/oracle-xe) - confirmed working image from your terminal
  # Exposes 1521. Service name "oracle" is the in-network hostname.
  # The SQL in ./oracle-init runs once on first boot (creates PVM / TAM users).
  # ---------------------------------------------------------------------------
  oracle:
    # Pulled via the Nexus Docker Hub proxy (dockerhub/...) — no direct docker.io.
    image: dockerhub/gvenzl/oracle-xe:21-slim-faststart
    container_name: oracle-local-trauth
    ports:
      - "1521:1521"
    environment:
      ORACLE_PASSWORD: "12345"          # password for SYS / SYSTEM
    volumes:
      - oracle-data:/opt/oracle/oradata
      - ./oracle-init:/container-entrypoint-initdb.d:ro
    healthcheck:
      # gvenzl image ships this helper; "READY" once the DB + initdb scripts finish
      test: ["CMD", "healthcheck.sh"]
      interval: 10s
      timeout: 10s
      retries: 30
      start_period: 90s

  # ---------------------------------------------------------------------------
  # ActiveMQ Artemis - apache/activemq-artemis (from the chat screenshot)
  # 61616 = JMS/core, 8161 = web console (http://localhost:8161/console/login)
  # ---------------------------------------------------------------------------
  artemis:
    # Pulled via the Nexus Docker Hub proxy (dockerhub/...) — no direct docker.io.
    image: dockerhub/apache/activemq-artemis:latest
    container_name: activemq-artemis
    ports:
      - "61616:61616"
      - "8161:8161"
    environment:
      ARTEMIS_USER: "localUser"
      ARTEMIS_PASSWORD: "12345"
    healthcheck:
      test: ["CMD-SHELL", "curl -fsS http://localhost:8161 >/dev/null 2>&1 || exit 1"]
      interval: 10s
      timeout: 5s
      retries: 20
      start_period: 30s

  # ---------------------------------------------------------------------------
  # HashiCorp Vault (dev mode) - using the image that pulled successfully
  # in your terminal. hashicorp/vault:1.19.5 failed with "unexpected EOF",
  # so we use the approved-registry 1.16 that completed.
  # Dev mode: in-memory, auto-unsealed, fixed root token.
  # ---------------------------------------------------------------------------
  vault:
    image: i-ckdregistry.pro.be.xpi.net.intra/approved/hashicorp/vault:1.16
    container_name: vault-dev
    ports:
      - "8200:8200"
    environment:
      VAULT_DEV_ROOT_TOKEN_ID: "${VAULT_TOKEN}"
      VAULT_DEV_LISTEN_ADDRESS: "0.0.0.0:8200"
      VAULT_ADDR: "http://127.0.0.1:8200"
    cap_add:
      - IPC_LOCK
    command: ["server", "-dev"]
    healthcheck:
      test: ["CMD", "vault", "status", "-address=http://127.0.0.1:8200"]
      interval: 5s
      timeout: 5s
      retries: 20
      start_period: 10s

  # ---------------------------------------------------------------------------
  # One-shot init: enables transit + kv (steps 4 in your README) and writes the
  # DB credentials the app reads from kv/local/trauth-sc/credentials.
  # Exits 0 when done; the app waits for it to complete successfully.
  # ---------------------------------------------------------------------------
  vault-init:
    image: i-ckdregistry.pro.be.xpi.net.intra/approved/hashicorp/vault:1.16
    container_name: vault-init
    depends_on:
      vault:
        condition: service_healthy
    environment:
      VAULT_ADDR: "http://vault:8200"
      VAULT_TOKEN: "${VAULT_TOKEN}"
      H2_LOCAL_USR: "${H2_LOCAL_USR}"
      H2_LOCAL_PASS: "${H2_LOCAL_PASS}"
    volumes:
      - ./vault-init/init.sh:/init.sh:ro
    entrypoint: ["/bin/sh", "/init.sh"]

  # ---------------------------------------------------------------------------
  # The Spring Boot application (trauth-sc) - NO Dockerfile.
  # You build the jar yourself first:   ./gradlew clean bootJar -x test
  # Compose then runs that jar on a stock Temurin JRE (pulled via Nexus proxy).
  # The jar is mounted in read-only from your repo's build/libs.
  # All host references are overridden from 127.0.0.1 -> service names.
  # ---------------------------------------------------------------------------
  app:
    image: dockerhub/eclipse-temurin:21-jre
    container_name: trauth-sc
    working_dir: /app
    # ${APP_JAR} is the path to the built jar on your machine (see .env).
    volumes:
      - ${APP_JAR}:/app/app.jar:ro
    command: ["java", "-jar", "/app/app.jar"]
    ports:
      - "8323:8323"   # server.port
      - "9323:9323"   # management.server.port
    depends_on:
      oracle:
        condition: service_healthy
      artemis:
        condition: service_healthy
      vault:
        condition: service_healthy
      vault-init:
        condition: service_completed_successfully
    environment:
      # --- Vault (spring.cloud.vault.*) ---
      SPRING_CLOUD_VAULT_HOST: "vault"
      SPRING_CLOUD_VAULT_URI: "http://vault:8200"
      SPRING_CLOUD_VAULT_TOKEN: "${VAULT_TOKEN}"
      SPRING_CLOUD_VAULT_AUTHENTICATION: "TOKEN"

      # --- H2 creds (the app pulls these via ${H2_LOCAL_USR}/${H2_LOCAL_PASS}) ---
      H2_LOCAL_USR: "${H2_LOCAL_USR}"
      H2_LOCAL_PASS: "${H2_LOCAL_PASS}"

      # --- Oracle datasources (uncomment the Oracle block in your yaml to use) ---
      # The app's yaml hardcodes localhost:1521 for Oracle; point it at the
      # "oracle" service instead via a Spring property override:
      SPRING_DATASOURCE_PVM_URL: "jdbc:oracle:thin:@oracle:1521/XEPDB1"
      SPRING_DATASOURCE_TAM_URL: "jdbc:oracle:thin:@oracle:1521/XEPDB1"

      # --- Artemis broker (override the local 127.0.0.1 brokerUrl) ---
      SPRING_ARTEMIS_BROKER_URL: "tcp://artemis:61616"
      SPRING_ARTEMIS_USER: "localUser"
      SPRING_ARTEMIS_PASSWORD: "12345"

      # Pick the spring profile your non-ssl yaml belongs to, if any:
      SPRING_PROFILES_ACTIVE: "${SPRING_PROFILES_ACTIVE}"

volumes:
  oracle-data:


```


```
# =============================================================================
# Dockerfile for trauth-sc (Spring Boot — Gradle + Java 21)
# Multi-stage: build the fat jar with Gradle, then run on a slim JRE.
# =============================================================================

# ---- Stage 1: build -------------------------------------------------------
FROM gradle:8-jdk21 AS build
WORKDIR /workspace

# Cache dependencies first (only re-downloads when build files change)
COPY build.gradle settings.gradle ./
COPY gradle ./gradle
RUN gradle dependencies --no-daemon || true

# Build the application
COPY src ./src
RUN gradle clean bootJar --no-daemon -x test

# ---- Stage 2: runtime -----------------------------------------------------
FROM eclipse-temurin:21-jre
WORKDIR /app

# Run as a non-root user
RUN groupadd --system spring && useradd --system --gid spring spring
USER spring:spring

# Copy the built jar
COPY --from=build /workspace/build/libs/*.jar app.jar

# server.port (8323) and management.server.port (9323)
EXPOSE 8323 9323

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
```


```
# Build output (rebuilt inside the image)
target/
build/
*.jar
!**/src/**

# IDE / OS noise
.idea/
*.iml
.vscode/
.DS_Store

# Git
.git/
.gitignore

# Local env / secrets - never bake into the image
.env
*.log
```

```
services:
  # ---------------------------------------------------------------------------
  # Oracle XE  (gvenzl/oracle-xe) - confirmed working image from your terminal
  # Exposes 1521. Service name "oracle" is the in-network hostname.
  # The SQL in ./oracle-init runs once on first boot (creates PVM / TAM users).
  # ---------------------------------------------------------------------------
  oracle:
    image: gvenzl/oracle-xe:21-slim-faststart
    container_name: oracle-local-trauth
    ports:
      - "1521:1521"
    environment:
      ORACLE_PASSWORD: "12345"          # password for SYS / SYSTEM
    volumes:
      - oracle-data:/opt/oracle/oradata
      - ./oracle-init:/container-entrypoint-initdb.d:ro
    healthcheck:
      # gvenzl image ships this helper; "READY" once the DB + initdb scripts finish
      test: ["CMD", "healthcheck.sh"]
      interval: 10s
      timeout: 10s
      retries: 30
      start_period: 90s

  # ---------------------------------------------------------------------------
  # ActiveMQ Artemis - apache/activemq-artemis (from the chat screenshot)
  # 61616 = JMS/core, 8161 = web console (http://localhost:8161/console/login)
  # ---------------------------------------------------------------------------
  artemis:
    image: apache/activemq-artemis:latest
    container_name: activemq-artemis
    ports:
      - "61616:61616"
      - "8161:8161"
    environment:
      ARTEMIS_USER: "localUser"
      ARTEMIS_PASSWORD: "12345"
    healthcheck:
      test: ["CMD-SHELL", "curl -fsS http://localhost:8161 >/dev/null 2>&1 || exit 1"]
      interval: 10s
      timeout: 5s
      retries: 20
      start_period: 30s

  # ---------------------------------------------------------------------------
  # HashiCorp Vault (dev mode) - using the image that pulled successfully
  # in your terminal. hashicorp/vault:1.19.5 failed with "unexpected EOF",
  # so we use the approved-registry 1.16 that completed.
  # Dev mode: in-memory, auto-unsealed, fixed root token.
  # ---------------------------------------------------------------------------
  vault:
    image: i-ckdregistry.pro.be.xpi.net.intra/approved/hashicorp/vault:1.16
    container_name: vault-dev
    ports:
      - "8200:8200"
    environment:
      VAULT_DEV_ROOT_TOKEN_ID: "${VAULT_TOKEN}"
      VAULT_DEV_LISTEN_ADDRESS: "0.0.0.0:8200"
      VAULT_ADDR: "http://127.0.0.1:8200"
    cap_add:
      - IPC_LOCK
    command: ["server", "-dev"]
    healthcheck:
      test: ["CMD", "vault", "status", "-address=http://127.0.0.1:8200"]
      interval: 5s
      timeout: 5s
      retries: 20
      start_period: 10s

  # ---------------------------------------------------------------------------
  # One-shot init: enables transit + kv (steps 4 in your README) and writes the
  # DB credentials the app reads from kv/local/trauth-sc/credentials.
  # Exits 0 when done; the app waits for it to complete successfully.
  # ---------------------------------------------------------------------------
  vault-init:
    image: i-ckdregistry.pro.be.xpi.net.intra/approved/hashicorp/vault:1.16
    container_name: vault-init
    depends_on:
      vault:
        condition: service_healthy
    environment:
      VAULT_ADDR: "http://vault:8200"
      VAULT_TOKEN: "${VAULT_TOKEN}"
      H2_LOCAL_USR: "${H2_LOCAL_USR}"
      H2_LOCAL_PASS: "${H2_LOCAL_PASS}"
    volumes:
      - ./vault-init/init.sh:/init.sh:ro
    entrypoint: ["/bin/sh", "/init.sh"]

  # ---------------------------------------------------------------------------
  # The Spring Boot application (trauth-sc).
  # Build context points at your source repo - adjust APP_CONTEXT in .env.
  # All host references are overridden from 127.0.0.1 -> service names so the
  # container talks to the other containers over the compose network.
  # ---------------------------------------------------------------------------
  app:
    build:
      context: ${APP_CONTEXT}
      dockerfile: ${APP_DOCKERFILE}
    container_name: trauth-sc
    ports:
      - "8323:8323"   # server.port
      - "9323:9323"   # management.server.port
    depends_on:
      oracle:
        condition: service_healthy
      artemis:
        condition: service_healthy
      vault:
        condition: service_healthy
      vault-init:
        condition: service_completed_successfully
    environment:
      # --- Vault (spring.cloud.vault.*) ---
      SPRING_CLOUD_VAULT_HOST: "vault"
      SPRING_CLOUD_VAULT_URI: "http://vault:8200"
      SPRING_CLOUD_VAULT_TOKEN: "${VAULT_TOKEN}"
      SPRING_CLOUD_VAULT_AUTHENTICATION: "TOKEN"

      # --- H2 creds (the app pulls these via ${H2_LOCAL_USR}/${H2_LOCAL_PASS}) ---
      H2_LOCAL_USR: "${H2_LOCAL_USR}"
      H2_LOCAL_PASS: "${H2_LOCAL_PASS}"

      # --- Oracle datasources (uncomment the Oracle block in your yaml to use) ---
      # The app's yaml hardcodes localhost:1521 for Oracle; point it at the
      # "oracle" service instead via a Spring property override:
      SPRING_DATASOURCE_PVM_URL: "jdbc:oracle:thin:@oracle:1521/XEPDB1"
      SPRING_DATASOURCE_TAM_URL: "jdbc:oracle:thin:@oracle:1521/XEPDB1"

      # --- Artemis broker (override the local 127.0.0.1 brokerUrl) ---
      SPRING_ARTEMIS_BROKER_URL: "tcp://artemis:61616"
      SPRING_ARTEMIS_USER: "localUser"
      SPRING_ARTEMIS_PASSWORD: "12345"

      # Pick the spring profile your non-ssl yaml belongs to, if any:
      SPRING_PROFILES_ACTIVE: "${SPRING_PROFILES_ACTIVE}"

volumes:
  oracle-data:

```


```
# trauth-sc — Local Stack (Docker Compose)

Runs the application end-to-end with the three backing services it needs:

| Service | Image | Purpose | Ports |
|---|---|---|---|
| **oracle** | `gvenzl/oracle-xe:21-slim-faststart` | Oracle XE database (PVM / TAM schemas) | `1521` |
| **artemis** | `apache/activemq-artemis` | JMS broker | `61616` (core), `8161` (console) |
| **vault** | `i-ckdregistry.pro.be.xpi.net.intra/approved/hashicorp/vault:1.16` | Secrets (transit + kv) | `8200` |
| **vault-init** | same as vault | One-shot: enables engines, writes creds | — |
| **app** | built from your repo | Spring Boot `trauth-sc` | `8323` (api), `9323` (mgmt) |

> **Note on the Vault image:** the public `hashicorp/vault:1.19.5` pull failed in
> your terminal with `unexpected EOF`. The approved-registry image
> `…/approved/hashicorp/vault:1.16` pulled successfully, so the stack uses that.
> You must be logged in to that registry first:
> `docker login i-ckdregistry.pro.be.xpi.net.intra`

---

## Prerequisites

- Docker Desktop / Docker Engine with the Compose plugin
- Access (and `docker login`) to `i-ckdregistry.pro.be.xpi.net.intra`
- Your application source with a `Dockerfile` that produces a runnable jar

---

## 1. Configure

Edit **`.env`**:

```dotenv
VAULT_TOKEN=root            # dev-mode root token (must match the app's ${VAULT_TOKEN})
H2_LOCAL_USR=sa             # DB creds stored in Vault and read by the app
H2_LOCAL_PASS=password
SPRING_PROFILES_ACTIVE=local
APP_CONTEXT=../trauth-sc    # path to the folder with your app's Dockerfile
APP_DOCKERFILE=Dockerfile
```

Edit **`oracle-init/01-create-users.sql`** — replace the placeholder passwords
(`"12345"`) with whatever PVM / TAM should use, matching your app config.

---

## 2. Start everything

```bash
docker login i-ckdregistry.pro.be.xpi.net.intra   # for the Vault image
docker compose up -d --build
```

Startup order is handled automatically via health checks:

1. **oracle** boots and runs `oracle-init/*.sql` (creates PVM & TAM) → becomes healthy
2. **artemis** starts → healthy
3. **vault** starts in dev mode → healthy
4. **vault-init** runs once: enables `transit` + `kv`, creates `transit/keys/abc`,
   writes `kv/local/trauth-sc/credentials` → exits 0
5. **app** starts only after all of the above succeed

Watch progress:

```bash
docker compose ps
docker compose logs -f app
```

---

## 3. Verify

- **App / Swagger:** http://localhost:8323/svc/trauth/swagger-ui/index.html
- **Management:** http://localhost:9323
- **Artemis console:** http://localhost:8161/console/login  (`localUser` / `12345`)
- **Vault:** http://localhost:8200  (token = `VAULT_TOKEN`)

Check the Vault secret was written:

```bash
docker compose exec vault \
  vault kv get -address=http://127.0.0.1:8200 kv/local/trauth-sc/credentials
```

Check Oracle users exist:

```bash
docker compose exec oracle sqlplus system/12345@//localhost:1521/XEPDB1 \
  <<< "SELECT username FROM all_users WHERE username IN ('PVM','TAM');"
```

---

## 4. How the wiring works (important)

Your YAML config points the app at **`127.0.0.1`** for Vault, Oracle and Artemis.
Inside Compose, containers reach each other by **service name**, not localhost.
The compose file therefore overrides those hosts for the `app` container via
environment variables (Spring relaxed binding):

| App expects (yaml) | Overridden to |
|---|---|
| `spring.cloud.vault.uri: http://127.0.0.1:8200` | `http://vault:8200` |
| Oracle `jdbc:...@localhost:1521/XEPDB1` | `jdbc:...@oracle:1521/XEPDB1` |
| Artemis `tcp://127.0.0.1:61616` | `tcp://artemis:61616` |

If your property names differ from the `SPRING_*` env vars in the compose file,
adjust them to match your actual config keys.

> The app's `application*.yaml` ships with the Oracle datasource block commented
> out and H2 in-memory active. To actually use the Oracle container, uncomment
> the Oracle `url/username/password/driverClassName/...` block for the `pvm` and
> `tam` datasources (and comment the H2 lines), then rebuild.

---

## Manual Vault setup (reference — automated by `vault-init`)

For setting up Hvault locally **without** Compose (matches the README images):

```bash
brew tap hashicorp/tap
brew install hashicorp/tap/vault
vault server -dev

export VAULT_ADDR='http://127.0.0.1:8200'
vault secrets list
vault secrets enable transit
vault secrets enable kv
vault read  transit/keys/abc
vault write -f transit/keys/abc
vault kv get kv/local/trauth-sc/credentials
```

Oracle user creation (run as SYSTEM) is in `oracle-init/01-create-users.sql`.

---

## Common commands

```bash
docker compose up -d --build      # start / rebuild
docker compose logs -f <service>  # tail logs
docker compose restart app        # restart just the app
docker compose down               # stop (keeps Oracle volume)
docker compose down -v            # stop and wipe Oracle data + re-run init SQL
```

> Oracle init SQL only runs on a **fresh** volume. After changing it, run
> `docker compose down -v` to force re-initialisation.

---

## Troubleshooting

- **`unexpected EOF` pulling Vault** → use the approved registry image (already
  configured) and `docker login` first.
- **App can't reach Vault/DB** → confirm you're using service names, not
  `127.0.0.1`, in the app's effective config (see section 4).
- **Oracle "user already exists"** → the volume persisted; `docker compose down -v`.
- **Artemis auth fails** → check `ARTEMIS_USER`/`ARTEMIS_PASSWORD` match the
  app's `localUser` / `12345`.
- **Registry image unreachable** → your network may block it; check Docker's
  network/proxy settings or pull while on VPN.

```


```
# =============================================================================
# Environment for the trauth-sc local stack.
# docker compose reads this automatically. Do NOT commit real secrets.
# =============================================================================

# ---- Vault ----
# Dev-mode root token. The app's yaml uses ${VAULT_TOKEN}; keep them identical.
VAULT_TOKEN=root

# ---- H2 / DB credentials stored in Vault and read by the app ----
H2_LOCAL_USR=sa
H2_LOCAL_PASS=password

# ---- Spring profile (set to whatever your non-ssl yaml maps to, else leave blank) ----
SPRING_PROFILES_ACTIVE=local

# ---- Application build ----
# Point this at the folder containing your app's Dockerfile (the repo root).
# Example: APP_CONTEXT=../trauth-sc
APP_CONTEXT=./app
APP_DOCKERFILE=Dockerfile
```


```
-- ============================================================================
-- Oracle bootstrap for trauth-sc  (transcribed from README images 7 & 8)
-- gvenzl/oracle-xe runs every .sql in /container-entrypoint-initdb.d ONCE,
-- on first container start, connected as SYSTEM to the XEPDB1 PDB.
-- Replace the 'xx' passwords below with real values before running.
-- ============================================================================

-- Make sure we are operating inside the pluggable DB the app connects to.
ALTER SESSION SET CONTAINER = XEPDB1;

-- ---- PVM user --------------------------------------------------------------
CREATE USER PVM IDENTIFIED BY "12345";
GRANT CONNECT, RESOURCE TO PVM;
GRANT CREATE SESSION, CREATE TABLE, CREATE SEQUENCE TO PVM;
GRANT CREATE SESSION TO PVM;

-- ---- TAM user --------------------------------------------------------------
CREATE USER TAM IDENTIFIED BY "12345";
GRANT CONNECT, RESOURCE TO TAM;
GRANT CREATE SESSION, CREATE TABLE, CREATE SEQUENCE TO TAM;
GRANT CREATE SESSION TO TAM;

-- ---- Additional grants (image 8) ------------------------------------------
GRANT CREATE TABLE TO TAM;
GRANT CREATE TABLE TO PVM;

ALTER USER PVM DEFAULT TABLESPACE USERS;
ALTER USER TAM DEFAULT TABLESPACE USERS;

GRANT CREATE SESSION TO PVM;
GRANT RESOURCE TO PVM;
ALTER USER PVM QUOTA UNLIMITED ON USERS;

GRANT CREATE SESSION TO TAM;
GRANT RESOURCE TO TAM;
ALTER USER TAM QUOTA UNLIMITED ON USERS;

-- ---- Session timezone (image 8) -------------------------------------------
ALTER SESSION SET TIME_ZONE = 'UTC';

EXIT;
```


```
#!/bin/sh
# -----------------------------------------------------------------------------
# Vault bootstrap - mirrors step 4 of the README (manual CLI commands) plus
# writing the DB credentials the app expects at kv/local/trauth-sc/credentials.
# Runs against the "vault" service in dev mode. Idempotent: safe to re-run.
# -----------------------------------------------------------------------------
set -e

export VAULT_ADDR="${VAULT_ADDR:-http://vault:8200}"
export VAULT_TOKEN="${VAULT_TOKEN}"

echo ">> Waiting for Vault to be reachable at ${VAULT_ADDR} ..."
until vault status >/dev/null 2>&1; do
  sleep 2
done
echo ">> Vault is up."

# --- Enable the secrets engines (ignore 'already enabled' on re-run) ---
echo ">> Enabling transit engine"
vault secrets enable transit 2>/dev/null || echo "   transit already enabled"

echo ">> Enabling kv engine"
vault secrets enable kv 2>/dev/null || echo "   kv already enabled"

# --- Transit key used by the app (vault read/write transit/keys/abc) ---
echo ">> Creating transit key 'abc'"
vault write -f transit/keys/abc >/dev/null

# --- Write DB credentials the app reads from kv/local/trauth-sc/credentials ---
# spring.cloud.vault.kv.default-context = local/trauth-sc/credentials
# backend = secret? -> your yaml shows backend: secret with default-base-path
# 'secret/data/'. The kv path your README reads is kv/local/trauth-sc/credentials,
# so we write there. Adjust keys to whatever your app expects inside the secret.
echo ">> Writing kv/local/trauth-sc/credentials"
vault kv put kv/local/trauth-sc/credentials \
  H2_LOCAL_USR="${H2_LOCAL_USR}" \
  H2_LOCAL_PASS="${H2_LOCAL_PASS}"

echo ">> Verifying"
vault secrets list
vault kv get kv/local/trauth-sc/credentials || true

echo ">> Vault initialisation complete."
```
