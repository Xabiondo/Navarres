package com.example.navarres.view

import android.location.Location
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.navarres.ui.theme.RestaurantCard
import com.example.navarres.viewmodel.RestaurantesViewModel

@Composable
fun RestaurantesScreen(
    viewModel: RestaurantesViewModel,
    onNavigateToDetail: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Gastronomía Foral",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = Color(0xFFB30000),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(viewModel.listaRestaurantes) { restaurante ->
                val isFav = viewModel.favoritosState[restaurante.nombre] == true

                // Distancia desde Merindades
                val resultados = FloatArray(1)
                Location.distanceBetween(42.8137, -1.6406, restaurante.latitud, restaurante.longitud, resultados)
                val distStr = "${String.format("%.1f", resultados[0] / 1000)} km"

                // EXTRAEMOS EL NÚMERO (Ej: "Tercera/1 tenedor" -> 1)
                val numTenedores = restaurante.categoria.filter { it.isDigit() }.toIntOrNull() ?: 1

                RestaurantCard(
                    name = restaurante.nombre,
                    category = restaurante.categoria,
                    rating = numTenedores,
                    distance = distStr,
                    fotoUrl = restaurante.foto,
                    isFavorite = isFav,
                    onFavoriteClick = { viewModel.toggleFavorite(restaurante) },
                    onClick = { onNavigateToDetail(restaurante.nombre) }
                )
            }
        }
    }
}