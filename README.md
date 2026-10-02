databaseChangeLog:
  - changeSet:
      id: BCKNDCORE-7703-insert-initial-templates-data
      author: piciorus.alexandru@externe.bnpparibas.com (cs32610)
      comment: "Insert initial NEO_TRANSACTION templates migrated from OMS_S.ERRORCODE (origin=neo-secure-template)"
      changes:

        # ── 1. OWNER SYSTEM ─────────────────────────────────────────────────────
        - insert:
            schemaName: tam
            tableName: owner_systems
            columns:
              - column:
                  name: id
                  valueComputed: sys_guid()
              - column:
                  name: system_name
                  value: NEO_SECURE_TEMPLATE
              - column:
                  name: description
                  value: "NEO secure template origin system (migrated from OMS_S.ERRORCODE)"
              - column:
                  name: status
                  value: ACTIVE
              - column:
                  name: is_deleted
                  valueNumeric: 0
              - column:
                  name: created_by
                  value: cs32610
              - column:
                  name: updated_by
                  value: cs32610
              - column:
                  name: created_at
                  valueComputed: CURRENT_TIMESTAMP
              - column:
                  name: updated_at
                  valueComputed: CURRENT_TIMESTAMP
              - column:
                  name: version
                  valueNumeric: 0

        # ── 2. TEMPLATE: MWExemptionOrderAdd ────────────────────────────────────
        - insert:
            schemaName: tam
            tableName: templates
            columns:
              - column:
                  name: id
                  valueComputed: sys_guid()
              - column:
                  name: code
                  value: MWExemptionOrderAdd
              - column:
                  name: language
                  value: DE
              - column:
                  name: name
                  value: "Freistellungsauftrag anlegen"
              - column:
                  name: owner_system_id
                  valueComputed: "(SELECT id FROM tam.owner_systems WHERE system_name = 'NEO_SECURE_TEMPLATE' AND is_deleted = 0)"
              - column:
                  name: category_id
                  value: null
              - column:
                  name: active_version_id
                  value: null
              - column:
                  name: status
                  value: ACTIVE
              - column:
                  name: is_deleted
                  valueNumeric: 0
              - column:
                  name: created_by
                  value: cs32610
              - column:
                  name: updated_by
                  value: cs32610
              - column:
                  name: created_at
                  valueComputed: CURRENT_TIMESTAMP
              - column:
                  name: updated_at
                  valueComputed: CURRENT_TIMESTAMP
              - column:
                  name: version
                  valueNumeric: 0

        # ── 3. TEMPLATE VERSION: MWExemptionOrderAdd ────────────────────────────
        - insert:
            schemaName: tam
            tableName: template_versions
            columns:
              - column:
                  name: id
                  valueComputed: sys_guid()
              - column:
                  name: template_id
                  valueComputed: "(SELECT id FROM tam.templates WHERE code = 'MWExemptionOrderAdd' AND language = 'DE' AND owner_system_id = (SELECT id FROM tam.owner_systems WHERE system_name = 'NEO_SECURE_TEMPLATE' AND is_deleted = 0))"
              - column:
                  name: version_number
                  valueNumeric: 1
              - column:
                  name: template_content
                  value: >
                    {"metadata":{"supportedLanguages":["de"],"defaultLanguage":"de","frontendId":"${FEID}","credentialFlow":"SIGNATURE","transactionTemplateType":"NEO_TRANSACTION","showTransactionDataOnSameChannel":true,"version":1},"notification":{"title":{"value":{"de":"Freistellungsauftrag anlegen"}},"pageHeader":{"value":{"de":"Freistellungsauftrag anlegen"}},"description":{"value":{"de":"Bitte bestätigen Sie die Anlage Ihres Freistellungsauftrags."}},"subTitle":{"value":{"de":"Bitte bestätigen Sie die Anlage Ihres Freistellungsauftrags."}},"button":{"accept":{"value":{"de":"Fortfahren"}},"continue":{"value":{"de":"Fortfahren"}}}},"authorisation":{"pageHeader":{"value":{"de":"Freistellungsauftrag anlegen?"},"applyTo":["overview","extended"]},"title":{"value":{"de":"Möchten Sie Ihren Freistellungsauftrag anlegen?"},"applyTo":["overview","extended"]},"button":{"accept":{"value":{"de":"Freigeben"},"applyTo":["overview","extended"]},"decline":{"value":{"de":"Ablehnen"},"applyTo":["extended"]}}},"data":[{"title":{"de":"Betrag"},"value":[{"type":"text","de":"%{NEO_FR_AMOUNT:${EXEMPTION}}%"}],"applyTo":["overview","extended"]},{"title":{"de":"Gültig bis"},"value":[{"type":"text","de":"%{NEO_FR_SHORT_DATE:${VALID_UNTIL_DATE}}%"}],"applyTo":["overview","extended"]}]}
              - column:
                  name: checksum
                  valueComputed: "STANDARD_HASH('{\"code\":\"MWExemptionOrderAdd\",\"version\":1}', 'SHA256')"
              - column:
                  name: lifecycle_status
                  value: ACTIVE
              - column:
                  name: validation_status
                  value: PASSED
              - column:
                  name: created_by
                  value: cs32610
              - column:
                  name: created_at
                  valueComputed: CURRENT_TIMESTAMP

        # ── 4. UPDATE templates.active_version_id for MWExemptionOrderAdd ───────
        - update:
            schemaName: tam
            tableName: templates
            columns:
              - column:
                  name: active_version_id
                  valueComputed: "(SELECT id FROM tam.template_versions WHERE template_id = (SELECT id FROM tam.templates WHERE code = 'MWExemptionOrderAdd' AND language = 'DE' AND owner_system_id = (SELECT id FROM tam.owner_systems WHERE system_name = 'NEO_SECURE_TEMPLATE' AND is_deleted = 0)) AND version_number = 1)"
            where: "code = 'MWExemptionOrderAdd' AND language = 'DE' AND owner_system_id = (SELECT id FROM tam.owner_systems WHERE system_name = 'NEO_SECURE_TEMPLATE' AND is_deleted = 0)"

        # ── 5. TEMPLATE: MWExemptionOrderChange ─────────────────────────────────
        - insert:
            schemaName: tam
            tableName: templates
            columns:
              - column:
                  name: id
                  valueComputed: sys_guid()
              - column:
                  name: code
                  value: MWExemptionOrderChange
              - column:
                  name: language
                  value: DE
              - column:
                  name: name
                  value: "Freistellungsauftrag ändern"
              - column:
                  name: owner_system_id
                  valueComputed: "(SELECT id FROM tam.owner_systems WHERE system_name = 'NEO_SECURE_TEMPLATE' AND is_deleted = 0)"
              - column:
                  name: category_id
                  value: null
              - column:
                  name: active_version_id
                  value: null
              - column:
                  name: status
                  value: ACTIVE
              - column:
                  name: is_deleted
                  valueNumeric: 0
              - column:
                  name: created_by
                  value: cs32610
              - column:
                  name: updated_by
                  value: cs32610
              - column:
                  name: created_at
                  valueComputed: CURRENT_TIMESTAMP
              - column:
                  name: updated_at
                  valueComputed: CURRENT_TIMESTAMP
              - column:
                  name: version
                  valueNumeric: 0

        # ── 6. TEMPLATE VERSION: MWExemptionOrderChange ──────────────────────────
        - insert:
            schemaName: tam
            tableName: template_versions
            columns:
              - column:
                  name: id
                  valueComputed: sys_guid()
              - column:
                  name: template_id
                  valueComputed: "(SELECT id FROM tam.templates WHERE code = 'MWExemptionOrderChange' AND language = 'DE' AND owner_system_id = (SELECT id FROM tam.owner_systems WHERE system_name = 'NEO_SECURE_TEMPLATE' AND is_deleted = 0))"
              - column:
                  name: version_number
                  valueNumeric: 1
              - column:
                  name: template_content
                  value: >
                    {"metadata":{"supportedLanguages":["de"],"defaultLanguage":"de","frontendId":"${FEID}","credentialFlow":"SIGNATURE","transactionTemplateType":"NEO_TRANSACTION","showTransactionDataOnSameChannel":true,"version":1},"notification":{"title":{"value":{"de":"Freistellungsauftrag ändern"}},"pageHeader":{"value":{"de":"Freistellungsauftrag ändern"}},"description":{"value":{"de":"Bitte bestätigen Sie die Änderung Ihres Freistellungsauftrags."}},"subTitle":{"value":{"de":"Bitte bestätigen Sie die Änderung Ihres Freistellungsauftrags."}},"button":{"accept":{"value":{"de":"Fortfahren"}},"continue":{"value":{"de":"Fortfahren"}}}},"authorisation":{"pageHeader":{"value":{"de":"Freistellungsauftrag ändern?"},"applyTo":["overview","extended"]},"title":{"value":{"de":"Möchten Sie Ihren Freistellungsauftrag ändern?"},"applyTo":["overview","extended"]},"button":{"accept":{"value":{"de":"Freigeben"},"applyTo":["overview","extended"]},"decline":{"value":{"de":"Ablehnen"},"applyTo":["extended"]}}},"data":[{"title":{"de":"Betrag"},"value":[{"type":"text","de":"%{NEO_FR_AMOUNT:${EXEMPTION}}%"}],"applyTo":["overview","extended"]},{"title":{"de":"Gültig bis"},"value":[{"type":"text","de":"%{NEO_FR_SHORT_DATE:${VALID_UNTIL_DATE}}%"}],"applyTo":["overview","extended"]},{"title":{"de":"Konto"},"value":[{"type":"text","de":"${AFFECTED_ACCOUNTS}"}],"applyTo":["overview","extended"]}]}
              - column:
                  name: checksum
                  valueComputed: "STANDARD_HASH('{\"code\":\"MWExemptionOrderChange\",\"version\":1}', 'SHA256')"
              - column:
                  name: lifecycle_status
                  value: ACTIVE
              - column:
                  name: validation_status
                  value: PASSED
              - column:
                  name: created_by
                  value: cs32610
              - column:
                  name: created_at
                  valueComputed: CURRENT_TIMESTAMP

        # ── 7. UPDATE templates.active_version_id for MWExemptionOrderChange ────
        - update:
            schemaName: tam
            tableName: templates
            columns:
              - column:
                  name: active_version_id
                  valueComputed: "(SELECT id FROM tam.template_versions WHERE template_id = (SELECT id FROM tam.templates WHERE code = 'MWExemptionOrderChange' AND language = 'DE' AND owner_system_id = (SELECT id FROM tam.owner_systems WHERE system_name = 'NEO_SECURE_TEMPLATE' AND is_deleted = 0)) AND version_number = 1)"
            where: "code = 'MWExemptionOrderChange' AND language = 'DE' AND owner_system_id = (SELECT id FROM tam.owner_systems WHERE system_name = 'NEO_SECURE_TEMPLATE' AND is_deleted = 0)"

      rollback:
        - delete:
            schemaName: tam
            tableName: template_versions
            where: "template_id IN (SELECT id FROM tam.templates WHERE owner_system_id = (SELECT id FROM tam.owner_systems WHERE system_name = 'NEO_SECURE_TEMPLATE' AND is_deleted = 0))"
        - delete:
            schemaName: tam
            tableName: templates
            where: "owner_system_id = (SELECT id FROM tam.owner_systems WHERE system_name = 'NEO_SECURE_TEMPLATE' AND is_deleted = 0)"
        - delete:
            schemaName: tam
            tableName: owner_systems
            where: "system_name = 'NEO_SECURE_TEMPLATE'"



openssl s_client -connect localhost:8323 \
  -cert .certs/local-ssl/x0/taxsrv.tls.crt \
  -key  .certs/local-ssl/x0/taxsrv.tls.key </dev/null 2>&1 \
  | grep -A5 "Acceptable client certificate CA names"



sudo security add-trusted-cert -d -r trustRoot \
  -k /Library/Keychains/System.keychain \
  ~/development/trauth-sc/.certs/local-ssl/ca.crt




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
