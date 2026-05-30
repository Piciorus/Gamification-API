```
#!/bin/sh

vault server -dev -dev-root-token-id="root" &
VAULT_PID=$!

# Wait for vault to be ready
export VAULT_ADDR="http://127.0.0.1:8200"
export VAULT_TOKEN="root"

echo ">> Waiting for Vault to be ready..."
until vault status >/dev/null 2>&1; do
  sleep 1
done
echo ">> Vault is up. Root Token: root"

echo ">> Enabling transit engine"
vault secrets enable transit 2>/dev/null || echo "transit already enabled"

echo ">> Enabling kv engine"
vault secrets enable -path=secret kv-v2 2>/dev/null || echo "kv already enabled"

echo ">> Creating transit key 'abc'"
vault write -f transit/keys/abc >/dev/null

echo ">> Writing secret/data/local/trauth-sc/credentials"
vault kv put secret/data/local/trauth-sc/credentials \
  encryptionKey=abc \
  H2_LOCAL_USR="${H2_LOCAL_USR:-sa}" \
  H2_LOCAL_PASS="${H2_LOCAL_PASS:-password}"

echo ">> Verifying"
vault kv get secret/data/local/trauth-sc/credentials

echo ">> Vault initialisation complete. Server running on PID ${VAULT_PID}."

wait ${VAULT_PID}
```
