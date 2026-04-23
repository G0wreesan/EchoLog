package com.echolog.app.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.echolog.app.data.LogEntity
import com.echolog.app.data.LogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import io.github.jan.supabase.postgrest.postgrest


@HiltViewModel
class LogViewModel @Inject constructor(
    private val repository: LogRepository,
    private val supabase: SupabaseClient
) : ViewModel() {

    private val currentUserId: String?
        get() = supabase.auth.currentUserOrNull()?.id

    private val _userCategories = MutableStateFlow(listOf("Study", "Work", "Workout", "Personal", "Travel", "General"))
    val userCategories = _userCategories.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing = _isSyncing.asStateFlow()
    fun addNewCategory(name: String) {
        if (!_userCategories.value.contains(name)) {
            _userCategories.value = _userCategories.value + name
            // Optional: Save to Supabase 'user_preferences' table here
        }
    }
    @OptIn(ExperimentalCoroutinesApi::class)
    val recentLogs: StateFlow<List<LogEntity>> = supabase.auth.sessionStatus
        .flatMapLatest { status ->
            if (status is SessionStatus.Authenticated) {
                // Fetch logs using the ID from the active session
                repository.getLogsForUser(status.session.user?.id ?: "")
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun syncLocalLogsToSupabase() {
        viewModelScope.launch {
            _isSyncing.value = true
            try {
                val unsyncedLogs = repository.getUnsyncedLogs()
                if (unsyncedLogs.isNotEmpty()) {
                    // Correct syntax for the latest SDK
                    supabase.postgrest.from("logs").upsert(unsyncedLogs)

                    unsyncedLogs.forEach { log ->
                        repository.updateSyncStatus(log.id, true)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("LogViewModel", "Sync Error: ${e.localizedMessage}")
            } finally {
                _isSyncing.value = false
            }
        }
    }

    fun saveNewLog(
        title: String,
        caption: String,
        category: String,
        type: String,
        mediaPaths: List<String>,
        colorHex: String = "#000000",
        scheduledAt: Long? = null, // UI still gives us a Long
        context: Context
    ) {
        val userId = currentUserId ?: return

        // Convert Long? to ISO String?
        val isoScheduledAt = scheduledAt?.let {
            java.time.Instant.ofEpochMilli(it).toString()
        }

        viewModelScope.launch {
            val newLog = LogEntity(
                userId = userId,
                title = title,
                caption = caption,
                category = category,
                logType = type,
                localMediaPaths = mediaPaths,
                scheduledAt = isoScheduledAt, // Matches new String? type
                createdAt = java.time.Instant.now().toString(), // Matches new String type
                isSynced = false,
                colorHex = colorHex
            )
            repository.saveAndSyncLog(newLog, context)
        }
    }

    fun syncPending() {
        viewModelScope.launch {
            repository.syncPendingLogs()
        }
    }
}