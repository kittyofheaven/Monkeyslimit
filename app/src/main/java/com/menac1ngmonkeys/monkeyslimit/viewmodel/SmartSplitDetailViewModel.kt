package com.menac1ngmonkeys.monkeyslimit.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.menac1ngmonkeys.monkeyslimit.data.repository.ItemsRepository
import com.menac1ngmonkeys.monkeyslimit.data.repository.MemberItemsRepository
import com.menac1ngmonkeys.monkeyslimit.data.repository.MembersRepository
import com.menac1ngmonkeys.monkeyslimit.data.repository.SmartSplitsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SmartSplitDetailUiState(
    val splitName: String = "",
    val totalAmount: Double = 0.0,
    val memberDetails: List<MemberSplitDetail> = emptyList(),
    val isLoading: Boolean = true
)

class SmartSplitDetailViewModel(
    private val splitId: Int,
    private val smartSplitsRepository: SmartSplitsRepository,
    private val membersRepository: MembersRepository,
    private val itemsRepository: ItemsRepository,
    private val memberItemsRepository: MemberItemsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SmartSplitDetailUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadDetail()
    }

    private fun loadDetail() {
        viewModelScope.launch {
            // 1. Get Split Info
            val splitList = smartSplitsRepository.getAllSmartSplits().first() // Assuming no getById yet
            val split = splitList.find { it.id == splitId } ?: return@launch

            // 2. Get Members
            val members = membersRepository.getMembersBySplitId(splitId).first()

            // 3. Get Items (needed for calculating total subtotal)
            val items = itemsRepository.getItemsBySplitId(splitId).first()
            val totalBillSubtotal = items.sumOf { it.totalPrice }

            val memberDetailsList = mutableListOf<MemberSplitDetail>()

            // 4. Build Detail for each member
            members.forEach { member ->
                val memberItems = memberItemsRepository.getMemberItemsByMemberId(member.id).first()

                val myItems = memberItems.map { mi ->
                    val item = items.find { it.id == mi.itemId }
                    MemberItemDetail(
                        itemName = item?.name ?: "Unknown Item",
                        splitPrice = mi.price // This is already the split share
                    )
                }

                val memberSubtotal = myItems.sumOf { it.splitPrice }

                // Calculate ratios based on the stored totals in SmartSplit entity
                val ratio = if (totalBillSubtotal > 0) memberSubtotal / totalBillSubtotal else 0.0

                val taxShare = split.tax * ratio
                val serviceShare = split.service * ratio
                val discountShare = split.discount * ratio
                val othersShare = split.others * ratio
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
                it.copy(
                    splitName = split.name,
                    totalAmount = split.amountOwed,
                    memberDetails = memberDetailsList,
                    isLoading = false
                )
            }
        }
    }
}