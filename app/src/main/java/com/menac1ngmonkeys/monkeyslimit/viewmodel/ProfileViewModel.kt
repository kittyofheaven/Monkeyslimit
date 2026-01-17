package com.menac1ngmonkeys.monkeyslimit.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.User
import com.menac1ngmonkeys.monkeyslimit.data.repository.UsersRepository
import com.menac1ngmonkeys.monkeyslimit.ui.state.ProfileAuthStatus
import com.menac1ngmonkeys.monkeyslimit.ui.state.ProfileUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ProfileViewModel(
    private val usersRepository: UsersRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState = _uiState.asStateFlow()

    private val auth = FirebaseAuth.getInstance()

    // FIX: Keep track of the running job so we can kill it
    private var loadDataJob: Job? = null

    init {
        auth.addAuthStateListener { firebaseAuth ->
            val currentUser = firebaseAuth.currentUser
            Log.d("ProfileViewModel", "AuthStateChanged. User: ${currentUser?.uid}")

            if (currentUser == null) {
                // Cancel any running listeners immediately
                loadDataJob?.cancel()

                // Reset State
                _uiState.value = ProfileUiState(status = ProfileAuthStatus.Loading)
                Log.d("ProfileViewModel", "User logged out. State reset to Loading.")
            } else {
                // Load data for the new user
                loadUserData(currentUser.uid)
            }
        }
    }

    private fun loadUserData(uid: String) {
        // FIX: Cancel the previous job to prevent "Zombie" updates
        loadDataJob?.cancel()

        loadDataJob = viewModelScope.launch {
            Log.d("ProfileViewModel", "Starting Room Collection for UID: $uid")

            // Initial loading state
            _uiState.update { it.copy(status = ProfileAuthStatus.Loading) }

            usersRepository.getUser(uid).collect { localUser ->
                Log.d("ProfileViewModel", "Room emitted update for $uid. User found? ${localUser != null}")

                if (localUser != null) {
                    val isDatePlaceholder = localUser.birthDate.time == 0L ||
                            formatDate(localUser.birthDate) == "1 January 1970"

                    val isComplete = localUser.mobileNumber.isNotBlank()
                            && localUser.mobileNumber != "Not set"
                            && !isDatePlaceholder

                    val newStatus = if (isComplete) {
                        ProfileAuthStatus.Verified(localUser)
                    } else {
                        ProfileAuthStatus.Incomplete(localUser)
                    }

                    Log.d("ProfileViewModel", "Setting Status -> $newStatus")

                    _uiState.update {
                        it.copy(
                            status = newStatus,
                            uid = localUser.uid,
                            name = "${localUser.firstName} ${localUser.lastName}".trim().ifEmpty { "User" },
                            email = localUser.email,
                            photoUrl = localUser.photoUrl ?: auth.currentUser?.photoUrl?.toString(),
                            age = if (isDatePlaceholder) 0 else calculateAge(localUser.birthDate),
                            dateOfBirth = if (isDatePlaceholder) "Not set" else formatDate(localUser.birthDate),
                            phoneNumber = localUser.mobileNumber
                        )
                    }
                } else {
                    // Auth exists, but Room is empty -> Ghost State
                    Log.d("ProfileViewModel", "User $uid not in Room yet. Setting Status -> Ghost")
                    _uiState.update {
                        it.copy(status = ProfileAuthStatus.Ghost)
                    }
                }
            }
        }
    }

    fun updateProfile(updatedUser: User) {
        viewModelScope.launch(Dispatchers.IO) {
            usersRepository.saveUser(updatedUser)
            Firebase.firestore
                .collection("users")
                .document(updatedUser.uid)
                .set(updatedUser)
        }
    }

    private fun calculateAge(birthDate: Date): Int {
        val dob = Calendar.getInstance().apply { time = birthDate }
        val today = Calendar.getInstance()
        var age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR)
        if (today.get(Calendar.MONTH) < dob.get(Calendar.MONTH) ||
            (today.get(Calendar.MONTH) == dob.get(Calendar.MONTH) &&
                    today.get(Calendar.DAY_OF_MONTH) < dob.get(Calendar.DAY_OF_MONTH))) {
            age--
        }
        return if (age < 0) 0 else age
    }

    private fun formatDate(date: Date): String {
        val formatter = SimpleDateFormat("d MMMM yyyy", Locale.getDefault())
        return formatter.format(date)
    }
}