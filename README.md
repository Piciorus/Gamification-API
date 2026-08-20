```
package contracts

import org.springframework.cloud.contract.spec.Contract

[Contract.make {
    priority(1)
    description("""
        Represents a successful scenario for validate add cash transfer reference account.

        when:
            api request to validate add cash transfer reference account.
        then:
            return 204 with Successful Validation
    """)

    request {
        method 'POST'
        urlPath($(consumer('/v1/cash-transfer-account-references/add/validate'), producer('/v1/cash-transfer-account-references/add/validate')))
        headers {
            contentType applicationJson()
            header 'Authorization': value(consumer(regex('.+')), producer('aSessionId'))
            header 'FeId': value(consumer(regex('.+')), producer('WEB'))
            header 'Language': value(consumer(regex('.+')), producer('DE'))
            header 'TraceId': value(consumer(regex('.+')), producer('traceId'))
            header 'User-Agent': value(consumer(regex('.*')), producer('User-Agent'))
            header 'x-source-service': value(consumer(regex('.+')), producer('xSourceService'))
            header 'x-request-id': value(consumer(regex('.+')), producer('12345678'))
        }
        body([
            "transactionPayload": [
                "crmCustomerNumber"  : value(consumer(regex('.+')), producer('1234567')),
                "accountno"          : value(consumer(regex('[0-9]{7,10}')), producer('1234567')),
                "ibanOpponent"       : value(consumer(regex('[A-Z]{2}[0-9]{2}[0-9A-Z]{1,30}')), producer('DE89370400440532013000')),
                "bicOpponent"        : value(consumer(regex('[0-9A-Z]{4}[A-Z]{2}[0-9A-Z]{2}$|^[0-9A-Z]{4}[A-Z]{2}[0-9A-Z]{5}')), producer('COBADEFFXXX')),
                "bankNameOpponent"   : value(consumer(regex('.+')), producer('Deutsche Bank AG')),
                "accountingOpponent" : value(consumer(regex('[a-zA-Z0-9 /\\|\\-?:().,\'\\+]{0,35}')), producer('Test Receiver')),
                "addressOpponent"    : value(consumer(regex('.+')), producer('Test address opponent')),
                "countryCodeOpponent": value(consumer(regex('[^;#]{2,3}')), producer('DE'))
            ]
        ])
    }

    response {
        status NO_CONTENT()
        headers {
            header 'x-correlation-id': fromRequest().header("x-request-id")
        }
    }
}]

```


```
package contracts

import org.springframework.cloud.contract.spec.Contract

[Contract.make {
    priority(2)
    description("""
        Represents a successful scenario for validate add cash transfer reference account with only required parameters.

        when:
            api request to validate add cash transfer reference account with only required parameters.
        then:
            return 204 with Successful Validation
    """)

    request {
        method 'POST'
        urlPath($(consumer('/v1/cash-transfer-account-references/add/validate'), producer('/v1/cash-transfer-account-references/add/validate')))
        headers {
            contentType applicationJson()
            header 'Authorization': value(consumer(regex('.+')), producer('aSessionId'))
            header 'FeId': value(consumer(regex('.+')), producer('WEB'))
            header 'Language': value(consumer(regex('.+')), producer('DE'))
            header 'TraceId': value(consumer(regex('.+')), producer('traceId'))
            header 'User-Agent': value(consumer(regex('.*')), producer('User-Agent'))
            header 'x-source-service': value(consumer(regex('.+')), producer('xSourceService'))
            header 'x-request-id': value(consumer(regex('.+')), producer('12345678'))
        }
        body([
            "transactionPayload": [
                "accountno"         : value(consumer(regex('[0-9]{7,10}')), producer('1234567')),
                "ibanOpponent"      : value(consumer(regex('[A-Z]{2}[0-9]{2}[0-9A-Z]{1,30}')), producer('DE89370400440532013000')),
                "accountingOpponent": value(consumer(regex('[a-zA-Z0-9 /\\|\\-?:().,\'\\+]{0,35}')), producer('Test Receiver'))
            ]
        ])
    }

    response {
        status NO_CONTENT()
        headers {
            header 'x-correlation-id': fromRequest().header("x-request-id")
        }
    }
}]

```


```
package contracts

import org.springframework.cloud.contract.spec.Contract

[Contract.make {
    priority(10)
    description("""
        Represents an unsuccessful scenario for validate add cash transfer reference account.

        when:
            api request to validate add cash transfer reference account.
        then:
            return 400 with Bad Request
    """)

    request {
        method 'POST'
        urlPath($(consumer('/v1/cash-transfer-account-references/add/validate'), producer('/v1/cash-transfer-account-references/add/validate')))
        headers {
            contentType applicationJson()
            header 'Authorization': value(consumer(regex('.+')), producer('aSessionId'))
            header 'FeId': value(consumer(regex('.+')), producer('WEB'))
            header 'Language': value(consumer(regex('.+')), producer('DE'))
            header 'TraceId': value(consumer(regex('.+')), producer('traceId'))
            header 'User-Agent': value(consumer(regex('.*')), producer('User-Agent'))
            header 'x-request-id': value(consumer(regex('.+')), producer('12345678'))
        }
        body([
            "transactionPayload": [
                "crmCustomerNumber"  : value(consumer(regex('.+')), producer('1234567')),
                "accountno"          : value(consumer(regex('[0-9]{7,10}')), producer('1234567')),
                "ibanOpponent"       : value(consumer(regex('[A-Z]{2}[0-9]{2}[0-9A-Z]{1,30}')), producer('DE89370400440532013000')),
                "bicOpponent"        : value(consumer(regex('[0-9A-Z]{4}[A-Z]{2}[0-9A-Z]{2}$|^[0-9A-Z]{4}[A-Z]{2}[0-9A-Z]{5}')), producer('COBADEFFXXX')),
                "bankNameOpponent"   : value(consumer(regex('.+')), producer('Deutsche Bank AG')),
                "accountingOpponent" : value(consumer(regex('[a-zA-Z0-9 /\\|\\-?:().,\'\\+]{0,35}')), producer('Test Receiver')),
                "addressOpponent"    : value(consumer(regex('.+')), producer('Test address opponent')),
                "countryCodeOpponent": value(consumer(regex('[^;#]{2,3}')), producer('DE'))
            ]
        ])
    }

    response {
        status BAD_REQUEST()
        headers {
            contentType applicationJson()
            header 'x-correlation-id': fromRequest().header("x-request-id")
        }
        body([
            "status"  : "400",
            "traceId" : "",
            "code"    : "",
            "title"   : "Header X-source-service ist erforderlich",
            "detail"  : "Required request header 'x-source-service' for method parameter type String is not present",
            "errors"  : []
        ])
    }
}]

```


```
package contracts

import org.springframework.cloud.contract.spec.Contract

[Contract.make {
    priority(1)
    description("""
        Represents a successful scenario for initiate add cash transfer reference account transaction.

        when:
            api request to initiate add cash transfer reference account transaction.
        then:
            return 200 with successful transaction
    """)

    request {
        method 'POST'
        urlPath($(consumer('/v1/cash-transfer-account-references/add/initiate-transaction'), producer('/v1/cash-transfer-account-references/add/initiate-transaction')))
        headers {
            contentType applicationJson()
            header 'Authorization': value(consumer(regex('.+')), producer('aSessionId'))
            header 'FeId': value(consumer(regex('.+')), producer('WEB'))
            header 'Language': value(consumer(regex('.+')), producer('DE'))
            header 'TraceId': value(consumer(regex('.+')), producer('traceId'))
            header 'User-Agent': value(consumer(regex('.*')), producer('User-Agent'))
            header 'x-source-service': value(consumer(regex('.+')), producer('xSourceService'))
            header 'x-request-id': value(consumer(regex('.+')), producer('12345678'))
            header 'idempotency-key': value(consumer(regex('.+')), producer('b0589506-65eb-4fb9-9747-f023b5101b5a'))
        }
        body([
            "transactionIntentId": value(consumer(regex('.+')), producer('11111111')),
            "authorization"      : [
                "tan"        : value(consumer(regex('.+')), producer('123456')),
                "tanAuthType": value(consumer(regex('.+')), producer('SMS_TAN'))
            ],
            "transactionPayload" : [
                "crmCustomerNumber"  : value(consumer(regex('.+')), producer('1234567')),
                "accountno"          : value(consumer(regex('[0-9]{7,10}')), producer('1234567')),
                "ibanOpponent"       : value(consumer(regex('[A-Z]{2}[0-9]{2}[0-9A-Z]{1,30}')), producer('DE89370400440532013000')),
                "bicOpponent"        : value(consumer(regex('[0-9A-Z]{4}[A-Z]{2}[0-9A-Z]{2}$|^[0-9A-Z]{4}[A-Z]{2}[0-9A-Z]{5}')), producer('COBADEFFXXX')),
                "bankNameOpponent"   : value(consumer(regex('.+')), producer('Deutsche Bank AG')),
                "accountingOpponent" : value(consumer(regex('[a-zA-Z0-9 /\\|\\-?:().,\'\\+]{0,35}')), producer('Test Receiver')),
                "addressOpponent"    : value(consumer(regex('.+')), producer('Test address opponent')),
                "countryCodeOpponent": value(consumer(regex('[^;#]{2,3}')), producer('DE'))
            ]
        ])
    }

    response {
        status OK()
        headers {
            contentType applicationJson()
            header 'x-correlation-id': fromRequest().header("x-request-id")
        }
        body([
            "transactionId": "b0589506-65eb-4fb9-9747-f023b5101b5a",
            "status"       : "TX_EXEC_SUCCESS"
        ])
    }
}]
```

```
package contracts

import org.springframework.cloud.contract.spec.Contract

[Contract.make {
    priority(2)
    description("""
        Represents a successful scenario for initiate add cash transfer reference account transaction with only required parameters.

        when:
            api request to initiate add cash transfer reference account transaction with only required parameters.
        then:
            return 200 with successful transaction
    """)

    request {
        method 'POST'
        urlPath($(consumer('/v1/cash-transfer-account-references/add/initiate-transaction'), producer('/v1/cash-transfer-account-references/add/initiate-transaction')))
        headers {
            contentType applicationJson()
            header 'Authorization': value(consumer(regex('.+')), producer('aSessionId'))
            header 'FeId': value(consumer(regex('.+')), producer('WEB'))
            header 'Language': value(consumer(regex('.+')), producer('DE'))
            header 'TraceId': value(consumer(regex('.+')), producer('traceId'))
            header 'User-Agent': value(consumer(regex('.*')), producer('User-Agent'))
            header 'x-source-service': value(consumer(regex('.+')), producer('xSourceService'))
            header 'x-request-id': value(consumer(regex('.+')), producer('12345678'))
            header 'idempotency-key': value(consumer(regex('.+')), producer('b0589506-65eb-4fb9-9747-f023b5101b5a'))
        }
        body([
            "transactionIntentId": value(consumer(regex('.+')), producer('11111111')),
            "authorization"      : [
                "tan"        : value(consumer(regex('.+')), producer('123456')),
                "tanAuthType": value(consumer(regex('.+')), producer('SMS_TAN'))
            ],
            "transactionPayload" : [
                "accountno"         : value(consumer(regex('[0-9]{7,10}')), producer('1234567')),
                "ibanOpponent"      : value(consumer(regex('[A-Z]{2}[0-9]{2}[0-9A-Z]{1,30}')), producer('DE89370400440532013000')),
                "accountingOpponent": value(consumer(regex('[a-zA-Z0-9 /\\|\\-?:().,\'\\+]{0,35}')), producer('Test Receiver'))
            ]
        ])
    }

    response {
        status OK()
        headers {
            contentType applicationJson()
            header 'x-correlation-id': fromRequest().header("x-request-id")
        }
        body([
            "transactionId": "b0589506-65eb-4fb9-9747-f023b5101b5a",
            "status"       : "TX_EXEC_SUCCESS"
        ])
    }
}]
```



```
package contracts

import org.springframework.cloud.contract.spec.Contract

[Contract.make {
    priority(10)
    description("""
        Represents an unsuccessful scenario for initiate add cash transfer reference account transaction.

        when:
            api request to initiate add cash transfer reference account transaction.
        then:
            return 400 with Bad Request
    """)

    request {
        method 'POST'
        urlPath($(consumer('/v1/cash-transfer-account-references/add/initiate-transaction'), producer('/v1/cash-transfer-account-references/add/initiate-transaction')))
        headers {
            contentType applicationJson()
            header 'Authorization': value(consumer(regex('.+')), producer('aSessionId'))
            header 'FeId': value(consumer(regex('.+')), producer('WEB'))
            header 'Language': value(consumer(regex('.+')), producer('DE'))
            header 'TraceId': value(consumer(regex('.+')), producer('traceId'))
            header 'User-Agent': value(consumer(regex('.*')), producer('User-Agent'))
            header 'x-request-id': value(consumer(regex('.+')), producer('12345678'))
            header 'idempotency-key': value(consumer(regex('.+')), producer('b0589506-65eb-4fb9-9747-f023b5101b5a'))
        }
        body([
            "transactionIntentId": value(consumer(regex('.+')), producer('11111111')),
            "authorization"      : [
                "tan"        : value(consumer(regex('.+')), producer('123456')),
                "tanAuthType": value(consumer(regex('.+')), producer('SMS_TAN'))
            ],
            "transactionPayload" : [
                "crmCustomerNumber"  : value(consumer(regex('.+')), producer('1234567')),
                "accountno"          : value(consumer(regex('[0-9]{7,10}')), producer('1234567')),
                "ibanOpponent"       : value(consumer(regex('[A-Z]{2}[0-9]{2}[0-9A-Z]{1,30}')), producer('DE89370400440532013000')),
                "bicOpponent"        : value(consumer(regex('[0-9A-Z]{4}[A-Z]{2}[0-9A-Z]{2}$|^[0-9A-Z]{4}[A-Z]{2}[0-9A-Z]{5}')), producer('COBADEFFXXX')),
                "bankNameOpponent"   : value(consumer(regex('.+')), producer('Deutsche Bank AG')),
                "accountingOpponent" : value(consumer(regex('[a-zA-Z0-9 /\\|\\-?:().,\'\\+]{0,35}')), producer('Test Receiver')),
                "addressOpponent"    : value(consumer(regex('.+')), producer('Test address opponent')),
                "countryCodeOpponent": value(consumer(regex('[^;#]{2,3}')), producer('DE'))
            ]
        ])
    }

    response {
        status BAD_REQUEST()
        headers {
            contentType applicationJson()
            header 'x-correlation-id': fromRequest().header("x-request-id")
        }
        body([
            "status"  : "400",
            "traceId" : "",
            "code"    : "",
            "title"   : "Header X-source-service ist erforderlich",
            "detail"  : "Required request header 'x-source-service' for method parameter type String is not present",
            "errors"  : []
        ])
    }
}]

```
