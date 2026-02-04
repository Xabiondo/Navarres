package com.example.navarres.view

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AppRegistration
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.navarres.viewmodel.RegisterViewModel

@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel,
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            onRegisterSuccess()
            viewModel.resetState()
        }
    }

    RegisterContent(
        email = state.email,
        password = state.password,
        isLoading = state.isLoading,
        error = state.error,
        // Pasamos los nuevos estados a la UI
        passwordStrength = state.passwordStrength,
        passwordFeedback = state.passwordFeedback,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onRegisterClick = viewModel::register,
        onNavigateToLogin = onNavigateToLogin
    )
}

@Composable
fun RegisterContent(
    email: String,
    password: String,
    isLoading: Boolean,
    error: String?,
    passwordStrength: Float,   // Nuevo
    passwordFeedback: String,  // Nuevo
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onRegisterClick: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    // Calculamos el color de la barra dinámicamente
    val strengthColor by animateColorAsState(
        targetValue = when {
            passwordStrength < 0.3f -> Color.Red
            passwordStrength < 0.7f -> Color(0xFFFFA000) // Naranja/Amarillo
            else -> Color(0xFF2E7D32) // Verde Navarres
        },
        label = "colorAnimation"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFDFCF0)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.AppRegistration,
                contentDescription = null,
                modifier = Modifier.size(70.dp),
                tint = Color(0xFF2E7D32)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Únete a NavarRes",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFFB30000)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {

                    OutlinedTextField(
                        value = email,
                        onValueChange = onEmailChange,
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF2E7D32),
                            focusedLabelColor = Color(0xFF2E7D32)
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = onPasswordChange,
                        label = { Text("Contraseña") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = strengthColor, // El borde también cambia de color
                            focusedLabelColor = strengthColor
                        ),
                        singleLine = true
                    )

                    // --- INICIO NUEVA BARRA DE PROGRESO ---
                    if (password.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))

                        // Barra de progreso
                        LinearProgressIndicator(
                            progress = { passwordStrength },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp),
                            color = strengthColor,
                            trackColor = Color.LightGray.copy(alpha = 0.3f),
                        )

                        // Texto de feedback (qué falta)
                        Text(
                            text = passwordFeedback,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (passwordStrength == 1f) Color(0xFF2E7D32) else Color.Gray,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    // --- FIN NUEVA BARRA DE PROGRESO ---

                    if (error != null) {
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = onRegisterClick,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        shape = RoundedCornerShape(12.dp),
                        // Desactivamos el botón si no es segura (o si está cargando)
                        enabled = !isLoading && passwordStrength == 1f
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("REGISTRARSE", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = onNavigateToLogin) {
                Text("¿Ya eres miembro? Inicia sesión", color = Color.DarkGray)
            }
        }
    }
}