import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.menac1ngmonkeys.monkeyslimit.MonkeyslimitApplication
import com.menac1ngmonkeys.monkeyslimit.viewmodel.AnalyticsViewModel
import com.menac1ngmonkeys.monkeyslimit.viewmodel.AppViewModel
import com.menac1ngmonkeys.monkeyslimit.viewmodel.AuthViewModel
import com.menac1ngmonkeys.monkeyslimit.viewmodel.BudgetDetailViewModel
import com.menac1ngmonkeys.monkeyslimit.viewmodel.BudgetRecommendationViewModel
import com.menac1ngmonkeys.monkeyslimit.viewmodel.BudgetViewModel
import com.menac1ngmonkeys.monkeyslimit.viewmodel.DashboardViewModel
import com.menac1ngmonkeys.monkeyslimit.viewmodel.EditProfileViewModel
import com.menac1ngmonkeys.monkeyslimit.viewmodel.ManualTransactionViewModel
import com.menac1ngmonkeys.monkeyslimit.viewmodel.ProfileViewModel
import com.menac1ngmonkeys.monkeyslimit.viewmodel.ReviewSmartSplitViewModel
import com.menac1ngmonkeys.monkeyslimit.viewmodel.ReviewTransactionViewModel
import com.menac1ngmonkeys.monkeyslimit.viewmodel.ScanTransactionViewModel
import com.menac1ngmonkeys.monkeyslimit.viewmodel.SelectMemberViewModel
import com.menac1ngmonkeys.monkeyslimit.viewmodel.SmartSplitHistoryViewModel
import com.menac1ngmonkeys.monkeyslimit.viewmodel.SmartSplitDetailViewModel
import com.menac1ngmonkeys.monkeyslimit.viewmodel.SmartSplitViewModel
import com.menac1ngmonkeys.monkeyslimit.viewmodel.SplashViewModel
import com.menac1ngmonkeys.monkeyslimit.viewmodel.SplitResultViewModel
import com.menac1ngmonkeys.monkeyslimit.viewmodel.TransactionDetailViewModel

/**
 * Central ViewModel factory wiring repositories from the application container.
 * Keeps UI code decoupled from concrete data implementations.
 */
object AppViewModelProvider {
    val Factory = viewModelFactory {
        // Initializer for SplashViewModel
        initializer {
            SplashViewModel()
        }
        // Initializer for AuthViewModel
        initializer {
            AuthViewModel(
                usersRepository = monkeysLimitApplication().container.usersRepository
            )
        }
        // Initializer for DashboardViewModel
        initializer {
            DashboardViewModel(
                transactionsRepository = monkeysLimitApplication().container.transactionsRepository,
                categoriesRepository = monkeysLimitApplication().container.categoriesRepository,
                budgetsRepository = monkeysLimitApplication().container.budgetsRepository,
                usersRepository = monkeysLimitApplication().container.usersRepository
            )
        }
        // Initializer for AppViewModel
        initializer {
            AppViewModel(
                transactionsRepository = monkeysLimitApplication().container.transactionsRepository,
                budgetsRepository = monkeysLimitApplication().container.budgetsRepository
            )
        }
        // Initializer for AnalyticsViewModel
        initializer {
            AnalyticsViewModel(
                transactionsRepository = monkeysLimitApplication().container.transactionsRepository,
                categoriesRepository = monkeysLimitApplication().container.categoriesRepository
            )
        }
        // Initializer for BudgetViewModel
        initializer {
            BudgetViewModel(
                budgetsRepository = monkeysLimitApplication().container.budgetsRepository,
                categoriesRepository = monkeysLimitApplication().container.categoriesRepository,
                transactionsRepository = monkeysLimitApplication().container.transactionsRepository,
            )
        }
        // Initializer for ProfileViewModel
        initializer {
            ProfileViewModel(
                usersRepository = monkeysLimitApplication().container.usersRepository
            ) // It has no dependencies for now
        }
        // Initializer for EditProfileViewModel
        initializer {
            EditProfileViewModel(
                monkeysLimitApplication().container.usersRepository
            )
        }
        // Initializer for ScanTransactionViewModel
        initializer {
            ScanTransactionViewModel()
        }
        // Initializer for ReviewTransactionViewModel
        initializer {
            ReviewTransactionViewModel(
                transactionsRepository = monkeysLimitApplication().container.transactionsRepository,
                budgetsRepository = monkeysLimitApplication().container.budgetsRepository,
                categoriesRepository = monkeysLimitApplication().container.categoriesRepository
            )
        }
        // Initializer for ManualTransactionViewModel
        initializer {
            ManualTransactionViewModel(
                transactionsRepository = monkeysLimitApplication().container.transactionsRepository,
                budgetsRepository = monkeysLimitApplication().container.budgetsRepository,
                categoriesRepository = monkeysLimitApplication().container.categoriesRepository
            )
        }
        // Initializer for ReviewSmartSplitViewModel
        initializer {
            ReviewSmartSplitViewModel()
        }
        // Initializer for SplitResultViewModel
        initializer {
            SplitResultViewModel(
                smartSplitsRepository = monkeysLimitApplication().container.smartSplitsRepository,
                membersRepository = monkeysLimitApplication().container.membersRepository,
                itemsRepository = monkeysLimitApplication().container.itemsRepository,
                memberItemsRepository = monkeysLimitApplication().container.memberItemsRepository,
                transactionsRepository = monkeysLimitApplication().container.transactionsRepository,
                categoriesRepository = monkeysLimitApplication().container.categoriesRepository,
                budgetsRepository = monkeysLimitApplication().container.budgetsRepository
            )
        }
        // Initializer for SelectMemberViewModel
        initializer {
            SelectMemberViewModel(
                membersRepository = monkeysLimitApplication().container.membersRepository
            )
        }
        // Initializer for SmartSplitHistoryViewModel
        initializer {
            SmartSplitHistoryViewModel(
                smartSplitsRepository = monkeysLimitApplication().container.smartSplitsRepository
            )
        }
        // Initializer for SmartSplitViewModel
        initializer {
            SmartSplitViewModel()
        }
        // Initializer for BudgetRecommendationViewModel
        initializer {
            BudgetRecommendationViewModel(
                usersRepository = monkeysLimitApplication().container.usersRepository,
                budgetsRepository = monkeysLimitApplication().container.budgetsRepository
            )
        }
        // You'll add initializers for other ViewModels here in the future
    }
}

/**
 * A custom factory for creating ViewModels that require a specific ID at runtime.
 */
class BudgetDetailViewModelFactory(private val budgetId: Int) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        if (modelClass.isAssignableFrom(BudgetDetailViewModel::class.java)) {
            val application = extras.monkeysLimitApplication()
            val container = application.container
            @Suppress("UNCHECKED_CAST")
            return BudgetDetailViewModel(
                budgetId = budgetId,
                budgetsRepository = container.budgetsRepository,
                transactionsRepository = container.transactionsRepository,
                categoriesRepository = container.categoriesRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

/**
 * Factory for creating SmartSplitDetailViewModel with a dynamic splitId.
 */
class SmartSplitDetailViewModelFactory(private val splitId: Int) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        if (modelClass.isAssignableFrom(SmartSplitDetailViewModel::class.java)) {
            val application = extras.monkeysLimitApplication()
            val container = application.container
            @Suppress("UNCHECKED_CAST")
            return SmartSplitDetailViewModel(
                splitId = splitId,
                smartSplitsRepository = container.smartSplitsRepository,
                membersRepository = container.membersRepository,
                itemsRepository = container.itemsRepository,
                memberItemsRepository = container.memberItemsRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

/**
 * Factory for creating TransactionDetailViewModel with a dynamic transactionId.
 */
class TransactionDetailViewModelFactory(private val transactionId: Int) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        if (modelClass.isAssignableFrom(TransactionDetailViewModel::class.java)) {
            val application = extras.monkeysLimitApplication()
            val container = application.container
            @Suppress("UNCHECKED_CAST")
            return TransactionDetailViewModel(
                transactionId = transactionId,
                transactionsRepository = container.transactionsRepository,
                categoriesRepository = container.categoriesRepository,
                budgetsRepository = container.budgetsRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

/**
 * Convenience accessor for the application instance inside CreationExtras.
 * Helper function to get the application container
 * 
 * @return the app instance cast to [MonkeyslimitApplication].
 */
fun CreationExtras.monkeysLimitApplication(): MonkeyslimitApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MonkeyslimitApplication)