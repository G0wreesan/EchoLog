package com.echolog.app.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.echolog.app.viewmodel.RegistrationViewModel

// CRITICAL IMPORTS: These fix the "getValue" and "Cannot infer type" errors
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState

@Composable
fun RegistrationStepA(
    viewModel: RegistrationViewModel,
    onNext: () -> Unit,
    onBack: () -> Unit,
    onContinueAsGuest: () -> Unit
) {
    val username by viewModel.username.collectAsState()
    val displayName by viewModel.displayName.collectAsState()
    val isChecking by viewModel.isChecking.collectAsState()
    val usernameError by viewModel.usernameError.collectAsState() // Linked to new VM variable

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp)
    ) {
        Text("EchoLog", fontSize = 32.sp, fontWeight = FontWeight.Bold)
        Text("STEP 1: IDENTITY", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 40.dp))

        Text("Display Name", fontSize = 14.sp, fontWeight = FontWeight.Medium)
        OutlinedTextField(
            value = displayName,
            onValueChange = { viewModel.onDisplayNameChange(it) },
            placeholder = { Text("How people see you") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text("Username", fontSize = 14.sp, fontWeight = FontWeight.Medium)
        OutlinedTextField(
            value = username,
            onValueChange = { viewModel.onUsernameChange(it) },
            placeholder = { Text("@unique_handle") },
            modifier = Modifier.fillMaxWidth(),
            isError = usernameError != null, // Updated name
            supportingText = { usernameError?.let { Text(it, color = Color.Red) } }, // Updated name
            singleLine = true
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { viewModel.validateUsername(onSuccess = onNext) },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            enabled = !isChecking && username.isNotEmpty() && displayName.isNotEmpty(),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
        ) {
            if (isChecking) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("Next Step")
            }
        }

        OutlinedButton(
            onClick = onContinueAsGuest,
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp).height(50.dp)
        ) {
            Text("Continue as Guest", color = Color.Black)
        }

        TextButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        ) {
            Text("Already have an account? Login", color = Color.Gray)
        }
    }
}