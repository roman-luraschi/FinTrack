package com.fintrack.app.feature.accounts.domain

import com.fintrack.core.common.Result
import com.fintrack.core.domain.model.Account
import com.fintrack.core.domain.model.AccountType
import com.fintrack.core.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import javax.inject.Inject

class ObserveAccountsUseCase @Inject constructor(
    private val accountRepository: AccountRepository,
) {
    operator fun invoke(): Flow<List<Account>> = accountRepository.observeAccounts()
}

class GetDefaultAccountUseCase @Inject constructor(
    private val accountRepository: AccountRepository,
) {
    suspend operator fun invoke(): Account? = accountRepository.getDefaultAccount()
}

class AddAccountUseCase @Inject constructor(
    private val accountRepository: AccountRepository,
) {
    suspend operator fun invoke(
        name: String,
        type: AccountType,
        colorHex: String? = null,
        isDefault: Boolean = false,
    ): Result<Long> {
        if (name.isBlank()) return Result.Error("El nombre es obligatorio")
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
        return Result.Success(id)
    }
}

class UpdateAccountUseCase @Inject constructor(
    private val accountRepository: AccountRepository,
) {
    suspend operator fun invoke(account: Account): Result<Unit> {
        if (account.name.isBlank()) return Result.Error("El nombre es obligatorio")
        accountRepository.updateAccount(account.copy(updatedAt = Instant.now()))
        return Result.Success(Unit)
    }
}

class DeleteAccountUseCase @Inject constructor(
    private val accountRepository: AccountRepository,
) {
    suspend operator fun invoke(id: Long): Result<Unit> {
        accountRepository.softDeleteAccount(id, Instant.now())
        return Result.Success(Unit)
    }
}

class SetDefaultAccountUseCase @Inject constructor(
    private val accountRepository: AccountRepository,
) {
    suspend operator fun invoke(id: Long): Result<Unit> {
        accountRepository.setDefaultAccount(id)
        return Result.Success(Unit)
    }
}
