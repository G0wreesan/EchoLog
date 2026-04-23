package com.echolog.app.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.echolog.app.viewmodel.RegistrationViewModel

@Composable
fun RegistrationStepA(
    viewModel: RegistrationViewModel,
    onNext: () -> Unit,
    onBack: () -> Unit,
    onContinueAsGuest: () -> Unit
) {
    // Explicitly using 'by' delegate requires androidx.compose.runtime.getValue import
    val username by viewModel.username.collectAsState()
    val displayName by viewModel.displayName.collectAsState()
    val isChecking by viewModel.isChecking.collectAsState()
    val usernameError by viewModel.usernameError.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(50.dp))

        // ===== HEADER =====
        Text(
            text = "EchoLog",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "Step 1 • Create your identity",
            fontSize = 12.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(30.dp))

        // ===== FORM CARD =====
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {

            Column(modifier = Modifier.padding(20.dp)) {

                Text(
                    text = "Tell us about you",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // ===== DISPLAY NAME =====
                Text(
                    "Display Name",
                    fontSize = 12.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = displayName,
                    onValueChange = { viewModel.onDisplayNameChange(it) },
                    placeholder = { Text("What is your display name?") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // ===== USERNAME =====
                Text(
                    "Username",
                    fontSize = 12.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = username,
                    onValueChange = { viewModel.onUsernameChange(it) },
                    placeholder = { Text("@unique_handle") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = usernameError != null,
                    supportingText = {
                        usernameError?.let {
                            Text(it, color = MaterialTheme.colorScheme.error)
                        }
                    },
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // ===== NEXT BUTTON =====
                Button(
                    onClick = { viewModel.validateUsername(onSuccess = onNext) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    enabled = !isChecking && username.isNotEmpty() && displayName.isNotEmpty(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    if (isChecking) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(22.dp)
                        )
                    } else {
                        Text("Continue")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ===== GUEST BUTTON =====
        OutlinedButton(
            onClick = onContinueAsGuest,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Continue as Guest")
        }

        Spacer(modifier = Modifier.height(10.dp))

        // ===== LOGIN LINK =====
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Already have an account?",
                fontSize = 13.sp,
                color = Color.Gray
            )

            TextButton(
                onClick = onBack,
                // PaddingValues(0.dp) requires specific import
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
            ) {
                Text(
                    "Login",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}