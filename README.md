```
databaseChangeLog:
  - changeSet:
      id: BCKNDCORE-7703-add-columns-validation-results-table
      author: piciorus.alexandru@externe.bnpparibas.com (cs32610)
      changes:
        - addColumn:
            tableName: validation_results
            schemaName: tam
            columns:
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
      rollback:
        - dropColumn:
            tableName: validation_results
            schemaName: tam
            columnNames: executed_at, executed_by

```


```
- include:
    file: migrations/v1.0/034-add-columns-validation-results-table.yaml
    relativeToChangelogFile: true

```
