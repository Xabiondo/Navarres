package com.example.navarres.view

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.navarres.viewmodel.ProfileViewModel // Using the English ViewModel
import java.io.File

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onLogoutClick: () -> Unit
) {
    // Collecting State from ViewModel
    val email by viewModel.userEmail.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    // Camera Logic
    val context = LocalContext.current
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success && tempPhotoUri != null) {
                viewModel.onPhotoTaken(tempPhotoUri!!)
            }
        }
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // --- PROFILE PICTURE SECTION ---
        Box(contentAlignment = Alignment.Center) {
            if (uiState.photoUrl.isNotEmpty()) {
                AsyncImage(
                    model = uiState.photoUrl,
                    contentDescription = "Profile Picture",
                    modifier = Modifier.size(120.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                // Default Icon
                Icon(
                    imageVector = Icons.Default.Restaurant, // Or Icons.Default.Person
                    contentDescription = null,
                    modifier = Modifier.size(100.dp),
                    tint = Color(0xFF2E7D32)
                )
            }

            // Loading Spinner
            if (uiState.isLoading) {
                CircularProgressIndicator(color = Color(0xFFB30000))
            }
        }

        Spacer(Modifier.height(24.dp))

        // --- TEXT SECTION (Translated) ---
        Text(
            text = "Bon Appétit!",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = Color(0xFFB30000)
        )
        Text(
            text = "Logged in as: $email",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray
        )

        Spacer(Modifier.height(32.dp))

        // --- BUTTON 1: CHANGE PHOTO ---
        Button(
            onClick = {
                val uri = ComposeFileProvider.getImageUri(context)
                tempPhotoUri = uri
                cameraLauncher.launch(uri)
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
            modifier = Modifier.fillMaxWidth(0.7f).height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.CameraAlt, null)
            Spacer(Modifier.width(8.dp))
            Text("CHANGE PHOTO")
        }

        Spacer(Modifier.height(16.dp))

        // --- BUTTON 2: LOGOUT ---
        Button(
            onClick = {
                viewModel.logout()
                onLogoutClick()
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Color(0xFFB30000)),
            modifier = Modifier.fillMaxWidth(0.7f).height(50.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color(0xFFB30000))
        ) {
            Icon(Icons.Default.ExitToApp, null)
            Spacer(Modifier.width(8.dp))
            Text("LOG OUT", fontWeight = FontWeight.Bold)
        }
    }
}

// --- HELPER OBJECT FOR FILES ---
object ComposeFileProvider {
    fun getImageUri(context: Context): Uri {
        val directory = File(context.cacheDir, "images")
        directory.mkdirs()
        val file = File.createTempFile("selected_image_", ".jpg", directory)
        val authority = context.packageName + ".provider"
        return FileProvider.getUriForFile(context, authority, file)
    }
}