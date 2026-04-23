package com.echolog.app.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.OtpType
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import io.github.jan.supabase.auth.auth
@HiltViewModel
class RegistrationViewModel @Inject constructor(
    private val supabase: SupabaseClient
) : ViewModel() {

    // Identity (Step A)
    private val _username = MutableStateFlow("")
    val username = _username.asStateFlow()

    private val _displayName = MutableStateFlow("")
    val displayName = _displayName.asStateFlow()

    private val _isChecking = MutableStateFlow(false)
    val isChecking = _isChecking.asStateFlow()

    private val _usernameError = MutableStateFlow<String?>(null)
    val usernameError = _usernameError.asStateFlow()

    // Appearance (Step B)
    private val _selectedAvatarRes = MutableStateFlow<Int?>(null)
    val selectedAvatarRes = _selectedAvatarRes.asStateFlow()

    private val _selectedBitmap = MutableStateFlow<Bitmap?>(null)
    val selectedBitmap = _selectedBitmap.asStateFlow()

    // Security (Step C)
    private val _email = MutableStateFlow("")
    val email = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password = _password.asStateFlow()

    private val _otpCode = MutableStateFlow("")
    val otpCode = _otpCode.asStateFlow()

    private val _isOtpSent = MutableStateFlow(false)
    val isOtpSent = _isOtpSent.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError = _authError.asStateFlow()


    private val _interests = MutableStateFlow<Set<String>>(emptySet())
    val interests = _interests.asStateFlow()
    val availableCategories = listOf("Tech", "Design", "Gaming", "Music", "Art", "Food", "Travel", "Fitness")
    // --- State Management ---
    fun onUsernameChange(it: String) {
        _username.value = it
        _usernameError.value = null
    }
    fun onDisplayNameChange(it: String) { _displayName.value = it }
    fun onEmailChange(it: String) { _email.value = it }
    fun onPasswordChange(it: String) { _password.value = it }
    fun onOtpChange(it: String) { _otpCode.value = it }

    fun selectAvatar(resId: Int) {
        _selectedAvatarRes.value = resId
        _selectedBitmap.value = null
    }

    fun setCustomBitmap(bitmap: Bitmap) {
        _selectedBitmap.value = bitmap
        _selectedAvatarRes.value = null
    }

    // --- Business Logic ---
    fun validateUsername(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isChecking.value = true
            delay(500)
            if (_username.value.length < 3) {
                _usernameError.value = "Username too short"
            } else {
                _usernameError.value = null
                onSuccess()
            }
            _isChecking.value = false
        }
    }

    fun signUpWithEmail() {
        viewModelScope.launch {
            _isChecking.value = true
            _authError.value = null
            try {
                supabase.auth.signUpWith(Email) {
                    email = _email.value
                    password = _password.value
                }
                _isOtpSent.value = true
            } catch (e: Exception) {
                _authError.value = e.localizedMessage ?: "Sign up failed"
            } finally {
                _isChecking.value = false
            }
        }
    }

    fun verifyOtp(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isChecking.value = true
            _authError.value = null
            try {
                supabase.auth.verifyEmailOtp(
                    type = OtpType.Email.SIGNUP,
                    email = _email.value,
                    token = _otpCode.value
                )
                onSuccess()
            } catch (e: Exception) {
                _authError.value = "Invalid verification code"
            } finally {
                _isChecking.value = false
            }
        }
    }
    fun toggleInterest(category: String) {
        val current = _interests.value
        if (current.contains(category)) {
            _interests.value = current - category
        } else if (current.size < 6) {
            _interests.value = current + category
        }
    }

    suspend fun checkSession(): io.github.jan.supabase.auth.user.UserInfo? {
        return try {
            // This forces Supabase to talk to the server
            val user = supabase.auth.retrieveUserForCurrentSession(updateSession = true)
            user
        } catch (e: Exception) {
            // If the user was deleted from the DB, this will fail
            // Clear the local session just in case
            supabase.auth.signOut()
            null
        }
    }

    fun logout() {
        viewModelScope.launch {
            supabase.auth.signOut()
        }
    }
}