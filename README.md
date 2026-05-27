```
new org.springframework.web.client.RestTemplate().exchange("http://127.0.0.1:8200/v1/secret/data/local/trauth-sc/credentials", org.springframework.http.HttpMethod.GET, new org.springframework.http.HttpEntity<>(new org.springframework.http.HttpHeaders() {{ set("X-Vault-Token", "root"); }}), String.class).getBody()

```

```
try { return vaultTemplate.read("secret/data/local/trauth-sc/credentials"); } catch (Exception e) { e; }
```
