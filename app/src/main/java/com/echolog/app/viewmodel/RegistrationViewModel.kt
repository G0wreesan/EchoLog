package com.echolog.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.echolog.app.data.Profile
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.filter.PostgrestFilterBuilder
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

class RegistrationViewModel @Inject constructor(
    private val postgrest: Postgrest
) : ViewModel() {

    private val _username = MutableStateFlow("")
    val username = _username.asStateFlow()

    private val _isUsernameAvailable = MutableStateFlow<Boolean?>(null)
    val isUsernameAvailable = _isUsernameAvailable.asStateFlow()

    init {
        setupUsernameCheck()
    }

    fun onUsernameChange(input: String) {
        _username.value = input
    }

    @OptIn(FlowPreview::class)
    private fun setupUsernameCheck() {
        username
            .debounce(500)
            // This is the Flow filter (checks string length)
            .filter { it.length >= 4 }
            .onEach { input ->
                viewModelScope.launch {
                    try {
                        // We use the full path for the Supabase filter to avoid conflicts
                        val result = postgrest["profiles"]
                            .select {
                                filter {
                                    eq("username", input)
                                }
                            }
                            .decodeSingleOrNull<Profile>()

                        _isUsernameAvailable.value = (result == null)
                    } catch (e: Exception) {
                        _isUsernameAvailable.value = null
                    }
                }
            }
            .launchIn(viewModelScope)
    }
}