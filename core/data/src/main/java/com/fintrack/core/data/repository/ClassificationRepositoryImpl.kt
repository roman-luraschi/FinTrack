package com.fintrack.core.data.repository

import com.fintrack.core.common.DispatcherProvider
import com.fintrack.core.database.dao.ClassificationDao
import com.fintrack.core.database.mapper.toDomain
import com.fintrack.core.database.mapper.toEntity
import com.fintrack.core.domain.model.ClassificationRule
import com.fintrack.core.domain.model.LearnedMerchantCategory
import com.fintrack.core.domain.repository.ClassificationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClassificationRepositoryImpl @Inject constructor(
    private val classificationDao: ClassificationDao,
    private val dispatchers: DispatcherProvider,
) : ClassificationRepository {

    override fun observeRules(): Flow<List<ClassificationRule>> =
        classificationDao.observeRules().map { list -> list.map { it.toDomain() } }

    override fun observeLearnedMappings(): Flow<List<LearnedMerchantCategory>> =
        classificationDao.observeLearned().map { list -> list.map { it.toDomain() } }

    override suspend fun getActiveRules(): List<ClassificationRule> = withContext(dispatchers.io) {
        classificationDao.getActiveRules().map { it.toDomain() }
    }

    override suspend fun getLearnedMappings(): List<LearnedMerchantCategory> = withContext(dispatchers.io) {
        classificationDao.getLearned().map { it.toDomain() }
    }

    override suspend fun insertRule(rule: ClassificationRule): Long = withContext(dispatchers.io) {
        classificationDao.insertRule(rule.toEntity())
    }

    override suspend fun updateRule(rule: ClassificationRule) = withContext(dispatchers.io) {
        classificationDao.updateRule(rule.toEntity())
    }

    override suspend fun deleteRule(id: Long) = withContext(dispatchers.io) {
        classificationDao.deleteRule(id)
    }

    override suspend fun upsertLearnedMapping(mapping: LearnedMerchantCategory): Long =
        withContext(dispatchers.io) {
            classificationDao.upsertLearned(mapping.toEntity())
        }

    override suspend fun softDeleteLearnedMapping(id: Long, deletedAt: Instant) =
        withContext(dispatchers.io) {
            classificationDao.softDeleteLearned(id, deletedAt)
        }
}
