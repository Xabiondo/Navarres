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

        // Iteramos sobre tu lista de objetos Restaurant
        items(viewModel.localesDePrueba) { restaurante ->

            // Verificamos si este ID está en el mapa de favoritos
            val isFav = viewModel.favoritosState[restaurante.identificador] ?: false

            RestaurantCard(
                name = restaurante.nombre,
                // Usamos tus campos, con valores por defecto si vienen vacíos
                category = if (restaurante.categoria.isNotEmpty()) restaurante.categoria else "Cocina Navarra",
                rating = 4, // Tu modelo no tiene rating numérico, ponle un valor o calcúlalo
                distance = restaurante.municipio, // Usamos municipio o dirección

                isFavorite = isFav,

                // Al hacer click, pasamos el OBJETO COMPLETO al ViewModel
                onFavoriteClick = { viewModel.toggleFavorite(restaurante) },
                onClick = { /* Navegación a detalle */ }
            )
        }
    }
}