package com.menac1ngmonkeys.monkeyslimit.ui.state

import com.menac1ngmonkeys.monkeyslimit.data.local.entity.User

/**
 * Defines the specific authentication/profile stages.
 * Sealed Interface prevents "impossible states" (like being Loaded but also Loading).
 */
sealed interface ProfileAuthStatus {
    object Loading : ProfileAuthStatus      // Fetching data from Room/Firebase
    object Ghost : ProfileAuthStatus        // Auth exists, but Data is missing (needs sync)
    data class Incomplete(val user: User) : ProfileAuthStatus // Data exists, but missing fields (Phone/DOB)
    data class Verified(val user: User) : ProfileAuthStatus   // All good, ready for Dashboard
}

data class ProfileUiState(
    // The Master Status for the Gatekeeper
    val status: ProfileAuthStatus = ProfileAuthStatus.Loading,

    // We keep these flat fields so your existing ProfileScreen UI doesn't break
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val age: Int = 0,
    val dateOfBirth: String = "",
    val phoneNumber: String = "",
    val photoUrl: String? = null
)