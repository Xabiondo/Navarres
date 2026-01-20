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


    private val authRepository = AuthRepository()
    private val userRepository = UserRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            NavarresTheme {
                // 2. Estado Global de Navegación
                // Comprobamos al inicio si ya hay un usuario logueado en Firebase
                var isUserLoggedIn by remember {
                    mutableStateOf(authRepository.getCurrentUser() != null)
                }

                if (isUserLoggedIn) {
                    // ----------------------------------------------------
                    // FLUJO HOME (Usuario Logueado)
                    // ----------------------------------------------------

                    // Creamos el ViewModel específico de Home
                    val homeViewModel = remember { HomeViewModel(authRepository) }

                    HomeScreen(
                        viewModel = homeViewModel,
                        onLogoutSuccess = {
                            // Cuando el Home avise que cerró sesión, cambiamos el estado
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
                            // Creamos ViewModel de Login con AuthRepo
                            val loginViewModel = remember { LoginViewModel(authRepository) }

                            LoginScreen(
                                viewModel = loginViewModel,
                                onNavigateToRegister = { currentAuthScreen = "register" },
                                onLoginSuccess = {
                                    // Si el login tiene éxito, cambiamos el estado global para ir a Home
                                    isUserLoggedIn = true
                                }
                            )
                        }
                        "register" -> {
                            // Creamos ViewModel de Register con AuthRepo Y UserRepo
                            val registerViewModel = remember {
                                RegisterViewModel(authRepository, userRepository)
                            }

                            RegisterScreen(
                                viewModel = registerViewModel,
                                onNavigateToLogin = { currentAuthScreen = "login" },
                                onRegisterSuccess = {
                                    // Si el registro tiene éxito, vamos directo al Home
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