@PostConstruct
public void logActiveMqProperties() {
    log.info("=== ActiveMQ Config ===");
    log.info("Broker URL: {}", activeMqProperties.getBrokerUrl());
    log.info("Username: {}", activeMqProperties.getUsername());
    log.info("Password: {}", activeMqProperties.getPassword());
    log.info("EnableMQ: {}", activeMqProperties.isEnableMQ());
    log.info("======================");
}
`- column:
    name: business_slug
    type: VARCHAR2(90)
    valueComputed: >-
      owner || '-' || service || '-' || service_version
    constraints:
      nullable: false
``
- changeSet:
    id: 003-add-business-slug-to-services-table
    author: alexandru.piciorus
    changes:
      - addColumn:
          tableName: services
          schemaName: tam
          columns:
            - column:
                name: business_slug
                type: VARCHAR2(90)
                constraints:
                  nullable: false

      - addCheckConstraint:
          tableName: services
          schemaName: tam
          constraintName: chk_business_slug_format
          constraintBody: "business_slug = owner || '-' || service || '-' || service_version"

      - createProcedure:
          procedureName: trg_services_business_slug
          schemaName: tam

      - sql:
          sql: |
            CREATE OR REPLACE TRIGGER tam.trg_services_business_slug
            BEFORE INSERT OR UPDATE ON tam.services
            FOR EACH ROW
            BEGIN
              :NEW.business_slug := :NEW.owner || '-' || :NEW.service || '-' || :NEW.service_version;
            END;

```

```
- changeSet:
    id: 003-add-business-slug-to-services-table
    author: alexandru.piciorus
    changes:
      - sql:
          sql: >
            ALTER TABLE tam.services 
            ADD business_slug VARCHAR2(90) 
            GENERATED ALWAYS AS (owner || '-' || service || '-' || service_version) VIRTUAL;
```


```
databaseChangeLog:
  - changeSet:
      id: 002-create-services-table
      author: vlad.pop@externe.bnpparibas.com
      changes:
        - createTable:
            tableName: services
            schemaName: tam
            columns:
              - column:
                  name: owner
                  type: VARCHAR2(30)
                  constraints:
                    nullable: false
              - column:
                  name: service
                  type: VARCHAR2(50)
                  constraints:
                    nullable: false
              - column:
                  name: service_version
                  type: VARCHAR2(5)
                  constraints:
                    nullable: false
              - column:
                  name: is_deleted
                  type: NUMBER(1)
                  defaultValue: 0
                  constraints:
                    nullable: false
              - column:                          # ← adaugă direct aici
                  name: business_slug
                  type: VARCHAR2(90)
                  constraints:
                    nullable: false

  - changeSet:
      id: 003-add-business-slug-trigger
      author: alexandru.piciorus
      changes:
        - sql:
            sql: >
              CREATE OR REPLACE TRIGGER tam.trg_services_business_slug
              BEFORE INSERT OR UPDATE ON tam.services
              FOR EACH ROW
              BEGIN
                :NEW.business_slug := :NEW.owner || '-' || :NEW.service || '-' || :NEW.service_version;
              END;
```
