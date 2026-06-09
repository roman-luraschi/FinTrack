package com.fintrack.core.data.notification

import com.fintrack.core.data.dto.BankNotificationDto
import javax.inject.Inject

class NotificationRelevanceFilterImpl @Inject constructor(
    private val enabledAccountsProvider: NotificationEnabledAccountsProvider,
) : NotificationRelevanceFilter {

    override fun isRelevant(dto: BankNotificationDto): Boolean {
        if (!FinancialAppAllowlist.isAllowed(dto.packageName)) return false

        val provider = IntegrationProviderResolver.fromPackage(dto.packageName)
        val hasMatchingAccount = enabledAccountsProvider.snapshot().any { account ->
            account.integrationProvider == provider
        }
        if (!hasMatchingAccount) return false

        if (MarketingNotificationKeywords.isMarketing(dto)) return false

        return true
    }
}
