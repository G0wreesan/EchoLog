package com.echolog.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.echolog.app.data.LogEntity

@Composable
fun HomeLogCard(
    log: LogEntity,
    onClick: () -> Unit = {} // 1. Add this parameter with a default empty lambda
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .clickable { onClick() }, // 2. Attach the click listener here
        colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = log.title, style = MaterialTheme.typography.titleMedium)
            if (!log.caption.isNullOrBlank()) {
                Text(
                    text = log.caption,
                    style = MaterialTheme.typography.bodySmall,
                    color = androidx.compose.ui.graphics.Color.Gray,
                    maxLines = 2
                )
            }
        }
    }
}