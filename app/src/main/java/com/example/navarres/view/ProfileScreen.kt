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

// Colores Corporativos (Estos se mantienen igual porque son la marca)
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

    var showEditBioDialog by remember { mutableStateOf(false) }
    var showEditCityDialog by remember { mutableStateOf(false) }

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
            // CAMBIO 1: Usar color de fondo del tema (Blanco en light, Gris oscuro en dark)
            .background(MaterialTheme.colorScheme.background)
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
                    color = Color.White.copy(alpha = 0.9f),
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
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            // El borde se adapta al fondo (blanco o negro)
                            .border(4.dp, MaterialTheme.colorScheme.background, CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        shape = CircleShape,
                        // CAMBIO 2: Surface color adapta el fondo del icono
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(4.dp, MaterialTheme.colorScheme.background),
                        shadowElevation = 4.dp
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            // CAMBIO 3: Tint adapta el color del icono
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(20.dp)
                        )
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

            Text(
                text = displayName,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                // CAMBIO 4: Texto principal adapta color
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = if (userProfile.isEmailPublic) userProfile.email else "Email privado",
                style = MaterialTheme.typography.bodyMedium,
                // CAMBIO 5: Texto secundario usa Variant
                color = MaterialTheme.colorScheme.onSurfaceVariant
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

            // CAMBIO 6: Separador dinámico
            Box(modifier = Modifier.width(1.dp).height(40.dp).background(MaterialTheme.colorScheme.outlineVariant))

            val favCount = userProfile.favorites.size.toString()
            ProfileStatItem(number = favCount, label = "Favoritos", icon = Icons.Outlined.FavoriteBorder)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 4. BIOGRAFÍA
        ProfileSectionCard(title = "Sobre mí", onEditClick = { showEditBioDialog = true }) {
            Text(
                text = userProfile.bio.ifEmpty { "Escribe algo sobre tus gustos gastronómicos..." },
                style = MaterialTheme.typography.bodyLarge,
                // CAMBIO 7: Color de texto condicional dinámico
                color = if (userProfile.bio.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
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
                    Icon(Icons.Default.Email, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Email público", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(userProfile.email, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Switch(
                        checked = userProfile.isEmailPublic,
                        onCheckedChange = { isPublic -> viewModel.updateEmailPrivacy(isPublic) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = NavarresRed,
                            uncheckedTrackColor = MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier.graphicsLayer(scaleX = 0.8f, scaleY = 0.8f)
                    )
                    Text(
                        text = if (userProfile.isEmailPublic) "Visible" else "Oculto",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (userProfile.isEmailPublic) NavarresRed else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp
                    )
                }
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant)

            // CIUDAD
            Row(
                modifier = Modifier.fillMaxWidth().clickable { showEditCityDialog = true },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Ubicación", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (userProfile.city.isNotEmpty()) {
                            Text(userProfile.city, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                        } else {
                            Text("Añadir ciudad", style = MaterialTheme.typography.bodyMedium, color = NavarresRed, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
                Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
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
            // CAMBIO 8: Colores del botón adaptados
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = NavarresRed
            ),
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
        // CAMBIO 9: Iconos y texto adaptados
        Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(number, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onBackground)
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun ProfileSectionCard(title: String, showEdit: Boolean = true, onEditClick: () -> Unit = {}, content: @Composable ColumnScope.() -> Unit) {
    // CAMBIO 10: La tarjeta ahora usa el color "Surface" (Gris oscuro en dark mode, blanco en light)
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp) // Un poco de elevación para que destaque en dark mode
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
    // CAMBIO 11: Diálogo adaptado
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
                // Solo forzamos el color corporativo en el borde activo y cursor
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