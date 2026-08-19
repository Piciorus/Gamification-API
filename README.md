```
package de.consorsbank.banking.payments.rest.adapter.controller;

import de.consorsbank.banking.payments.rest.adapter.controller.model.CashTransferRestrictionAccount;
import de.consorsbank.banking.payments.rest.adapter.controller.model.CashTransferRestrictionInqResponse;
import de.consorsbank.banking.payments.rest.adapter.controller.model.CurrentLimitType;
import de.consorsbank.banking.payments.rest.adapter.controller.model.ReferenceAccount;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

public class TestUtils {

    public static CashTransferRestrictionInqResponse buildSimpleCashTransferRestrictionInqResponse() {
        return CashTransferRestrictionInqResponse.builder()
                .account(List.of(buildCashTransferRestrictionAccount()))
                .build();
    }

    public static CashTransferRestrictionInqResponse buildEmptyCashTransferRestrictionInqResponse() {
        return CashTransferRestrictionInqResponse.builder()
                .account(Collections.emptyList())
                .build();
    }

    private static CashTransferRestrictionAccount buildCashTransferRestrictionAccount() {
        return CashTransferRestrictionAccount.builder()
                .accountno("1234567")
                .accountTypeDescription("DEPOT")
                .dailyTransferLimit(new BigDecimal("10000.00"))
                .usedTransferLimit(new BigDecimal("1250.75"))
                .availableSingleTransferAmount(new BigDecimal("8749.25"))
                .currentLimitIsUserLimit(false)
                .currentLimitType(CurrentLimitType.DAILY_LIMIT)
                .referenceAccounts(List.of(buildReferenceAccount()))
                .instantDailyTransferLimit(new BigDecimal("5000.00"))
                .instantSingleTransferLimit(new BigDecimal("1000.00"))
                .usedInstantTransferLimit(new BigDecimal("250.00"))
                .availableInstantTransferAmount(new BigDecimal("4750.00"))
                .build();
    }

    private static ReferenceAccount buildReferenceAccount() {
        return ReferenceAccount.builder()
                .ibanOpponent("DE89370400440532013000")
                .bicOpponent("COBADEFFXXX")
                .bankNameOpponent("Deutsche Bank AG")
                .accountingOpponent("Test Receiver")
                .addressOpponent("Test address opponent")
                .countryCodeOpponent("DE")
                .createdAt(LocalDate.of(2026, 8, 19))
                .build();
    }
}

```
