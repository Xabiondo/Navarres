package com.example.navarres

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.example.navarres.model.repository.AuthRepository
import com.example.navarres.model.repository.UserRepository
import com.example.navarres.view.HomeScreen
import com.example.navarres.view.LoginScreen
import com.example.navarres.view.RegisterScreen
import com.example.navarres.ui.theme.NavarresTheme
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
            NavarresTheme {
                // Estado Global de Navegación: Firebase manda sobre si estamos dentro o fuera
                var isUserLoggedIn by remember {
                    mutableStateOf(authRepository.getCurrentUser() != null)
                }

                if (isUserLoggedIn) {
                    // ----------------------------------------------------
                    // FLUJO HOME (Con el nuevo sistema de pestañas)
                    // ----------------------------------------------------

                    // Usamos remember para que el ViewModel persista durante la sesión
                    val homeViewModel = remember { HomeViewModel(authRepository) }

                    HomeScreen(
                        viewModel = homeViewModel,
                        onLogoutSuccess = {
                            // Al cerrar sesión, volvemos al flujo de Login
                            isUserLoggedIn = false
                        }
                    )

                } else {
                    // ----------------------------------------------------
                    // FLUJO AUTH (Login / Registro)
                    // ----------------------------------------------------

                    var currentAuthScreen by remember { mutableStateOf("login") }

                    when (currentAuthScreen) {
                        "login" -> {
                            val loginViewModel = remember { LoginViewModel(authRepository) }

                            LoginScreen(
                                viewModel = loginViewModel,
                                onNavigateToRegister = { currentAuthScreen = "register" },
                                onLoginSuccess = {
                                    isUserLoggedIn = true
                                }
                            )
                        }
                        "register" -> {
                            val registerViewModel = remember {
                                RegisterViewModel(authRepository, userRepository)
                            }

                            RegisterScreen(
                                viewModel = registerViewModel,
                                onNavigateToLogin = { currentAuthScreen = "login" },
                                onRegisterSuccess = {
                                    isUserLoggedIn = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}