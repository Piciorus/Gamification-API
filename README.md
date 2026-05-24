```
private String getKvPath() {
    var path = kvVaultConfiguration.isLocalVaultEnabled()
        ? kvVaultConfiguration.getLocal().getDefaultBasePath() 
          + kvVaultConfiguration.getLocal().getDefaultContext()
        : kvVaultConfiguration.getCloud().getDefaultBasePath() 
          + kvVaultConfiguration.getCloud().getDefaultContext();
    log.info("Vault KV path: {}", path);  // ← add this
    return path;
}
```

```
export VAULT_ADDR=http://127.0.0.1:8200

# Grant full access policy
vault policy write trauth-sc-policy - << EOF
path "transit/*" {
  capabilities = ["create", "read", "update", "delete", "list"]
}
path "secret/*" {
  capabilities = ["create", "read", "update", "delete", "list"]
}
EOF

# Apply policy to token — or since it's dev mode, root token has all permissions
# Verify the root token is being used
vault token lookup
```


```
vault write -f transit/keys/trauth-sc
vault list transit/keys
```
