package com.echolog.app.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
// IMPORT YOUR MODELS CORRECTLY
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
                    fetchUserProfile() // This pulls from Supabase 'profiles' table
                }
            }
        }
    }
    // --- State Management ---
    private val _username = MutableStateFlow("")
    val username = _username.asStateFlow()

    private val _displayName = MutableStateFlow("")
    val displayName = _displayName.asStateFlow()

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

    private val _interests = MutableStateFlow<Set<String>>(emptySet())
    val interests = _interests.asStateFlow()

    private val _isChecking = MutableStateFlow(false)
    val isChecking = _isChecking.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError = _authError.asStateFlow()

    val availableCategories = listOf("Tech", "Design", "Gaming", "Music", "Art", "Food", "Travel", "Fitness")

    private val _usernameError = MutableStateFlow<String?>(null)
    val usernameError = _usernameError.asStateFlow()

    private val _isLoggingIn = MutableStateFlow(false)
    val isLoggingIn = _isLoggingIn.asStateFlow()

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile = _userProfile.asStateFlow()

    // --- Actions ---
    fun onUsernameChange(it: String) { _username.value = it }
    fun onDisplayNameChange(it: String) { _displayName.value = it }
    fun onEmailChange(it: String) { _email.value = it }
    fun onPasswordChange(it: String) { _password.value = it }
    fun onOtpChange(it: String) { _otpCode.value = it }
    fun selectAvatar(resId: Int) { _selectedAvatarRes.value = resId; _selectedBitmap.value = null }
    fun setCustomBitmap(bitmap: Bitmap) { _selectedBitmap.value = bitmap; _selectedAvatarRes.value = null }

    fun toggleInterest(category: String) {
        val current = _interests.value
        if (current.contains(category)) _interests.value = current - category
        else if (current.size < 6) _interests.value = current + category
    }

    // --- Logic ---

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

    fun validateUsername(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isChecking.value = true
            _usernameError.value = null // Clear old errors
            delay(800) // Simulating a network check

            if (_username.value.length < 6) {
                _usernameError.value = "Username must be at least 6 characters"
            } else {
                // If you want to check if it exists in Supabase:
                // val exists = supabase.postgrest.from("profiles").select { filter { eq("username", _username.value) } }.data.isNotEmpty()
                onSuccess()
            }
            _isChecking.value = false
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
                val user = supabase.auth.currentUserOrNull() ?: throw Exception("Auth session missing")
                var finalAvatarUrl: String? = null

                // 1. Handle Custom Bitmap (Gallery Upload)
                if (_selectedBitmap.value != null) {
                    val stream = ByteArrayOutputStream()
                    _selectedBitmap.value!!.compress(Bitmap.CompressFormat.JPEG, 80, stream)
                    val fileName = "${user.id}_avatar.jpg"
                    val bucket = supabase.storage.from("avatars")
                    bucket.upload(fileName, stream.toByteArray()) { upsert = true }
                    finalAvatarUrl = bucket.publicUrl(fileName)
                }
                // 2. Handle Default Avatars (PNGs)
                else if (_selectedAvatarRes.value != null) {
                    // We store a special identifier or the resource name to know it's a local resource
                    finalAvatarUrl = "local_res_${_selectedAvatarRes.value}"
                }

                // 3. Save to 'profiles' table
                val profile = UserProfile(
                    id = user.id,
                    username = _username.value,
                    display_name = _displayName.value,
                    email = _email.value,
                    date_of_birth = "2000-01-01", // Placeholder or get from State
                    avatar_url = finalAvatarUrl
                )

                supabase.postgrest.from("profiles").upsert(profile)
                _userProfile.value = profile // Update local state
                onComplete()
            } catch (e: Exception) {
                _authError.value = "Finalization failed: ${e.localizedMessage}"
            } finally {
                _isChecking.value = false
            }
        }
    }

    fun signIn(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isChecking.value = true
            _authError.value = null // Clear previous errors
            try {
                supabase.auth.signInWith(Email) {
                    email = _email.value
                    password = _password.value
                }
                // If we get here, it worked!
                onSuccess()
            } catch (e: Exception) {
                _authError.value = "Invalid email or password."
            } finally {
                _isChecking.value = false
            }
        }
    }


    suspend fun checkSession(): io.github.jan.supabase.auth.user.UserInfo? {
        return try {
            supabase.auth.retrieveUserForCurrentSession(updateSession = true)
        } catch (e: Exception) {
            supabase.auth.signOut()
            null
        }
    }

    fun fetchUserProfile() {
        viewModelScope.launch {
            try {
                val user = supabase.auth.currentUserOrNull()
                if (user != null) {
                    val profile = supabase.postgrest.from("profiles")
                        .select { filter { eq("id", user.id) } }
                        .decodeSingle<UserProfile>()
                    _userProfile.value = profile
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            supabase.auth.signOut()
        }
    }
}