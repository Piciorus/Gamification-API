```
// ---- Shared schema -> canonical package mapping ----
// Tenant / AuthorizationMethod / AuthorizationAttemptStatus / AuthorizationStatus / ApiError
// are defined once in common/schemas.yaml and $ref'd from every spec.
// We pick ONE generated copy as canonical and point every other spec at it
// via importMappings, so the generator does not recreate them per-module.
def commonImports = [
        "Tenant"                    : "de.consorsbank.core.trauthsc.rest.api.tam.initiate.transaction.authorization.model.Tenant",
        "AuthorizationMethod"       : "de.consorsbank.core.trauthsc.rest.api.tam.initiate.transaction.authorization.model.AuthorizationMethod",
        "AuthorizationStatus"       : "de.consorsbank.core.trauthsc.rest.api.tam.initiate.transaction.authorization.model.AuthorizationStatus",
        "AuthorizationAttemptStatus": "de.consorsbank.core.trauthsc.rest.api.tam.initiate.transaction.authorization.model.AuthorizationAttemptStatus",
        "ApiError"                  : "de.consorsbank.core.trauthsc.rest.api.tam.initiate.transaction.authorization.model.ApiError"
]

if (!ext.has('openApiSpecs')) ext.openApiSpecs = []
ext.openApiSpecs += [

        // ---- CANONICAL ENTRY ----
        // This is the one spec allowed to actually generate Tenant, AuthorizationMethod, etc.
        // No importMappings here — it's the source of truth.
        [name        : "init-transaction",
         file        : "${projectDir}/src/main/resources/openapi/tam/initiate-transaction-authorization.yaml",
         apiPackage  : "de.consorsbank.core.trauthsc.rest.api.tam.initiate.transaction.authorization.api",
         modelPackage: "de.consorsbank.core.trauthsc.rest.api.tam.initiate.transaction.authorization.model"],

        // ---- ALL OTHER ENTRIES: same as before, plus importMappings ----
        [name          : "submit-authorization-method",
         file          : "${projectDir}/src/main/resources/openapi/tam/submit-authorization-method.yaml",
         apiPackage    : "de.consorsbank.core.trauthsc.rest.api.tam.submit.authorization.method.api",
         modelPackage  : "de.consorsbank.core.trauthsc.rest.api.tam.submit.authorization.method.model",
         importMappings: commonImports],

        [name          : "submit-authorization-credential",
         file          : "${projectDir}/src/main/resources/openapi/tam/submit-authorization-credential.yaml",
         apiPackage    : "de.consorsbank.core.trauthsc.rest.api.tam.submit.authorization.credential.api",
         modelPackage  : "de.consorsbank.core.trauthsc.rest.api.tam.submit.authorization.credential.model",
         importMappings: commonImports],

        [name          : "get-authorization-status",
         file          : "${projectDir}/src/main/resources/openapi/tam/get-authorization-status.yaml",
         apiPackage    : "de.consorsbank.core.trauthsc.rest.api.tam.get.authorization.status.api",
         modelPackage  : "de.consorsbank.core.trauthsc.rest.api.tam.get.authorization.status.model",
         importMappings: commonImports],

        [name          : "get-authorization-attempt-status",
         file          : "${projectDir}/src/main/resources/openapi/tam/get-authorization-attempt-status.yaml",
         apiPackage    : "de.consorsbank.core.trauthsc.rest.api.tam.get.authorization.attempt.status.api",
         modelPackage  : "de.consorsbank.core.trauthsc.rest.api.tam.get.authorization.attempt.status.model",
         importMappings: commonImports],

        [name          : "get-payload-transaction-authorization",
         file          : "${projectDir}/src/main/resources/openapi/tam/get-payload-transaction-authorization.yaml",
         apiPackage    : "de.consorsbank.core.trauthsc.rest.api.tam.get.payload.transaction.authorization.status.api",
         modelPackage  : "de.consorsbank.core.trauthsc.rest.api.tam.get.payload.transaction.authorization.status.model",
         importMappings: commonImports]
]

```
