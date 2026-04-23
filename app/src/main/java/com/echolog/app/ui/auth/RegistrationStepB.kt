package com.echolog.app.ui.auth

import android.graphics.Bitmap
import android.os.Build
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
import com.echolog.app.R
import com.echolog.app.viewmodel.RegistrationViewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.layout.size // For the progress indicator

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

    val galleryLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        uri?.let {
            val bitmap: Bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = android.graphics.ImageDecoder.createSource(context.contentResolver, it)
                android.graphics.ImageDecoder.decodeBitmap(source)
            } else {
                @Suppress("DEPRECATION")
                android.provider.MediaStore.Images.Media.getBitmap(context.contentResolver, it)
            }

            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 512, 512, true)
            viewModel.setCustomBitmap(scaledBitmap)
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
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("EchoLog", fontSize = 32.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
        Text("STEP 2: APPEARANCE", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.align(Alignment.Start).padding(bottom = 24.dp))

        Card(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9))
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(60.dp).clip(CircleShape).background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedBitmap != null) {
                        Image(bitmap = selectedBitmap!!.asImageBitmap(), contentDescription = null, modifier = Modifier.fillMaxSize())
                    } else if (selectedRes != null) {
                        Image(painter = painterResource(id = selectedRes!!), contentDescription = null, modifier = Modifier.fillMaxSize())
                    } else {
                        Icon(painter = painterResource(id = android.R.drawable.ic_menu_gallery), contentDescription = null, tint = Color.White)
                    }
                }

                Column(modifier = Modifier.padding(start = 12.dp)) {
                    Text(displayName.ifEmpty { "Your Name" }, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(if (username.isEmpty()) "@username" else "@$username", color = Color.Gray, fontSize = 14.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("Select an Avatar", fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))

        // FIXED: Added horizontal scroll for 14 avatars
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            defaultAvatars.forEach { resId ->
                Image(
                    painter = painterResource(id = resId),
                    contentDescription = null,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .border(
                            width = 3.dp,
                            color = if (selectedRes == resId) Color.Black else Color.Transparent,
                            shape = CircleShape
                        )
                        .clickable { viewModel.selectAvatar(resId) }
                )
            }
        }

        Text("OR", color = Color.Gray, fontSize = 12.sp)

        OutlinedButton(
            onClick = { galleryLauncher.launch("image/*") },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Upload from Device", color = Color.Black)
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
        ) {
            Text("Next: Security")
        }

        TextButton(onClick = onBack) {
            Text("Back", color = Color.Gray)
        }
    }
}