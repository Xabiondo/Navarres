package com.example.navarres.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.navarres.viewmodel.*

// Definición de las rutas de navegación
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
    val isLoggedOut by viewModel.isLoggedOut.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()

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
            // Modularización: cada pestaña llama a su propio archivo Screen
            when (selectedTab) {
                NavItem.Restaurantes.route -> {
                    val resVM: RestaurantesViewModel = viewModel()
                    RestaurantesScreen(viewModel = resVM)
                }
                NavItem.Favoritos.route -> {
                    val favVM: FavoritosViewModel = viewModel()
                    FavoritosScreen(viewModel = favVM)
                }
                NavItem.Perfil.route -> {
                    // SOLUCIÓN: Creamos el VM pasando manualmente el repositorio del HomeViewModel
                    val perVM = remember { PerfilViewModel(viewModel.authRepository) }
                    PerfilScreen(viewModel = perVM, onLogoutClick = { viewModel.logout() })
                }
                NavItem.Ajustes.route -> {
                    val confVM: ConfigViewModel = viewModel()
                    ConfigScreen(viewModel = confVM)
                }
            }
        }
    }
}