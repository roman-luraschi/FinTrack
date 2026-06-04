package com.fintrack.core.domain.usecase.account

import com.fintrack.core.domain.common.DomainResult
import com.fintrack.core.domain.model.Account
import com.fintrack.core.domain.model.AccountType
import com.fintrack.core.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ObserveAccountsUseCase @Inject constructor(
    private val accountRepository: AccountRepository,
) {
    operator fun invoke(): Flow<List<Account>> = accountRepository.observeAccounts()
}

@Singleton
class GetDefaultAccountUseCase @Inject constructor(
    private val accountRepository: AccountRepository,
) {
    suspend operator fun invoke(): Account? = accountRepository.getDefaultAccount()
}

@Singleton
class AddAccountUseCase @Inject constructor(
    private val accountRepository: AccountRepository,
) {
    suspend operator fun invoke(
        name: String,
        type: AccountType,
        colorHex: String? = null,
        isDefault: Boolean = false,
    ): DomainResult<Long> {
        if (name.isBlank()) return DomainResult.Error("El nombre es obligatorio")
        val now = Instant.now()
        val id = accountRepository.insertAccount(
            Account(
                name = name.trim(),
                type = type,
                colorHex = colorHex,
                isDefault = isDefault,
                createdAt = now,
                updatedAt = now,
            ),
        )
        return DomainResult.Success(id)
    }
}

@Singleton
class UpdateAccountUseCase @Inject constructor(
    private val accountRepository: AccountRepository,
) {
    suspend operator fun invoke(account: Account): DomainResult<Unit> {
        if (account.name.isBlank()) return DomainResult.Error("El nombre es obligatorio")
        accountRepository.updateAccount(account.copy(updatedAt = Instant.now()))
        return DomainResult.Success(Unit)
    }
}

@Singleton
class DeleteAccountUseCase @Inject constructor(
    private val accountRepository: AccountRepository,
) {
    suspend operator fun invoke(id: Long): DomainResult<Unit> {
        accountRepository.softDeleteAccount(id, Instant.now())
        return DomainResult.Success(Unit)
    }
}

@Singleton
class SetDefaultAccountUseCase @Inject constructor(
    private val accountRepository: AccountRepository,
) {
    suspend operator fun invoke(id: Long): DomainResult<Unit> {
        accountRepository.setDefaultAccount(id)
        return DomainResult.Success(Unit)
    }
}
