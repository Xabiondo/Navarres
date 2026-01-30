package com.example.navarres.view

import android.location.Location
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.navarres.ui.theme.RestaurantCard
import com.example.navarres.viewmodel.FavoritosViewModel

@Composable
fun FavoritosScreen(
    viewModel: FavoritosViewModel,
    onNavigateToDetail: (String) -> Unit // Añadido para poder navegar al pulsar
) {
    // Coordenadas por defecto (o podrías inyectar la ubicación real del usuario si quisieras)
    val merindadesLat = 42.8137
    val merindadesLon = -1.6406

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
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Aún no tienes sitios favoritos guardados.", color = Color.Gray)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(viewModel.listaFavoritos) { restaurante ->

                    // Cálculo de distancia
                    val resultados = FloatArray(1)
                    Location.distanceBetween(merindadesLat, merindadesLon, restaurante.latitud, restaurante.longitud, resultados)
                    val distStr = "${String.format("%.1f", resultados[0] / 1000)} km"

                    RestaurantCard(
                        name = restaurante.nombre,
                        category = restaurante.categoria,
                        // CAMBIO: Pasamos directamente el Double (ej: 4.4)
                        rating = restaurante.valoracion,
                        distance = distStr,
                        fotoUrl = restaurante.foto,
                        isFavorite = true, // En esta pantalla siempre son true
                        onFavoriteClick = { viewModel.eliminarFavorito(restaurante) },
                        onClick = { onNavigateToDetail(restaurante.nombre) }
                    )
                }
            }
        }
    }
}