package com.example.navarres.view

import android.location.Location
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.navarres.model.data.Restaurant
import com.example.navarres.ui.theme.RestaurantCard
import com.example.navarres.util.HorarioHelper
import com.example.navarres.viewmodel.FavoritosViewModel

@Composable
fun FavoritosScreen(
    viewModel: FavoritosViewModel,
    onNavigateToDetail: (String) -> Unit
) {
    val merindadesLat = 42.8137
    val merindadesLon = -1.6406

    // --- ZONA DE DATOS FALSOS PARA TESTEAR UI ---
    val listaTemporal = remember {
        // Creamos un mapa de días para no escribirlo 7 veces
        val diasSemana = listOf("lunes", "martes", "miercoles", "jueves", "viernes", "sabado", "domingo")

        // Generador rápido de horarios
        fun crearHorario(texto: String): Map<String, String> {
            return diasSemana.associateWith { texto }
        }

        listOf(
            // CASO 1: ABIERTO (Forzamos con palabra clave "24h" o un rango válido actual)
            Restaurant(
                nombre = "Asador Siempre Abierto",
                categoria = "Asador",
                valoracion = 4.8,
                foto = "https://via.placeholder.com/150", // Foto falsa o vacía
                latitud = 42.8130, longitud = -1.6400,
                horarios = crearHorario("Abierto 24h") // <--- ESTO FUERZA EL VERDE
            ),
            // CASO 2: CERRADO
            Restaurant(
                nombre = "Bar La Siesta",
                categoria = "Bar",
                valoracion = 3.5,
                latitud = 42.8140, longitud = -1.6410,
                horarios = crearHorario("Cerrado") // <--- ESTO FUERZA EL GRIS
            ),
            // CASO 3: DESCONOCIDO (Mapa vacío)
            Restaurant(
                nombre = "Tasca El Misterio",
                categoria = "Tasca",
                valoracion = 4.2,
                latitud = 42.8150, longitud = -1.6420,
                horarios = emptyMap() // <--- ESTO FUERZA EL NARANJA
            )
        )
    }
    // ---------------------------------------------

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Tus Favoritos (Modo Test)",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = Color(0xFFB30000),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // CAMBIO: Usamos listaTemporal en vez de viewModel.listaFavoritos
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(listaTemporal) { restaurante ->

                // 1. Cálculo de distancia
                val resultados = FloatArray(1)
                Location.distanceBetween(merindadesLat, merindadesLon, restaurante.latitud, restaurante.longitud, resultados)
                val distStr = "${String.format("%.1f", resultados[0] / 1000)} km"

                // 2. Cálculo de Horario (¡IMPORTANTE!)
                val estadoHorario = HorarioHelper.getStatus(restaurante.horarios)

                RestaurantCard(
                    name = restaurante.nombre,
                    category = restaurante.categoria,
                    rating = restaurante.valoracion,
                    distance = distStr,
                    fotoUrl = restaurante.foto,

                    // 3. Pasamos el estado calculado
                    openStatus = estadoHorario,

                    isFavorite = true,
                    onFavoriteClick = { /* No hace nada en modo test */ },
                    onClick = { onNavigateToDetail(restaurante.nombre) }
                )
            }
        }
    }
}