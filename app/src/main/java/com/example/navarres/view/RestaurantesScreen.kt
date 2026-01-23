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
        // Encabezado de la pantalla
        item {
            Text(
                text = "Gastronomía Foral",
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        // Iteramos sobre la lista de objetos Restaurant (usando el nombre de variable de main)
        items(viewModel.localesDePrueba) { restaurante ->

            // Verificamos si este ID está en el mapa de favoritos
            // Main utiliza 'identificador' en lugar del nombre para evitar duplicados
            val isFav = viewModel.favoritosState[restaurante.identificador] ?: false

            RestaurantCard(
                name = restaurante.nombre,
                // Si la categoría viene vacía del JSON, ponemos un valor por defecto
                category = if (restaurante.categoria.isNotEmpty()) restaurante.categoria else "Cocina Navarra",
                rating = 4, // Valor estático temporal
                distance = restaurante.municipio, // Usamos el municipio como dato de ubicación
                isFavorite = isFav,
                
                // Acción para marcar/desmarcar favorito pasando el objeto completo
                onFavoriteClick = { 
                    viewModel.toggleFavorite(restaurante) 
                },
                
                // Acción para abrir el detalle (tu lógica de navegación)
                onClick = { 
                    onRestaurantClick(restaurante) 
                }
            )
        }
    }
}