@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    configViewModel: ConfigViewModel,
    onLogoutSuccess: () -> Unit
) {
    val isLoggedOut by viewModel.isLoggedOut.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val uiStrings by configViewModel.uiStrings.collectAsState()

    // Estado para saber si hay un restaurante seleccionado para ver detalles
    var selectedRestaurantForDetail by remember { mutableStateOf<Restaurant?>(null) }
    
    // ViewModel para el detalle (se crea/recupera automáticamente)
    val detailViewModel: RestauranteDetailViewModel = viewModel()

    LaunchedEffect(isLoggedOut) {
        if (isLoggedOut) onLogoutSuccess()
    }

    // Si estamos viendo un detalle, el botón "Atrás" del móvil cierra el detalle
    BackHandler(enabled = selectedRestaurantForDetail != null) {
        selectedRestaurantForDetail = null
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                val navItems = listOf(
                    NavItem.Restaurantes to (uiStrings["nav_rest"] ?: "Restaurantes"),
                    NavItem.Favoritos to (uiStrings["nav_fav"] ?: "Favoritos"),
                    NavItem.Perfil to (uiStrings["nav_perfil"] ?: "Perfil"),
                    NavItem.Ajustes to (uiStrings["nav_config"] ?: "Ajustes")
                )

                navItems.forEach { (item, title) ->
                    NavigationBarItem(
                        selected = selectedTab == item.route,
                        onClick = {
                            selectedRestaurantForDetail = null
                            viewModel.selectTab(item.route)
                        },
                        label = { Text(title) },
                        icon = { Icon(item.icon, contentDescription = title) }
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
            // LÓGICA PRINCIPAL: ¿Mostramos detalle o lista?
            if (selectedRestaurantForDetail != null) {
                // 1. PANTALLA DE DETALLE
                LaunchedEffect(selectedRestaurantForDetail) {
                    selectedRestaurantForDetail?.let { detailViewModel.selectRestaurant(it) }
                }
                
                RestauranteDetailScreen(
                    viewModel = detailViewModel,
                    configViewModel = configViewModel,
                    onBack = { selectedRestaurantForDetail = null }
                )
            } else {
                // 2. PANTALLAS DEL MENÚ INFERIOR
                when (selectedTab) {
                    NavItem.Restaurantes.route -> {
                        val resVM: RestaurantesViewModel = viewModel()
                        RestaurantesScreen(
                            viewModel = resVM,
                            onNavigateToDetail = { id -> 
                                // Buscamos el restaurante y lo asignamos para abrir detalle
                                val restaurant = resVM.getRestaurantById(id)
                                selectedRestaurantForDetail = restaurant
                            }
                        )
                    }
                    NavItem.Favoritos.route -> {
                        val favVM: FavoritosViewModel = viewModel()
                        FavoritosScreen(viewModel = favVM)
                    }
                    NavItem.Perfil.route -> {
                        // --- CORRECCIÓN CLAVE AQUÍ ---
                        // Usamos viewModel() sin parámetros porque tu ProfileViewModel
                        // ya inicializa sus repositorios internamente.
                        val profileVM: ProfileViewModel = viewModel()

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
}