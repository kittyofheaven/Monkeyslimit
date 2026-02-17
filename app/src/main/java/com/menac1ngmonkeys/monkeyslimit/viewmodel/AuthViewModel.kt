package com.menac1ngmonkeys.monkeyslimit.viewmodel

import android.util.Log
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.User
import com.menac1ngmonkeys.monkeyslimit.data.repository.UsersRepository
import com.menac1ngmonkeys.monkeyslimit.ui.state.AuthUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Date

class AuthViewModel(
    private val usersRepository: UsersRepository
) : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = Firebase.firestore
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState = _uiState.asStateFlow()

    private var snapshotListener: ListenerRegistration? = null
    // Flag to prevent double-triggering the sync listener for the same UID
    private var currentSyncUid: String? = null

    init {
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser

            // SECURITY CHECK: Check if the user is disabled/banned on startup
            user?.getIdToken(true)?.addOnFailureListener {
                Log.e("AuthViewModel", "Token invalid (Password changed or Banned). Signing out.")
                signOut()
            }
            _uiState.update { it.copy(currentUser = user) }
        }
    }

    override fun onCleared() {
        super.onCleared()
        snapshotListener?.remove()
    }

    // =========================================================================
    //  SYNC LOGIC (With "Draft Mode" Protection)
    // =========================================================================
    fun startRealtimeSync() {
        val uid = auth.currentUser?.uid ?: return

        if (currentSyncUid == uid && snapshotListener != null) {
            return
        }

        snapshotListener?.remove()
        currentSyncUid = uid

        snapshotListener = firestore.collection("users").document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    currentSyncUid = null
                    snapshotListener = null
                    return@addSnapshotListener
                }

                viewModelScope.launch(Dispatchers.IO) {
                    if (snapshot != null && snapshot.exists()) {
                        // CASE 1: Cloud Data Exists -> Normal Sync
                        val remoteUser = snapshot.toObject(User::class.java)

                        // Explicitly grab the marriage status to ensure it's captured
                        val cloudIsMarried = snapshot.getBoolean("isMarried") ?: false

                        remoteUser?.let { incomingUser ->
                            // 1. Check what we currently have in Room
                            val localUser = usersRepository.getUser(uid).firstOrNull()

                            // 2. Check if the local photo is a custom file path (starts with "/")
                            val localPhoto = localUser?.photoUrl
                            val isLocalCustomPhoto = localPhoto?.startsWith("/") == true

                            // 3. Preserve the local path if it exists
                            val userToSave = incomingUser.copy(
                                isSynced = true,
                                isMarried = cloudIsMarried,
                                photoUrl = if (isLocalCustomPhoto) localPhoto else incomingUser.photoUrl
                            )

                            // 4. Save the merged data to Room
                            usersRepository.saveUser(userToSave)
                        }
                    } else {
                        // CASE 2: Cloud is Empty (New User OR Deleted)
                        val localUser = usersRepository.getUser(uid).first()

                        if (localUser != null) {
                            if (localUser.isSynced) {
                                // Was synced, now gone -> Remote Delete
                                usersRepository.deleteUser(localUser)
                                if (auth.currentUser?.uid == localUser.uid) signOut()
                            } else {
                                // Was NOT synced (Offline / Draft).
                                // CHECK: Is this a "Not set" stub user?
                                if (localUser.mobileNumber == "Not set" || localUser.job == "Not set") {
                                    // DRAFT MODE DETECTED: Do NOT upload yet.
                                    // We wait for the user to finish the profile screen.
                                    Log.d("AuthViewModel", "SYNC: Skipping upload of incomplete 'Draft' profile.")
                                } else {
                                    // Valid Offline Data -> Heal Cloud
                                    Log.i("AuthViewModel", "SYNC: Uploading valid offline data.")
                                    val healedUser = localUser.copy(isSynced = true)
                                    firestore.collection("users").document(uid).set(healedUser)
                                    usersRepository.saveUser(healedUser)
                                }
                            }
                        }
                    }
                }
            }
    }

    // =========================================================================
    //  AUTHENTICATION ACTIONS
    // =========================================================================

    fun signInWithEmail(email: String, password: String) {
        val cleanEmail = email.trim()
        // Pure Kotlin Regex so it doesn't break your local JUnit tests
        val emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$".toRegex()

        // 1. Validation Block
        when {
            cleanEmail.isBlank() -> {
                _uiState.update { it.copy(error = "Please enter your email") }
                return
            }
            !emailRegex.matches(cleanEmail) -> {
                _uiState.update { it.copy(error = "Please enter a valid email address") }
                return
            }
            password.isBlank() -> {
                _uiState.update { it.copy(error = "Please enter your password") }
                return
            }
             password.length < 6 -> {
                 _uiState.update { it.copy(error = "Password must be at least 6 characters") }
                 return
             }
        }

        _uiState.update { it.copy(isLoading = true, error = null) }
        auth.signInWithEmailAndPassword(cleanEmail, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser

                    // NEW: CHECK IF EMAIL IS VERIFIED
                    if (user != null && user.isEmailVerified) {
                        _uiState.update { it.copy(currentUser = user, isLoading = false) }
                    } else {
                        // If not verified, sign them back out and show an error
                        auth.signOut()
                        _uiState.update {
                            it.copy(
                                error = "Please verify your email address before logging in. Check your inbox!",
                                isLoading = false
                            )
                        }
                    }
                } else {
                    _uiState.update { it.copy(error = task.exception?.message, isLoading = false) }
                }
            }
    }

    fun sendPasswordResetEmail(email: String, onSuccess: () -> Unit) {
        if (email.isBlank()) {
            _uiState.update { it.copy(error = "Please enter your email address to reset your password.") }
            return
        }

        // NEW: Check if it actually looks like an email address
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.update { it.copy(error = "Please enter a valid email address.") }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                _uiState.update { it.copy(isLoading = false) }
                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    // Firebase might throw an error if the user doesn't exist
                    _uiState.update { it.copy(error = task.exception?.message ?: "Failed to send reset email.") }
                }
            }
    }

    fun signUpWithEmail(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        mobile: String,
        job: String,
        birthDate: Date?,
        gender: String,
        income: String,
        isMarried: Boolean
    ) {
        if (birthDate == null) { _uiState.update { it.copy(error = "Birth date required") }; return }

        _uiState.update { it.copy(isLoading = true) }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = auth.currentUser
                    firebaseUser?.let { currentUser ->
                        val user = User(
                            uid = currentUser.uid,
                            firstName = firstName,
                            lastName = lastName,
                            email = email,
                            mobileNumber = mobile,
                            job = job,
                            birthDate = birthDate,
                            gender = gender,
                            income = income,
                            isMarried = isMarried,
                            isSynced = false
                        )

                        // 1. SEND THE VERIFICATION EMAIL
                        currentUser.sendEmailVerification().addOnCompleteListener { verifyTask ->
                            if (verifyTask.isSuccessful) {
                                // Launch a coroutine so we can use our new 'suspend' function
                                viewModelScope.launch(Dispatchers.IO) {

                                    // 2. Save user data to Room/Firestore and WAIT for it to finish
                                    saveUserToBoth(user)

                                    // 3. Data is safely in Firestore! NOW we can sign out without causing errors.
                                    auth.signOut()

                                    // 4. Update the UI to show a success message
                                    _uiState.update {
                                        it.copy(
                                            isLoading = false,
                                            error = "Success! Please check your email inbox and click the verification link before logging in."
                                        )
                                    }
                                }
                            } else {
                                _uiState.update {
                                    it.copy(
                                        isLoading = false,
                                        error = verifyTask.exception?.message ?: "Failed to send verification email"
                                    )
                                }
                            }
                        }
                    }
                } else {
                    _uiState.update { it.copy(error = task.exception?.message ?: "Registration failed", isLoading = false) }
                }
            }
    }

//    GOOGLE SIGN IN (Modified for Draft Mode)
    fun signInWithGoogle(idToken: String) {
        if (idToken.isBlank()) {
            _uiState.update { it.copy(error = "Google Sign-In failed: Missing ID Token") }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = auth.currentUser
                    val uid = firebaseUser?.uid

                    if (firebaseUser != null && uid != null) {
                        viewModelScope.launch(Dispatchers.IO) {
                            // 1. Check Firestore first
                            firestore.collection("users").document(uid).get()
                                .addOnSuccessListener { snapshot ->
                                    if (snapshot.exists()) {
                                        // Existing User -> Do nothing, let Sync handle it.
                                        Log.d("AuthViewModel", "Existing user detected.")
                                    } else {
                                        // New User -> Create DRAFT in Room Only
                                        Log.d("AuthViewModel", "New User detected. Creating Local Draft.")
                                        val nameParts = firebaseUser.displayName?.split(" ") ?: listOf("User", "")
                                        val fName = nameParts.getOrNull(0) ?: "User"
                                        val lName = if (nameParts.size > 1) nameParts.drop(1).joinToString(" ") else ""

                                        val newLocalUser = User(
                                            uid = uid,
                                            firstName = fName,
                                            lastName = lName,
                                            email = firebaseUser.email ?: "",
                                            mobileNumber = "Not set",
                                            job = "Not set",
                                            birthDate = Date(0),
                                            gender = "Not set",
                                            income = "Not set", // Default for draft
                                            isMarried = false,  // Default for draft
                                            photoUrl = firebaseUser.photoUrl?.toString(),
                                            isSynced = false
                                        )
                                        // SAVE TO ROOM ONLY (Skip Cloud)
                                        viewModelScope.launch(Dispatchers.IO) {
                                            usersRepository.saveUser(newLocalUser)
                                        }
                                    }
                                    _uiState.update { it.copy(currentUser = firebaseUser, isLoading = false) }
                                }
                                .addOnFailureListener {
                                    _uiState.update { it.copy(currentUser = firebaseUser, isLoading = false) }
                                }
                        }
                    }
                } else {
                    _uiState.update { it.copy(error = task.exception?.message, isLoading = false) }
                }
            }
    }

    fun completeGoogleProfile(
        mobile: String,
        job: String,
        birthDate: Date?,
        gender: String,
        income: String,
        isMarried: Boolean
    ) {
        val firebaseUser = auth.currentUser ?: return
        if (birthDate == null) { _uiState.update { it.copy(error = "Date required") }; return }

        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val existingUser = usersRepository.getUser(firebaseUser.uid).first()
                val updatedUser = User(
                    uid = firebaseUser.uid,
                    firstName = existingUser?.firstName ?: "User",
                    lastName = existingUser?.lastName ?: "",
                    email = existingUser?.email ?: firebaseUser.email ?: "",
                    mobileNumber = mobile,
                    job = job,
                    birthDate = birthDate,
                    gender = gender,
                    income = income,
                    isMarried = isMarried,
                    photoUrl = existingUser?.photoUrl ?: firebaseUser.photoUrl?.toString(),
                    isSynced = false
                )
                saveUserToBoth(updatedUser)
                _uiState.update { it.copy(isLoading = false, isRegistrationComplete = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    // Notice the 'suspend' keyword
    private suspend fun saveUserToBoth(user: User) {
        try {
            // 1. Upload to Cloud and WAIT for it to finish completely
            firestore.collection("users").document(user.uid)
                .set(user.copy(isSynced = true))
                .await() // <-- This is the magic word! It pauses here until success or failure.

            // 2. If we reach this line, Firestore succeeded. Mark Local as Synced.
            usersRepository.saveUser(user.copy(isSynced = true))

        } catch (e: Exception) {
            // 3. If Firestore fails (e.g., no internet), it throws an exception.
            // Catch it and save locally as Unsynced.
            usersRepository.saveUser(user.copy(isSynced = false))
        }
    }

    fun signOut(onSignOutComplete: () -> Unit = {}) {
        Log.d("AuthViewModel", "Signing Out / Canceling...")

        snapshotListener?.remove()
        snapshotListener = null
        currentSyncUid = null

        viewModelScope.launch(Dispatchers.IO) {
            val uid = auth.currentUser?.uid
            if (uid != null) {
                // WIPE DATA (This deletes the 'Draft' user effectively canceling registration)
                val user = usersRepository.getUser(uid).first()
                user?.let { usersRepository.deleteUser(it) }
            }

            auth.signOut()
            _uiState.value = AuthUiState(currentUser = null, isRegistrationComplete = false)

            withContext(Dispatchers.Main) {
                onSignOutComplete()
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}