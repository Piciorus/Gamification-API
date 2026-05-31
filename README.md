# Getting Started

## Local development

### With Docker Compose (recommended)

1. Start all dependencies:
```bash
cd ./docker-compose
docker compose up -d
```

2. Wait for all services to be healthy (Oracle takes ~90s on first start):
```bash
docker compose ps
```

3. Start the application with profile `local-development-non-ssl-with-compose`

4. To reset everything (clean DB + volumes):
```bash
docker compose down -v
docker compose up -d
```

> **Note:** On first run Oracle will execute init scripts automatically (create users, grants). No manual DB setup needed.

> **Note:** Vault is **disabled by default** in this profile. Encryption uses local AES. To enable Vault, set `spring.vault.enabled=true` and start the `hvault` service:
> ```bash
> docker compose up -d hvault
> ```

---

### With local DB (alternative)

1. Start local Docker
2. Start Oracle:
```bash
docker run -d --name oracle-local-trauth -p 1521:1521 -e ORACLE_PASSWORD=12345 gvenzl/oracle-xe:21-slim-faststart
```
3. Create local database with name `oracle-local-trauth`
4. Start the application with profile `local-development-non-ssl`

---

### With Vault (optional)

#### Option 1 — via Docker Compose
```bash
cd ./docker-compose
docker compose up -d hvault
```

#### Option 2 — via brew
```bash
brew tap hashicorp/tap
brew install hashicorp/tap/vault
vault server -dev
```

Then run:
```bash
export VAULT_ADDR='http://127.0.0.1:8200'
vault secrets list
vault secrets enable transit
vault kv put secret/data/local/trauth-sc/credentials encryptionKey=abc
vault kv get secret/data/local/trauth-sc/credentials
```

Set `spring.vault.enabled=true` in your local profile yaml.

---

## Swagger

http://localhost:8323/svc/trauth/swagger-ui/index.html

---

## Nexus Credentials Update

...

---

## Profiles

| Profile | DB | Vault | Use case |
|---|---|---|---|
| `local-development-non-ssl-with-compose` | Docker Oracle | disabled | Docker Compose |
| `local-development-non-ssl` | Local Oracle | disabled | Manual setup |
| `local-development-ssl` | Local Oracle | disabled | SSL local |
| `openshift-d0/d1/...` | OpenShift Oracle | enabled | Server |
