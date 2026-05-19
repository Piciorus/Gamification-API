```
databaseChangeLog:
  - changeSet:
      id: add-audit-columns-${audit.table}
      author: vlad.pop@externe.bnpparibas.com
      changes:
        - addColumn:
            tableName: ${audit.table}
            schemaName: ${audit.schema}
            columns:
              - column:
                  name: is_deleted
                  type: NUMBER(1)
                  defaultValueNumeric: 0
                  constraints:
                    nullable: false
              - column:
                  name: created_by
                  type: VARCHAR2(25)
                  constraints:
                    nullable: false
              - column:
                  name: updated_by
                  type: VARCHAR2(25)
                  constraints:
                    nullable: false
              - column:
                  name: deleted_by
                  type: VARCHAR2(25)
              - column:
                  name: created_at
                  type: TIMESTAMP WITH TIME ZONE
                  defaultValueComputed: CURRENT_TIMESTAMP
                  constraints:
                    nullable: false
              - column:
                  name: updated_at
                  type: TIMESTAMP WITH TIME ZONE
                  defaultValueComputed: CURRENT_TIMESTAMP
                  constraints:
                    nullable: false
              - column:
                  name: deleted_at
                  type: TIMESTAMP WITH TIME ZONE
              - column:
                  name: version
                  type: NUMBER(19)
                  defaultValueNumeric: 1
                  constraints:
                    nullable: false

```
```
databaseChangeLog:

  - include:
      file: classpath:/db/tam/migrations/v1.0/001-create-authorization-methods-table.yaml
  - property:
      name: audit.table
      value: authorization_methods
  - property:
      name: audit.schema
      value: tam
  - include:
      file: classpath:/db/tam/migrations/v1.0/common/000-inject-audit-columns.yaml

  - include:
      file: classpath:/db/tam/migrations/v1.0/002-create-services-table.yaml
  - property:
      name: audit.table
      value: services
  - include:
      file: classpath:/db/tam/migrations/v1.0/common/000-inject-audit-columns.yaml

  # ... restul la fel
```
