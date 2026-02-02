package com.example.navarres.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarHalf
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
import com.example.navarres.model.data.OpenStatus

@Composable
fun RestaurantCard(
    name: String,
    category: String,
    rating: Double,
    distance: String,
    fotoUrl: String,
    openStatus: OpenStatus, // Parámetro de estado
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit,
    onClick: () -> Unit
) {
    // Paleta de colores local
    val GoldStar = Color(0xFFFFC107)
    val CorporativeRed = Color(0xFFB30000)

    // Configuración visual según el estado
    val (statusText, statusColor, containerColor) = when (openStatus) {
        OpenStatus.OPEN -> Triple("ABIERTO", Color(0xFF2E7D32), Color(0xFFE8F5E9)) // Verde
        // --- CAMBIO AQUI: ROJO PARA CERRADO ---
        OpenStatus.CLOSED -> Triple("CERRADO", Color(0xFFD32F2F), Color(0xFFFFEBEE)) // Rojo / Fondo Rojo claro
        OpenStatus.UNKNOWN -> Triple("HORARIO DESC.", Color(0xFFEF6C00), Color(0xFFFFF3E0)) // Naranja
    }

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
            // 1. IMAGEN
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

            // 2. CONTENIDO PRINCIPAL
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // --- A. CABECERA (Nombre + Fav) ---
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

                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorito",
                        tint = if (isFavorite) CorporativeRed else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        modifier = Modifier
                            .size(22.dp)
                            .clickable { onFavoriteClick() }
                    )
                }

                // --- B. CATEGORÍA ---
                if (category.isNotEmpty()) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.padding(vertical = 2.dp)
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

                // --- C. RATING ---
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$rating",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    RatingBar(rating = rating, color = GoldStar)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "(${rating.toInt() * 10 + 5})",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        fontSize = 10.sp
                    )
                }

                // --- D. FOOTER: DISTANCIA Y ESTADO ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Distancia
                    Row(verticalAlignment = Alignment.CenterVertically) {
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

                    // Estado (Abierto/Cerrado)
                    Surface(
                        color = containerColor,
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.2f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            // Punto indicador (solo si no es desconocido)
                            if (openStatus != OpenStatus.UNKNOWN) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(statusColor, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }

                            Text(
                                text = statusText,
                                style = MaterialTheme.typography.labelSmall,
                                color = statusColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// Helper de estrellas
@Composable
fun RatingBar(
    rating: Double,
    maxStars: Int = 5,
    color: Color = Color(0xFFFFC107)
) {
    Row {
        for (i in 1..maxStars) {
            val icon = when {
                i <= rating + 0.25 -> Icons.Rounded.Star
                i <= rating + 0.75 -> Icons.Rounded.StarHalf
                else -> Icons.Rounded.StarOutline
            }
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