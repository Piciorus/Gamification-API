```
Looking at your images, I can see:

The existing TAM Liquibase pattern (images 16-17): schema tam, YAML format, numbered files, TIMESTAMP WITH TIME ZONE, soft deletes with is_deleted/deleted_by/deleted_at, optimistic locking via version column
The Templates Manager DB schema (images 7-14): all tables clearly visible

My recommendation on scope: Yes, create all of them. Here's why:

TEMPLATE + TEMPLATE_VERSION → core, obviously needed
OWNER_SYSTEM → needed for FK integrity from day 1
CATEGORY + TAG + TEMPLATE_TAG → taxonomy; without these you can't test search/filtering
VALIDATION_RESULT → needed to test Activate flow (step 3 stores validation result)
AUDIT_LOG → mandatory from day 1 ⚠️ BaFin — you cannot add this later and claim compliance retroactively
PROMOTION_HISTORY → debatable; skip it for now if env promotion is out of scope for the first sprint

Now let me generate the scripts following your exact pattern:

9 migration scripts + master changelog. A few design decisions worth noting:

Matches your existing TAM pattern exactly: TIMESTAMP WITH TIME ZONE, NUMBER(1) for booleans, VARCHAR2, soft delete columns, optimistic locking version column, tam schema.

Circular FK handled in 006: template.active_version_id → template_version can only be added after template_version exists, so it's a separate changeSet. This is the correct Liquibase approach.

TEMPLATE_VERSION and AUDIT_LOG are intentionally immutable — no updated_*/deleted_*/version columns. Commented in the YAML so reviewers don't "fix" it.

AUDIT_LOG has no FKs on template_id/version_id by design — audit records must survive template deletion. If you FK them, a future DELETE on template would cascade-block or wipe audit history.

Things to verify with your team:

old_value/new_value as JSONB — your existing TAM tables use VARCHAR2 for these; check if your PostgreSQL version and Liquibase dialect handle JSONB or if you prefer TEXT
author field — I used a placeholder, update to your actual email
Whether PROMOTION_HISTORY lands in this sprint or the next

```


```
databaseChangeLog:
  - changeSet:
      id: 001-create-owner-system-table
      author: piciorus.alexandru@externe.bnpparibas.com
      changes:
        - createTable:
            tableName: owner_system
            schemaName: tam
            columns:
              - column:
                  name: id
                  type: UUID
                  defaultValueComputed: gen_random_uuid()
                  constraints:
                    nullable: false
              - column:
                  name: system_name
                  type: VARCHAR2(100)
                  constraints:
                    nullable: false
              - column:
                  name: description
                  type: VARCHAR2(500)
                  constraints:
                    nullable: true
              - column:
                  name: status
                  type: VARCHAR2(20)
                  defaultValue: ACTIVE
                  constraints:
                    nullable: false
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
                  constraints:
                    nullable: true
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
                  constraints:
                    nullable: true
              - column:
                  name: version
                  type: NUMBER(19)
                  defaultValue: 1
                  constraints:
                    nullable: false
        - addPrimaryKey:
            tableName: owner_system
            schemaName: tam
            columnNames: id
            primaryKeyName: pk_tam_owner_system_id
        - addUniqueConstraint:
            tableName: owner_system
            schemaName: tam
            columnNames: system_name
            constraintName: uq_tam_owner_system_name
        - sql:
            sql: ALTER TABLE tam.owner_system ADD CONSTRAINT chk_tam_owner_system_status CHECK (status IN ('ACTIVE', 'INACTIVE'));

```




```
databaseChangeLog:
  - changeSet:
      id: 002-create-category-table
      author: piciorus.alexandru@externe.bnpparibas.com
      changes:
        - createTable:
            tableName: category
            schemaName: tam
            columns:
              - column:
                  name: id
                  type: UUID
                  defaultValueComputed: gen_random_uuid()
                  constraints:
                    nullable: false
              - column:
                  name: name
                  type: VARCHAR2(100)
                  constraints:
                    nullable: false
              - column:
                  name: description
                  type: VARCHAR2(500)
                  constraints:
                    nullable: true
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
                  constraints:
                    nullable: true
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
                  constraints:
                    nullable: true
              - column:
                  name: version
                  type: NUMBER(19)
                  defaultValue: 1
                  constraints:
                    nullable: false
        - addPrimaryKey:
            tableName: category
            schemaName: tam
            columnNames: id
            primaryKeyName: pk_tam_category_id
        - addUniqueConstraint:
            tableName: category
            schemaName: tam
            columnNames: name
            constraintName: uq_tam_category_name

```


```
databaseChangeLog:
  - changeSet:
      id: 003-create-tag-table
      author: piciorus.alexandre@externe.bnpparibas.com
      changes:
        - createTable:
            tableName: tag
            schemaName: tam
            columns:
              - column:
                  name: id
                  type: UUID
                  defaultValueComputed: gen_random_uuid()
                  constraints:
                    nullable: false
              - column:
                  name: value
                  type: VARCHAR2(100)
                  constraints:
                    nullable: false
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
                  constraints:
                    nullable: true
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
                  constraints:
                    nullable: true
              - column:
                  name: version
                  type: NUMBER(19)
                  defaultValue: 1
                  constraints:
                    nullable: false
        - addPrimaryKey:
            tableName: tag
            schemaName: tam
            columnNames: id
            primaryKeyName: pk_tam_tag_id
        - addUniqueConstraint:
            tableName: tag
            schemaName: tam
            columnNames: value
            constraintName: uq_tam_tag_value

```


```
databaseChangeLog:
  - changeSet:
      id: 004-create-template-table
      author: piciorus.alexandru@externe.bnpparibas.com
      changes:
        - createTable:
            tableName: template
            schemaName: tam
            columns:
              - column:
                  name: id
                  type: UUID
                  defaultValueComputed: gen_random_uuid()
                  constraints:
                    nullable: false
              - column:
                  name: owner_system_id
                  type: UUID
                  constraints:
                    nullable: false
              - column:
                  name: category_id
                  type: UUID
                  constraints:
                    nullable: true
              # active_version_id FK added after template_version table exists (see 006)
              - column:
                  name: active_version_id
                  type: UUID
                  constraints:
                    nullable: true
              - column:
                  name: code
                  type: VARCHAR2(100)
                  constraints:
                    nullable: false
              - column:
                  name: language
                  type: VARCHAR2(10)
                  constraints:
                    nullable: false
              - column:
                  name: name
                  type: VARCHAR2(255)
                  constraints:
                    nullable: false
              - column:
                  name: status
                  type: VARCHAR2(20)
                  defaultValue: DRAFT
                  constraints:
                    nullable: false
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
                  constraints:
                    nullable: true
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
                  constraints:
                    nullable: true
              - column:
                  name: version
                  type: NUMBER(19)
                  defaultValue: 1
                  constraints:
                    nullable: false
        - addPrimaryKey:
            tableName: template
            schemaName: tam
            columnNames: id
            primaryKeyName: pk_tam_template_id
        # Business key: code+language unique per owner — matches UCR composite BK
        - addUniqueConstraint:
            tableName: template
            schemaName: tam
            columnNames: owner_system_id, code, language
            constraintName: uq_tam_template_code_language_owner
        - addForeignKeyConstraint:
            baseTableSchemaName: tam
            baseTableName: template
            baseColumnNames: owner_system_id
            referencedTableSchemaName: tam
            referencedTableName: owner_system
            referencedColumnNames: id
            constraintName: fk_tam_template_owner_system
            onDelete: RESTRICT
        - addForeignKeyConstraint:
            baseTableSchemaName: tam
            baseTableName: template
            baseColumnNames: category_id
            referencedTableSchemaName: tam
            referencedTableName: category
            referencedColumnNames: id
            constraintName: fk_tam_template_category
            onDelete: SET NULL
        - sql:
            sql: ALTER TABLE tam.template ADD CONSTRAINT chk_tam_template_status CHECK (status IN ('DRAFT', 'ACTIVE', 'DEPRECATED', 'ARCHIVED'));
        - createIndex:
            indexName: idx_tam_template_owner
            tableName: template
            schemaName: tam
            columns:
              - column:
                  name: owner_system_id
        - createIndex:
            indexName: idx_tam_template_status
            tableName: template
            schemaName: tam
            columns:
              - column:
                  name: status
        - createIndex:
            indexName: idx_tam_template_name
            tableName: template
            schemaName: tam
            columns:
              - column:
                  name: name
        - createIndex:
            indexName: idx_tam_template_category
            tableName: template
            schemaName: tam
            columns:
              - column:
                  name: category_id

```


```
databaseChangeLog:
  - changeSet:
      id: 005-create-template-version-table
      author: piciorus.alexandru@externe.bnpparibas.com
      # IMMUTABLE records — no updated_at/updated_by/deleted_*/version columns by design.
      # Updates are forbidden; every change creates a new version row.
      changes:
        - createTable:
            tableName: template_version
            schemaName: tam
            columns:
              - column:
                  name: id
                  type: UUID
                  defaultValueComputed: gen_random_uuid()
                  constraints:
                    nullable: false
              - column:
                  name: template_id
                  type: UUID
                  constraints:
                    nullable: false
              - column:
                  name: version_number
                  type: NUMBER(19)
                  constraints:
                    nullable: false
              - column:
                  name: template_content
                  type: TEXT
                  constraints:
                    nullable: false
              - column:
                  name: checksum
                  type: VARCHAR2(64)
                  constraints:
                    nullable: false
              - column:
                  name: lifecycle_status
                  type: VARCHAR2(20)
                  defaultValue: DRAFT
                  constraints:
                    nullable: false
              - column:
                  name: validation_status
                  type: VARCHAR2(20)
                  defaultValue: PENDING
                  constraints:
                    nullable: false
              - column:
                  name: created_at
                  type: TIMESTAMP WITH TIME ZONE
                  defaultValueComputed: CURRENT_TIMESTAMP
                  constraints:
                    nullable: false
              - column:
                  name: created_by
                  type: VARCHAR2(25)
                  constraints:
                    nullable: false
        - addPrimaryKey:
            tableName: template_version
            schemaName: tam
            columnNames: id
            primaryKeyName: pk_tam_template_version_id
        - addUniqueConstraint:
            tableName: template_version
            schemaName: tam
            columnNames: template_id, version_number
            constraintName: uq_tam_template_version_number
        - addForeignKeyConstraint:
            baseTableSchemaName: tam
            baseTableName: template_version
            baseColumnNames: template_id
            referencedTableSchemaName: tam
            referencedTableName: template
            referencedColumnNames: id
            constraintName: fk_tam_template_version_template
            onDelete: RESTRICT
        - sql:
            sql: ALTER TABLE tam.template_version ADD CONSTRAINT chk_tam_template_version_lifecycle CHECK (lifecycle_status IN ('DRAFT', 'ACTIVE', 'DEPRECATED'));
        - sql:
            sql: ALTER TABLE tam.template_version ADD CONSTRAINT chk_tam_template_version_validation CHECK (validation_status IN ('PENDING', 'PASSED', 'FAILED'));
        - createIndex:
            indexName: idx_tam_template_version_template
            tableName: template_version
            schemaName: tam
            columns:
              - column:
                  name: template_id
        - createIndex:
            indexName: idx_tam_template_version_status
            tableName: template_version
            schemaName: tam
            columns:
              - column:
                  name: lifecycle_status
        - createIndex:
            indexName: idx_tam_template_version_number
            tableName: template_version
            schemaName: tam
            columns:
              - column:
                  name: template_id
              - column:
                  name: version_number
```


```
databaseChangeLog:
  - changeSet:
      id: 006-add-template-active-version-fk
      author: piciorus.alexandru@externe.bnpparibas.com
      # Circular FK resolved: template.active_version_id -> template_version
      # Only possible after template_version table is created (changeSet 005)
      changes:
        - addForeignKeyConstraint:
            baseTableSchemaName: tam
            baseTableName: template
            baseColumnNames: active_version_id
            referencedTableSchemaName: tam
            referencedTableName: template_version
            referencedColumnNames: id
            constraintName: fk_tam_template_active_version
            onDelete: SET NULL

```


```

databaseChangeLog:
  - changeSet:
      id: 007-create-template-tag-table
      author: piciorus.alexandru@externe.bnpparibas.com
      changes:
        - createTable:
            tableName: template_tag
            schemaName: tam
            columns:
              - column:
                  name: template_id
                  type: UUID
                  constraints:
                    nullable: false
              - column:
                  name: tag_id
                  type: UUID
                  constraints:
                    nullable: false
        - addPrimaryKey:
            tableName: template_tag
            schemaName: tam
            columnNames: template_id, tag_id
            primaryKeyName: pk_tam_template_tag
        - addForeignKeyConstraint:
            baseTableSchemaName: tam
            baseTableName: template_tag
            baseColumnNames: template_id
            referencedTableSchemaName: tam
            referencedTableName: template
            referencedColumnNames: id
            constraintName: fk_tam_template_tag_template
            onDelete: CASCADE
        - addForeignKeyConstraint:
            baseTableSchemaName: tam
            baseTableName: template_tag
            baseColumnNames: tag_id
            referencedTableSchemaName: tam
            referencedTableName: tag
            referencedColumnNames: id
            constraintName: fk_tam_template_tag_tag
            onDelete: RESTRICT
```


```
databaseChangeLog:
  - changeSet:
      id: 008-create-validation-result-table
      author: piciorus.alexandru@externe.bnpparibas.com
      # IMMUTABLE — append-only validation history per version.
      # No updates, no soft delete; validation history must be preserved for audit.
      changes:
        - createTable:
            tableName: validation_result
            schemaName: tam
            columns:
              - column:
                  name: id
                  type: UUID
                  defaultValueComputed: gen_random_uuid()
                  constraints:
                    nullable: false
              - column:
                  name: version_id
                  type: UUID
                  constraints:
                    nullable: false
              - column:
                  name: type
                  type: VARCHAR2(50)
                  constraints:
                    nullable: false
              - column:
                  name: status
                  type: VARCHAR2(20)
                  constraints:
                    nullable: false
              - column:
                  name: error_details
                  type: TEXT
                  constraints:
                    nullable: true
              - column:
                  name: executed_at
                  type: TIMESTAMP WITH TIME ZONE
                  defaultValueComputed: CURRENT_TIMESTAMP
                  constraints:
                    nullable: false
              - column:
                  name: executed_by
                  type: VARCHAR2(25)
                  constraints:
                    nullable: false
        - addPrimaryKey:
            tableName: validation_result
            schemaName: tam
            columnNames: id
            primaryKeyName: pk_tam_validation_result_id
        - addForeignKeyConstraint:
            baseTableSchemaName: tam
            baseTableName: validation_result
            baseColumnNames: version_id
            referencedTableSchemaName: tam
            referencedTableName: template_version
            referencedColumnNames: id
            constraintName: fk_tam_validation_result_version
            onDelete: RESTRICT
        - sql:
            sql: ALTER TABLE tam.validation_result ADD CONSTRAINT chk_tam_validation_result_status CHECK (status IN ('PASSED', 'FAILED'));
        - createIndex:
            indexName: idx_tam_validation_result_version
            tableName: validation_result
            schemaName: tam
            columns:
              - column:
                  name: version_id

```


```
databaseChangeLog:
  - changeSet:
      id: 009-create-audit-log-table
      author: piciorus.alexandru@externe.bnpparibas.com
      # IMMUTABLE, APPEND-ONLY — BaFin compliance requires:
      # - Complete history of all administrative operations on templates
      # - Minimum 10-year retention
      # - No UPDATE or DELETE ever permitted on this table
      # old_value / new_value stored as JSONB for flexible field-level delta capture
      changes:
        - createTable:
            tableName: audit_log
            schemaName: tam
            columns:
              - column:
                  name: id
                  type: UUID
                  defaultValueComputed: gen_random_uuid()
                  constraints:
                    nullable: false
              - column:
                  name: template_id
                  type: UUID
                  constraints:
                    nullable: false
              - column:
                  name: version_id
                  type: UUID
                  constraints:
                    nullable: true
              - column:
                  name: operation
                  type: VARCHAR2(50)
                  constraints:
                    nullable: false
              - column:
                  name: actor
                  type: VARCHAR2(100)
                  constraints:
                    nullable: false
              - column:
                  name: timestamp
                  type: TIMESTAMP WITH TIME ZONE
                  defaultValueComputed: CURRENT_TIMESTAMP
                  constraints:
                    nullable: false
              - column:
                  name: old_value
                  type: JSONB
                  constraints:
                    nullable: true
              - column:
                  name: new_value
                  type: JSONB
                  constraints:
                    nullable: true
        - addPrimaryKey:
            tableName: audit_log
            schemaName: tam
            columnNames: id
            primaryKeyName: pk_tam_audit_log_id
        # NOTE: FKs intentionally omitted on template_id / version_id.
        # Audit records must survive template deletion — no CASCADE risk.
        - sql:
            sql: >
              ALTER TABLE tam.audit_log ADD CONSTRAINT chk_tam_audit_log_operation
              CHECK (operation IN (
                'CREATE_TEMPLATE', 'UPDATE_TEMPLATE', 'DELETE_TEMPLATE',
                'CREATE_VERSION', 'ACTIVATE_VERSION', 'DEPRECATE_VERSION',
                'VALIDATE_VERSION', 'PROMOTE_TEMPLATE'
              ));
        - createIndex:
            indexName: idx_tam_audit_log_template
            tableName: audit_log
            schemaName: tam
            columns:
              - column:
                  name: template_id
        - createIndex:
            indexName: idx_tam_audit_log_timestamp
            tableName: audit_log
            schemaName: tam
            columns:
              - column:
                  name: timestamp
        - createIndex:
            indexName: idx_tam_audit_log_actor
            tableName: audit_log
            schemaName: tam
            columns:
              - column:
                  name: actor


```


```
databaseChangeLog:
  - include:
      file: tam.migrations.v1.0/001-create-owner-system-table.yaml
      relativeToChangelogFile: true
  - include:
      file: tam.migrations.v1.0/002-create-category-table.yaml
      relativeToChangelogFile: true
  - include:
      file: tam.migrations.v1.0/003-create-tag-table.yaml
      relativeToChangelogFile: true
  - include:
      file: tam.migrations.v1.0/004-create-template-table.yaml
      relativeToChangelogFile: true
  - include:
      file: tam.migrations.v1.0/005-create-template-version-table.yaml
      relativeToChangelogFile: true
  - include:
      file: tam.migrations.v1.0/006-add-template-active-version-fk.yaml
      relativeToChangelogFile: true
  - include:
      file: tam.migrations.v1.0/007-create-template-tag-table.yaml
      relativeToChangelogFile: true
  - include:
      file: tam.migrations.v1.0/008-create-validation-result-table.yaml
      relativeToChangelogFile: true
  - include:
      file: tam.migrations.v1.0/009-create-audit-log-table.yaml
      relativeToChangelogFile: true


```
