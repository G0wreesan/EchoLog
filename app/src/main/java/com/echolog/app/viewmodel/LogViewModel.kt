package com.echolog.app.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.echolog.app.data.LogEntity
import com.echolog.app.data.LogRepository
import com.echolog.app.util.FileStorageHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class LogViewModel @Inject constructor(
    private val repository: LogRepository,
    private val supabase: SupabaseClient
) : ViewModel() {

    private val _userCategories = MutableStateFlow(listOf("General", "Work", "Personal"))
    val userCategories: StateFlow<List<String>> = _userCategories.asStateFlow()

    private val _selectedCalendarDate = MutableStateFlow(LocalDate.now())
    val selectedCalendarDate: StateFlow<LocalDate> = _selectedCalendarDate.asStateFlow()

    private val userIdFlow: Flow<String> = supabase.auth.sessionStatus.map { status ->
        when (status) {
            is SessionStatus.Authenticated -> status.session.user?.id ?: "anonymous"
            else -> "anonymous"
        }
    }.distinctUntilChanged()

    val recentLogs: StateFlow<List<LogEntity>> = userIdFlow.flatMapLatest { userId ->
        repository.getLogsForUser(userId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val calendarLogs: StateFlow<List<LogEntity>> = combine(
        recentLogs,
        _selectedCalendarDate
    ) { logs, date ->
        logs.filter { log ->
            try {
                val logDate = java.time.Instant.parse(log.createdAt)
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate()
                logDate == date
            } catch (e: Exception) {
                false
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList<LogEntity>())

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _saveFinished = MutableSharedFlow<Boolean>()
    val saveFinished = _saveFinished.asSharedFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    fun updateSelectedDate(date: LocalDate) {
        _selectedCalendarDate.value = date
    }

    fun syncLocalLogsToSupabase(context: Context) {
        viewModelScope.launch {
            _isSyncing.value = true
            try {
                repository.syncPendingLogs()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isSyncing.value = false
            }
        }
    }

    fun addNewCategory(category: String) {
        if (category.isNotBlank() && !_userCategories.value.contains(category)) {
            _userCategories.value = _userCategories.value + category
        }
    }

    fun saveAndSyncLog(log: LogEntity, context: Context) {
        viewModelScope.launch {
            repository.saveAndSyncLog(log, context)
        }
    }

    /**
     * Processes and stores a log entry on a background thread.
     */
    fun saveNewLogCategorized(
        title: String,
        caption: String,
        category: String,
        imagePaths: List<String>,
        audioPaths: List<String>,
        videoPaths: List<String>,
        scheduledAt: Long?,
        context: Context
    ) {
        viewModelScope.launch {
            _isSaving.value = true
            try {
                // Move IO operations to a safe context
                val result = kotlinx.coroutines.withContext(Dispatchers.IO) {
                    val persistentImages = imagePaths.map { path ->
                        if (path.contains("/files/media/IMG_")) path
                        else FileStorageHelper.saveFileToInternalStorage(context, path, "IMG", "jpg")
                    }

                    val persistentAudios = audioPaths.map { path ->
                        if (path.contains("/files/media/VOICE_")) path
                        else FileStorageHelper.saveFileToInternalStorage(context, path, "VOICE", "m4a")
                    }

                    val persistentVideos = videoPaths.map { path ->
                        if (path.contains("/files/media/VID_")) path
                        else FileStorageHelper.saveFileToInternalStorage(context, path, "VID", "mp4")
                    }

                    val userId = supabase.auth.currentUserOrNull()?.id ?: "anonymous"
                    val newLog = LogEntity(
                        userId = userId,
                        title = title,
                        caption = caption,
                        category = category,
                        logType = "MANUAL",
                        localImagePaths = persistentImages,
                        localAudioPaths = persistentAudios,
                        localVideoPaths = persistentVideos,
                        scheduledAt = scheduledAt?.toString(),
                        createdAt = java.time.Instant.now().toString()
                    )

                    repository.saveAndSyncLog(newLog, context)
                    true
                }
                if (result) {
                    _saveFinished.emit(true)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isSaving.value = false
            }
        }
    }
}
