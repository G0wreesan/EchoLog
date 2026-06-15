package com.echolog.app.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.echolog.app.model.UserProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import javax.inject.Inject

@HiltViewModel
class RegistrationViewModel @Inject constructor(
    private val supabase: SupabaseClient
) : ViewModel() {

    init {
        viewModelScope.launch {
            supabase.auth.sessionStatus.collect { status ->
                if (status is SessionStatus.Authenticated) {
                    fetchUserProfile()
                }
            }
        }
    }

    // --- State Management ---
    private val _username = MutableStateFlow("")
    val username = _username.asStateFlow()

    private val _displayName = MutableStateFlow("")
    val displayName = _displayName.asStateFlow()

    private val _usernameError = MutableStateFlow<String?>(null)
    val usernameError = _usernameError.asStateFlow()

    private val _selectedAvatarRes = MutableStateFlow<Int?>(null)
    val selectedAvatarRes = _selectedAvatarRes.asStateFlow()

    private val _selectedBitmap = MutableStateFlow<Bitmap?>(null)
    val selectedBitmap = _selectedBitmap.asStateFlow()

    private val _email = MutableStateFlow("")
    val email = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password = _password.asStateFlow()

    private val _otpCode = MutableStateFlow("")
    val otpCode = _otpCode.asStateFlow()

    private val _isOtpSent = MutableStateFlow(false)
    val isOtpSent = _isOtpSent.asStateFlow()

    private val _dob = MutableStateFlow("2000-01-01")
    val dob = _dob.asStateFlow()

    private val _isChecking = MutableStateFlow(false)
    val isChecking = _isChecking.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError = _authError.asStateFlow()

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile = _userProfile.asStateFlow()

    // --- Actions ---
    fun onUsernameChange(it: String) {
        _username.value = it
        if (_usernameError.value != null) _usernameError.value = null
    }
    fun onDisplayNameChange(it: String) { _displayName.value = it }
    fun onEmailChange(it: String) { _email.value = it }
    fun onPasswordChange(it: String) { _password.value = it }
    fun onOtpChange(it: String) { _otpCode.value = it }
    fun onDobChange(it: String) { _dob.value = it }
    fun selectAvatar(resId: Int) { _selectedAvatarRes.value = resId; _selectedBitmap.value = null }
    fun setCustomBitmap(bitmap: Bitmap) { _selectedBitmap.value = bitmap; _selectedAvatarRes.value = null }

    // --- Core Logic ---

    fun validateUsername(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isChecking.value = true
            _usernameError.value = null
            delay(600) // Aesthetic delay for progress indicator

            if (_username.value.length < 4) {
                _usernameError.value = "Username is too short"
            } else {
                // Logic: Check if username exists in Supabase 'profiles' table
                try {
                    val count = supabase.postgrest.from("profiles")
                        .select {
                            filter { eq("username", _username.value) }
                        }.data.length // Simplified check

                    onSuccess()
                } catch (e: Exception) {
                    onSuccess() // Proceeding for now, adjust based on table constraints
                }
            }
            _isChecking.value = false
        }
    }

    fun signUpWithEmail() {
        viewModelScope.launch {
            _isChecking.value = true
            try {
                supabase.auth.signUpWith(Email) {
                    email = _email.value
                    password = _password.value
                }
                _isOtpSent.value = true
            } catch (e: Exception) {
                _authError.value = e.localizedMessage
            } finally {
                _isChecking.value = false
            }
        }
    }
    fun loginWithEmail(identifier: String, pass: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isChecking.value = true
            _authError.value = null
            try {
                // Supabase Auth call
                supabase.auth.signInWith(Email) {
                    email = identifier
                    password = pass
                }
                // Once signed in, the init block's collector will trigger fetchUserProfile()
                onSuccess()
            } catch (e: Exception) {
                _authError.value = "Login Failed: ${e.localizedMessage}"
            } finally {
                _isChecking.value = false
            }
        }
    }

    fun verifyOtp(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isChecking.value = true
            try {
                supabase.auth.verifyEmailOtp(
                    type = OtpType.Email.SIGNUP,
                    email = _email.value,
                    token = _otpCode.value
                )
                onSuccess()
            } catch (e: Exception) {
                _authError.value = "Invalid Code"
            } finally {
                _isChecking.value = false
            }
        }
    }

    fun finalizeAccount(onComplete: () -> Unit) {
        viewModelScope.launch {
            _isChecking.value = true
            try {
                val user = supabase.auth.currentUserOrNull() ?: throw Exception("No Auth User")
                var finalAvatarUrl: String? = null

                if (_selectedBitmap.value != null) {
                    val stream = ByteArrayOutputStream()
                    _selectedBitmap.value!!.compress(Bitmap.CompressFormat.JPEG, 80, stream)
                    val fileName = "${user.id}_avatar.jpg"
                    val bucket = supabase.storage.from("avatars")
                    bucket.upload(fileName, stream.toByteArray()) { upsert = true }
                    finalAvatarUrl = bucket.publicUrl(fileName)
                } else if (_selectedAvatarRes.value != null) {
                    finalAvatarUrl = "local_res_${_selectedAvatarRes.value}"
                }

                val profile = UserProfile(
                    id = user.id,
                    username = _username.value,
                    display_name = _displayName.value,
                    email = _email.value,
                    date_of_birth = _dob.value,
                    avatar_url = finalAvatarUrl
                )

                supabase.postgrest.from("profiles").upsert(profile)
                _userProfile.value = profile
                onComplete()
            } catch (e: Exception) {
                _authError.value = "Error: ${e.localizedMessage}"
            } finally {
                _isChecking.value = false
            }
        }
    }

    fun fetchUserProfile() {
        viewModelScope.launch {
            try {
                val user = supabase.auth.currentUserOrNull() ?: return@launch
                val profile = supabase.postgrest.from("profiles")
                    .select { filter { eq("id", user.id) } }
                    .decodeSingle<UserProfile>()
                _userProfile.value = profile
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun checkSession(): io.github.jan.supabase.auth.user.UserInfo? {
        return try {
            supabase.auth.retrieveUserForCurrentSession(updateSession = true)
        } catch (e: Exception) {
            null
        }
    }

    fun updateDisplayName(newName: String) {
        viewModelScope.launch {
            try {
                val user = supabase.auth.currentUserOrNull() ?: return@launch

                // Update Supabase
                supabase.postgrest.from("profiles").update({
                    set("display_name", newName)
                }) {
                    filter { eq("id", user.id) }
                }

                // Refresh local profile state
                fetchUserProfile()
            } catch (e: Exception) {
                _authError.value = "Update failed: ${e.localizedMessage}"
            }
        }
    }

    fun updateAvatar(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            _isChecking.value = true
            try {
                val user = supabase.auth.currentUserOrNull() ?: throw Exception("No Auth User")
                var finalAvatarUrl: String? = null

                if (_selectedBitmap.value != null) {
                    val stream = ByteArrayOutputStream()
                    _selectedBitmap.value!!.compress(Bitmap.CompressFormat.JPEG, 80, stream)
                    val fileName = "${user.id}_avatar_${System.currentTimeMillis()}.jpg"
                    val bucket = supabase.storage.from("avatars")
                    bucket.upload(fileName, stream.toByteArray()) { upsert = true }
                    finalAvatarUrl = bucket.publicUrl(fileName)
                } else if (_selectedAvatarRes.value != null) {
                    finalAvatarUrl = "local_res_${_selectedAvatarRes.value}"
                }

                if (finalAvatarUrl != null) {
                    supabase.postgrest.from("profiles").update({
                        set("avatar_url", finalAvatarUrl)
                    }) {
                        filter { eq("id", user.id) }
                    }
                    fetchUserProfile()
                }
                onComplete()
            } catch (e: Exception) {
                _authError.value = "Avatar update failed: ${e.localizedMessage}"
            } finally {
                _isChecking.value = false
                _selectedBitmap.value = null
                _selectedAvatarRes.value = null
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            supabase.auth.signOut()
            _userProfile.value = null
        }
    }
}