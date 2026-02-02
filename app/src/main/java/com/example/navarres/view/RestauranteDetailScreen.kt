package com.example.navarres.view

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.navarres.model.data.Restaurant
import com.example.navarres.viewmodel.ConfigViewModel
import com.example.navarres.viewmodel.RestauranteDetailViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@Composable
fun RestauranteDetailScreen(
    viewModel: RestauranteDetailViewModel,
    configViewModel: ConfigViewModel,
    onBack: () -> Unit
) {
    val restaurant by viewModel.selectedRestaurant.collectAsState()
    val isOwner by viewModel.isOwner.collectAsState() // Detectamos si es dueño
    val context = LocalContext.current

    var isHoursExpanded by remember { mutableStateOf(false) }
    var showCarta by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) } // Estado para el diálogo de edición

    val diasOrdenados = listOf("lunes", "martes", "miercoles", "jueves", "viernes", "sabado", "domingo")

    restaurant?.let { res ->
        val posRestaurante = LatLng(res.latitud, res.longitud)
        val cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(posRestaurante, 17f)
        }

        LaunchedEffect(res) {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(posRestaurante, 17f)
        }

        // --- DIÁLOGO DE EDICIÓN (Sólo para el dueño) ---
        if (showEditDialog) {
            EditRestaurantDialog(
                restaurant = res,
                onDismiss = { showEditDialog = false },
                onSave = { updatedRes ->
                    viewModel.updateRestaurantData(updatedRes) { success ->
                        if (success) {
                            showEditDialog = false
                            Toast.makeText(context, "¡Actualizado con éxito!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Error al guardar", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            )
        }

        // --- VISOR DE CARTA ---
        if (showCarta && !res.rutaCarta.isNullOrBlank()) {
            Dialog(onDismissRequest = { showCarta = false }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
                Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
                    AsyncImage(
                        model = res.rutaCarta,
                        contentDescription = "Carta",
                        modifier = Modifier.fillMaxSize().clickable { showCarta = false },
                        contentScale = ContentScale.Fit
                    )
                    FilledIconButton(onClick = { showCarta = false }, modifier = Modifier.align(Alignment.TopEnd).padding(20.dp), colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color.White.copy(alpha = 0.3f))) {
                        Icon(Icons.Default.Close, null, tint = Color.White)
                    }
                }
            }
        }

        Scaffold(
            floatingActionButton = {
                // EL BOTÓN SOLO APARECE SI LA APP DICE QUE ERES DUEÑO
                if (isOwner) {
                    FloatingActionButton(
                        onClick = { showEditDialog = true },
                        containerColor = Color(0xFFB30000),
                        contentColor = Color.White
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar")
                    }
                }
            }
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).background(MaterialTheme.colorScheme.background)) {

                    // --- EL CHIVATO DE DEBUG (Para ver por qué no funciona) ---
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (isOwner) Color(0xFF2E7D32) else Color(0xFFC62828))
                            .padding(8.dp)
                    ) {
                        Text("DEBUG: ID Detectado = '${res.id}'", color = Color.White, style = MaterialTheme.typography.labelSmall)
                        Text("DEBUG: ¿Eres Dueño? = $isOwner", color = Color.White, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }

                    // CABECERA
                    Box(modifier = Modifier.fillMaxWidth().height(300.dp)) {
                        AsyncImage(model = res.foto, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)), startY = 300f)))
                        Column(modifier = Modifier.align(Alignment.BottomStart).padding(20.dp)) {
                            Surface(color = Color(0xFFB30000), shape = RoundedCornerShape(8.dp)) {
                                Text(res.categoria.uppercase(), Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Text(res.nombre, style = MaterialTheme.typography.headlineLarge, color = Color.White, fontWeight = FontWeight.ExtraBold)
                        }
                    }

                    Column(modifier = Modifier.padding(20.dp)) {
                        // BOTONES
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            QuickActionButton(Icons.Default.Place, "Ruta") {
                                val uri = Uri.parse("google.navigation:q=${res.latitud},${res.longitud}")
                                context.startActivity(Intent(Intent.ACTION_VIEW, uri).apply { setPackage("com.google.android.apps.maps") })
                            }
                            QuickActionButton(Icons.Default.Phone, "Llamar") {
                                context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${res.telefono}")))
                            }
                            QuickActionButton(Icons.Default.MenuBook, "Carta") {
                                if (!res.rutaCarta.isNullOrBlank()) showCarta = true
                                else Toast.makeText(context, "No hay carta disponible", Toast.LENGTH_SHORT).show()
                            }
                        }

                        HorizontalDivider(Modifier.padding(vertical = 24.dp), thickness = 0.5.dp)

                        // INFO
                        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)), shape = RoundedCornerShape(20.dp)) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                InfoRow(Icons.Default.LocationOn, "Dirección", res.direccion)
                                Spacer(Modifier.height(16.dp))
                                val distStr = viewModel.calculateDistanceStr(res.latitud, res.longitud)
                                InfoRow(Icons.Default.Schedule, "Distancia", distStr)
                                Spacer(Modifier.height(16.dp))
                                InfoRow(Icons.Default.EuroSymbol, "Rango de precio", res.precio)
                            }
                        }

                        Spacer(Modifier.height(24.dp))

                        // HORARIOS
                        val rotationState by animateFloatAsState(targetValue = if (isHoursExpanded) 180f else 0f)
                        Card(modifier = Modifier.fillMaxWidth().clickable { isHoursExpanded = !isHoursExpanded }, shape = RoundedCornerShape(20.dp)) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.AccessTime, null, tint = Color(0xFFB30000))
                                        Spacer(Modifier.width(12.dp)); Text("Ver Horarios", fontWeight = FontWeight.Bold)
                                    }
                                    Icon(Icons.Default.ExpandMore, null, Modifier.rotate(rotationState))
                                }
                                AnimatedVisibility(visible = isHoursExpanded) {
                                    Column(modifier = Modifier.padding(top = 16.dp)) {
                                        diasOrdenados.forEach { dia ->
                                            val horarioTexto = res.horarios[dia] ?: "Desconocido"
                                            val colorTexto = if (horarioTexto.lowercase() == "cerrado") Color.Red else MaterialTheme.colorScheme.onSurface
                                            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), Arrangement.SpaceBetween) {
                                                Text(dia.replaceFirstChar { it.uppercase() }, fontWeight = FontWeight.Medium)
                                                Text(horarioTexto, color = colorTexto)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(24.dp))
                        Text("Ubicación", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(12.dp))
                        Card(modifier = Modifier.fillMaxWidth().height(200.dp).clickable {
                            val uri = Uri.parse("google.navigation:q=${res.latitud},${res.longitud}")
                            context.startActivity(Intent(Intent.ACTION_VIEW, uri).apply { setPackage("com.google.android.apps.maps") })
                        }, shape = RoundedCornerShape(20.dp), elevation = CardDefaults.cardElevation(4.dp)) {
                            GoogleMap(modifier = Modifier.fillMaxSize(), cameraPositionState = cameraPositionState, uiSettings = MapUiSettings(zoomControlsEnabled = false, scrollGesturesEnabled = false, zoomGesturesEnabled = false)) {
                                Marker(state = MarkerState(position = posRestaurante), title = res.nombre)
                            }
                        }
                        Spacer(Modifier.height(100.dp))
                    }
                }
                FilledIconButton(onClick = onBack, Modifier.padding(16.dp).align(Alignment.TopStart), colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color.Black.copy(alpha = 0.5f))) {
                    Icon(Icons.Default.Close, null, tint = Color.White)
                }
            }
        }
    }
}

// --- NUEVO: DIÁLOGO DE EDICIÓN ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditRestaurantDialog(
    restaurant: Restaurant,
    onDismiss: () -> Unit,
    onSave: (Restaurant) -> Unit
) {
    var telefono by remember { mutableStateOf(restaurant.telefono) }
    var precio by remember { mutableStateOf(restaurant.precio) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Panel de Gestión") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Edita la información pública de tu local", style = MaterialTheme.typography.bodySmall)
                OutlinedTextField(
                    value = telefono,
                    onValueChange = { telefono = it },
                    label = { Text("Teléfono de contacto") },
                    leadingIcon = { Icon(Icons.Default.Phone, null) }
                )
                OutlinedTextField(
                    value = precio,
                    onValueChange = { precio = it },
                    label = { Text("Rango de precio (ej: 15-25€)") },
                    leadingIcon = { Icon(Icons.Default.EuroSymbol, null) }
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSave(restaurant.copy(telefono = telefono, precio = precio)) }) {
                Text("Guardar cambios")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun QuickActionButton(icon: ImageVector, label: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(8.dp).clickable { onClick() }) {
        Surface(shape = CircleShape, color = Color(0xFFB30000).copy(alpha = 0.1f), modifier = Modifier.size(56.dp)) {
            Box(contentAlignment = Alignment.Center) { Icon(icon, label, tint = Color(0xFFB30000), modifier = Modifier.size(28.dp)) }
        }
        Spacer(Modifier.height(8.dp))
        Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = null, tint = Color(0xFFB30000), modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(16.dp))
        Column {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color(0xFFB30000))
            Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
        }
    }
}