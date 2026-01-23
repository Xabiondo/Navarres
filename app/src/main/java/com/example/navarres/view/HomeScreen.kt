package com.example.navarres.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.navarres.model.repository.UserRepository // <--- IMPORTANTE: AÑADE ESTO
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
    configViewModel: ConfigViewModel,
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
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
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
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.outline,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
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
                .background(MaterialTheme.colorScheme.background)
        ) {
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
                    // --- ARREGLO AQUÍ ---
                    // Creamos el repositorio aquí porque HomeViewModel no nos lo da
                    val userRepo = remember { UserRepository() }

                    // Inicializamos el ViewModel pasando ambos repositorios
                    val profileVM = remember {
                        ProfileViewModel(
                            authRepository = viewModel.authRepository, // Este asumimos que sí es público
                            userRepository = userRepo
                        )
                    }

                    ProfileScreen(
                        viewModel = profileVM,
                        onLogoutClick = { viewModel.logout() }
                    )
                }
                NavItem.Ajustes.route -> {
                    ConfigScreen(viewModel = configViewModel)
                }
            }
        }
    }
}