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
import androidx.compose.foundation.shape.RoundedCornerShape
import com.echolog.app.viewmodel.RegistrationViewModel
import androidx.compose.runtime.getValue

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onContinueAsGuest: () -> Unit,
    viewModel: RegistrationViewModel
) {
    var identifier by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val isChecking by viewModel.isChecking.collectAsState()
    val authError by viewModel.authError.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(80.dp))

        // App Title
        Text(
            text = "EchoLog",
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Sign in to continue",
            color = Color.Gray,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Card Container (modern touch)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F7F7)),
            elevation = CardDefaults.cardElevation(6.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {

                OutlinedTextField(
                    value = identifier,
                    onValueChange = { identifier = it },
                    label = { Text("Email or Username") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    textStyle = LocalTextStyle.current.copy(color = Color.Black),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    textStyle = LocalTextStyle.current.copy(color = Color.Black),
                    modifier = Modifier.fillMaxWidth()
                )

                TextButton(
                    onClick = { },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Forgot Password?")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onLoginSuccess,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Login")
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Divider with OR
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Divider(modifier = Modifier.weight(1f))
            Text("  OR  ", color = Color.Gray)
            Divider(modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Guest Button
        OutlinedButton(
            onClick = onContinueAsGuest,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Continue as Guest")
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Don’t have an account?",
                fontSize = 13.sp
            )

            TextButton(
                onClick = onNavigateToRegister,
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    "Register",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}