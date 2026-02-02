package com.menac1ngmonkeys.monkeyslimit.viewmodel

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.Budgets
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.Categories
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.TransactionType
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.Transactions
import com.menac1ngmonkeys.monkeyslimit.data.repository.BudgetsRepository
import com.menac1ngmonkeys.monkeyslimit.data.repository.CategoriesRepository
import com.menac1ngmonkeys.monkeyslimit.data.repository.TransactionsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import androidx.core.net.toUri

data class ReviewItemUi(
    val id: Int,
    val name: String,
    val categoryId: Int,
    val budgetId: Int,
    val quantity: Int,
    val pricePerUnit: Double
)

data class ReviewTransactionUiState(
    val budgets: List<Budgets> = emptyList(),
    val categories: List<Categories> = emptyList(),
    val imageUri: String? = null,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false
)

class ReviewTransactionViewModel(
    private val transactionsRepository: TransactionsRepository,
    private val budgetsRepository: BudgetsRepository,
    private val categoriesRepository: CategoriesRepository
) : ViewModel() {

    private val _imageUri = MutableStateFlow<String?>(null)
    private val _isSaving = MutableStateFlow(false)

    val uiState: StateFlow<ReviewTransactionUiState> = combine(
        budgetsRepository.getAllBudgets(),
        categoriesRepository.getAllCategories(),
        _imageUri,
        _isSaving
    ) { budgets, categories, imageUri, isSaving ->
        ReviewTransactionUiState(
            budgets = budgets,
            categories = categories,
            imageUri = imageUri,
            isLoading = false,
            isSaving = isSaving
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ReviewTransactionUiState()
    )

    fun setImageUri(uri: String?) {
        _imageUri.update { uri }
    }

    fun saveTransaction(
        context: Context,
        date: Date,
        reviewItems: List<ReviewItemUi>,
        type: TransactionType,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _isSaving.update { true }

            // 1. Handle Image: Move from Cache -> Public Gallery (Pictures/MonkeysLimit)
            var permanentImagePath: String? = null
            val currentImageUri = _imageUri.value

            if (currentImageUri != null) {
                permanentImagePath = saveImageToGallery(context, currentImageUri)
            }

            // 2. Save Transactions
            reviewItems.forEach { item ->
                val lineItemTotal = item.pricePerUnit * item.quantity
                val finalBudgetId = if (type == TransactionType.EXPENSE) item.budgetId else null

                val newTransaction = Transactions(
                    id = 0,
                    date = date,
                    totalAmount = lineItemTotal,
                    note = if (item.quantity > 1) "${item.name} (x${item.quantity})" else item.name,
                    imagePath = permanentImagePath, // Save the MediaStore URI
                    budgetId = finalBudgetId,
                    categoryId = item.categoryId,
                    type = type
                )

                transactionsRepository.insert(newTransaction)
            }

            _isSaving.update { false }
            launch(Dispatchers.Main) { onSuccess() }
        }
    }

    // UPDATED: Saves to "Pictures/MonkeysLimit" using MediaStore
    private fun saveImageToGallery(context: Context, cacheUriString: String): String? {
        try {
            val cacheUri = cacheUriString.toUri()
            val inputStream = context.contentResolver.openInputStream(cacheUri) ?: return null

            val name = "trans_${System.currentTimeMillis()}.jpg"
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, name)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/MonkeysLimit")
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
            }

            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                ?: return null

            resolver.openOutputStream(uri)?.use { outputStream ->
                inputStream.copyTo(outputStream)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)
            }

            return uri.toString()
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}