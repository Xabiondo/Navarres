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
fun FavoritosScreen(viewModel: FavoritosViewModel) {
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
                Text("Aún no tienes sitios favoritos guardados.")
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(viewModel.listaFavoritos) { restaurante ->
                    val resultados = FloatArray(1)
                    Location.distanceBetween(merindadesLat, merindadesLon, restaurante.latitud, restaurante.longitud, resultados)
                    val distStr = "${String.format("%.1f", resultados[0] / 1000)} km"

                    // EXTRAEMOS EL NÚMERO DE TENEDORES
                    val numTenedores = restaurante.categoria.filter { it.isDigit() }.toIntOrNull() ?: 1

                    RestaurantCard(
                        name = restaurante.nombre,
                        category = restaurante.categoria,
                        rating = numTenedores,
                        distance = distStr,
                        fotoUrl = restaurante.foto,
                        isFavorite = true,
                        onFavoriteClick = { viewModel.eliminarFavorito(restaurante) },
                        onClick = { /* Navegar al detalle */ }
                    )
                }
            }
        }
    }
}