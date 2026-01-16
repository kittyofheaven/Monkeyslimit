package com.menac1ngmonkeys.monkeyslimit.ui.state

import com.google.firebase.auth.FirebaseUser

data class AuthUiState(
    val currentUser: FirebaseUser? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isRegistrationComplete: Boolean = false
)