package com.example.navarres.view

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
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
    val context = LocalContext.current

    // ESTADO: ¿Estamos viendo la lista completa de reseñas?
    var isViewingReviews by remember { mutableStateOf(false) }

    // Interceptamos el botón atrás físico
    BackHandler(enabled = isViewingReviews) {
        isViewingReviews = false
    }

    // Transición profesional entre Info <-> Comentarios
    Crossfade(targetState = isViewingReviews, label = "Transition") { showReviews ->
        if (showReviews) {
            ComentariosView(
                // --- AÑADE ESTA LÍNEA ---
                restaurantId = restaurant?.nombre ?: "",
                // ------------------------
                restaurantName = restaurant?.nombre ?: "Restaurante",
                onBack = { isViewingReviews = false }
            )
        } else {
            // CONTENIDO DEL DETALLE
            restaurant?.let { res ->
                RestauranteDetailContent(
                    res = res,
                    viewModel = viewModel,
                    context = context,
                    onBack = onBack,
                    onOpenReviews = { isViewingReviews = true }
                )
            }
        }
    }
}

// -------------------------------------------------------------------------
// CONTENIDO PRINCIPAL (INFO + DASHBOARD)
// -------------------------------------------------------------------------
@Composable
fun RestauranteDetailContent(
    res: com.example.navarres.model.data.Restaurant,
    viewModel: RestauranteDetailViewModel,
    context: android.content.Context,
    onBack: () -> Unit,
    onOpenReviews: () -> Unit
) {
    var isHoursExpanded by remember { mutableStateOf(false) }
    var showCarta by remember { mutableStateOf(false) }
    val diasOrdenados = listOf("lunes", "martes", "miercoles", "jueves", "viernes", "sabado", "domingo")

    val posRestaurante = LatLng(res.latitud, res.longitud)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(posRestaurante, 17f)
    }

    if (showCarta && !res.rutaCarta.isNullOrBlank()) {
        Dialog(onDismissRequest = { showCarta = false }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
                AsyncImage(model = res.rutaCarta, contentDescription = "Carta", modifier = Modifier.fillMaxSize().clickable { showCarta = false }, contentScale = ContentScale.Fit)
                FilledIconButton(onClick = { showCarta = false }, modifier = Modifier.align(Alignment.TopEnd).padding(20.dp), colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color.White.copy(alpha = 0.3f))) {
                    Icon(Icons.Default.Close, null, tint = Color.White)
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).background(MaterialTheme.colorScheme.background)) {

            // 1. HEADER
            Box(modifier = Modifier.fillMaxWidth().height(280.dp)) {
                AsyncImage(model = res.foto, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)), startY = 300f)))
                Column(modifier = Modifier.align(Alignment.BottomStart).padding(20.dp)) {
                    Surface(color = Color(0xFFB30000), shape = RoundedCornerShape(8.dp)) {
                        Text(res.categoria.uppercase(), Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Text(res.nombre, style = MaterialTheme.typography.headlineLarge, color = Color.White, fontWeight = FontWeight.ExtraBold)

                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                        Icon(Icons.Rounded.Star, null, tint = Color(0xFFFFC107), modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(text = "${res.valoracion} (120 opiniones)", color = Color.White.copy(alpha = 0.9f), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    }
                }
            }

            Column(modifier = Modifier.padding(20.dp)) {
                // 2. ACCIONES
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    QuickActionButton(Icons.Default.Place, "Ruta") {
                        val uri = Uri.parse("google.navigation:q=${res.latitud},${res.longitud}")
                        context.startActivity(Intent(Intent.ACTION_VIEW, uri).apply { setPackage("com.google.android.apps.maps") })
                    }
                    QuickActionButton(Icons.Default.Phone, "Llamar") {
                        context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${res.telefono}")))
                    }
                    QuickActionButton(Icons.Default.MenuBook, "Carta") {
                        if (!res.rutaCarta.isNullOrBlank()) showCarta = true else Toast.makeText(context, "No hay carta disponible", Toast.LENGTH_SHORT).show()
                    }
                }

                HorizontalDivider(Modifier.padding(vertical = 24.dp), thickness = 0.5.dp)

                // 3. INFO
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)), shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        InfoRow(Icons.Default.LocationOn, "Dirección", res.direccion)
                        Spacer(Modifier.height(16.dp))
                        val distStr = viewModel.calculateDistanceStr(res.latitud, res.longitud)
                        InfoRow(Icons.Default.Schedule, "Distancia", distStr)
                    }
                }

                Spacer(Modifier.height(24.dp))

                // 4. HORARIOS
                val rotationState by animateFloatAsState(targetValue = if (isHoursExpanded) 180f else 0f, label = "rot")
                Card(modifier = Modifier.fillMaxWidth().clickable { isHoursExpanded = !isHoursExpanded }, shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AccessTime, null, tint = Color(0xFFB30000))
                                Spacer(Modifier.width(12.dp)); Text("Horarios", fontWeight = FontWeight.Bold)
                            }
                            Icon(Icons.Default.ExpandMore, null, Modifier.rotate(rotationState))
                        }
                        AnimatedVisibility(visible = isHoursExpanded) {
                            Column(modifier = Modifier.padding(top = 16.dp)) {
                                diasOrdenados.forEach { dia ->
                                    val horario = res.horarios[dia] ?: "Cerrado"
                                    val color = if(horario.equals("cerrado", true)) Color.Red.copy(0.7f) else MaterialTheme.colorScheme.onSurface
                                    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), Arrangement.SpaceBetween) {
                                        Text(dia.replaceFirstChar { it.uppercase() }, fontWeight = FontWeight.Medium, color = Color.Gray)
                                        Text(horario, color = color, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))

                // ---------------------------------------------------------
                // 5. DASHBOARD PREVIEW (INVITACIÓN A VER MÁS)
                // ---------------------------------------------------------
                Text("Opiniones de la comunidad", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))

                RatingDashboardCard(
                    rating = res.valoracion,
                    totalReviews = 120,
                    onSeeAllClick = onOpenReviews
                )
                // ---------------------------------------------------------

                Spacer(Modifier.height(32.dp))

                // 6. MAPA
                Text("Ubicación", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                Card(modifier = Modifier.fillMaxWidth().height(180.dp).clickable {
                    val uri = Uri.parse("google.navigation:q=${res.latitud},${res.longitud}")
                    context.startActivity(Intent(Intent.ACTION_VIEW, uri).apply { setPackage("com.google.android.apps.maps") })
                }, shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(2.dp)) {
                    GoogleMap(modifier = Modifier.fillMaxSize(), cameraPositionState = cameraPositionState, uiSettings = MapUiSettings(zoomControlsEnabled = false, scrollGesturesEnabled = false, zoomGesturesEnabled = false)) {
                        Marker(state = MarkerState(position = posRestaurante), title = res.nombre)
                    }
                }
                Spacer(Modifier.height(100.dp))
            }
        }

        FilledIconButton(onClick = onBack, Modifier.padding(16.dp).align(Alignment.TopStart), colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color.Black.copy(alpha = 0.5f))) {
            Icon(Icons.Default.ArrowBack, null, tint = Color.White)
        }
    }
}

// -------------------------------------------------------------------------
// COMPONENTES DE UI (Dashboard & Botones)
// -------------------------------------------------------------------------

@Composable
fun RatingDashboardCard(rating: Double, totalReviews: Int, onSeeAllClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // NOTA
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(0.4f)) {
                    Text(
                        text = rating.toString(),
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFB30000)
                    )
                    Row {
                        repeat(5) { i ->
                            val icon = if (i < rating.toInt()) Icons.Rounded.Star else Icons.Rounded.StarOutline
                            Icon(icon, null, tint = Color(0xFFFFC107), modifier = Modifier.size(16.dp))
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text("$totalReviews opiniones", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }

                Box(modifier = Modifier.width(1.dp).height(60.dp).background(Color.LightGray.copy(alpha = 0.5f)))

                // BARRAS
                Column(modifier = Modifier.weight(0.6f).padding(start = 16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    val distribution = listOf(0.7f, 0.2f, 0.05f, 0.02f, 0.03f)
                    distribution.forEachIndexed { index, progress ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.height(8.dp)) {
                            Text((5 - index).toString(), style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontSize = 10.sp, modifier = Modifier.width(12.dp))
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier.weight(1f).clip(RoundedCornerShape(4.dp)),
                                color = if(index == 0) Color(0xFFB30000) else Color(0xFFB30000).copy(alpha = 0.5f),
                                trackColor = Color.LightGray.copy(alpha = 0.3f),
                            )
                        }
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), thickness = 0.5.dp)

            // BOTÓN VER MÁS
            Button(
                onClick = onSeeAllClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Leer todas las reseñas", color = Color(0xFFB30000), fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(8.dp))
                Icon(Icons.Default.ArrowForward, null, tint = Color(0xFFB30000), modifier = Modifier.size(16.dp))
            }
        }
    }
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