package com.example.navarres.view

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.RestaurantMenu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.navarres.viewmodel.ProfileViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Colores Corporativos
private val NavarresRed = Color(0xFFB30000)
private val NavarresDarkRed = Color(0xFF800000)
private val NavarresGreen = Color(0xFF2E7D32)

// --- OBJETO FILE PROVIDER (LIMPIO PARA PRODUCCIÓN) ---
object ComposeFileProvider {
    fun getImageUri(context: Context): Uri? {
        return try {
            val directory = File(context.cacheDir, "images")
            directory.mkdirs()

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val file = File.createTempFile("JPEG_${timestamp}_", ".jpg", directory)

            val authority = "com.example.navarrest2.fileprovider"

            FileProvider.getUriForFile(context, authority, file)
        } catch (e: Exception) {
            null
        }
    }
}

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onLogoutClick: () -> Unit
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    // Estados UI
    var showEditBioDialog by remember { mutableStateOf(false) }
    var showEditCityDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }

    // LAUNCHER DE CÁMARA
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success && tempPhotoUri != null) {
                viewModel.onPhotoTaken(tempPhotoUri!!)
            }
        }
    )

    // FUNCIÓN SEGURA PARA LANZAR CÁMARA
    fun launchCamera() {
        try {
            val uri = ComposeFileProvider.getImageUri(context)
            if (uri != null) {
                tempPhotoUri = uri
                cameraLauncher.launch(uri)
            } else {
                Toast.makeText(context, "No se pudo acceder al almacenamiento", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error al iniciar la cámara", Toast.LENGTH_SHORT).show()
        }
    }

    // --- PANTALLA PRINCIPAL ---
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9F9F9))
            .verticalScroll(scrollState)
    ) {

        // 1. HEADER
        Box(
            modifier = Modifier.fillMaxWidth().height(260.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(brush = Brush.verticalGradient(colors = listOf(NavarresRed, NavarresDarkRed)))
            ) {
                Text(
                    text = "Perfil Gourmet",
                    color = Color.White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp).align(Alignment.TopCenter)
                )
            }

            Box(
                modifier = Modifier.align(Alignment.BottomCenter).size(140.dp)
            ) {
                val currentPhoto = if(uiState.photoUrl.isNotEmpty()) uiState.photoUrl else userProfile.photoUrl

                if (currentPhoto.isNotEmpty()) {
                    AsyncImage(
                        model = currentPhoto,
                        contentDescription = "Foto de perfil",
                        modifier = Modifier.fillMaxSize().clip(CircleShape).border(4.dp, Color.White, CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        shape = CircleShape,
                        color = Color.White,
                        border = BorderStroke(4.dp, Color.White),
                        shadowElevation = 4.dp
                    ) {
                        Icon(Icons.Default.Person, null, tint = Color.Gray, modifier = Modifier.padding(20.dp))
                    }
                }

                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = NavarresRed)
                }

                SmallFloatingActionButton(
                    onClick = { if (!uiState.isLoading) launchCamera() },
                    containerColor = NavarresGreen,
                    contentColor = Color.White,
                    modifier = Modifier.align(Alignment.BottomEnd).offset(x = (-4).dp, y = (-4).dp),
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.CameraAlt, "Editar", modifier = Modifier.size(18.dp))
                }
            }
        }

        // 2. DATOS BÁSICOS
        Column(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val displayName = if (userProfile.email.contains("@")) {
                userProfile.email.substringBefore("@").replaceFirstChar { it.uppercase() }
            } else {
                userProfile.email.ifEmpty { "Usuario" }
            }

            Text(text = displayName, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), color = Color.Black)
            Text(
                text = if (userProfile.isEmailPublic) userProfile.email else "Email privado",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // 3. ESTADÍSTICAS
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 40.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProfileStatItem(number = "12", label = "Reseñas", icon = Icons.Outlined.RestaurantMenu)
            Box(modifier = Modifier.width(1.dp).height(40.dp).background(Color.LightGray))
            val favCount = userProfile.favorites.size.toString()
            ProfileStatItem(number = favCount, label = "Favoritos", icon = Icons.Outlined.FavoriteBorder)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 4. BIOGRAFÍA
        ProfileSectionCard(title = "Sobre mí", onEditClick = { showEditBioDialog = true }) {
            Text(
                text = userProfile.bio.ifEmpty { "Escribe algo sobre tus gustos gastronómicos..." },
                style = MaterialTheme.typography.bodyLarge,
                color = if (userProfile.bio.isEmpty()) Color.Gray else Color(0xFF444444),
                lineHeight = 24.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 5. AJUSTES
        ProfileSectionCard(title = "Ajustes de Cuenta", showEdit = false) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Email, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Email público", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        Text(userProfile.email, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Switch(
                        checked = userProfile.isEmailPublic,
                        onCheckedChange = { isPublic -> viewModel.updateEmailPrivacy(isPublic) },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = NavarresRed),
                        modifier = Modifier.graphicsLayer(scaleX = 0.8f, scaleY = 0.8f)
                    )
                    Text(text = if (userProfile.isEmailPublic) "Visible" else "Oculto", style = MaterialTheme.typography.labelSmall, color = if (userProfile.isEmailPublic) NavarresRed else Color.Gray, fontSize = 10.sp)
                }
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.LightGray.copy(alpha = 0.5f))

            // CIUDAD
            Row(
                modifier = Modifier.fillMaxWidth().clickable { showEditCityDialog = true },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Ubicación", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        if (userProfile.city.isNotEmpty()) {
                            Text(userProfile.city, style = MaterialTheme.typography.bodyMedium, color = Color.Black)
                        } else {
                            Text("Añadir ciudad", style = MaterialTheme.typography.bodyMedium, color = NavarresRed, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
                Icon(Icons.Default.ChevronRight, null, tint = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // BOTÓN CERRAR SESIÓN
        Button(
            onClick = {
                viewModel.logout()
                onLogoutClick()
            },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = NavarresRed),
            border = BorderStroke(1.dp, NavarresRed.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(Icons.Default.ExitToApp, null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Cerrar Sesión")
        }
        Spacer(modifier = Modifier.height(32.dp))
    }

    // DIÁLOGOS
    if (showEditBioDialog) {
        EditDialog("Editar biografía", userProfile.bio, { showEditBioDialog = false }, { viewModel.updateBio(it); showEditBioDialog = false })
    }
    if (showEditCityDialog) {
        EditDialog("Editar ciudad", userProfile.city, { showEditCityDialog = false }, { viewModel.updateCity(it); showEditCityDialog = false })
    }
}

// --- COMPONENTES AUXILIARES ---
@Composable
fun ProfileStatItem(number: String, label: String, icon: ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = Color.Gray, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(number, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = Color.Black)
        Text(label, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
    }
}

@Composable
fun ProfileSectionCard(title: String, showEdit: Boolean = true, onEditClick: () -> Unit = {}, content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(0.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                if (showEdit) IconButton(onClick = onEditClick, modifier = Modifier.size(24.dp)) { Icon(Icons.Default.Edit, "Editar", tint = Color.Gray) }
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun EditDialog(title: String, initialValue: String, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var text by remember { mutableStateOf(initialValue) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.SemiBold) },
        text = { OutlinedTextField(value = text, onValueChange = { text = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Escribe aquí...") }, keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NavarresRed, cursorColor = NavarresRed)) },
        confirmButton = { Button(onClick = { onConfirm(text) }, colors = ButtonDefaults.buttonColors(containerColor = NavarresRed)) { Text("Guardar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar", color = Color.Gray) } }
    )
}