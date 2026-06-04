package com.fintrack.core.domain.usecase.category

import com.fintrack.core.domain.common.DomainResult
import com.fintrack.core.domain.model.Category
import com.fintrack.core.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class DeleteCategoryUseCaseTest {

    @Test
    fun invoke_rejectsSystemCategory() = runTest {
        val useCase = DeleteCategoryUseCase(
            FakeCategoryRepository(
                category = Category(id = 1L, name = "Sistema", isSystem = true),
                usageCount = 0,
            ),
        )
        val result = useCase(1L)
        assertTrue(result is DomainResult.Error)
        assertTrue((result as DomainResult.Error).message.contains("sistema"))
    }

    @Test
    fun invoke_rejectsCategoryInUse() = runTest {
        val useCase = DeleteCategoryUseCase(
            FakeCategoryRepository(
                category = Category(id = 2L, name = "Comida", isSystem = false),
                usageCount = 3,
            ),
        )
        val result = useCase(2L)
        assertTrue(result is DomainResult.Error)
        assertTrue((result as DomainResult.Error).message.contains("3"))
    }

    private class FakeCategoryRepository(
        private val category: Category?,
        private val usageCount: Int,
    ) : CategoryRepository {
        override fun observeCategories(): Flow<List<Category>> = flowOf(emptyList())
        override fun observeRootCategories(): Flow<List<Category>> = flowOf(emptyList())
        override suspend fun getCategory(id: Long): Category? = category?.takeIf { it.id == id }
        override suspend fun getCategoriesByIds(ids: List<Long>): List<Category> = emptyList()
        override suspend fun insertCategory(category: Category): Long = 0L
        override suspend fun updateCategory(category: Category) = Unit
        override suspend fun softDeleteCategory(id: Long, deletedAt: Instant) = Unit
        override suspend fun countTransactionsUsingCategory(categoryId: Long): Int = usageCount
    }
}
