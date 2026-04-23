package com.echolog.app.ui.auth

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
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

// 1. ADD THIS OPT-IN TO REMOVE THE WARNING
@OptIn(ExperimentalLayoutApi::class)
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
    val selectedInterests by viewModel.interests.collectAsState()

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
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray)
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

        Spacer(modifier = Modifier.height(24.dp))

        // Locked Fields (ReadOnly Visuals)
        Text("Username", fontSize = 12.sp, color = Color.Gray)
        OutlinedTextField(
            value = "@$username",
            onValueChange = {},
            enabled = false,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = Color.Black,
                disabledBorderColor = Color.LightGray,
                disabledLabelColor = Color.Gray
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text("Email", fontSize = 12.sp, color = Color.Gray)
        OutlinedTextField(
            value = email,
            onValueChange = {},
            enabled = false,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = Color.Black,
                disabledBorderColor = Color.LightGray
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Editable Display Name
        Text("Display Name", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        OutlinedTextField(
            value = displayName,
            onValueChange = { viewModel.onDisplayNameChange(it) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Interests", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "(${selectedInterests.size}/6)",
                fontSize = 12.sp,
                color = if(selectedInterests.size in 2..6) Color.DarkGray else Color.Red
            )
        }

        // 2. FLOWROW HANDLING
        // Using horizontalArrangement and verticalArrangement for spacing
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            viewModel.availableCategories.forEach { category ->
                val isSelected = selectedInterests.contains(category)
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.toggleInterest(category) },
                    label = { Text(category) },
                    leadingIcon = if (isSelected) {
                        {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(FilterChipDefaults.IconSize)
                            )
                        }
                    } else null
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onFinish,
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            enabled = selectedInterests.size in 2..6,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
        ) {
            Text("Complete Account Setup")
        }
    }
}