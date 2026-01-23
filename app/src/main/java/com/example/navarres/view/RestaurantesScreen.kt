package com.example.navarres.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.navarres.model.data.Restaurant
import com.example.navarres.ui.theme.RestaurantCard
import com.example.navarres.viewmodel.RestaurantesViewModel

@Composable
fun RestaurantesScreen(
    viewModel: RestaurantesViewModel,
    onRestaurantClick: (Restaurant) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Gastronomía Foral",
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        items(viewModel.listaRestaurantes) { restaurante ->
            RestaurantCard(
                name = restaurante.nombre,
                category = restaurante.categoria,
                rating = 4, // Valor temporal
                distance = "A 1.2 km", // Valor temporal
                isFavorite = viewModel.favoritosState[restaurante.nombre] ?: false,
                onFavoriteClick = { viewModel.toggleFavorite(restaurante.nombre) },
                onClick = { onRestaurantClick(restaurante) }
            )
        }
    }
}