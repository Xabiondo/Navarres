package com.example.navarres

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.navarres.model.repository.AuthRepository
import com.example.navarres.model.repository.UserRepository
import com.example.navarres.view.HomeScreen
import com.example.navarres.view.LoginScreen
import com.example.navarres.view.RegisterScreen
import com.example.navarres.ui.theme.NavarresTheme
import com.example.navarres.viewmodel.ConfigViewModel
import com.example.navarres.viewmodel.HomeViewModel
import com.example.navarres.viewmodel.LoginViewModel
import com.example.navarres.viewmodel.RegisterViewModel

class MainActivity : ComponentActivity() {

    // Repositorios compartidos
    private val authRepository = AuthRepository()
    private val userRepository = UserRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            // 1. Instanciamos el ConfigViewModel aquí para que persista en toda la App
            val configViewModel: ConfigViewModel = viewModel()
            val isDarkMode by configViewModel.isDarkMode.collectAsState()

            // 2. Pasamos el estado del modo oscuro al Tema
            NavarresTheme(darkTheme = isDarkMode) {

                // Estado Global de Navegación
                var isUserLoggedIn by remember {
                    mutableStateOf(authRepository.getCurrentUser() != null)
                }

                if (isUserLoggedIn) {
                    // FLUJO HOME
                    val homeViewModel = remember { HomeViewModel(authRepository) }

                    HomeScreen(
                        viewModel = homeViewModel,
                        onLogoutSuccess = {
                            isUserLoggedIn = false
                        }
                    )

                } else {
                    // FLUJO AUTH (Login / Registro)
                    var currentAuthScreen by remember { mutableStateOf("login") }

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
                            val registerViewModel = remember {
                                RegisterViewModel(authRepository, userRepository)
                            }
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