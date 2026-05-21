```
services:

  oracle-db:
    image: gvenzl/oracle-xe:21-slim
    container_name: oracle-local-trauth
    environment:
      ORACLE_PASSWORD: 12345
      ORACLE_DATABASE: XEPDB1
    ports:
      - "1521:1521"
    volumes:
      - oracle-data:/opt/oracle/oradata
      - ./init-oracle.sql:/container-entrypoint-initdb.d/init.sql
    healthcheck:
      test: ["CMD", "healthcheck.sh"]
      interval: 30s
      timeout: 10s
      retries: 10
    networks:
      - app-network

  vault:
    image: hashicorp/vault:latest
    container_name: vault-local
    environment:
      VAULT_DEV_ROOT_TOKEN_ID: root-token
      VAULT_DEV_LISTEN_ADDRESS: "0.0.0.0:8200"
    ports:
      - "8200:8200"
    cap_add:
      - IPC_LOCK
    command: vault server -dev
    healthcheck:
      test: ["CMD", "vault", "status"]
      interval: 10s
      timeout: 5s
      retries: 5
    networks:
      - app-network

  vault-init:
    image: hashicorp/vault:latest
    container_name: vault-init
    depends_on:
      vault:
        condition: service_healthy
    environment:
      VAULT_ADDR: "http://vault:8200"
      VAULT_TOKEN: "root-token"
    command: >
      sh -c "
        vault secrets enable transit || true &&
        vault secrets enable -path=secret kv || true &&
        vault write -f transit/keys/abc || true &&
        vault kv put secret/local/trauth-sc/credentials
          db_username=system
          db_password=12345
      "
    networks:
      - app-network

  activemq:
    image: apache/activemq-artemis:latest
    container_name: activemq-local
    environment:
      ARTEMIS_USER: localUser
      ARTEMIS_PASSWORD: 12345
    ports:
      - "61616:61616"
      - "8161:8161"
    volumes:
      - activemq-data:/var/lib/artemis-instance
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8161"]
      interval: 15s
      timeout: 5s
      retries: 5
    networks:
      - app-network

volumes:
  oracle-data:
  activemq-data:

networks:
  app-network:
    driver: bridge

```

