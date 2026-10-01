package de.consorsbank.core.trautnsc.rest.api.tam.template.materialization;

import de.consorsbank.core.trautnsc.rest.api.tam.template.materialization.model.TemplateMaterializationRequest;
import de.consorsbank.core.trautnsc.rest.api.tam.template.materialization.model.TemplateMaterializationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TemplateMaterializationController implements TemplateMaterializationApi {

    private final TemplateMaterializationService templateMaterializationService;

    @Override
    public ResponseEntity<TemplateMaterializationResponse> materializeTemplate(
            String authorization,
            String feId,
            String language,
            String traceId,
            String userAgent,
            String xSourceService,
            String xRequestId,
            String code,
            String lang,
            TemplateMaterializationRequest templateMaterializationRequest) {

        if (templateMaterializationRequest.getParameters() == null
                || templateMaterializationRequest.getParameters().isEmpty()) {
            return ResponseEntity.ok()
                    .body(templateMaterializationService.materializeTemplate(code, lang));
        }

        return ResponseEntity.ok()
                .body(templateMaterializationService.materializeTemplate(
                        code, lang, templateMaterializationRequest.getParameters()));
    }
}


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
