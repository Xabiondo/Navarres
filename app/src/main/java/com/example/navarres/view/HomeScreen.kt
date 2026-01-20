package com.example.navarres.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.navarres.ui.theme.RestaurantCard
import com.example.navarres.viewmodel.HomeViewModel


sealed class NavItem(val route: String, val title: String, val icon: ImageVector) {
    object Restaurantes : NavItem("restaurantes", "Restaurantes", Icons.Default.Restaurant)
    object Favoritos : NavItem("favoritos", "Favoritos", Icons.Default.Favorite)
    object Perfil : NavItem("perfil", "Perfil", Icons.Default.Person)
    object Ajustes : NavItem("config", "Ajustes", Icons.Default.Settings)
}

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onLogoutSuccess: () -> Unit
) {
    val email by viewModel.currentUserEmail.collectAsState()
    val isLoggedOut by viewModel.isLoggedOut.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()

    // Manejo de estados para la prueba de restaurantes
    val localesDePrueba = remember {
        mutableStateListOf(
            Triple("El Redín", "0.5 km", 4),
            Triple("La Viña", "1.2 km", 2),
            Triple("Mesón de la Tortilla", "2.1 km", 3),
            Triple("Taberna NavarRes", "0.2 km", 1)
        )
    }
    val favoritosState = remember { mutableStateMapOf<String, Boolean>() }

    LaunchedEffect(isLoggedOut) {
        if (isLoggedOut) {
            onLogoutSuccess()
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFFFDFCF0),
                contentColor = Color(0xFFB30000)
            ) {
                val navItems = listOf(
                    NavItem.Restaurantes, NavItem.Favoritos, NavItem.Perfil, NavItem.Ajustes
                )
                navItems.forEach { item ->
                    NavigationBarItem(
                        selected = selectedTab == item.route,
                        onClick = { viewModel.selectTab(item.route) },
                        label = { Text(item.title) },
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFFB30000),
                            unselectedIconColor = Color.Gray,
                            indicatorColor = Color(0xFFFFEBEE)
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFFDFCF0))
        ) {
            when (selectedTab) {
                "restaurantes" -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        item {
                            Text(
                                text = "Restaurantes Cercanos",
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFFB30000),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        items(localesDePrueba) { (nombre, distancia, nota) ->
                            val isFav = favoritosState[nombre] ?: false
                            RestaurantCard(
                                name = nombre,
                                category = "Cocina Navarra",
                                rating = nota,
                                distance = distancia,
                                isFavorite = isFav,
                                onFavoriteClick = { favoritosState[nombre] = !isFav },
                                onClick = { println("Click en $nombre") }
                            )
                        }
                    }
                }
                "favoritos" -> {
                    Text("Tus sitios favoritos", Modifier.align(Alignment.Center))
                }
                "perfil" -> {
                    HomeContent(userEmail = email, onLogoutClick = viewModel::logout)
                }
                "config" -> {
                    Text("Configuración de la App", Modifier.align(Alignment.Center))
                }
            }
        }
    }
}

@Composable
fun HomeContent(userEmail: String, onLogoutClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.Restaurant, null, Modifier.size(100.dp), Color(0xFF2E7D32))
        Spacer(Modifier.height(24.dp))
        Text("¡Buen provecho!", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = Color(0xFFB30000))
        Text("Explora los sabores del Reyno", style = MaterialTheme.typography.bodyLarge, color = Color.DarkGray)
        Spacer(Modifier.height(16.dp))
        Text("Conectado como: $userEmail", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        Spacer(Modifier.height(48.dp))
        Button(
            onClick = onLogoutClick,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Color(0xFFB30000)),
            modifier = Modifier.padding(horizontal = 32.dp).height(50.dp).fillMaxWidth(0.7f),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color(0xFFB30000))
        ) {
            Icon(Icons.Default.ExitToApp, null)
            Spacer(Modifier.width(8.dp))
            Text("ABANDONAR LA MESA", fontWeight = FontWeight.Bold)
        }
    }
}