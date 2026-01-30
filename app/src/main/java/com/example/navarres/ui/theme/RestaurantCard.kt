package com.example.navarres.ui.theme

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarHalf // Importante para medias estrellas
import androidx.compose.material.icons.rounded.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@Composable
fun RestaurantCard(
    name: String,
    category: String,
    rating: Double, // --- AHORA ES DOUBLE (ej: 4.4) ---
    distance: String,
    fotoUrl: String,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit,
    onClick: () -> Unit
) {
    // Paleta de colores local para este componente
    val GoldStar = Color(0xFFFFC107) // Amarillo Google/Amber
    val CorporativeRed = Color(0xFFB30000)

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            // 1. IMAGEN DE ALTO IMPACTO
            Card(
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(110.dp),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                AsyncImage(
                    model = fotoUrl,
                    contentDescription = name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 2. CONTENIDO
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Cabecera: Nombre y Favorito
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )

                    // Botón corazón sutil
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorito",
                        tint = if (isFavorite) CorporativeRed else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        modifier = Modifier
                            .size(22.dp)
                            .clickable { onFavoriteClick() }
                    )
                }

                // Etiqueta de Categoría (Estilo Chip)
                if (category.isNotEmpty()) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = category.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 10.sp
                        )
                    }
                }

                // Rating Bar (Estrellas + Número)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$rating", // Muestra el decimal real
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(4.dp))

                    // Renderizado de estrellas (con medias)
                    RatingBar(rating = rating, color = GoldStar)

                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "(${rating.toInt() * 10 + 5})", // Número simulado
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        fontSize = 10.sp
                    )
                }

                // Ubicación / Distancia
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = CorporativeRed
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = distance,
                        style = MaterialTheme.typography.labelMedium,
                        color = CorporativeRed,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

// --- HELPER PARA DIBUJAR ESTRELLAS (Versión DOUBLE con Medias Estrellas) ---
@Composable
fun RatingBar(
    rating: Double,
    maxStars: Int = 5,
    color: Color = Color(0xFFFFC107)
) {
    Row {
        for (i in 1..maxStars) {
            // Lógica matemática para decidir el icono (Llena, Media o Vacía)
            val icon = when {
                i <= rating + 0.25 -> Icons.Rounded.Star         // Ej: i=4, rating=3.8 -> 4 <= 4.05 (True)
                i <= rating + 0.75 -> Icons.Rounded.StarHalf     // Ej: i=4, rating=3.4 -> 4 <= 4.15 (True)
                else -> Icons.Rounded.StarOutline
            }

            // Coloreamos solo si es llena o media
            val tintColor = if (i <= rating + 0.75) color else Color.LightGray.copy(alpha = 0.4f)

            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tintColor,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}