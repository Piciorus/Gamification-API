```
# transauth-kobil — Certificate Store

This directory contains the TLS certificate files required to run the `transauth-kobil` legacy WAR inside Docker.

These files are **NOT committed to git** (see `.gitignore`).
Request them from the team via a **secure channel** (e.g. encrypted mail, Vault, secure share).

---

## Required files

| File | Description |
|---|---|
| `cacerts` | Java truststore (JKS) — contains the CA chain trusted by transauth-kobil |
| `keystore-local` | Application keystore — client certificate + private key for mTLS |

---

## Directory structure

docker-compose/
└── transauth-kobil/
├── certs/
│ ├── README.md ← you are here
│ ├── cacerts ← NOT in git — obtain via secure channel
│ └── keystore-local ← NOT in git — obtain via secure channel
└── Dockerfile


---

## Why these files are needed

`transauth-kobil` is a legacy WAR deployed on an older JVM image.
It communicates with downstream services over **mTLS** and requires:

1. **`cacerts`** — so the JVM trusts the internal CA chain (Consorsbank / BNP Paribas PKI).
   Replaces the default JDK truststore.
2. **`keystore-local`** — so the application can present its own client certificate
   when establishing outbound mTLS connections.

Without these files the container will start but **all outbound TLS handshakes will fail**.

---

## Volume mounts (compose.yaml reference)

```yaml
transauth-kobil:
  platform: linux/amd64
  volumes:
    - ./docker-compose/transauth-kobil/certs/cacerts:/usr/local/openjdk-11/lib/security/cacerts:ro
    - ./docker-compose/transauth-kobil/certs/keystore-local:/app/keystore-local:ro
```

---

## How to obtain

Contact the team lead or check the team's secure secret-sharing channel.
Do **not** request or transmit these files over unencrypted channels (Slack, Teams, plain email, etc.).

---

## .gitignore entries (verify these exist)

docker-compose/transauth-kobil/certs/cacerts
docker-compose/transauth-kobil/certs/keystore-local


> ⚠️ If you accidentally commit either file, rotate the certificates immediately
> and notify the security team. Git history must be purged (`git filter-repo`).

```
