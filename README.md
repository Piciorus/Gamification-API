```
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

3. Start the application with profile:
```
local-development-non-ssl-with-compose
```

4. To reset everything (clean DB + volumes):
```bash
docker compose down -v
docker compose up -d
```

> **Note:** On first run Oracle will execute init scripts automatically
> (create users, grants). No manual DB setup needed.

> **Note:** Vault is **disabled by default** in this profile.
> Encryption uses local AES. To enable Vault, set
> `spring.vault.enabled=true` and start the `hvault` service.
```
