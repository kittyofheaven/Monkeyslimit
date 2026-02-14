package com.menac1ngmonkeys.monkeyslimit.ui.state

import com.example.budgetrecommendation.BudgetResult

data class BudgetRecommendationUiState(
    val isLoading: Boolean = true,
    val recommendations: List<RecommendedBudgetUi> = emptyList(),
    val errorMessage: String? = null,
    val userIncome: Double = 0.0 // The numeric income used for calculation
)

data class RecommendedBudgetUi(
    val category: String,
    val percentage: Double,
    val amount: Double
)