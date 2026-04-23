package com.echolog.app.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.echolog.app.viewmodel.RegistrationViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState

@Composable
fun LoginScreen(
    viewModel: RegistrationViewModel, // Add the ViewModel here
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onContinueAsGuest: () -> Unit
) {
    // Collect states from ViewModel
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()
    val isChecking by viewModel.isChecking.collectAsState()
    val authError by viewModel.authError.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        Text("EchoLog", fontSize = 40.sp, fontWeight = FontWeight.Bold)
        Text("Your personal vault.", color = Color.Gray) // Updated vibe

        Spacer(modifier = Modifier.height(48.dp))

        // Email Field
        OutlinedTextField(
            value = email,
            onValueChange = { viewModel.onEmailChange(it) },
            label = { Text("Email Address") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = authError != null
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Password Field
        OutlinedTextField(
            value = password,
            onValueChange = { viewModel.onPasswordChange(it) },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = authError != null
        )

        // Error Message Display
        if (authError != null) {
            Text(
                text = authError!!,
                color = Color.Red,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 8.dp).align(Alignment.Start)
            )
        }

        TextButton(
            onClick = { /* Handle Forgot Password later */ },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Forgot Password?", color = Color.Gray)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Login Button
        Button(
            onClick = { viewModel.signIn(onSuccess = onLoginSuccess) },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            enabled = !isChecking && email.isNotEmpty() && password.isNotEmpty(),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
        ) {
            if (isChecking) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("Login")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Guest Access
        OutlinedButton(
            onClick = onContinueAsGuest,
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("Continue as Guest", color = Color.Black)
        }

        Spacer(modifier = Modifier.weight(1f))

        Row {
            Text("New user? ")
            TextButton(onClick = onNavigateToRegister, contentPadding = PaddingValues(0.dp)) {
                Text("Register here", fontWeight = FontWeight.Bold, color = Color.Black)
            }
        }
    }
}