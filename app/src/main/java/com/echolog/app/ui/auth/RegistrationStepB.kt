package com.echolog.app.ui.auth

import android.graphics.Bitmap
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.echolog.app.R
import com.echolog.app.viewmodel.RegistrationViewModel

@Composable
fun RegistrationStepB(
    viewModel: RegistrationViewModel,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    val username by viewModel.username.collectAsState()
    val displayName by viewModel.displayName.collectAsState()
    val selectedRes by viewModel.selectedAvatarRes.collectAsState()
    val selectedBitmap by viewModel.selectedBitmap.collectAsState()

    val context = androidx.compose.ui.platform.LocalContext.current

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val bitmap: Bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = android.graphics.ImageDecoder.createSource(context.contentResolver, it)
                android.graphics.ImageDecoder.decodeBitmap(source)
            } else {
                @Suppress("DEPRECATION")
                android.provider.MediaStore.Images.Media.getBitmap(context.contentResolver, it)
            }

            val scaled = Bitmap.createScaledBitmap(bitmap, 512, 512, true)
            viewModel.setCustomBitmap(scaled)
        }
    }

    val defaultAvatars = listOf(
        R.drawable.avatar_1, R.drawable.canva_avatar_2, R.drawable.canva_avatar_3,
        R.drawable.canva_avatar_4, R.drawable.canva_avatar_5, R.drawable.canva_avatar_6,
        R.drawable.canva_avatar_7, R.drawable.canva_avatar_8, R.drawable.canva_avatar_9,
        R.drawable.canva_avatar_10, R.drawable.canva_avatar_11, R.drawable.canva_avatar_12,
        R.drawable.canva_avatar_13, R.drawable.canva_avatar_14
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(50.dp))

        Text(
            "EchoLog",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            "STEP 2 • Choose your identity",
            fontSize = 12.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(30.dp))

        // ===== PROFILE PREVIEW CARD =====
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F7F8)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        selectedBitmap != null ->
                            Image(
                                bitmap = selectedBitmap!!.asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize()
                            )

                        selectedRes != null ->
                            Image(
                                painter = painterResource(id = selectedRes!!),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize()
                            )

                        else ->
                            Icon(
                                painter = painterResource(id = android.R.drawable.ic_menu_gallery),
                                contentDescription = null,
                                tint = Color.White
                            )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = displayName.ifEmpty { "Your Name" },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = if (username.isEmpty()) "@username" else "@$username",
                        color = Color.Gray,
                        fontSize = 13.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(25.dp))

        Text(
            "Pick an Avatar",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // ===== MODERN GRID AVATARS =====
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier.height(260.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(defaultAvatars.size) { index ->
                val resId = defaultAvatars[index]

                Image(
                    painter = painterResource(id = resId),
                    contentDescription = null,
                    modifier = Modifier
                        .size(70.dp)
                        .clip(CircleShape)
                        .border(
                            width = 2.dp,
                            color = if (selectedRes == resId) Color.Black else Color.Transparent,
                            shape = CircleShape
                        )
                        .clickable { viewModel.selectAvatar(resId) }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            "OR",
            color = Color.Gray,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = { galleryLauncher.launch("image/*") },
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Upload from Device")
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onNext,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text("Continue")
        }

        TextButton(onClick = onBack) {
            Text("Back", color = Color.Gray)
        }

        Spacer(modifier = Modifier.height(10.dp))
    }
}