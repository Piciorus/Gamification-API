```
# Un singur fisier care adauga audit columns la TOATE tabelele
databaseChangeLog:
  - changeSet:
      id: audit-001-add-audit-columns-to-all-tables
      author: vlad.pop@externe.bnpparibas.com
      changes:

        # ========== authorization_methods ==========
        - addColumn:
            tableName: authorization_methods
            schemaName: tam
            columns:
              - column:
                  name: is_deleted
                  type: NUMBER(1)
                  defaultValue: 0
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
                  defaultValue: CURRENT_TIMESTAMP
                  constraints:
                    nullable: false
              - column:
                  name: updated_at
                  type: TIMESTAMP WITH TIME ZONE
                  defaultValue: CURRENT_TIMESTAMP
                  constraints:
                    nullable: false
              - column:
                  name: deleted_at
                  type: TIMESTAMP WITH TIME ZONE
              - column:
                  name: version
                  type: NUMBER(19)
                  defaultValue: 1
                  constraints:
                    nullable: false

        # ========== services ==========
        - addColumn:
            tableName: services
            schemaName: tam
            columns:
              - column:
                  name: is_deleted
                  type: NUMBER(1)
                  defaultValue: 0
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
                  defaultValue: CURRENT_TIMESTAMP
                  constraints:
                    nullable: false
              - column:
                  name: updated_at
                  type: TIMESTAMP WITH TIME ZONE
                  defaultValue: CURRENT_TIMESTAMP
                  constraints:
                    nullable: false
              - column:
                  name: deleted_at
                  type: TIMESTAMP WITH TIME ZONE
              - column:
                  name: version
                  type: NUMBER(19)
                  defaultValue: 1
                  constraints:
                    nullable: false

        # ========== authorization_attempts ==========
        - addColumn:
            tableName: authorization_attempts
            schemaName: tam
            columns:
              - column:
                  name: is_deleted
                  type: NUMBER(1)
                  defaultValue: 0
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
                  defaultValue: CURRENT_TIMESTAMP
                  constraints:
                    nullable: false
              - column:
                  name: updated_at
                  type: TIMESTAMP WITH TIME ZONE
                  defaultValue: CURRENT_TIMESTAMP
                  constraints:
                    nullable: false
              - column:
                  name: deleted_at
                  type: TIMESTAMP WITH TIME ZONE
              - column:
                  name: version
                  type: NUMBER(19)
                  defaultValue: 1
                  constraints:
                    nullable: false

```

