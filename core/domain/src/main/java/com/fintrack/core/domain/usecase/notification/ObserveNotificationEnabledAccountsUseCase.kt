package com.fintrack.core.domain.usecase.notification

import com.fintrack.core.domain.model.Account
import com.fintrack.core.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ObserveNotificationEnabledAccountsUseCase @Inject constructor(
    private val accountRepository: AccountRepository,
) {
    operator fun invoke(): Flow<List<Account>> =
        accountRepository.observeNotificationEnabledAccounts()
}
