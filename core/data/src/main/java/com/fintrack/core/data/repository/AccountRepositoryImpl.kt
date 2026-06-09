package com.fintrack.core.data.repository

import com.fintrack.core.common.DispatcherProvider
import com.fintrack.core.database.dao.AccountDao
import com.fintrack.core.database.mapper.toDomain
import com.fintrack.core.database.mapper.toEntity
import com.fintrack.core.domain.model.Account
import com.fintrack.core.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountRepositoryImpl @Inject constructor(
    private val accountDao: AccountDao,
    private val dispatchers: DispatcherProvider,
) : AccountRepository {

    override fun observeAccounts(): Flow<List<Account>> =
        accountDao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeNotificationEnabledAccounts(): Flow<List<Account>> =
        accountDao.observeNotificationEnabled().map { list -> list.map { it.toDomain() } }

    override fun observeAccount(id: Long): Flow<Account?> =
        accountDao.observeById(id).map { it?.toDomain() }

    override suspend fun getAccount(id: Long): Account? = withContext(dispatchers.io) {
        accountDao.getById(id)?.toDomain()
    }

    override suspend fun getDefaultAccount(): Account? = withContext(dispatchers.io) {
        accountDao.getDefault()?.toDomain()
    }

    override suspend fun insertAccount(account: Account): Long = withContext(dispatchers.io) {
        if (account.isDefault) {
            accountDao.clearDefaultFlags()
        }
        accountDao.insert(account.toEntity())
    }

    override suspend fun updateAccount(account: Account) = withContext(dispatchers.io) {
        if (account.isDefault) {
            accountDao.clearDefaultFlags()
        }
        accountDao.update(account.toEntity())
    }

    override suspend fun softDeleteAccount(id: Long, deletedAt: Instant) = withContext(dispatchers.io) {
        accountDao.softDelete(id, deletedAt)
    }

    override suspend fun setDefaultAccount(id: Long) = withContext(dispatchers.io) {
        accountDao.clearDefaultFlags()
        accountDao.setDefault(id, Instant.now())
    }
}
