package com.menac1ngmonkeys.monkeyslimit.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.User
import com.menac1ngmonkeys.monkeyslimit.data.repository.UsersRepository
import com.menac1ngmonkeys.monkeyslimit.ui.state.EditProfileUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.Date

class EditProfileViewModel(
    private val usersRepository: UsersRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState = _uiState.asStateFlow()

    private val auth = FirebaseAuth.getInstance()
    private val db = Firebase.firestore

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        val uid = auth.currentUser?.uid ?: return
        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            // 1. Get the current local data from Room first
            val localUser = usersRepository.getUser(uid).firstOrNull()

            // 2. Fetch the latest data from Firestore
            db.collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val firestorePhoto = document.getString("photoUrl")

                        // CHECK: If Room already has a local file path (starts with /data)
                        // we MUST keep it and ignore the Firestore Google URL.
                        val currentLocalPath = localUser?.photoUrl
                        val isLocalPath = currentLocalPath?.startsWith("/") ?: false

                        val finalPhotoUrl = if (isLocalPath) currentLocalPath else firestorePhoto

                        _uiState.update {
                            it.copy(
                                uid = uid,
                                name = "${document.getString("firstName") ?: ""} ${document.getString("lastName") ?: ""}".trim(),
                                email = document.getString("email") ?: "",
                                mobileNumber = document.getString("mobileNumber") ?: "",
                                birthDate = (document.get("birthDate") as? com.google.firebase.Timestamp)?.toDate()?.time ?: 0L,
                                gender = document.getString("gender") ?: "",
                                job = document.getString("job") ?: "",
                                income = document.getString("income") ?: "",
                                isMarried = document.getBoolean("isMarried") ?: false,
                                photoUrl = finalPhotoUrl, // This ensures the local path wins
                                isLoading = false
                            )
                        }
                    }
                }
        }
    }

    // --- Save Logic ---
    fun saveProfile(context: Context, onSuccess: () -> Unit) {
        val state = _uiState.value
        _uiState.update { it.copy(isSaving = true) }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Persist new image if selected, otherwise keep current
                val finalPhotoUrl = if (state.newImageUri != null) {
                    persistImageLocally(context, state.newImageUri)
                } else {
                    state.photoUrl
                }

                val nameParts = state.name.trim().split(" ")
                val firstName = nameParts.firstOrNull() ?: ""
                val lastName = if (nameParts.size > 1) nameParts.drop(1).joinToString(" ") else ""

                val updatedUser = User(
                    uid = state.uid,
                    firstName = firstName,
                    lastName = lastName,
                    email = state.email,
                    mobileNumber = state.mobileNumber,
                    birthDate = Date(state.birthDate),
                    gender = state.gender,
                    job = state.job,
                    income = state.income,
                    photoUrl = finalPhotoUrl,
                    isMarried = state.isMarried
                )

                // Save locally to Room
                usersRepository.saveUser(updatedUser)

                // Update Firestore but EXCLUDE photoUrl so Google URL stays in cloud
                val firestoreData = mapOf(
                    "firstName" to firstName,
                    "lastName" to lastName,
                    "mobileNumber" to state.mobileNumber,
                    "birthDate" to updatedUser.birthDate,
                    "gender" to state.gender,
                    "job" to state.job,
                    "income" to state.income,
                    "isMarried" to state.isMarried
                )
                db.collection("users").document(updatedUser.uid).update(firestoreData)

                _uiState.update { it.copy(isSaving = false, isSaved = true, photoUrl = finalPhotoUrl, newImageUri = null) }
                withContext(Dispatchers.Main) { onSuccess() }

            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, errorMessage = e.message) }
            }
        }
    }

    private fun persistImageLocally(context: Context, uri: Uri): String? {
        return try {
            val fileName = "profile_${System.currentTimeMillis()}.jpg"
            val permanentFile = File(context.filesDir, fileName)
            val inputStream = context.contentResolver.openInputStream(uri)
            val outputStream = FileOutputStream(permanentFile)

            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            permanentFile.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    fun useDefaultProfilePhoto() {
        val defaultUrl = auth.currentUser?.photoUrl?.toString() //
        _uiState.update {
            it.copy(
                newImageUri = null, // Clear any pending crop
                photoUrl = defaultUrl // Revert to Google's URL
            )
        }
    }

    // --- Helper updates ---
    fun updateName(newName: String) { _uiState.update { it.copy(name = newName) } }
    fun updateEmail(newEmail: String) { _uiState.update { it.copy(email = newEmail) } }
    fun updatePhone(newPhone: String) { _uiState.update { it.copy(mobileNumber = newPhone) } }
    fun updateBirthDate(newDate: Long) { _uiState.update { it.copy(birthDate = newDate) } }
    fun updateGender(newGender: String) { _uiState.update { it.copy(gender = newGender) } }
    fun updateJob(newJob: String) { _uiState.update { it.copy(job = newJob) } }
    fun updateIncome(newIncome: String) { _uiState.update { it.copy(income = newIncome) } }
    fun updateMarriageStatus(isMarried: Boolean) { _uiState.update { it.copy(isMarried = isMarried) } }
    fun updateImageUri(uri: Uri?) { _uiState.update { it.copy(newImageUri = uri) } }
}