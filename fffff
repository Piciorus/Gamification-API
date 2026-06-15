# trauth-sc — Local Development with Docker Compose

## Prerequisites

- Docker Desktop running
- Access to the internal Docker registry: `i-ckdregistry.pro.be.xpi.net.intra`
- Nexus credentials (user token — see below)

---

## 1. Authenticate with the Internal Registry

If you cannot pull images (you'll see `unauthorized` errors), you need to log in first:

```bash
docker login i-ckdregistry.pro.be.xpi.net.intra
```

When prompted:
- **Username**: your Nexus user token **name code**
- **Password**: your Nexus user token **pass code**

> To get your token: log in to Nexus Repository Manager → top-right menu → **User Token**.
> The dialog shows your token name code and pass code. Keep these secret and do not share them.

---

## 2. Start the Infrastructure (Docker Compose)

```bash
cd ./docker/
docker compose up
```

This starts all required infrastructure containers (database, Vault, etc.).

To stop and clean up volumes (fresh start):

```bash
cd ./docker/
docker compose down
```

> To wipe data completely, also delete the Docker volumes manually or use `docker compose down -v`.

---

## 3. Start the Application

Start the Spring Boot application with the local compose profile:

```bash
./gradlew bootRun --args='--spring.profiles.active=local-development-non-ssl-with-compose'
```

Or from IntelliJ IDEA: set the active profile to `local-development-non-ssl-with-compose` in your run configuration.

---

## 4. Verify

- **Swagger UI**: [http://localhost:8323/svc/trauth/swagger-ui/index.html](http://localhost:8323/svc/trauth/swagger-ui/index.html)

---

## Local Vault Setup (optional, if not using compose Vault)

If you need a local Vault instance instead:

```bash
brew tap hashicorp/tap
brew install hashicorp/tap/vault
vault server -dev
```

> ⚠️ Dev mode runs Vault entirely in-memory. Data does not persist across restarts.

---

## Local Oracle DB (alternative to Docker Compose DB)

If you prefer a standalone Oracle XE container:

```bash
docker run -d \
  --name oracle-local-trauth \
  -p 1521:1521 \
  -e ORACLE_PASSWORD=12345 \
  gvenzl/oracle-xe:21-slim-faststart
```

Then create the local database schema with name `oracle-local-trauth`.

JDBC URL: `jdbc:oracle:thin:@localhost:1521/XEPDB1`

---

## Nexus Credentials Update

If your Nexus user token expires (tokens expire on a rolling basis — check the expiry date shown in the Nexus token dialog), regenerate a new token via Nexus → User Token, and re-run `docker login` with the new credentials.
