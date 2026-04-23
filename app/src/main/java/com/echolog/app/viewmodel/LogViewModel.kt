package com.echolog.app.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.echolog.app.data.LogEntity
import com.echolog.app.data.LogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LogViewModel @Inject constructor(
    private val repository: LogRepository
) : ViewModel() {

    val recentLogs: StateFlow<List<LogEntity>> = repository.allLogs.stateIn(
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
        viewModelScope.launch {
            val newLog = LogEntity(
                title = title,
                caption = caption,
                category = category,
                logType = type,
                localMediaPaths = mediaPaths, // Repository will handle the copying
                scheduledAt = scheduledAt,
                colorHex = colorHex
            )
            repository.saveAndSyncLog(newLog, context)
        }
    }
}