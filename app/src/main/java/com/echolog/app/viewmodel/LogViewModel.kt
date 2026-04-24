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
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class LogViewModel @Inject constructor(
    private val repository: LogRepository,
    private val supabase: SupabaseClient
) : ViewModel() {

    private val currentUserId: String?
        get() = supabase.auth.currentUserOrNull()?.id

    private val _userCategories = MutableStateFlow(listOf("Study", "Work", "Workout", "Personal", "Travel", "General"))
    val userCategories = _userCategories.asStateFlow()

    private val _selectedCalendarDate = MutableStateFlow(LocalDate.now())
    val selectedCalendarDate = _selectedCalendarDate.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing = _isSyncing.asStateFlow()

    fun addNewCategory(name: String) {
        if (!_userCategories.value.contains(name)) {
            _userCategories.value = _userCategories.value + name
        }
    }

    // Explicitly typed flatMapLatest to avoid inference errors
    @OptIn(ExperimentalCoroutinesApi::class)
    val recentLogs: StateFlow<List<LogEntity>> = supabase.auth.sessionStatus
        .flatMapLatest { status: SessionStatus ->
            if (status is SessionStatus.Authenticated) {
                val userId = status.session.user?.id ?: ""
                repository.getLogsForUser(userId)
            } else {
                flowOf(emptyList<LogEntity>()) // Explicitly provide type
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun updateSelectedDate(date: LocalDate) {
        _selectedCalendarDate.value = date
    }

    // Explicitly typed flatMapLatest for Calendar logs
    @OptIn(ExperimentalCoroutinesApi::class)
    val calendarLogs: StateFlow<List<LogEntity>> = _selectedCalendarDate
        .flatMapLatest { date: LocalDate ->
            val userId = currentUserId
            if (userId != null) {
                repository.getLogsByDate(userId, date.toString())
            } else {
                flowOf(emptyList<LogEntity>()) // Explicitly provide type
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun saveNewLog(
        title: String,
        caption: String,
        category: String,
        type: String,
        mediaPaths: List<String>,
        colorHex: String = "#000000",
        scheduledAt: Long? = null,
        context: Context
    ) {
        val userId = currentUserId ?: return

        val isoScheduledAt = scheduledAt?.let {
            Instant.ofEpochMilli(it).toString()
        }

        viewModelScope.launch {
            val newLog = LogEntity(
                userId = userId,
                title = title,
                caption = caption,
                category = category,
                logType = type,
                localMediaPaths = mediaPaths,
                scheduledAt = isoScheduledAt,
                createdAt = Instant.now().toString(),
                isSynced = false,
                colorHex = colorHex
            )
            repository.saveAndSyncLog(newLog, context)
        }
    }

    fun syncLocalLogsToSupabase() {
        viewModelScope.launch {
            _isSyncing.value = true
            try {
                val unsyncedLogs = repository.getUnsyncedLogs()
                if (unsyncedLogs.isNotEmpty()) {
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

    fun syncPending() {
        viewModelScope.launch {
            repository.syncPendingLogs()
        }
    }
}