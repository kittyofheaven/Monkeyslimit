import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.menac1ngmonkeys.monkeyslimit.MonkeyslimitApplication
import com.menac1ngmonkeys.monkeyslimit.viewmodel.AnalyticsViewModel
import com.menac1ngmonkeys.monkeyslimit.viewmodel.AppViewModel
import com.menac1ngmonkeys.monkeyslimit.viewmodel.DashboardViewModel

/**
 * Central ViewModel factory wiring repositories from the application container.
 * Keeps UI code decoupled from concrete data implementations.
 */
object AppViewModelProvider {
    val Factory = viewModelFactory {
        // Initializer for DashboardViewModel
        initializer {
            DashboardViewModel(
                transactionsRepository = monkeysLimitApplication().container.transactionsRepository,
                categoriesRepository = monkeysLimitApplication().container.categoriesRepository,
                budgetsRepository = monkeysLimitApplication().container.budgetsRepository
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
                transactionsRepository = monkeysLimitApplication().container.transactionsRepository
            )
        }
        // You'll add initializers for other ViewModels here in the future
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