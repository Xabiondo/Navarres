package com.example.navarres.view

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
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

private val NavarresRed = Color(0xFFB30000)
private val NavarresDarkRed = Color(0xFF800000)
private val NavarresGreen = Color(0xFF2E7D32)

object ComposeFileProvider {
    fun getImageUri(context: Context): Uri? {
        return try {
            val directory = File(context.cacheDir, "images")
            directory.mkdirs()
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val file = File.createTempFile("JPEG_${timestamp}_", ".jpg", directory)
            // IMPORTANTE: Revisa que esto coincida con tu AndroidManifest.xml -> provider -> authorities
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

    // Estados para diálogos
    var showEditBioDialog by remember { mutableStateOf(false) }
    var showEditCityDialog by remember { mutableStateOf(false) }
    var showEditNameDialog by remember { mutableStateOf(false) } // <--- NUEVO
    var showPhotoSourceDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success -> if (success && tempPhotoUri != null) viewModel.onPhotoTaken(tempPhotoUri!!) }
    )

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? -> if (uri != null) viewModel.onPhotoTaken(uri) }

    fun launchCamera() {
        try {
            val uri = ComposeFileProvider.getImageUri(context)
            if (uri != null) {
                tempPhotoUri = uri
                cameraLauncher.launch(uri)
            } else { Toast.makeText(context, "Error almacenamiento", Toast.LENGTH_SHORT).show() }
        } catch (e: Exception) { Toast.makeText(context, "Error cámara", Toast.LENGTH_SHORT).show() }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).verticalScroll(scrollState)
    ) {
        // HEADER
        Box(modifier = Modifier.fillMaxWidth().height(260.dp)) {
            Box(modifier = Modifier.fillMaxWidth().height(180.dp).background(brush = Brush.verticalGradient(colors = listOf(NavarresRed, NavarresDarkRed)))) {
                Text("Perfil Gourmet", color = Color.White.copy(alpha = 0.9f), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(16.dp).align(Alignment.TopCenter))
            }
            Box(modifier = Modifier.align(Alignment.BottomCenter).size(140.dp)) {
                val currentPhoto = if(uiState.photoUrl.isNotEmpty()) uiState.photoUrl else userProfile.photoUrl
                if (currentPhoto.isNotEmpty()) {
                    AsyncImage(model = currentPhoto, contentDescription = "Foto", modifier = Modifier.fillMaxSize().clip(CircleShape).border(4.dp, MaterialTheme.colorScheme.background, CircleShape), contentScale = ContentScale.Crop)
                } else {
                    Surface(modifier = Modifier.fillMaxSize(), shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant, border = BorderStroke(4.dp, MaterialTheme.colorScheme.background)) {
                        Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(20.dp))
                    }
                }
                if (uiState.isLoading) CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = NavarresRed)
                SmallFloatingActionButton(onClick = { if (!uiState.isLoading) showPhotoSourceDialog = true }, containerColor = NavarresGreen, contentColor = Color.White, modifier = Modifier.align(Alignment.BottomEnd).offset(x = (-4).dp, y = (-4).dp), shape = CircleShape) {
                    Icon(Icons.Default.CameraAlt, "Editar foto", modifier = Modifier.size(18.dp))
                }
            }
        }

        // DATOS BÁSICOS (NOMBRE Y EMAIL)
        Column(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // LÓGICA PARA MOSTRAR NOMBRE O EMAIL SI ESTÁ VACÍO
            val displayName = if (userProfile.displayName.isNotBlank()) {
                userProfile.displayName
            } else if (userProfile.email.contains("@")) {
                userProfile.email.substringBefore("@").replaceFirstChar { it.uppercase() }
            } else {
                "Usuario"
            }

            // Nombre con botón de editar
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                IconButton(onClick = { showEditNameDialog = true }, modifier = Modifier.size(24.dp).padding(start = 4.dp)) {
                    Icon(Icons.Default.Edit, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                }
            }

            Text(
                text = if (userProfile.isEmailPublic) userProfile.email else "Email privado",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // ESTADÍSTICAS
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 40.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
            ProfileStatItem(number = "12", label = "Reseñas", icon = Icons.Outlined.RestaurantMenu)
            Box(modifier = Modifier.width(1.dp).height(40.dp).background(MaterialTheme.colorScheme.outlineVariant))
            ProfileStatItem(number = userProfile.favorites.size.toString(), label = "Favoritos", icon = Icons.Outlined.FavoriteBorder)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // BIOGRAFÍA
        ProfileSectionCard(title = "Sobre mí", onEditClick = { showEditBioDialog = true }) {
            Text(
                text = userProfile.bio.ifEmpty { "Escribe algo sobre tus gustos gastronómicos..." },
                style = MaterialTheme.typography.bodyLarge,
                color = if (userProfile.bio.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                lineHeight = 24.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // AJUSTES
        ProfileSectionCard(title = "Ajustes de Cuenta", showEdit = false) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Email, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Email público", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(userProfile.email, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Switch(checked = userProfile.isEmailPublic, onCheckedChange = { viewModel.updateEmailPrivacy(it) }, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = NavarresRed, uncheckedTrackColor = MaterialTheme.colorScheme.outline), modifier = Modifier.graphicsLayer(scaleX = 0.8f, scaleY = 0.8f))
                    Text(if (userProfile.isEmailPublic) "Visible" else "Oculto", style = MaterialTheme.typography.labelSmall, color = if (userProfile.isEmailPublic) NavarresRed else MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                }
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant)

            // CIUDAD
            Row(modifier = Modifier.fillMaxWidth().clickable { showEditCityDialog = true }, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Ubicación", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (userProfile.city.isNotEmpty()) Text(userProfile.city, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface) else Text("Añadir ciudad", style = MaterialTheme.typography.bodyMedium, color = NavarresRed, fontWeight = FontWeight.SemiBold)
                    }
                }
                Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = { viewModel.logout(); onLogoutClick() }, modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface, contentColor = NavarresRed), border = BorderStroke(1.dp, NavarresRed.copy(alpha = 0.5f)), shape = RoundedCornerShape(8.dp)) {
            Icon(Icons.Default.ExitToApp, null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Cerrar Sesión")
        }
        Spacer(modifier = Modifier.height(32.dp))
    }

    // --- DIÁLOGOS ---
    if (showEditBioDialog) EditDialog("Editar biografía", userProfile.bio, { showEditBioDialog = false }, { viewModel.updateBio(it); showEditBioDialog = false })
    if (showEditCityDialog) EditDialog("Editar ciudad", userProfile.city, { showEditCityDialog = false }, { viewModel.updateCity(it); showEditCityDialog = false })

    // DIÁLOGO EDITAR NOMBRE (NUEVO)
    if (showEditNameDialog) {
        EditDialog("Editar nombre de usuario", userProfile.displayName, { showEditNameDialog = false }, { viewModel.updateDisplayName(it); showEditNameDialog = false })
    }

    if (showPhotoSourceDialog) {
        AlertDialog(onDismissRequest = { showPhotoSourceDialog = false }, title = { Text("Cambiar foto") }, text = { Column {
            Row(modifier = Modifier.fillMaxWidth().clickable { showPhotoSourceDialog = false; launchCamera() }.padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.PhotoCamera, null, tint = NavarresRed); Spacer(modifier = Modifier.width(12.dp)); Text("Hacer una foto") }
            Row(modifier = Modifier.fillMaxWidth().clickable { showPhotoSourceDialog = false; galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }.padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.PhotoLibrary, null, tint = NavarresRed); Spacer(modifier = Modifier.width(12.dp)); Text("Elegir de la galería") }
        }}, confirmButton = {}, dismissButton = { TextButton(onClick = { showPhotoSourceDialog = false }) { Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant) } }, containerColor = MaterialTheme.colorScheme.surfaceContainer)
    }
}

// ... ProfileStatItem, ProfileSectionCard, EditDialog (se mantienen igual que antes) ...
@Composable
fun ProfileStatItem(number: String, label: String, icon: ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(number, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onBackground)
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun ProfileSectionCard(title: String, showEdit: Boolean = true, onEditClick: () -> Unit = {}, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                if (showEdit) IconButton(onClick = onEditClick, modifier = Modifier.size(24.dp)) { Icon(Icons.Default.Edit, "Editar", tint = MaterialTheme.colorScheme.onSurfaceVariant) }
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
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.SemiBold) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Escribe aquí...") },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NavarresRed,
                    cursorColor = NavarresRed,
                    focusedLabelColor = NavarresRed,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
        },
        confirmButton = { Button(onClick = { onConfirm(text) }, colors = ButtonDefaults.buttonColors(containerColor = NavarresRed)) { Text("Guardar", color = Color.White) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant) } }
    )
}