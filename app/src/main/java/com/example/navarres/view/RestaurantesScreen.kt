package com.example.navarres.view

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.NearMe
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.navarres.ui.theme.RestaurantCard
import com.example.navarres.util.HorarioHelper
import com.example.navarres.viewmodel.RestaurantesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantesScreen(
    viewModel: RestaurantesViewModel,
    onNavigateToDetail: (String) -> Unit
) {
    val navarresRed = Color(0xFFB30000)
    var showFilterSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    // --- GESTIÓN DE PERMISOS ---
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (granted) {
            viewModel.refreshLocationAndSort()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {

            Spacer(modifier = Modifier.height(16.dp))

            // --- 1. BARRA SUPERIOR ---
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {

                // Barra de Búsqueda
                Surface(
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(25.dp),
                    shadowElevation = 4.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Icon(Icons.Default.Search, null, tint = Color.Gray)
                        Spacer(Modifier.width(8.dp))
                        Box(Modifier.weight(1f)) {
                            if (viewModel.searchText.isEmpty()) Text("Buscar...", color = Color.Gray)
                            androidx.compose.foundation.text.BasicTextField(
                                value = viewModel.searchText,
                                onValueChange = { viewModel.onSearchTextChange(it) },
                                singleLine = true,
                                textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface)
                            )
                        }
                        if (viewModel.searchText.isNotEmpty()) {
                            Icon(Icons.Default.Close, null, tint = Color.Gray, modifier = Modifier.clickable { viewModel.onSearchTextChange("") })
                        }
                    }
                }

                Spacer(Modifier.width(8.dp))

                // --- BOTÓN GPS ---
                val hasLocation = viewModel.userLocation != null
                FilledIconButton(
                    onClick = {
                        if (hasLocation) {
                            viewModel.clearLocationFilter()
                        } else {
                            locationPermissionLauncher.launch(
                                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                            )
                            viewModel.refreshLocationAndSort()
                        }
                    },
                    modifier = Modifier.size(50.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = if (hasLocation) navarresRed else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (hasLocation) Color.White else Color.Gray
                    )
                ) {
                    if (hasLocation) {
                        Icon(Icons.Default.LocationDisabled, "Desactivar Ubicación")
                    } else {
                        Icon(Icons.Outlined.NearMe, "Activar Ubicación")
                    }
                }

                Spacer(Modifier.width(8.dp))

                // Botón Filtros
                val hasFilters = viewModel.filterRating > 0 || viewModel.filterPrice > 0
                FilledIconButton(
                    onClick = { showFilterSheet = true },
                    modifier = Modifier.size(50.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = if (hasFilters) navarresRed else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (hasFilters) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Icon(Icons.Default.Tune, "Filtros")
                }
            }

            // --- 2. CHIPS DE FILTROS ACTIVOS ---
            if (viewModel.filterRating > 0 || viewModel.filterPrice > 0) {
                LazyRow(
                    modifier = Modifier.padding(top = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (viewModel.filterRating > 0) {
                        item {
                            ActiveFilterChip("${viewModel.filterRating}+ Estrellas") {
                                viewModel.updateFilters(0, viewModel.filterPrice)
                            }
                        }
                    }
                    if (viewModel.filterPrice > 0) {
                        item {
                            val labels = listOf("", "€ Económico", "€€ Medio", "€€€ Alto")
                            val label = labels.getOrElse(viewModel.filterPrice) { "" }
                            ActiveFilterChip(label) {
                                viewModel.updateFilters(viewModel.filterRating, 0)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // --- 3. LISTA DE RESTAURANTES ---
            if (viewModel.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = navarresRed)
                }
            } else {
                // Info de ordenación
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
                    if (viewModel.userLocation != null) {
                        Icon(Icons.Outlined.NearMe, null, modifier = Modifier.size(14.dp), tint = navarresRed)
                        Text(" Ordenado por cercanía a ti", style = MaterialTheme.typography.labelSmall, color = navarresRed)
                    } else {
                        Text(" Distancia desde Pamplona (GPS inactivo)", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                }

                if (viewModel.listaVisualizable.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Sin resultados", color = Color.Gray)
                    }
                }

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(viewModel.listaVisualizable) { res ->
                        val isFav = viewModel.favoritosState[res.nombre] == true
                        val distStr = viewModel.calculateDistanceStr(res.latitud, res.longitud)

                        // CÁLCULO DE HORARIO
                        val statusCalculado = HorarioHelper.getStatus(res.horarios)

                        RestaurantCard(
                            name = res.nombre,
                            category = res.categoria,
                            rating = res.valoracion,
                            distance = distStr,
                            fotoUrl = res.foto,
                            openStatus = statusCalculado,
                            isFavorite = isFav,
                            onFavoriteClick = { viewModel.toggleFavorite(res) },
                            onClick = { onNavigateToDetail(res.nombre) }
                        )
                    }
                }
            }
        }

        // --- BOTTOM SHEET FILTROS ---
        if (showFilterSheet) {
            ModalBottomSheet(onDismissRequest = { showFilterSheet = false }, sheetState = sheetState) {
                FilterSheetContent(
                    currentRating = viewModel.filterRating,
                    currentPrice = viewModel.filterPrice,
                    onApply = { r, p ->
                        viewModel.updateFilters(r, p)
                        showFilterSheet = false
                    },
                    onReset = {
                        viewModel.updateFilters(0, 0)
                        showFilterSheet = false
                    }
                )
            }
        }
    }
}

// --------------------------------------------------------
// FUNCIONES AUXILIARES (TIENEN QUE ESTAR AQUÍ O EN OTRO ARCHIVO)
// --------------------------------------------------------

@Composable
fun ActiveFilterChip(text: String, onDelete: () -> Unit) {
    Surface(
        color = Color(0xFFB30000).copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color(0xFFB30000).copy(alpha = 0.3f))
    ) {
        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(text, style = MaterialTheme.typography.labelMedium, color = Color(0xFFB30000))
            Spacer(Modifier.width(4.dp))
            Icon(Icons.Default.Close, null, modifier = Modifier.size(14.dp).clickable { onDelete() }, tint = Color(0xFFB30000))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterSheetContent(currentRating: Int, currentPrice: Int, onApply: (Int, Int) -> Unit, onReset: () -> Unit) {
    var tempRating by remember { mutableIntStateOf(currentRating) }
    var tempPrice by remember { mutableIntStateOf(currentPrice) }
    val navarresRed = Color(0xFFB30000)

    Column(modifier = Modifier.padding(24.dp)) {
        Text("Filtros", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(24.dp))

        // Valoración
        Text("Valoración mínima", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            (1..5).forEach { star ->
                val isSelected = tempRating == star
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { tempRating = if (tempRating == star) 0 else star }) {
                    Icon(Icons.Default.Star, null, tint = if (star <= tempRating) Color(0xFFFFB400) else Color.LightGray, modifier = Modifier.size(32.dp))
                    Text("$star+", style = MaterialTheme.typography.labelSmall, color = if (isSelected) navarresRed else Color.Gray, fontWeight = if(isSelected) FontWeight.Bold else FontWeight.Normal)
                }
            }
        }

        HorizontalDivider(Modifier.padding(vertical = 24.dp))

        // Precio
        Text("Rango de Precio", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            listOf(1 to "€", 2 to "€€", 3 to "€€€").forEach { (level, label) ->
                val isSelected = tempPrice == level
                FilterChip(
                    selected = isSelected,
                    onClick = { tempPrice = if (tempPrice == level) 0 else level },
                    label = { Text(label) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = navarresRed, selectedLabelColor = Color.White),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        // Botones
        Row(Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = onReset, modifier = Modifier.weight(1f), border = BorderStroke(1.dp, navarresRed), colors = ButtonDefaults.outlinedButtonColors(contentColor = navarresRed)) { Text("Borrar") }
            Spacer(Modifier.width(16.dp))
            Button(onClick = { onApply(tempRating, tempPrice) }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = navarresRed)) { Text("Aplicar") }
        }
        Spacer(Modifier.height(32.dp))
    }
}