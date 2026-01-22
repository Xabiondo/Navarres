package com.example.navarres.view

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
fun RestaurantesScreen(viewModel: RestaurantesViewModel) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                text = "Restaurantes Cercanos",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFFB30000),
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        items(viewModel.localesDePrueba) { (nombre, distancia, nota) ->
            RestaurantCard(
                name = nombre,
                category = "Cocina Navarra",
                rating = nota,
                distance = distancia,
                isFavorite = viewModel.favoritosState[nombre] ?: false,
                onFavoriteClick = { viewModel.toggleFavorite(nombre) },
                onClick = { /* Navegación a detalle */ }
            )
        }
    }
}