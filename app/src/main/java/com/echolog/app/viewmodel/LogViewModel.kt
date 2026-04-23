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

@HiltViewModel
class LogViewModel @Inject constructor(
    private val repository: LogRepository,
    private val supabase: SupabaseClient
) : ViewModel() {

    private val currentUserId: String?
        get() = supabase.auth.currentUserOrNull()?.id

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

        viewModelScope.launch {
            val newLog = LogEntity(
                userId = userId,
                title = title,
                caption = caption,
                category = category,
                logType = type,
                localMediaPaths = mediaPaths,
                scheduledAt = scheduledAt,
                colorHex = colorHex,
                isSynced = false
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