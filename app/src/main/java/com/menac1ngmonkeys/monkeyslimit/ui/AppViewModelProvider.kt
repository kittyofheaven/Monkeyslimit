import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.menac1ngmonkeys.monkeyslimit.MonkeyslimitApplication
import com.menac1ngmonkeys.monkeyslimit.ui.dashboard.DashboardViewModel

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

        // You'll add initializers for other ViewModels here in the future
    }
}

// Helper function to get the application container
fun CreationExtras.monkeysLimitApplication(): MonkeyslimitApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MonkeyslimitApplication)