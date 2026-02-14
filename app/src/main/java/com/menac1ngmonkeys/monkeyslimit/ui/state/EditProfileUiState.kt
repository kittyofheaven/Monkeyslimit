package com.menac1ngmonkeys.monkeyslimit.ui.state

import android.net.Uri

data class EditProfileUiState(
    val uid: String = "",
    val name: String = "", // Combined First + Last name for UI
    val email: String = "",
    val mobileNumber: String = "",
    val birthDate: Long = 0L, // Stored as timestamp
    val gender: String = "",
    val job: String = "",
    val income: String = "",
    val isMarried: Boolean = false,
    val photoUrl: String? = null,
    val newImageUri: Uri? = null, // Temporary URI for new selected image
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val errorMessage: String? = null
)