```
#!/bin/sh
# -----------------------------------------------------------------------------
# Combined Vault startup + init script.
# 1) Starts vault server in dev mode (background)
# 2) Waits for it to be ready
# 3) Enables transit, writes KV credentials
# 4) Keeps vault in foreground (so docker doesn't exit)
# -----------------------------------------------------------------------------

# Start vault dev server in background
vault server -dev -dev-root-token-id="${VAULT_DEV_ROOT_TOKEN_ID}" \
  -dev-listen-address="${VAULT_DEV_LISTEN_ADDRESS}" &
VAULT_PID=$!

# Wait for vault to be ready
export VAULT_ADDR="http://127.0.0.1:8200"
export VAULT_TOKEN="${VAULT_DEV_ROOT_TOKEN_ID}"

echo ">> Waiting for Vault to be ready..."
until vault status >/dev/null 2>&1; do
  sleep 1
done
echo ">> Vault is up."

# --- Enable transit engine ---
echo ">> Enabling transit engine"
vault secrets enable transit 2>/dev/null || echo "   transit already enabled"

# --- Transit key ---
echo ">> Creating transit key 'abc'"
vault write -f transit/keys/abc >/dev/null

# --- Write credentials to the default "secret/" KV v2 engine ---
echo ">> Writing secret/local/trauth-sc/credentials"
vault kv put secret/local/trauth-sc/credentials \
  H2_LOCAL_USR="${H2_LOCAL_USR}" \
  H2_LOCAL_PASS="${H2_LOCAL_PASS}"

echo ">> Verifying"
vault kv get secret/local/trauth-sc/credentials

echo ">> Vault initialisation complete. Server running on PID ${VAULT_PID}."

# Keep vault in foreground so the container stays alive
wait ${VAULT_PID}

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
    container_name: local-trauth-oracle
    ports:
      - "1521:1521"
    environment:
      ORACLE_PASSWORD: "12345"
    volumes:
      - oracle-data:/opt/oracle/oradata
      - ./oracle-init:/container-entrypoint-initdb.d:ro
    healthcheck:
      test: ["CMD", "healthcheck.sh"]
      interval: 10s
      timeout: 10s
      retries: 30
      start_period: 90s

  # ---------------------------------------------------------------------------
  # ActiveMQ Artemis
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
  # HashiCorp Vault (dev mode) + auto-init
  # Single container: starts vault in background, runs init (transit + kv
  # credentials), then keeps vault in foreground.
  # No separate vault-init container needed.
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
      VAULT_TOKEN: "${VAULT_TOKEN}"
      H2_LOCAL_USR: "${H2_LOCAL_USR}"
      H2_LOCAL_PASS: "${H2_LOCAL_PASS}"
    cap_add:
      - IPC_LOCK
    volumes:
      - ./vault-init/init.sh:/init.sh:ro
    entrypoint: ["/bin/sh", "/init.sh"]
    healthcheck:
      test: ["CMD", "vault", "status", "-address=http://127.0.0.1:8200"]
      interval: 5s
      timeout: 5s
      retries: 20
      start_period: 15s

volumes:
  oracle-data:


```
