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
// Asegúrate de que este import existe. Si RestaurantCard está en otro lado, ajusta esto:
import com.example.navarres.ui.theme.RestaurantCard
import com.example.navarres.viewmodel.RestaurantesViewModel

@Composable
fun RestaurantesScreen(
    viewModel: RestaurantesViewModel,
    // Usamos (String) -> Unit para que coincida con lo que envía HomeScreen
    onNavigateToDetail: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Título con el mismo estilo que en Favoritos (Rojo corporativo)
        Text(
            text = "Gastronomía Foral",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = Color(0xFFB30000),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Iteramos sobre la lista del ViewModel
            items(viewModel.localesDePrueba) { restaurante ->

                // Calculamos si es favorito mirando el mapa del ViewModel
                val isFav = viewModel.favoritosState[restaurante.identificador] == true

                // --- AQUÍ USAMOS TU COMPONENTE RESTAURANTCARD ---
                // Esto garantiza que el estilo sea idéntico al de Favoritos
                RestaurantCard(
                    name = restaurante.nombre,
                    // Si no hay categoría, ponemos un texto por defecto
                    category = if (restaurante.categoria.isNotEmpty()) restaurante.categoria else "Cocina Navarra",
                    rating = 4, // Valor fijo por ahora
                    distance = restaurante.municipio,
                    isFavorite = isFav,
                    onFavoriteClick = {
                        viewModel.toggleFavorite(restaurante)
                    },
                    onClick = {
                        // Al hacer clic, enviamos el ID al HomeScreen para navegar
                        onNavigateToDetail(restaurante.identificador)
                    }
                )
            }
        }
    }
}