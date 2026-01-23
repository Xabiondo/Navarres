package com.example.navarres.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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

    // Recargar la lista cada vez que se entra a la pantalla
    // (Por si añadiste uno nuevo en la pantalla Home y viniste aquí rápido)
    LaunchedEffect(Unit) {
        viewModel.cargarFavoritos()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Tus Favoritos",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = Color(0xFFB30000), // Tu color rojo
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (viewModel.listaFavoritos.isEmpty()) {
            // Mensaje por si no hay nada
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Aún no tienes sitios favoritos guardados.")
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(viewModel.listaFavoritos) { restaurante ->
                    RestaurantCard(
                        name = restaurante.nombre,
                        category = if (restaurante.categoria.isNotEmpty()) restaurante.categoria else "Cocina Navarra",
                        rating = 4,
                        distance = restaurante.municipio,
                        isFavorite = true, // Aquí SIEMPRE es true porque es la lista de favoritos
                        onFavoriteClick = {
                            // Al clicar el corazón aquí, lo eliminamos de la lista
                            viewModel.eliminarFavorito(restaurante)
                        },
                        onClick = { /* Navegar al detalle */ }
                    )
                }
            }
        }
    }
}