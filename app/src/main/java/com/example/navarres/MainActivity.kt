package com.example.navarres

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.navarres.model.repository.AuthRepository
import com.example.navarres.model.repository.UserRepository
// Make sure this import is present
import com.example.navarres.view.HomeScreen
import com.example.navarres.view.LoginScreen
import com.example.navarres.view.RegisterScreen
import com.example.navarres.ui.theme.NavarresTheme
import com.example.navarres.viewmodel.ConfigViewModel
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
            // 1. Obtenemos la configuración global
            val configViewModel: ConfigViewModel = viewModel()
            val isDarkMode by configViewModel.isDarkMode.collectAsState()
            val fontScale by configViewModel.fontScale.collectAsState()

            // 2. Usamos NavarresTheme como ÚNICO proveedor de estilo.
            // Nota: He quitado el MaterialTheme anidado porque causaba conflictos de colores.
            NavarresTheme(
                darkTheme = isDarkMode,
                fontScale = fontScale // Asegúrate de que tu Theme.kt reciba esto
            ) {
                var isUserLoggedIn by remember {
                    mutableStateOf(authRepository.getCurrentUser() != null)
                }

                if (isUserLoggedIn) {
                    val homeViewModel = remember { HomeViewModel(authRepository) }
                    HomeScreen(
                        viewModel = homeViewModel,
                        configViewModel = configViewModel,
                        onLogoutSuccess = { isUserLoggedIn = false }
                    )
                } else {
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
