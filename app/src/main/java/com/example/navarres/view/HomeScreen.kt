package com.example.navarres.view

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.navarres.viewmodel.HomeViewModel
import com.example.navarres.viewmodel.ProfileViewModel
import java.io.File

// Definición básica de rutas
sealed class NavItem(val route: String, val title: String, val icon: ImageVector) {
    object Restaurantes : NavItem("restaurantes", "Restaurantes", Icons.Default.Restaurant)
    object Favoritos : NavItem("favoritos", "Favoritos", Icons.Default.Favorite)
    object Perfil : NavItem("perfil", "Perfil", Icons.Default.Person)
    object Ajustes : NavItem("config", "Ajustes", Icons.Default.Settings)
}

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    profileViewModel: ProfileViewModel,
    onLogoutSuccess: () -> Unit
) {
    val selectedTab by viewModel.selectedTab.collectAsState()
    val isLoggedOut by viewModel.isLoggedOut.collectAsState()

    // --- LÓGICA DE CÁMARA ---
    val context = LocalContext.current
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }

    // Este es el "escuchador" que sube la foto cuando vuelves de la cámara
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success && tempPhotoUri != null) {
                Log.d("NAV_CAMERA", "Foto tomada con éxito, subiendo...")
                profileViewModel.onPhotoTaken(tempPhotoUri!!)
            }
        }
    )
    // ------------------------

    LaunchedEffect(isLoggedOut) {
        if (isLoggedOut) { onLogoutSuccess() }
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                listOf(NavItem.Restaurantes, NavItem.Favoritos, NavItem.Perfil, NavItem.Ajustes).forEach { item ->
                    NavigationBarItem(
                        selected = selectedTab == item.route,
                        onClick = { viewModel.selectTab(item.route) },
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title) }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            // Muestro SOLO el botón, esté en la pestaña que esté (para probar rápido)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {

                Button(
                    onClick = {
                        // 1. Crear archivo temporal
                        val uri = ComposeFileProvider.getImageUri(context)
                        tempPhotoUri = uri
                        // 2. Abrir cámara
                        cameraLauncher.launch(uri)
                    },
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text("ABRIR CÁMARA Y SUBIR FOTO")
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(onClick = { viewModel.logout() }) {
                    Text("Cerrar Sesión")
                }
            }
        }
    }
}

// Lógica de Archivos (Necesaria para que la cámara no falle)
object ComposeFileProvider {
    fun getImageUri(context: Context): Uri {
        val directory = File(context.cacheDir, "images")
        directory.mkdirs()
        val file = File.createTempFile("selected_image_", ".jpg", directory)
        val authority = context.packageName + ".provider"
        return FileProvider.getUriForFile(context, authority, file)
    }
}