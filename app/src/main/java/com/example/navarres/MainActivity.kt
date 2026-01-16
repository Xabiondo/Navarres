package com.example.navarres

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.navarres.ui.screens.HomeScreen
import com.example.navarres.ui.screens.LoginScreen
import com.example.navarres.ui.screens.RegisterScreen
import com.example.navarres.ui.theme.NavarresTheme
import com.example.navarres.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {

    // 1. ELIMINA las líneas de "private val Any.currentUser..." que tenías fuera.
    // 2. DECLARA el ViewModel aquí para que sea accesible en toda la clase.
    private lateinit var authViewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 3. INICIALIZA el ViewModel (él ya crea su propio repositorio internamente)
        authViewModel = AuthViewModel()

        enableEdgeToEdge()
        setContent {
            NavarresTheme {
                // Estado para controlar si estamos en login o registro
                var currentScreen by remember { mutableStateOf("login") }

                // Comprobamos si hay un usuario logueado en el ViewModel
                if (authViewModel.currentUser != null) {
                    // Pantalla de inicio centrada y bonita que hicimos
                    HomeScreen(viewModel = authViewModel)
                } else {
                    // Navegación simple entre Login y Register
                    when (currentScreen) {
                        "login" -> LoginScreen(
                            viewModel = authViewModel,
                            onNavigateToRegister = {
                                authViewModel.authError = null
                                currentScreen = "register"
                            }
                        )
                        "register" -> RegisterScreen(
                            viewModel = authViewModel,
                            onNavigateToLogin = {
                                authViewModel.authError = null
                                currentScreen = "login"
                            }
                        )
                    }
                }
            }
        }
    }
}