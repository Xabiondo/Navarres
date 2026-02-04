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
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.navarres.viewmodel.ProfileViewModel
import com.example.navarres.model.data.Restaurant
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex

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
    var showEditNameDialog by remember { mutableStateOf(false) } // Recuperado del MERGE
    var showPhotoSourceDialog by remember { mutableStateOf(false) }
    var showClaimDialog by remember { mutableStateOf(false) } // De IVAN

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

                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = NavarresRed)
                }

                // BOTÓN DE CAMBIO DE FOTO (Estilo mejorado de IVAN)
                SmallFloatingActionButton(
                    onClick = {
                        if (!uiState.isLoading) showPhotoSourceDialog = true
                    },
                    containerColor = NavarresGreen,
                    contentColor = Color.White,
                    modifier = Modifier.align(Alignment.BottomEnd).offset(x = (-4).dp, y = (-4).dp),
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.CameraAlt, "Editar foto", modifier = Modifier.size(18.dp))
                }
            }
        }

        // DATOS BÁSICOS (NOMBRE Y EMAIL)
        Column(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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

            // BOTÓN PARA RECLAMAR DUEÑO (De IVAN)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showClaimDialog = true }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Verified, null, tint = NavarresGreen, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("¿Eres dueño de un negocio?", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    Text("Haz clic aquí para reclamar tu local", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant)

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
    if (showEditBioDialog) {
        EditDialog("Editar biografía", userProfile.bio, { showEditBioDialog = false }, { viewModel.updateBio(it); showEditBioDialog = false })
    }

    if (showEditCityDialog) {
        EditDialog("Editar ciudad", userProfile.city, { showEditCityDialog = false }, { viewModel.updateCity(it); showEditCityDialog = false })
    }

    // DIÁLOGO EDITAR NOMBRE (Recuperado del MERGE)
    if (showEditNameDialog) {
        EditDialog("Editar nombre de usuario", userProfile.displayName, { showEditNameDialog = false }, { viewModel.updateDisplayName(it); showEditNameDialog = false })
    }

    // NUEVO: Diálogo de Reclamación (De IVAN)
    if (showClaimDialog) {
        ClaimRestaurantDialog(viewModel = viewModel, onDismiss = { showClaimDialog = false })
    }

    // NUEVO: Diálogo de Foto mejorado (De IVAN)
    if (showPhotoSourceDialog) {
        AlertDialog(
            onDismissRequest = { showPhotoSourceDialog = false },
            title = { Text("Cambiar foto de perfil") },
            text = {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showPhotoSourceDialog = false
                                launchCamera()
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.PhotoCamera, null, tint = NavarresRed)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Hacer una foto")
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showPhotoSourceDialog = false
                                galleryLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.PhotoLibrary, null, tint = NavarresRed)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Elegir de la galería")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showPhotoSourceDialog = false }) {
                    Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    }
}

// --- COMPONENTES AUXILIARES ---

@Composable
fun ClaimRestaurantDialog(viewModel: ProfileViewModel, onDismiss: () -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    var selectedId by rememberSaveable { mutableStateOf("") }
    var selectedName by rememberSaveable { mutableStateOf("") }
    var selectedLocation by rememberSaveable { mutableStateOf("") }

    var cargo by rememberSaveable { mutableStateOf("") }
    var telefono by rememberSaveable { mutableStateOf("") }
    var cif by rememberSaveable { mutableStateOf("") }
    var aceptaTerminos by rememberSaveable { mutableStateOf(false) }

    val sugerencias by viewModel.busquedaRestaurantes.collectAsState()
    val context = LocalContext.current
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Cabecera elegante
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(listOf(NavarresRed, Color(0xFFB71C1C)))
                        )
                        .padding(vertical = 24.dp, horizontal = 20.dp)
                ) {
                    Column {
                        Text(
                            "Verificación de Negocio",
                            color = Color.White,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Sigue los pasos para tomar el control de tu local",
                            color = Color.White.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // PASO 1
                    StepHeader(number = "1", title = "Selecciona tu establecimiento")

                    if (selectedId.isEmpty()) {
                        OutlinedTextField(
                            value = query,
                            onValueChange = {
                                query = it
                                viewModel.buscarRestaurantes(it)
                            },
                            placeholder = { Text("Escribe el nombre...") },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Default.Storefront, null, tint = NavarresRed) },
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        if (query.length >= 2) {
                            Surface(
                                modifier = Modifier.heightIn(max = 200.dp),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Color.LightGray.copy(0.5f)),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            ) {
                                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                                    items(sugerencias) { rest ->
                                        ListItem(
                                            headlineContent = { Text(rest.nombre, fontWeight = FontWeight.Medium) },
                                            supportingContent = { Text(rest.localidad, style = MaterialTheme.typography.bodySmall) },
                                            leadingContent = { Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(18.dp)) },
                                            modifier = Modifier.clickable {
                                                selectedId = rest.id
                                                selectedName = rest.nombre
                                                selectedLocation = rest.localidad
                                                focusManager.clearFocus()
                                            },
                                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                                        )
                                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color.LightGray.copy(0.2f))
                                    }
                                }
                            }
                        }
                    } else {
                        // Tarjeta de restaurante seleccionado
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                            border = BorderStroke(1.dp, Color(0xFF2E7D32)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CheckCircle, "Ok", tint = Color(0xFF2E7D32))
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(selectedName, fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20))
                                    Text(selectedLocation, style = MaterialTheme.typography.bodySmall, color = Color(0xFF1B5E20))
                                }
                                IconButton(onClick = { selectedId = ""; selectedName = "" }) {
                                    Icon(Icons.Default.Edit, "Cambiar", tint = Color(0xFF2E7D32))
                                }
                            }
                        }
                    }

                    // PASO 2
                    StepHeader(number = "2", title = "Información de contacto")

                    OutlinedTextField(
                        value = cargo,
                        onValueChange = { cargo = it },
                        label = { Text("Cargo (Ej: Gerente, Dueño)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = cif,
                            onValueChange = { cif = it.uppercase() },
                            label = { Text("CIF / NIF") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = telefono,
                            onValueChange = { if (it.length <= 15) telefono = it },
                            label = { Text("Teléfono") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true
                        )
                    }

                    Surface(
                        color = NavarresRed.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = aceptaTerminos,
                                onCheckedChange = { aceptaTerminos = it },
                                colors = CheckboxDefaults.colors(checkedColor = NavarresRed)
                            )
                            Text(
                                "Certifico que tengo autoridad legal sobre este negocio y acepto los términos.",
                                style = MaterialTheme.typography.labelSmall,
                                lineHeight = 14.sp
                            )
                        }
                    }
                }

                // Botones inferiores
                Row(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("CANCELAR", color = Color.Gray)
                    }
                    Button(
                        onClick = {
                            viewModel.enviarSolicitudDossierEmail(
                                restId = selectedId,
                                restNombre = selectedName,
                                datosFormulario = mapOf("cargo" to cargo, "cif" to cif, "telefono" to telefono)
                            ) { success ->
                                if (success) {
                                    Toast.makeText(context, "✅ Solicitud enviada", Toast.LENGTH_LONG).show()
                                    onDismiss()
                                } else {
                                    Toast.makeText(context, "❌ Error al enviar", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        enabled = selectedId.isNotEmpty() && cif.length > 5 && cargo.isNotBlank() && aceptaTerminos,
                        modifier = Modifier.weight(1.5f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NavarresRed)
                    ) {
                        Text("ENVIAR SOLICITUD", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun StepHeader(number: String, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            color = NavarresRed,
            shape = CircleShape,
            modifier = Modifier.size(24.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(number, color = Color.White, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.width(12.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}

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