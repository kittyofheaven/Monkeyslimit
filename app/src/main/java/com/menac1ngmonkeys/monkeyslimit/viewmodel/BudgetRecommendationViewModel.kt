package com.menac1ngmonkeys.monkeyslimit.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.budgetrecommendation.RecommendationEngine
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.Budgets
import com.menac1ngmonkeys.monkeyslimit.data.repository.BudgetsRepository
import com.menac1ngmonkeys.monkeyslimit.data.repository.UsersRepository
import com.menac1ngmonkeys.monkeyslimit.ui.state.BudgetRecommendationUiState
import com.menac1ngmonkeys.monkeyslimit.ui.state.RecommendedBudgetUi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.FileNotFoundException
import java.util.Calendar
import java.util.Date

class BudgetRecommendationViewModel(
    private val usersRepository: UsersRepository,
    private val budgetsRepository: BudgetsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BudgetRecommendationUiState())
    val uiState = _uiState.asStateFlow()

    fun generateRecommendations(context: Context, userId: String) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch(Dispatchers.IO) {
            val user = usersRepository.getUser(userId).firstOrNull()

            if (user == null) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "User profile not found. Please complete your profile.") }
                return@launch
            }

            try {
                // 1. Prepare Data
                val age = calculateAge(user.birthDate)
                val maritalStatus = if (user.isMarried) "Married" else "Single"

                // 2. Call Recommendation Engine
                val rawResults = RecommendationEngine.recommendFromRaw(
                    context = context,
                    age = age,
                    job = user.job,
                    gender = user.gender,
                    income = user.income,
                    marital = maritalStatus
                )

                // 3. Calculate Amounts
                val numericIncome = parseIncome(user.income)

                val uiResults = rawResults.map { result ->
                    RecommendedBudgetUi(
                        category = result.category,
                        percentage = result.percent,
                        amount = numericIncome * (result.percent / 100.0)
                    )
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        recommendations = uiResults,
                        userIncome = numericIncome
                    )
                }

            } catch (e: Exception) {
                Log.e("BudgetRec", "Error generating recommendations", e)

                // Provide a more helpful error message for missing files
                val msg = if (e is FileNotFoundException || e.message?.contains("raw_responses.csv") == true) {
                    "File 'raw_responses.csv' not found in assets folder."
                } else {
                    "Error: ${e.localizedMessage}"
                }

                _uiState.update { it.copy(isLoading = false, errorMessage = msg) }
            }
        }
    }

    fun applyRecommendations(onSuccess: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val recommendations = _uiState.value.recommendations

            // 1. Get the list of existing budgets currently in the DB
            val existingBudgets = budgetsRepository.getAllBudgets().first()

            recommendations.forEach { rec ->
                if (rec.amount > 0) {
                    // 2. Check if a budget with this name already exists
                    val existingBudget = existingBudgets.find { it.name.equals(rec.category, ignoreCase = true) }

                    if (existingBudget != null) {
                        // UPDATE: Keep the ID and other details, just update the limit
                        val updatedBudget = existingBudget.copy(
                            limitAmount = rec.amount,
                            // Optionally update the note if you want to indicate it was AI adjusted
                            // note = existingBudget.note + " (Updated by AI)"
                        )
                        budgetsRepository.update(updatedBudget)
                    } else {
                        // INSERT: Create a new budget if it doesn't exist
                        val newBudget = Budgets(
                            id = 0,
                            name = rec.category,
                            amount = 0.0,
                            limitAmount = rec.amount,
                            startDate = Date(),
                            endDate = null,
                            note = "AI Recommended"
                        )
                        budgetsRepository.insert(newBudget)
                    }
                }
            }

            launch(Dispatchers.Main) { onSuccess() }
        }
    }

    private fun calculateAge(birthDate: Date): Int {
        if (birthDate.time == 0L) return 25 // Default age if missing

        val dob = Calendar.getInstance().apply { time = birthDate }
        val today = Calendar.getInstance()
        var age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR)
        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
            age--
        }
        return age
    }

    private fun parseIncome(incomeStr: String): Double {
        return try {
            // 1. Remove "Rp" and dots "." to make it a raw number string
            //    Example: "Rp 1.000.000 - Rp 3.000.000" -> " 1000000 -  3000000"
            var clean = incomeStr.replace(".", "").replace("Rp", "").trim()

            // 2. Normalize Separators: Replace en-dash ("–") with standard hyphen ("-")
            //    This fixes the issue where different dash types cause parsing to fail
            clean = clean.replace("–", "-")

            when {
                // 3. Handle Ranges (e.g. "1000000 - 3000000")
                clean.contains("-") -> {
                    val parts = clean.split("-")
                    val low = parts[0].trim().toDouble()
                    val high = parts[1].trim().toDouble()
                    (low + high) / 2 // Return the average
                }
                // 4. Handle "Less than" (e.g. "< 1000000")
                clean.contains("<") -> {
                    clean.replace("<", "").trim().toDouble()
                }
                // 5. Handle "More than" (e.g. "> 20000000")
                clean.contains(">") -> {
                    clean.replace(">", "").trim().toDouble()
                }
                // 6. Handle exact numbers
                else -> clean.toDoubleOrNull() ?: 0.0
            }
        } catch (e: Exception) {
            e.printStackTrace()
            0.0
        }
    }
}