package com.fintrack.app.feature.categories.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fintrack.core.domain.usecase.category.AddCategoryUseCase
import com.fintrack.core.domain.usecase.category.DeleteCategoryUseCase
import com.fintrack.core.domain.usecase.category.ObserveCategoriesUseCase
import com.fintrack.core.domain.common.DomainResult
import com.fintrack.core.domain.model.Category
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CategoriesUiState(
    val categories: List<Category> = emptyList(),
    val errorMessage: String? = null,
    val successMessage: String? = null,
)

@HiltViewModel
class CategoriesViewModel @Inject constructor(
    observeCategoriesUseCase: ObserveCategoriesUseCase,
    private val addCategoryUseCase: AddCategoryUseCase,
    private val deleteCategoryUseCase: DeleteCategoryUseCase,
) : ViewModel() {

    private val _messages = kotlinx.coroutines.flow.MutableStateFlow<Pair<String?, String?>>(null to null)

    val uiState: StateFlow<CategoriesUiState> = kotlinx.coroutines.flow.combine(
        observeCategoriesUseCase(),
        _messages,
    ) { categories, (error, success) ->
        CategoriesUiState(
            categories = categories.filter { it.parentId == null },
            errorMessage = error,
            successMessage = success,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CategoriesUiState(),
    )

    fun addCategory(name: String) {
        viewModelScope.launch {
            when (val result = addCategoryUseCase(name)) {
                is DomainResult.Success -> _messages.value = null to "Categoría creada"
                is DomainResult.Error -> _messages.value = result.message to null
            }
        }
    }

    fun deleteCategory(id: Long) {
        viewModelScope.launch {
            when (val result = deleteCategoryUseCase(id)) {
                is DomainResult.Success -> _messages.value = null to "Categoría eliminada"
                is DomainResult.Error -> _messages.value = result.message to null
            }
        }
    }

    fun clearMessages() {
        _messages.value = null to null
    }
}
