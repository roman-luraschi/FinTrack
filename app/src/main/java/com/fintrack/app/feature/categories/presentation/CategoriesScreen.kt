package com.fintrack.app.feature.categories.presentation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fintrack.R
import com.fintrack.core.designsystem.components.EmptyStateMessage
import com.fintrack.core.designsystem.components.ErrorMessage
import com.fintrack.core.designsystem.components.FinTrackTopBar
import com.fintrack.core.domain.model.Category

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CategoriesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var newCategoryName by rememberSaveable { mutableStateOf("") }

    Column(modifier = modifier.fillMaxSize()) {
        FinTrackTopBar(
            title = stringResource(R.string.nav_categories),
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                }
            },
        )
        Column(modifier = Modifier.padding(16.dp)) {
            OutlinedTextField(
                value = newCategoryName,
                onValueChange = { newCategoryName = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Nueva categoría") },
            )
            Button(
                onClick = {
                    viewModel.addCategory(newCategoryName)
                    newCategoryName = ""
                },
                modifier = Modifier.padding(top = 8.dp),
            ) {
                Text("Agregar")
            }
        }
        uiState.errorMessage?.let { ErrorMessage(it) }
        uiState.successMessage?.let { Text(it, modifier = Modifier.padding(16.dp)) }
        if (uiState.categories.isEmpty()) {
            EmptyStateMessage(message = stringResource(R.string.empty_categories))
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(uiState.categories, key = { it.id }) { category ->
                    CategoryRow(
                        category = category,
                        onDelete = { viewModel.deleteCategory(category.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryRow(
    category: Category,
    onDelete: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        Text(text = category.name)
        if (!category.isSystem) {
            TextButton(onClick = onDelete) {
                Text(stringResource(R.string.delete))
            }
        }
    }
}
