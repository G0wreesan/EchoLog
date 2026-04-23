package com.echolog.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.echolog.app.data.LogEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LogViewModel @Inject constructor(
    // private val repository: LogRepository // You will create this next
) : ViewModel() {

    private val _recentLogs = MutableStateFlow<List<LogEntity>>(emptyList())
    val recentLogs = _recentLogs.asStateFlow()

    fun saveNewLog(
        title: String,
        caption: String,
        category: String,
        type: String,
        mediaPaths: List<String>,
        color: String = "#000000"
    ) {
        viewModelScope.launch {
            val newLog = LogEntity(
                title = title,
                caption = caption,
                category = category,
                logType = type,
                localMediaPaths = mediaPaths,
                remoteMediaUrls = emptyList(),
                scheduledAt = null,
                colorHex = color
            )

            // Temporarily update the UI state so you can see the card immediately
            _recentLogs.value = _recentLogs.value + newLog

            // repository.insertLog(newLog) // This will be used once Room is setup
        }

    }
}