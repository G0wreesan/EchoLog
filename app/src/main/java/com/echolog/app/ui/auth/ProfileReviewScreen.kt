package com.echolog.app.ui.auth

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.echolog.app.viewmodel.RegistrationViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.shape.RoundedCornerShape

@Composable
fun ProfileReviewScreen(
    viewModel: RegistrationViewModel,
    onFinish: () -> Unit
) {
    val username by viewModel.username.collectAsState()
    val email by viewModel.email.collectAsState()
    val displayName by viewModel.displayName.collectAsState()
    val selectedBitmap by viewModel.selectedBitmap.collectAsState()
    val selectedRes by viewModel.selectedAvatarRes.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Verify Profile", fontSize = 28.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(24.dp))

        // Profile Picture Section
        Box(modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF5F5F5))
                    .border(1.dp, Color.LightGray, CircleShape)
            ) {
                if (selectedBitmap != null) {
                    Image(
                        bitmap = selectedBitmap!!.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                } else if (selectedRes != null) {
                    Image(
                        painter = painterResource(id = selectedRes!!),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Locked Fields
        Text("Username", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
        OutlinedTextField(
            value = "@$username",
            onValueChange = {},
            enabled = false,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = Color.Black,
                disabledBorderColor = Color(0xFFE0E0E0),
                disabledContainerColor = Color(0xFFFAFAFA)
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("Email", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
        OutlinedTextField(
            value = email,
            onValueChange = {},
            enabled = false,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = Color.Black,
                disabledBorderColor = Color(0xFFE0E0E0),
                disabledContainerColor = Color(0xFFFAFAFA)
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Editable Display Name
        Text("Display Name", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        OutlinedTextField(
            value = displayName,
            onValueChange = { viewModel.onDisplayNameChange(it) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        // The space where interests were is now a flexible weight to push the button down
        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onFinish,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            // Button is now enabled by default as interests are gone
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Complete Account Setup", fontWeight = FontWeight.Bold)
        }
    }
}