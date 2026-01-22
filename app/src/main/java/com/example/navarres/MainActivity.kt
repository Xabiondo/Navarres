package com.example.navarres

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.navarres.model.repository.AuthRepository
import com.example.navarres.model.repository.UserRepository
import com.example.navarres.view.HomeScreen
import com.example.navarres.view.LoginScreen
import com.example.navarres.view.RegisterScreen
import com.example.navarres.ui.theme.NavarresTheme
import com.example.navarres.viewmodel.HomeViewModel
import com.example.navarres.viewmodel.LoginViewModel
import com.example.navarres.viewmodel.RegisterViewModel
import com.example.navarres.util.FirebaseMigration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

// --- MODELOS PARA LA API ---
data class ApiResponse(val result: ApiResult)
data class ApiResult(val records: List<RestauranteRaw>)
data class RestauranteRaw(
    val COD_INSCRIPCION: String,
    val NOMBRE: String,
    val DIRECCION: String,
    val LOCALIDAD: String,
    val MUNICIPIO: String,
    val Especialidad: String?,
    val CATEGORIA: String
)

interface NavarraApiService {
    @GET("api/3/action/datastore_search?resource_id=0a197290-2e3b-4b0c-b383-4f1ec46d9468&limit=600")
    suspend fun obtenerRestaurantes(): ApiResponse
}

class MainActivity : ComponentActivity() {
    private val authRepository = AuthRepository()
    private val userRepository = UserRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            NavarresTheme {
                var isUserLoggedIn by remember {
                    mutableStateOf(authRepository.getCurrentUser() != null)
                }
                var currentAuthScreen by remember { mutableStateOf("login") }
                val scope = rememberCoroutineScope()
                val context = LocalContext.current

                if (isUserLoggedIn) {
                    val homeViewModel = remember { HomeViewModel(authRepository) }

                    Column(modifier = Modifier.fillMaxSize()) {

                        HomeScreen(
                            viewModel = homeViewModel,
                            onLogoutSuccess = { isUserLoggedIn = false }
                        )
                    }
                } else {
                    // AQUÍ ESTÁ TU LOGIN Y REGISTRO DE VUELTA
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