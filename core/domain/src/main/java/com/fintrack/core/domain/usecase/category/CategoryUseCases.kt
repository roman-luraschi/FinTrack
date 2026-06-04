package com.fintrack.core.domain.usecase.category

import com.fintrack.core.domain.common.DomainResult
import com.fintrack.core.domain.model.Category
import com.fintrack.core.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import java.time.Instant

class ObserveCategoriesUseCase(
    private val categoryRepository: CategoryRepository,
) {
    operator fun invoke(): Flow<List<Category>> = categoryRepository.observeCategories()
}

class ObserveRootCategoriesUseCase(
    private val categoryRepository: CategoryRepository,
) {
    operator fun invoke(): Flow<List<Category>> = categoryRepository.observeRootCategories()
}

class AddCategoryUseCase(
    private val categoryRepository: CategoryRepository,
) {
    suspend operator fun invoke(name: String, parentId: Long? = null): DomainResult<Long> {
        if (name.isBlank()) return DomainResult.Error("El nombre es obligatorio")
        val id = categoryRepository.insertCategory(
            Category(
                name = name.trim(),
                parentId = parentId,
                isSystem = false,
                sortOrder = 50,
            ),
        )
        return DomainResult.Success(id)
    }
}

class DeleteCategoryUseCase(
    private val categoryRepository: CategoryRepository,
) {
    suspend operator fun invoke(categoryId: Long): DomainResult<Unit> {
        val category = categoryRepository.getCategory(categoryId)
            ?: return DomainResult.Error("Categoría no encontrada")
        if (category.isSystem) return DomainResult.Error("No se puede eliminar una categoría del sistema")
        val usageCount = categoryRepository.countTransactionsUsingCategory(categoryId)
        if (usageCount > 0) {
            return DomainResult.Error("La categoría tiene $usageCount transacciones asociadas")
        }
        categoryRepository.softDeleteCategory(categoryId, Instant.now())
        return DomainResult.Success(Unit)
    }
}
