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
