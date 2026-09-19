# Banking Migration Assistant — Project Instructions
# Consorsbank / BNP Paribas — Transaction Authorization Microservice (TAM)

> Specialized AI assistant context for a backend developer at Consorsbank / BNP Paribas working on a large-scale modernization project.

---

## Project Context

- **Goal**: Splitting a Java monolith into domain-driven microservices
- **Pattern**: Strangler Fig — gradual extraction, monolith runs in parallel
- **Status**: 10 microservices fully decoupled ✅ | TAM POC completed ✅
- **Team lead / reviewer**: Vlad (architect and PR reviewer)
- **Developer**: Alexandru Piciorus (piciorus.alexandru@externe.bnpparibas.com)

### Active Migrations

- Java 8 → Java 21
- Spring Boot 2 → Spring Boot 4 (stepping through SB3 where needed)
- SOAP / WS-Security → REST / OpenAPI 3.1 / OAuth2 / mTLS
- Shared monolith Oracle DB → PostgreSQL per-service
- ActiveMQ Classic → Artemis (OpenWire protocol for legacy WAR compatibility)
- Atomikos XA transactions (distributed 2PC across dual datasources: `tam` + `pvm`)

### Tech Stack

- Java 21, Spring Boot 3.x/4.x, Gradle (**NOT Maven**)
- PostgreSQL per service (TAM + PVM schemas, dual datasource via Atomikos JTA)
- Kafka (event streaming), Artemis JMS (internal messaging)
- Liquibase (YAML format, schema per service)
- HashiCorp Vault (payload/secret storage — `pvm` datasource)
- WireMock (integration test mocking for downstream services)
- Testcontainers (PostgreSQL in integration tests — **NOT H2**)
- OpenAPI Generator (Gradle plugin, 3.1 spec, Java codegen)
- MapStruct (DTO mapping)
- Resilience4j (circuit breaker, retry)
- Docker Compose (local dev stack)

### Regulatory Context

German bank — BaFin compliance, GDPR, PSD2/SCA

---

## Active Microservices in Docker Compose (`trauth-sc`)

| Service | Port | Notes |
|---|---|---|
| Oracle XE | 1521 | Legacy — TAM is migrating OFF Oracle |
| HashiCorp Vault (hvault) | 8200 | PVM secret/payload store |
| Artemis (main) | 8161, 61616 | OpenWire enabled — legacy WAR needs it |
| activemq-cnsEvent | 8162, 61617 | CnS event broker |
| activemq-dynaCampaign | 8163, 61618 | Campaign broker |
| draas | 18080 | Spring Boot, Oracle, Vault, Artemis |
| neo-simulator | 18082 | Spring Boot, Oracle |
| transauth-kobil | 18081 | Legacy WAR, Oracle, cacerts + keystore required |

### Docker Compose Conventions

- Credentials in `.env` (gitignored), `.env.example` committed
- Config YAMLs in `docker-compose/config/`, gitignored
- `transauth-kobil`: built from local WAR COPY (not multi-stage curl — ARM64/M-series Mac issue with amd64 emulation)
- Always set `platform: linux/amd64` for transauth-kobil in compose.yaml
- `cacerts` and `keystore-local` are volume-mounted, gitignored, distributed via secure channel
- Internal registry: `i-ckdregistry.pro.be.xpi.net.intra`
- Nexus: `nexus.pro.be.xpi.net.intra`
- Makefile for selective service startup

---

## TAM Service — Domain Model

**Transaction Authorization Microservice (TAM)** handles PSD2/SCA authorization flows.

### Core Entities

| Entity | Key columns |
|---|---|
| `authorization` | id, crm_customer_number (PII!), transaction_id, status, expires_at, payload_id |
| `authorization_attempt` | id, authorization_entity_id (FK), method, status, created_at |
| `service` | composite PK: owner + service + service_version |
| `audit_log` | immutable, BaFin-mandatory, who/what/when |

### Authorization Lifecycle

```
PENDING → IN_PROGRESS → AUTHORIZED | REJECTED | EXPIRED | CANCELLED
```

### APIs (OpenAPI 3.1)

1. `POST /v1/authorizations` — Initiate Transaction Authorization
2. `POST /v1/authorizations/{id}/attempts` — Submit Authorization Method
3. `PATCH /v1/authorizations/{id}/attempts` — Submit Authorization Credential
4. `GET /v1/authorizations/{id}/status` — Get Authorization Status (oneOf: Simple | Detailed)
5. `GET /v1/authorizations/{id}/methods/{method}/status` — Get Attempt Status
6. `GET /v1/authorizations/{id}/payload` — Get Payload (gated by `pvm-endpoints.enabled`)

### Authorization Methods

- `TAN_FROM_NEOAPP` (primary)
- `QR_RESPONSE` (specific to some response types)

### Dual Datasource (Atomikos XA)

- `tam` schema: core authorization data (PostgreSQL)
- `pvm` schema: payload vault (PostgreSQL, separate DB)
- Atomikos JTA coordinates 2PC across both
- `transactions.properties` on classpath controls Atomikos log location:

```properties
com.atomikos.icatch.log_base_dir=~/atomikos-logs/
com.atomikos.icatch.output_dir=~/atomikos-logs/
```

- `spring.jta.atomikos.*` YAML binding does **NOT** reliably reach the TM — use classpath file

### Atomikos Version

- Currently on 6.0.118 (upgraded from 5.0.9)
- Breaking change: `ConnectionPoolProperties` and `OrderedLifecycleComponent` removed in 6.x
- Fix: inject `DataSource` directly in health indicators, never reference Atomikos internal classes
- `DbHealthCheckIndicator` uses `dataSource.getConnection()`, not `DriverManager`

---

## Template Manager (TAM Sub-Feature)

Templates are reusable message blueprints with placeholders (e.g. SCA challenge messages).

### Template Lifecycle

```
DRAFT → ACTIVE → DEPRECATED → ARCHIVED
```

- Only ONE active version per template at a time (enforce at DB level with unique partial index)
- No rollback from ACTIVE → DRAFT
- Validation required before DRAFT → ACTIVE

### Template DB Tables (schema: `tam`)

| Table | Purpose |
|---|---|
| `template` | Master record. `status` IN ('DRAFT','ACTIVE','DEPRECATED','ARCHIVED'), `language` IN ('DE','EN','FR','NL') |
| `template_version` | Immutable snapshots. `lifecycle_status` IN ('DRAFT','ACTIVE','DEPRECATED'), `validation_status` IN ('PENDING','PASSED','FAILED') |
| `owner_system` | Which system owns the template |
| `category`, `tag`, `template_tag` | Taxonomy |
| `validation_result` | `status` IN ('PASSED','FAILED'), `type` IN ('SYNTAX','METADATA','PLACEHOLDER','COMPATIBILITY','BUSINESS') |
| `promotion_history` | `status` IN ('PENDING','SUCCESS','FAILED'), environments: ('DEV','SIT','UAT','PROD') |
| `audit_log` | BaFin-mandatory, immutable, `old_value`/`new_value` columns |

---

## Liquibase Conventions

- Format: YAML (not XML or SQL)
- Schema: explicit `schemaName: tam` on every `createTable`
- Numbering: `001-create-owner-system-table.yaml`, `002-...`
- Master changelog: `db/changelog/db.changelog-master.yaml`

### Mandatory Columns on All Tables

| Column | Definition |
|---|---|
| `id` | `UUID DEFAULT gen_random_uuid()` |
| `version` | optimistic locking |
| `is_deleted` | `BOOLEAN DEFAULT FALSE` |
| `deleted_by` | `VARCHAR(255)` |
| `deleted_at` | `TIMESTAMP WITH TIME ZONE` |
| `created_at` | `TIMESTAMP WITH TIME ZONE NOT NULL` |
| `created_by` | `VARCHAR(255) NOT NULL` |
| `updated_at` | `TIMESTAMP WITH TIME ZONE` |
| `updated_by` | `VARCHAR(255)` |

### Additional Rules

- Enums: use `checkConstraint` in Liquibase YAML (PostgreSQL check constraints)
- Rollback blocks: mandatory on every changeset
- `runOnChange: false` on schema changes
- Contexts: `local | dev | staging | prod`
- ⚠️ PII columns: tag with `remarks: "PII:GDPR"`
- ⚠️ BaFin: `audit_log` table mandatory from day 1 — cannot add retroactively

---

## Testing Standards

### Unit Tests

- Framework: JUnit 5 + Mockito + AssertJ
- Naming: `givenX_whenY_thenZ()`
- Always use explicit constructor injection in `@BeforeEach` — avoid `@InjectMocks` ambiguity
- Mock only the injected dependency at the method it directly calls (never mock private methods)
- Constants: define all test data as `private static final` at class top
- Verify: `verify(mock).method(args)` + `verifyNoInteractions(otherMock)` where relevant
- ⚠️ ObjectMapper import: use `com.fasterxml.jackson.databind.ObjectMapper`, **NOT** `tools.jackson.databind.ObjectMapper`

### Integration Tests

- Framework: `@SpringBootTest(RANDOM_PORT)` + `TestRestTemplate`
- DB: Testcontainers PostgreSQL (**NOT H2** — must match prod DB behavior)
- Downstream services: WireMock stubs only — no real network calls to intranet hosts
- Profile: `@ActiveProfiles("integration-test")`
- Base class: `IntegrationBaseTest` with `@DynamicPropertySource` overriding both datasource URLs
- WireMock stubs must set `Content-Type: application/json` — missing this causes `UnknownContentTypeException`
- Shared steps in `AuthorizationTestSteps` class
- Test data via `TestUtils.buildXxx()` factory methods
- Naming: `should_ReturnXXX_When_YYYCall()`
- ⚠️ `@ConditionalOnProperty(name = "pvm-endpoints.enabled", havingValue = "true")` must be `true` in integration profile or tests silently skip

### Gradle Integration Task

```groovy
tasks.register('integration', Test) {
    group = 'verification'
    description = 'Run Integration tests only'
    dependsOn 'compileTestJava'        // required — Gradle lazy task doesn't inherit this
    useJUnitPlatform()
    testClassesDirs = sourceSets.test.output.classesDirs
    classpath = sourceSets.test.runtimeClasspath
    include '**/integration/**'
    testLogging {
        events "passed", "skipped", "failed", "standardOut", "standardError"
        showStandardStreams = true
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}
```

- CI: always run `./gradlew compileTestJava integration`, not just `./gradlew integration`
- The 2-minute pause before tests is Spring context cold start — normal, context is cached afterward
- CI vs local failure gap: always check for WireMock stubs missing for intranet hosts (timeout = root cause)

---

## REST Client & OpenAPI Standards

### RestTemplate → RestClient Migration

- `RestTemplate` deprecated — use `RestClient` (Spring Boot 3.2+)
- Always set explicit `Accept: application/json` + `Content-Type: application/json` headers
- `@JsonIgnoreProperties(ignoreUnknown = true)` on all response DTOs
- Error decoders (Feign): null/non-JSON guard before `objectMapper.readValue()` — auth service errors may return HTML on 401/403
- Timeouts: `connectTimeout=3s`, `readTimeout=10s`
- All external calls wrapped in Resilience4j `CircuitBreaker`

### OpenAPI Generator (Gradle)

- Spec version: OpenAPI 3.1 (generator support still beta — some warnings expected)
- `mutualTLS` security scheme type: valid per 3.1 spec but generator doesn't support it yet
  - Workaround: filter warning in CI logs, keep spec correct, enforce mTLS at Spring Security layer
- `oneOf` + `discriminator`: child schemas must declare the discriminator property as a required field
  - Each variant must have `responseType` as a required string with enum constrained to its own value
- MapStruct + generated enums: when source enum has more values than target (e.g. `QR_RESPONSE` not valid for base response), use `@ValueMapping(target = MappingConstants.NULL, source = "QR_RESPONSE")` or `@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)`
- `discriminator` + `@JsonTypeInfo` in Spring Boot 4 / Jackson 2.17+: strict by default — discriminator property must be present and enum-constrained

### Date/Time

- All timestamps: `OffsetDateTime` (not `LocalDateTime`) — BaFin audit trails require unambiguous timestamps
- `birthDate`: use `format: date` in OpenAPI + `LocalDate` in Java (no timezone ambiguity)
- If timestamp without timezone must be accepted: custom `FlexibleDateTimeDeserializer` using `DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss[.SSS][.SS][.S]")`
- ⚠️ `birthDate` is PII — must not appear in logs

---

## Health Actuator

Custom health indicators following `AbstractHealthIndicator` pattern:

- `DbHealthCheckIndicator` — pool connection via `DataSource.getConnection()`
- `ArtemisHealthCheckIndicator` — `ConnectionFactory.createConnection()`
- `VaultHealthCheckIndicator` — `VaultTemplate.opsForSys().health()`
- `KobilHealthCheckIndicator` / `DraasHealthCheckIndicator` — HTTP ping via `RestClient`
- All use async `CompletableFuture` with timeout
- `getResult(future).orElse(null)` for object types — never `.orElse(false)` on non-boolean
- Never reference Atomikos internal classes (inject `DataSource`, not `AtomikosNonXADataSourceBean`)
- Actuator shows DOWN immediately when container for Artemis/Vault is stopped

---

## Messaging (JMS / Artemis)

### transauth-kobil (legacy WAR) → Artemis

- Uses ActiveMQ Classic client (OpenWire protocol)
- Artemis must have OpenWire enabled on port 61616:

```xml
<acceptor name="netty">tcp://0.0.0.0:61616?protocols=OPENWIRE,CORE,AMQP</acceptor>
```

- Broker URL must use Docker service name, not `127.0.0.1`:

```yaml
spring.activemq.broker-url: tcp://artemis:61616?jms.rmIdFromConnectionId=true&randomize=false
```

- All services must share the same Docker Compose network

### Outbox Pattern

- `TransactionalOutboxWorker` polls for pending audit events
- JMS + DB = two XA resources → Atomikos coordinates 2PC
- Outbox read phase: `@Transactional(readOnly = true, transactionManager = "tamTransactionManager")`
- Publish + delete phase: `@Transactional(transactionManager = "jtaTransactionManager")`
- ShedLock (or semaphore) to prevent parallel poller instances competing
- `borrowConnectionTimeout: 30` (not 5) to avoid pool exhaustion under load

---

## Security

### Certificate / Owner Resolution

- Owner MUST be derived from the certificate CN — never hardcoded fallback constants
- `resolveOwnerFromCertificate()` pattern: throw `CommonException(CERTIFICATE_OWNER_NOT_FOUND)` if CN is blank
- ⚠️ BaFin: hardcoded owner in audit records = wrong system identity attribution

### Credentials in Docker / Local Dev

- NEVER commit credentials to git — `.env` always gitignored
- `cacerts` and `keystore-local` distributed via secure channel (not git)
- Nexus credentials in Dockerfile ARGs must be explicitly passed — they don't auto-inherit
- Always check HTTP status of Nexus download: `--fail-with-body` not `-s` alone
- Validate WAR integrity after download: `unzip -t /app/app.war`

### Atomikos Transaction Logs (Security)

- `tmlog*.log` in project root = gitignored but still created locally
- Redirect via `transactions.properties` on classpath:

```properties
com.atomikos.icatch.log_base_dir=~/atomikos-logs/
com.atomikos.icatch.output_dir=~/atomikos-logs/
```

- JVM flag alternative (local dev only): `-Dcom.atomikos.icatch.enable_logging=false`
- ⚠️ Never disable recovery logging in any deployed environment

---

## Git / Branch Conventions

- Branch naming follows ticket/feature conventions
- To create branch from develop with code from another branch:

```bash
git checkout develop && git pull origin develop
git checkout -b new-branch-name
git merge origin/old-branch-name
git push -u origin new-branch-name
```

- Rotate credentials immediately if they appear in screenshots shared in chat

---

## Compliance — Always Flag These

### BaFin

- ⚠️ Audit log immutable, 10-year retention, `old_value`/`new_value` columns mandatory
- ⚠️ `created_by` must come from JWT/cert, never a free-text string field
- ⚠️ Authorization records must have correct system identity (cert CN, not hardcoded constant)
- ⚠️ CI runners must NOT reach production-adjacent intranet systems (use WireMock stubs)

### GDPR

- ⚠️ `crmCustomerNumber` is PII — must not appear in logs
- ⚠️ `birthDate` is PII — must not appear in logs
- ⚠️ All PII columns tagged `remarks: "PII:GDPR"` in Liquibase

### PSD2/SCA

- ⚠️ Strong Customer Authentication for payment initiation — TAN, QR, certificate-based
- ⚠️ Owner derived from mTLS certificate CN — never fallback to hardcoded value

### PCI DSS

- Minimize cardholder data scope per service; tokenization where possible

---

## How to Respond

- **Be direct and technical** — treat as senior Java developer, no hand-holding
- **Always show before/after code** for migration advice
- **Flag compliance risks with ⚠️**
- **When reviewing code**: Java version issues first → Spring Boot issues → architecture → compliance
- **Proactively note** risks the user didn't ask about
- Use **concise markdown** — headers, code blocks, bullets
- For long migrations, produce a **numbered checklist**
- **Gradle, not Maven** — this project uses Gradle build files
- **Romanian is acceptable** — developer sometimes communicates in Romanian; respond in the same language used
