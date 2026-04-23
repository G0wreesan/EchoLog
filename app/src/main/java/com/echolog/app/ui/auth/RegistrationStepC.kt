package com.echolog.app.ui.auth

import androidx.compose.animation.*
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
import androidx.compose.foundation.layout.size // For the progress indicator
@Composable
fun RegistrationStepC(
    viewModel: RegistrationViewModel,
    onComplete: () -> Unit,
    onBack: () -> Unit
) {
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()
    val otpCode by viewModel.otpCode.collectAsState()
    val isOtpSent by viewModel.isOtpSent.collectAsState()
    val isChecking by viewModel.isChecking.collectAsState()
    val error by viewModel.authError.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("EchoLog", fontSize = 32.sp, fontWeight = FontWeight.Bold)
        Text("STEP 3: SECURITY", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 40.dp))

        AnimatedContent(targetState = isOtpSent, label = "auth_state") { sent ->
            if (!sent) {
                // EMAIL & PASSWORD ENTRY
                Column {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { viewModel.onEmailChange(it) },
                        label = { Text("Email Address") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { viewModel.onPasswordChange(it) },
                        label = { Text("Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
                // OTP ENTRY
                Column {
                    Text("We sent a code to $email", fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = otpCode,
                        onValueChange = { viewModel.onOtpChange(it) },
                        label = { Text("6-Digit Verification Code") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
        }

        if (error != null) {
            Text(error!!, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                if (!isOtpSent) {
                    viewModel.signUpWithEmail()
                } else {
                    // This 'onComplete' is what sends the user to PROFILE_REVIEW
                    viewModel.verifyOtp(onSuccess = onComplete)
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            enabled = !isChecking,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
        ) {
            if (isChecking) CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp
            )
            else Text(if (!isOtpSent) "Send Verification Code" else "Complete Registration")
        }

        TextButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text("Go Back", color = Color.Gray)
        }
    }
}