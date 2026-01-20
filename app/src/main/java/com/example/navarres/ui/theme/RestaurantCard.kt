package com.example.navarres.ui.theme

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun RestaurantCard(
    name: String,
    category: String,
    rating: Int,
    distance: String,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Cuadro del logo a la izquierda
            Surface(
                modifier = Modifier.size(60.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFFDFCF0)
            ) {
                Icon(Icons.Default.Restaurant, null, Modifier.padding(12.dp), Color(0xFFB30000))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(name, fontWeight = FontWeight.Bold, color = Color(0xFFB30000))
                Text(category, style = MaterialTheme.typography.bodySmall, color = Color.Gray)

                Spacer(modifier = Modifier.height(4.dp))

                // VALORACIÓN SOLO CON EL ICONO DE TENEDOR (DINING)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    repeat(4) { index ->
                        Icon(
                            imageVector = Icons.Default.Dining, // Este es el icono más limpio (vertical)
                            contentDescription = null,
                            modifier = Modifier.size(18.dp), // Un pelo más grande para que se vea bien
                            tint = if (index < rating) Color(0xFFB30000) else Color(0xFFE0E0E0)
                        )
                    }

                    Text("  •  ", color = Color.LightGray)

                    Icon(Icons.Default.LocationOn, null, Modifier.size(14.dp), Color(0xFF2E7D32))
                    Text(" $distance", style = MaterialTheme.typography.labelSmall, color = Color(0xFF2E7D32))
                }
            }

            IconButton(onClick = onFavoriteClick) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = null,
                    tint = if (isFavorite) Color(0xFFB30000) else Color.Gray
                )
            }
        }
    }
}