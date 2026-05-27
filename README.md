```
#!/bin/sh
# -----------------------------------------------------------------------------
# Combined Vault startup + init script.
# 1) Starts vault server in dev mode (background) with root token = "root"
# 2) Waits for it to be ready
# 3) Enables transit, writes KV credentials
# 4) Keeps vault in foreground (so docker doesn't exit)
# -----------------------------------------------------------------------------

# Unset proxy for localhost — prevents proxy from intercepting vault calls
export NO_PROXY="127.0.0.1,localhost"
export no_proxy="127.0.0.1,localhost"
unset HTTP_PROXY http_proxy HTTPS_PROXY https_proxy

# Start vault dev server in background with FIXED root token "root"
vault server -dev -dev-root-token-id="root" \
  -dev-listen-address="0.0.0.0:8200" &
VAULT_PID=$!

# Wait for vault to be ready
export VAULT_ADDR="http://127.0.0.1:8200"
export VAULT_TOKEN="root"

echo ">> Waiting for Vault to be ready..."
until vault status >/dev/null 2>&1; do
  sleep 1
done
echo ">> Vault is up. Root Token: root"

# --- Enable transit engine ---
echo ">> Enabling transit engine"
vault secrets enable transit 2>/dev/null || echo "   transit already enabled"

# --- Transit key ---
echo ">> Creating transit key 'abc'"
vault write -f transit/keys/abc >/dev/null

# --- Write credentials to the default "secret/" KV v2 engine ---
echo ">> Writing secret/local/trauth-sc/credentials"
vault kv put secret/local/trauth-sc/credentials \
  H2_LOCAL_USR="${H2_LOCAL_USR:-sa}" \
  H2_LOCAL_PASS="${H2_LOCAL_PASS:-password}"

echo ">> Verifying"
vault kv get secret/local/trauth-sc/credentials

echo ">> Vault initialisation complete. Server running on PID ${VAULT_PID}."

# Keep vault in foreground so the container stays alive
wait ${VAULT_PID}

```
