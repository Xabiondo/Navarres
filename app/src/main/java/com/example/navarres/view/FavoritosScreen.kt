package com.example.navarres.view

import android.location.Location
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.navarres.ui.theme.RestaurantCard
import com.example.navarres.util.HorarioHelper
import com.example.navarres.viewmodel.FavoritosViewModel

@Composable
fun FavoritosScreen(
    viewModel: FavoritosViewModel,
    onNavigateToDetail: (String) -> Unit
) {
    // Coordenadas de referencia (Pamplona centro)
    // NOTA: Si quieres usar la ubicación real del usuario aquí también,
    // deberías inyectar el LocationRepository en el FavoritosViewModel igual que en RestaurantesViewModel.
    val merindadesLat = 42.8137
    val merindadesLon = -1.6406

    // Recargar la lista cada vez que entramos a esta pantalla para asegurar que esté actualizada
    LaunchedEffect(Unit) {
        viewModel.cargarFavoritos()
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Tus Favoritos",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = Color(0xFFB30000),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (viewModel.listaFavoritos.isEmpty()) {
            // Mensaje si no hay favoritos
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text(text = "No tienes favoritos guardados", color = Color.Gray)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // AQUI ESTABA EL ERROR: Usabas listaTemporal, ahora usamos la del ViewModel
                items(viewModel.listaFavoritos) { restaurante ->

                    // 1. Cálculo de distancia
                    val resultados = FloatArray(1)
                    Location.distanceBetween(merindadesLat, merindadesLon, restaurante.latitud, restaurante.longitud, resultados)
                    val distStr = "${String.format("%.1f", resultados[0] / 1000)} km"

                    // 2. Estado del horario
                    val estadoHorario = HorarioHelper.getStatus(restaurante.horarios)

                    RestaurantCard(
                        name = restaurante.nombre,
                        category = restaurante.categoria,
                        rating = restaurante.valoracion,
                        distance = distStr,
                        fotoUrl = restaurante.foto,
                        openStatus = estadoHorario,

                        isFavorite = true, // En esta pantalla siempre son true visualmente

                        // 3. LOGICA DE ELIMINAR: Llamamos al ViewModel
                        onFavoriteClick = {
                            viewModel.eliminarFavorito(restaurante)
                        },

                        // 4. LOGICA DE NAVEGACIÓN: Usamos el nombre real del restaurante
                        onClick = {
                            onNavigateToDetail(restaurante.nombre)
                        }
                    )
                }
            }
        }
    }
}