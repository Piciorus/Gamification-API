# transauth-kobil — Certificate Store

This directory contains the TLS certificate files required to run the
`transauth-kobil` legacy WAR inside Docker.

These files are **NOT committed to git** (see `.gitignore`).  
Request them from the team via a **secure channel** (e.g. encrypted mail, Vault, secure share).

---

## Required files

| File | Description | Mount target in container |
|---|---|---|
| `cacerts` | Java truststore (JKS) — contains the CA chain trusted by transauth-kobil | `/usr/local/openjdk-11/lib/security/cacerts` |
| `keystore-local` | Application keystore (JKS/PKCS12) — client certificate + private key for mTLS | `/app/keystore-local` |

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

## How to obtain

Contact the team lead (Vlad) or check the team's secure secret-sharing channel.  
Do **not** request or transmit these files over unencrypted channels (email without PGP, Slack, Teams, etc.).

---

## Why these files are needed

`transauth-kobil` is a legacy WAR deployed on an older JVM image.  
It communicates with downstream services over **mTLS** and needs:

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

> ⚠️ Paths above are illustrative — verify against the actual `Dockerfile` `COPY` / `ENV` directives.

---

## .gitignore entry (verify this exists)

docker-compose/transauth-kobil/certs/cacerts
docker-compose/transauth-kobil/certs/keystore-local


> ⚠️ **Security**: if you accidentally commit either file, rotate the certificates immediately  
> and notify the security team. Git history must be cleaned (`git filter-repo`).
