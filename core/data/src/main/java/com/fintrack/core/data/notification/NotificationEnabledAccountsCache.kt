package com.fintrack.core.data.notification

import com.fintrack.core.domain.model.Account
import com.fintrack.core.domain.repository.AccountRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import javax.inject.Singleton

interface NotificationEnabledAccountsProvider {
    fun snapshot(): List<Account>
}

@Singleton
class NotificationEnabledAccountsCache @Inject constructor(
    accountRepository: AccountRepository,
) : NotificationEnabledAccountsProvider {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val snapshotRef = AtomicReference<List<Account>>(emptyList())

    init {
        accountRepository.observeNotificationEnabledAccounts()
            .onEach { accounts -> snapshotRef.set(accounts) }
            .launchIn(scope)
    }

    override fun snapshot(): List<Account> = snapshotRef.get()
}
