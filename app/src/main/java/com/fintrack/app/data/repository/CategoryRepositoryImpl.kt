package com.fintrack.app.data.repository

import com.fintrack.core.common.DispatcherProvider
import com.fintrack.core.database.dao.CategoryDao
import com.fintrack.core.database.mapper.toDomain
import com.fintrack.core.database.mapper.toEntity
import com.fintrack.core.domain.model.Category
import com.fintrack.core.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepositoryImpl @Inject constructor(
    private val categoryDao: CategoryDao,
    private val dispatchers: DispatcherProvider,
) : CategoryRepository {

    override fun observeCategories(): Flow<List<Category>> =
        categoryDao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeRootCategories(): Flow<List<Category>> =
        categoryDao.observeRootCategories().map { list -> list.map { it.toDomain() } }

    override suspend fun getCategory(id: Long): Category? = withContext(dispatchers.io) {
        categoryDao.getById(id)?.toDomain()
    }

    override suspend fun getCategoriesByIds(ids: List<Long>): List<Category> = withContext(dispatchers.io) {
        categoryDao.getByIds(ids).map { it.toDomain() }
    }

    override suspend fun insertCategory(category: Category): Long = withContext(dispatchers.io) {
        categoryDao.insert(category.toEntity())
    }

    override suspend fun updateCategory(category: Category) = withContext(dispatchers.io) {
        categoryDao.update(category.toEntity())
    }

    override suspend fun softDeleteCategory(id: Long, deletedAt: Instant) = withContext(dispatchers.io) {
        categoryDao.softDelete(id, deletedAt)
    }

    override suspend fun countTransactionsUsingCategory(categoryId: Long): Int = withContext(dispatchers.io) {
        categoryDao.countTransactionsUsingCategory(categoryId)
    }
}
