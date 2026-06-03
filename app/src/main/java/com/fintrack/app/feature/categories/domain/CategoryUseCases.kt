package com.fintrack.app.feature.categories.domain

import com.fintrack.core.common.Result
import com.fintrack.core.domain.model.Category
import com.fintrack.core.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import javax.inject.Inject

class ObserveCategoriesUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository,
) {
    operator fun invoke(): Flow<List<Category>> = categoryRepository.observeCategories()
}

class ObserveRootCategoriesUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository,
) {
    operator fun invoke(): Flow<List<Category>> = categoryRepository.observeRootCategories()
}

class AddCategoryUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository,
) {
    suspend operator fun invoke(name: String, parentId: Long? = null): Result<Long> {
        if (name.isBlank()) return Result.Error("El nombre es obligatorio")
        val id = categoryRepository.insertCategory(
            Category(
                name = name.trim(),
                parentId = parentId,
                isSystem = false,
                sortOrder = 50,
            ),
        )
        return Result.Success(id)
    }
}

class DeleteCategoryUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository,
) {
    suspend operator fun invoke(categoryId: Long): Result<Unit> {
        val category = categoryRepository.getCategory(categoryId)
            ?: return Result.Error("Categoría no encontrada")
        if (category.isSystem) return Result.Error("No se puede eliminar una categoría del sistema")
        val usageCount = categoryRepository.countTransactionsUsingCategory(categoryId)
        if (usageCount > 0) {
            return Result.Error("La categoría tiene $usageCount transacciones asociadas")
        }
        categoryRepository.softDeleteCategory(categoryId, Instant.now())
        return Result.Success(Unit)
    }
}
