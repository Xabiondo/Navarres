@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    configViewModel: ConfigViewModel,
    onLogoutSuccess: () -> Unit
) {
    val isLoggedOut by viewModel.isLoggedOut.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val uiStrings by configViewModel.uiStrings.collectAsState()

    var selectedRestaurantForDetail by remember { mutableStateOf<Restaurant?>(null) }
    val detailViewModel: RestauranteDetailViewModel = viewModel()

    LaunchedEffect(isLoggedOut) {
        if (isLoggedOut) onLogoutSuccess()
    }

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
            if (selectedRestaurantForDetail != null) {
                LaunchedEffect(selectedRestaurantForDetail) {
                    selectedRestaurantForDetail?.let { detailViewModel.selectRestaurant(it) }
                }

                RestauranteDetailScreen(
                    viewModel = detailViewModel,
                    configViewModel = configViewModel,
                    onBack = { selectedRestaurantForDetail = null }
                )
            } else {
                when (selectedTab) {
                    NavItem.Restaurantes.route -> {
                        val resVM: RestaurantesViewModel = viewModel()
                        RestaurantesScreen(
                            viewModel = resVM,
                            onRestaurantClick = { selectedRestaurantForDetail = it }
                        )
                    }
                    NavItem.Favoritos.route -> {
                        val favVM: FavoritosViewModel = viewModel()
                        FavoritosScreen(viewModel = favVM)
                    }
                    NavItem.Perfil.route -> {
                        // Usamos la lógica de la rama main que es la más actualizada
                        val userRepo = remember { UserRepository() }
                        val profileVM = remember {
                            ProfileViewModel(
                                authRepository = viewModel.authRepository,
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
}