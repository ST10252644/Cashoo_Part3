package com.iie.st10320489.marene.viewmodel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.iie.st10320489.marene.data.repository.*
import com.iie.st10320489.marene.data.entities.*

// ViewModel for managing user-related data
class UserViewModel(private val repo: UserRepository) : ViewModel() { // (Android Developers, 2024)

    val allUsers = repo.getAllUsers().stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())


    fun insert(user: User) = viewModelScope.launch { repo.insertUser(user) } // (Android Developers, 2024)
}

// ViewModel for managing user settings
class UserSettingsViewModel(private val repo: UserSettingsRepository) : ViewModel() {
    // Collects all user settings as a StateFlow
    val all = repo.getAllSettings().stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())


    fun insert(settings: UserSettings) = viewModelScope.launch {
        repo.insertUserSettings(settings)
    } // (Android Developers, 2024)
}

// ViewModel for handling transactions
class TransactionViewModel(private val repo: TransactionRepository) : ViewModel() {
    // Collects all transactions as a StateFlow
    val all = repo.getAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())


    fun insert(transaction: Transaction) = viewModelScope.launch { repo.insert(transaction) }
}

// ViewModel for handling categories
class CategoryViewModel(private val repo: CategoryRepository) : ViewModel() {
    // Collects all categories from the repository (assumed to be LiveData or Flow)
    val all = repo.getAll()


    fun insert(category: Category) = viewModelScope.launch { repo.insert(category) }
}

// ViewModel for managing subcategories
class SubCategoryViewModel(private val repo: SubCategoryRepository) : ViewModel() {
    // Retrieves all subcategories as LiveData (not wrapped with stateIn)
    val all = repo.getAll()

    // Launches a coroutine to insert a subcategory
    fun insert(subCategory: SubCategory) = viewModelScope.launch { repo.insert(subCategory) }
}

// ViewModel for managing rewards
class RewardViewModel(private val repo: RewardRepository) : ViewModel() {
    // Collects all rewards as a StateFlow
    val all = repo.getAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    // Launches a coroutine to insert a reward
    fun insert(reward: Reward) = viewModelScope.launch { repo.insert(reward) }
} // (Android Developers, 2024)

// ViewModel for managing reward history
class RewardHistoryViewModel(private val repo: RewardHistoryRepository) : ViewModel() {

    val all = repo.getAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())


    fun insert(history: RewardHistory) = viewModelScope.launch { repo.insert(history) }
} // (Android Developers, 2024)

// ViewModel for tracking activity logs
class ActivityLogViewModel(private val repo: ActivityLogRepository) : ViewModel() {
    // Collects all activity logs as a StateFlow
    val all = repo.getAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())


    fun insert(log: ActivityLog) = viewModelScope.launch { repo.insert(log) }
}

//Bibliography
//AndroidDevelopers, 2024. Save data in a local database using Room. [Online] Available at: hRps://developer.android.com/training/data-storage/room [Accessed 27 April 2025].
//AndroidDevelopers, 2024. Write asynchronous DAO queries. [Online]
//Available at: hRps://developer.android.com/training/data-storage/room/async- queries?authuser=2
//[Accessed 26 April 2025].
//Raikwar, A., 2024. Ge=ng Started with Room Database in Android. [Online]
//Available at: hRps://amitraikwar.medium.com/ge[ng-started-with-room-database-in- android-fa1ca23ce21e
//[Accessed 27 April 2025].
//Raikwar, A., 2023. Ge=ng Started with Room Database in Android. [Online]
//Available at: hRps://developer.android.com/develop#core-areas
//[Accessed 28 April 2025].
//Cal, C. W., 2023. Room Database Android Studio Kotlin Example Tutorial. [Online] Available at: hRps://youtu.be/-LNg-K7SncM?si=y8cbMdvhhp48Pp9-
//[Accessed 27 April 2025].
//College, I. V., 2025. PROG7313 Module-Manual / Module-Outline. Pretoria: Varsity College Pretoria.
//Viegen, F. v., 2022. A PracKcal introducKon to Android Room-3 : EnKty, Dao and Database objects.. [Online]
//Available at: hRps://youtu.be/RstQg7f4Edk?si=8RoAGp-OKPpMNVdY
//[Accessed 28 April 2025].