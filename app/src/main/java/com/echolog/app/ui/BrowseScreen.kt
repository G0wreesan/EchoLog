package com.echolog.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.echolog.app.data.LogEntity
import com.echolog.app.ui.components.HomeLogCard
import com.echolog.app.viewmodel.LogViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowseScreen(viewModel: LogViewModel) {
    var searchQuery by remember { mutableStateOf("") }
    val logs by viewModel.recentLogs.collectAsState()
    val scope = rememberCoroutineScope()

    // Modal State
    var selectedLog by remember { mutableStateOf<LogEntity?>(null) }
    val sheetState = rememberModalBottomSheetState()
    var showSheet by remember { mutableStateOf(false) }

    val filteredLogs = logs.filter {
        it.title.contains(searchQuery, ignoreCase = true) ||
                it.category.contains(searchQuery, ignoreCase = true)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by title or category...") },
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Black,
                cursorColor = Color.Black
            )
        )

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(filteredLogs) { log ->
                // This now works because we added 'onClick' to HomeLogCard!
                HomeLogCard(log = log, onClick = {
                    selectedLog = log
                    showSheet = true
                })
            }
        }
    }

    if (showSheet && selectedLog != null) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState,
            containerColor = Color.White
        ) {
            Column(modifier = Modifier.padding(24.dp).fillMaxWidth()) {
                Text(selectedLog!!.title, style = MaterialTheme.typography.headlineSmall)
                Text("Category: ${selectedLog!!.category}", color = Color.Gray)
                Spacer(modifier = Modifier.height(16.dp))
                Text(selectedLog!!.caption ?: "No description available.")

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { /* Navigate to Edit Screen */ },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                ) { Text("Edit Entry") }

                TextButton(
                    onClick = {
                        // Add a delete function in your ViewModel to handle this
                        // viewModel.deleteLog(selectedLog!!.id)
                        showSheet = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                ) {
                    Icon(Icons.Default.Delete, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Delete Permanently")
                }
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}