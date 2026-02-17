package com.menac1ngmonkeys.monkeyslimit.viewmodel

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.Budgets
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.Categories
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.Items
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.MemberItems
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.Members
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.SmartSplits
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.TransactionType
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.Transactions
import com.menac1ngmonkeys.monkeyslimit.data.repository.BudgetsRepository
import com.menac1ngmonkeys.monkeyslimit.data.repository.CategoriesRepository
import com.menac1ngmonkeys.monkeyslimit.data.repository.ItemsRepository
import com.menac1ngmonkeys.monkeyslimit.data.repository.MemberItemsRepository
import com.menac1ngmonkeys.monkeyslimit.data.repository.MembersRepository
import com.menac1ngmonkeys.monkeyslimit.data.repository.SmartSplitsRepository
import com.menac1ngmonkeys.monkeyslimit.data.repository.TransactionsRepository
import com.menac1ngmonkeys.monkeyslimit.ui.state.SmartSplitDraft
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import androidx.core.net.toUri

data class MemberSplitDetail(
    val memberName: String,
    val items: List<MemberItemDetail>,
    val subtotal: Double,
    val taxShare: Double,
    val serviceShare: Double,
    val discountShare: Double,
    val othersShare: Double,
    val totalOwed: Double
)

data class MemberItemDetail(val itemName: String, val splitPrice: Double)

data class SplitResultUiState(
    val draft: SmartSplitDraft? = null,
    val memberDetails: List<MemberSplitDetail> = emptyList(),
    val categories: List<Categories> = emptyList(), // <-- Added Real Categories
    val budgets: List<Budgets> = emptyList(),       // <-- Added Real Budgets
    val isLoading: Boolean = false,
    val isSaving: Boolean = false
)

class SplitResultViewModel(
    private val smartSplitsRepository: SmartSplitsRepository,
    private val membersRepository: MembersRepository,
    private val itemsRepository: ItemsRepository,
    private val memberItemsRepository: MemberItemsRepository,
    private val transactionsRepository: TransactionsRepository,
    private val categoriesRepository: CategoriesRepository, // <-- Injected Category Repo
    private val budgetsRepository: BudgetsRepository        // <-- Injected Budget Repo
) : ViewModel() {

    private val _uiState = MutableStateFlow(SplitResultUiState())
    val uiState = _uiState.asStateFlow()

    init {
        // Fetch Categories and Budgets immediately when the ViewModel is created
        viewModelScope.launch {
            categoriesRepository.getCategoriesByType(TransactionType.EXPENSE).collect { cats ->
                _uiState.update { it.copy(categories = cats) }
            }
        }
        viewModelScope.launch {
            budgetsRepository.getAllBudgets().collect { buds ->
                _uiState.update { it.copy(budgets = buds) }
            }
        }
    }

    fun initializeWithDraft(draft: SmartSplitDraft) {
        if (_uiState.value.draft != null) return

        val memberDetailsList = mutableListOf<MemberSplitDetail>()
        val billSubtotal = draft.items.sumOf { it.price * it.quantity }

        draft.members.forEach { member ->
            val myItems = draft.items.filter { it.assignedMemberIds.contains(member.id) }
                .map { item ->
                    val splitPrice = (item.price * item.quantity) / item.assignedMemberIds.size
                    MemberItemDetail(item.name, splitPrice)
                }

            val memberSubtotal = myItems.sumOf { it.splitPrice }
            val ratio = if (billSubtotal > 0) memberSubtotal / billSubtotal else 0.0

            val taxShare = draft.tax * ratio
            val serviceShare = draft.service * ratio
            val discountShare = draft.discount * ratio
            val othersShare = draft.others * ratio
            val totalOwed = memberSubtotal + taxShare + serviceShare + othersShare - discountShare

            memberDetailsList.add(
                MemberSplitDetail(
                    memberName = member.name,
                    items = myItems,
                    subtotal = memberSubtotal,
                    taxShare = taxShare,
                    serviceShare = serviceShare,
                    discountShare = discountShare,
                    othersShare = othersShare,
                    totalOwed = totalOwed
                )
            )
        }

        _uiState.update {
            it.copy(draft = draft, memberDetails = memberDetailsList, isLoading = false)
        }
    }

    fun saveToDatabase(
        context: Context,
        categoryId: Int = 1,
        budgetId: Int? = null,
        onSuccess: () -> Unit
    ) {
        val draft = _uiState.value.draft ?: return
        _uiState.update { it.copy(isSaving = true) }

        viewModelScope.launch(Dispatchers.IO) {

            // A. Handle Image: Move from Cache -> Public Gallery (Pictures/MonkeysLimit)
            var permanentImagePath: String? = null
            if (draft.imageUri != null) {
                permanentImagePath = saveImageToGallery(context, draft.imageUri)
            }

            // B. Insert Bill
            val smartSplit = SmartSplits(
                id = 0,
                name = draft.splitName,
                amountOwed = draft.total,
                imagePath = permanentImagePath,
                tax = draft.tax,
                service = draft.service,
                discount = draft.discount,
                others = draft.others,
                createDate = Date(),
                isPaid = false
            )
            val realSplitId = smartSplitsRepository.insert(smartSplit).toInt()

            // C. Snapshot Members
            val tempToRealMemberIdMap = mutableMapOf<Int, Int>()
            draft.members.forEach { draftMember ->
                val billMemberCopy = Members(
                    id = 0,
                    smartSplitId = realSplitId,
                    name = draftMember.name,
                    contact = draftMember.contact,
                    note = draftMember.note
                )
                val realId = membersRepository.insert(billMemberCopy).toInt()
                tempToRealMemberIdMap[draftMember.id] = realId
            }

            // D. Insert Items
            draft.items.forEach { draftItem ->
                val itemEntity = Items(
                    id = 0,
                    smartSplitId = realSplitId,
                    name = draftItem.name,
                    quantity = draftItem.quantity,
                    totalPrice = draftItem.price * draftItem.quantity
                )
                val realItemId = itemsRepository.insert(itemEntity).toInt()

                val assigneesCount = draftItem.assignedMemberIds.size
                if (assigneesCount > 0) {
                    val splitPrice = (draftItem.price * draftItem.quantity) / assigneesCount
                    draftItem.assignedMemberIds.forEach { tempId ->
                        val realMemberId = tempToRealMemberIdMap[tempId]
                        if (realMemberId != null) {
                            val memberItem = MemberItems(
                                id = 0,
                                memberId = realMemberId,
                                itemId = realItemId,
                                price = splitPrice,
                                quantity = 1
                            )
                            memberItemsRepository.insert(memberItem)
                        }
                    }
                }
            }

            // E. Save "You" using the provided dialog categories!
            val youDetail = _uiState.value.memberDetails.find { it.memberName.equals("You", ignoreCase = true) }
            if (youDetail != null && youDetail.totalOwed > 0) {
                val userTransaction = Transactions(
                    id = 0,
                    userId = "",
                    totalAmount = youDetail.totalOwed,
                    note = "Smart Split: ${draft.splitName}",
                    date = Date(),
                    type = TransactionType.EXPENSE,
                    categoryId = categoryId,
                    imagePath = null,
                    budgetId = budgetId
                )
                transactionsRepository.insert(userTransaction)
            }

            _uiState.update { it.copy(isSaving = false) }
            launch(Dispatchers.Main) { onSuccess() }
        }
    }

    private fun saveImageToGallery(context: Context, cacheUriString: String): String? {
        try {
            val cacheUri = cacheUriString.toUri()
            val inputStream = context.contentResolver.openInputStream(cacheUri) ?: return null

            val name = "bill_${System.currentTimeMillis()}.jpg"
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