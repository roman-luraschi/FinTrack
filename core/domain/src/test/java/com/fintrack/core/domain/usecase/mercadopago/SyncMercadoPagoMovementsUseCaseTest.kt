package com.fintrack.core.domain.usecase.mercadopago

import com.fintrack.core.domain.classification.ExpenseClassifier
import com.fintrack.core.domain.common.DomainResult
import com.fintrack.core.domain.model.Account
import com.fintrack.core.domain.model.AccountType
import com.fintrack.core.domain.model.Category
import com.fintrack.core.domain.model.ClassificationRule
import com.fintrack.core.domain.model.DashboardPeriod
import com.fintrack.core.domain.model.IngestionRequest
import com.fintrack.core.domain.model.IngestionResult
import com.fintrack.core.domain.model.IntegrationProvider
import com.fintrack.core.domain.model.LearnedMerchantCategory
import com.fintrack.core.domain.model.ParseStatus
import com.fintrack.core.domain.model.MercadoPagoConnectionState
import com.fintrack.core.domain.model.MercadoPagoConnectionStatus
import com.fintrack.core.domain.model.ProvenanceDraft
import com.fintrack.core.domain.model.TransactionDraft
import com.fintrack.core.domain.model.TransactionSource
import com.fintrack.core.domain.model.TransactionType
import com.fintrack.core.domain.repository.AccountRepository
import com.fintrack.core.domain.repository.CategoryRepository
import com.fintrack.core.domain.repository.ClassificationRepository
import com.fintrack.core.domain.repository.DeviceIdentityPort
import com.fintrack.core.domain.repository.MercadoPagoConnectionPort
import com.fintrack.core.domain.repository.MercadoPagoFetchResult
import com.fintrack.core.domain.repository.MercadoPagoSyncMetadataPort
import com.fintrack.core.domain.repository.MercadoPagoSyncPort
import com.fintrack.core.domain.repository.TransactionIngestionPort
import com.fintrack.core.domain.repository.UserSettingsPort
import com.fintrack.core.domain.usecase.classification.ClassifyExpenseUseCase
import com.fintrack.core.domain.usecase.ingestion.IngestTransactionsUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal
import java.time.Instant

class SyncMercadoPagoMovementsUseCaseTest {

    @Test
    fun `fails when mercado pago is not connected`() = runTest {
        val useCase = buildUseCase(connectionStatus = MercadoPagoConnectionStatus.DISCONNECTED)

        val result = useCase()

        assertTrue(result is DomainResult.Error)
        assertEquals("Mercado Pago no está conectado", (result as DomainResult.Error).message)
    }

    @Test
    fun `fails when no target account exists`() = runTest {
        val useCase = buildUseCase(
            connectionStatus = MercadoPagoConnectionStatus.CONNECTED,
            accounts = emptyList(),
        )

        val result = useCase()

        assertTrue(result is DomainResult.Error)
        assertEquals(
            "Creá una cuenta y asociala a Mercado Pago en Ajustes",
            (result as DomainResult.Error).message,
        )
    }

    @Test
    fun `ingests fetched drafts into target mercado pago account`() = runTest {
        val mpAccount = sampleAccount(id = 3L, provider = IntegrationProvider.MERCADO_PAGO)
        val draft = sampleDraft(accountId = 3L)
        val useCase = buildUseCase(
            connectionStatus = MercadoPagoConnectionStatus.CONNECTED,
            accounts = listOf(mpAccount),
            fetchResult = DomainResult.Success(
                MercadoPagoFetchResult(
                    drafts = listOf(draft),
                    skipped = 0,
                    parseErrors = emptyList(),
                ),
            ),
        )

        val result = useCase()

        assertTrue(result is DomainResult.Success)
        val sync = (result as DomainResult.Success).data
        assertEquals(1, sync.fetched)
        assertEquals(1, sync.inserted)
    }

    private fun buildUseCase(
        connectionStatus: MercadoPagoConnectionStatus,
        accounts: List<Account> = emptyList(),
        fetchResult: DomainResult<MercadoPagoFetchResult> = DomainResult.Error("unused"),
    ): SyncMercadoPagoMovementsUseCase {
        val ingestTransactionsUseCase = IngestTransactionsUseCase(
            ingestionPort = object : TransactionIngestionPort {
                override suspend fun ingest(request: IngestionRequest): IngestionResult =
                    IngestionResult(
                        batchId = 1L,
                        inserted = request.drafts.size,
                        updated = 0,
                        skipped = 0,
                        errors = 0,
                        duplicateCandidates = emptyList(),
                    )
            },
            classifyExpenseUseCase = ClassifyExpenseUseCase(
                object : ClassificationRepository {
                    override fun observeRules(): Flow<List<ClassificationRule>> = flowOf(emptyList())
                    override fun observeLearnedMappings(): Flow<List<LearnedMerchantCategory>> = flowOf(emptyList())
                    override suspend fun getActiveRules(): List<ClassificationRule> = emptyList()
                    override suspend fun getLearnedMappings(): List<LearnedMerchantCategory> = emptyList()
                    override suspend fun insertRule(rule: ClassificationRule): Long = 0L
                    override suspend fun updateRule(rule: ClassificationRule) = Unit
                    override suspend fun deleteRule(id: Long) = Unit
                    override suspend fun upsertLearnedMapping(mapping: LearnedMerchantCategory): Long = 0L
                    override suspend fun softDeleteLearnedMapping(id: Long, deletedAt: Instant) = Unit
                },
                object : CategoryRepository {
                    override fun observeCategories(): Flow<List<Category>> = flowOf(emptyList())
                    override fun observeRootCategories(): Flow<List<Category>> = flowOf(emptyList())
                    override suspend fun getCategory(id: Long): Category? = null
                    override suspend fun getCategoriesByIds(ids: List<Long>): List<Category> = emptyList()
                    override suspend fun insertCategory(category: Category): Long = 0L
                    override suspend fun updateCategory(category: Category) = Unit
                    override suspend fun softDeleteCategory(id: Long, deletedAt: Instant) = Unit
                    override suspend fun countTransactionsUsingCategory(categoryId: Long): Int = 0
                },
                ExpenseClassifier(),
            ),
            userSettingsPort = object : UserSettingsPort {
                override fun observeFuzzyThreshold(): Flow<Float> = flowOf(0.85f)
                override fun observeDashboardPeriod(): Flow<DashboardPeriod> = flowOf(DashboardPeriod.MONTH)
            },
        )
        return SyncMercadoPagoMovementsUseCase(
            deviceIdentityPort = FakeDeviceIdentityPort("device-1"),
            mercadoPagoConnectionPort = FakeMercadoPagoConnectionPort(connectionStatus),
            accountRepository = FakeAccountRepository(accounts),
            mercadoPagoSyncPort = FakeMercadoPagoSyncPort(fetchResult),
            syncMetadataPort = FakeMercadoPagoSyncMetadataPort(),
            ingestTransactionsUseCase = ingestTransactionsUseCase,
        )
    }

    private fun sampleAccount(id: Long, provider: IntegrationProvider?) = Account(
        id = id,
        name = "Mercado Pago",
        type = AccountType.DIGITAL_WALLET,
        integrationProvider = provider,
        createdAt = Instant.parse("2026-01-01T00:00:00Z"),
        updatedAt = Instant.parse("2026-01-01T00:00:00Z"),
    )

    private fun sampleDraft(accountId: Long) = TransactionDraft(
        externalId = "mp-1",
        amount = BigDecimal("100.00"),
        type = TransactionType.EXPENSE,
        description = "Test",
        source = TransactionSource.MERCADO_PAGO_API,
        accountId = accountId,
        transactionDate = Instant.parse("2026-06-01T12:00:00Z"),
        provenance = ProvenanceDraft(
            integrationProvider = IntegrationProvider.MERCADO_PAGO,
            rawPayload = "{}",
            payloadFormat = "json",
            parseStatus = ParseStatus.SUCCESS,
            capturedAt = Instant.parse("2026-06-01T12:00:00Z"),
        ),
    )

    private class FakeDeviceIdentityPort(
        private val deviceId: String,
    ) : DeviceIdentityPort {
        override fun observeDeviceId(): Flow<String?> = MutableStateFlow(deviceId)
        override suspend fun getOrCreateDeviceId(): String = deviceId
    }

    private class FakeMercadoPagoConnectionPort(
        status: MercadoPagoConnectionStatus,
    ) : MercadoPagoConnectionPort {
        private val state = MercadoPagoConnectionState(status = status)

        override fun observeConnection(): Flow<MercadoPagoConnectionState> = MutableStateFlow(state)
        override suspend fun setConnected() = Unit
        override suspend fun setDisconnected() = Unit
        override suspend fun setError(message: String) = Unit
    }

    private class FakeAccountRepository(
        private val accounts: List<Account>,
    ) : AccountRepository {
        override fun observeAccounts(): Flow<List<Account>> = MutableStateFlow(accounts)
        override fun observeNotificationEnabledAccounts(): Flow<List<Account>> = MutableStateFlow(emptyList())
        override fun observeAccount(id: Long): Flow<Account?> = MutableStateFlow(accounts.firstOrNull { it.id == id })
        override suspend fun getAccount(id: Long): Account? = accounts.firstOrNull { it.id == id }
        override suspend fun getDefaultAccount(): Account? = accounts.firstOrNull { it.isDefault }
        override suspend fun insertAccount(account: Account): Long = 0L
        override suspend fun updateAccount(account: Account) = Unit
        override suspend fun softDeleteAccount(id: Long, deletedAt: Instant) = Unit
        override suspend fun setDefaultAccount(id: Long) = Unit
    }

    private class FakeMercadoPagoSyncPort(
        private val result: DomainResult<MercadoPagoFetchResult>,
    ) : MercadoPagoSyncPort {
        override suspend fun fetchMovementDrafts(
            deviceId: String,
            accountId: Long,
            since: Instant?,
            limit: Int,
        ): DomainResult<MercadoPagoFetchResult> = result
    }

    private class FakeMercadoPagoSyncMetadataPort : MercadoPagoSyncMetadataPort {
        override suspend fun getLastSyncAt(): Instant? = null
        override suspend fun setLastSyncAt(instant: Instant) = Unit
        override suspend fun clearLastSyncAt() = Unit
    }
}
