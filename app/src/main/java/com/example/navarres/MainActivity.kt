package com.example.navarres

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.navarres.model.repository.AuthRepository
import com.example.navarres.model.repository.UserRepository
import com.example.navarres.view.HomeScreen
import com.example.navarres.view.LoginScreen
import com.example.navarres.view.RegisterScreen
import com.example.navarres.ui.theme.NavarresTheme
import com.example.navarres.viewmodel.ConfigViewModel
import com.example.navarres.viewmodel.AppThemeMode // Importa el Enum
import com.example.navarres.viewmodel.HomeViewModel
import com.example.navarres.viewmodel.LoginViewModel
import com.example.navarres.viewmodel.RegisterViewModel

class MainActivity : ComponentActivity() {

    private val authRepository = AuthRepository()
    private val userRepository = UserRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val configViewModel: ConfigViewModel = viewModel()

            // 1. Observamos qué modo ha elegido el usuario (Sistema, Claro u Oscuro)
            val currentMode by configViewModel.themeMode.collectAsState()
            val fontScale by configViewModel.fontScale.collectAsState()

            // 2. Detectamos cómo está el móvil ahora mismo
            val systemIsDark = isSystemInDarkTheme()

            // 3. CALCULADORA DE TEMA: Esta es la lógica "Instagram"
            val useDarkTheme = when (currentMode) {
                AppThemeMode.LIGHT -> false           // Forzar Claro
                AppThemeMode.DARK -> true             // Forzar Oscuro
                AppThemeMode.SYSTEM -> systemIsDark   // Obedecer al móvil
            }

            // 4. Aplicamos el resultado
            NavarresTheme(
                darkTheme = useDarkTheme, // Pasamos el resultado calculado
                fontScale = fontScale
            ) {
                var isUserLoggedIn by remember {
                    mutableStateOf(authRepository.getCurrentUser() != null)
                }

                if (isUserLoggedIn) {
                    val homeViewModel = remember { HomeViewModel(authRepository) }
                    HomeScreen(
                        viewModel = homeViewModel,
                        configViewModel = configViewModel, // Pasamos el VM para poder cambiar el modo
                        onLogoutSuccess = { isUserLoggedIn = false }
                    )
                } else {
                    // ... (Tu lógica de login/registro sigue igual) ...
                    var currentAuthScreen by remember { mutableStateOf("login") }
                    // ... (Mismo código de siempre aquí dentro)
                    when (currentAuthScreen) {
                        "login" -> {
                            val loginViewModel = remember { LoginViewModel(authRepository) }
                            LoginScreen(
                                viewModel = loginViewModel,
                                onNavigateToRegister = { currentAuthScreen = "register" },
                                onLoginSuccess = { isUserLoggedIn = true }
                            )
                        }
                        "register" -> {
                            val registerViewModel = remember { RegisterViewModel(authRepository, userRepository) }
                            RegisterScreen(
                                viewModel = registerViewModel,
                                onNavigateToLogin = { currentAuthScreen = "login" },
                                onRegisterSuccess = { isUserLoggedIn = true }
                            )
                        }
                    }
                }
            }
        }
    }
}