```
version: '3.8'

services:

  # Oracle Database
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

  # HashiCorp Vault
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

  # Vault Init - configures secrets after vault starts
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

  # ActiveMQ Artemis
  activemq:
    image: apache/activemq-artemis:latest
    container_name: activemq-local
    environment:
      ARTEMIS_USER: localUser
      ARTEMIS_PASSWORD: 12345
    ports:
      - "61616:61616"   # TCP broker
      - "8161:8161"     # Web console
    volumes:
      - activemq-data:/var/lib/artemis-instance
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8161"]
      interval: 15s
      timeout: 5s
      retries: 5
    networks:
      - app-network

  # Spring Boot Application
  app:
    build:
      context: .
      dockerfile: Dockerfile
    container_name: trauth-app
    depends_on:
      oracle-db:
        condition: service_healthy
      vault:
        condition: service_healthy
      activemq:
        condition: service_healthy
    environment:
      SPRING_PROFILES_ACTIVE: local-development-non-ssl
      VAULT_TOKEN: root-token
      SPRING_VAULT_URI: http://vault:8200
      SPRING_DATASOURCE_PVM_URL: jdbc:oracle:thin:@oracle-db:1521/XEPDB1
      SPRING_DATASOURCE_TAM_URL: jdbc:oracle:thin:@oracle-db:1521/XEPDB1
      SPRING_DATASOURCE_PVM_USERNAME: PVM
      SPRING_DATASOURCE_PVM_PASSWORD: xx
      SPRING_DATASOURCE_TAM_USERNAME: TAM
      SPRING_DATASOURCE_TAM_PASSWORD: xx
      SPRING_ARTEMIS_BROKER_URL: tcp://activemq:61616
      JAVA_MEM_OPTIONS: "-Xms256m -Xmx512m"
    ports:
      - "8323:8323"   # App
      - "9323:9323"   # Actuator/management
    networks:
      - app-network

volumes:
  oracle-data:
  activemq-data:

networks:
  app-network:
    driver: bridge
```

```
-- Create PVM user
CREATE USER PVM IDENTIFIED BY xx;
GRANT CONNECT, RESOURCE TO PVM;
GRANT CREATE SESSION, CREATE TABLE, CREATE SEQUENCE TO PVM;
ALTER USER PVM DEFAULT TABLESPACE USERS;
ALTER USER PVM QUOTA UNLIMITED ON USERS;

-- Create TAM user
CREATE USER TAM IDENTIFIED BY xx;
GRANT CONNECT, RESOURCE TO TAM;
GRANT CREATE SESSION, CREATE TABLE, CREATE SEQUENCE TO TAM;
ALTER USER TAM DEFAULT TABLESPACE USERS;
ALTER USER TAM QUOTA UNLIMITED ON USERS;

ALTER SESSION SET TIME_ZONE = 'UTC';
```
