package com.example.navarres.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.navarres.viewmodel.PerfilViewModel

@Composable
fun PerfilScreen(viewModel: PerfilViewModel, onLogoutClick: () -> Unit) {
    val email by viewModel.userEmail.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.Restaurant, null, Modifier.size(100.dp), Color(0xFF2E7D32))
        Spacer(Modifier.height(24.dp))
        Text("¡Buen provecho!", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = Color(0xFFB30000))
        Text("Conectado como: $email", style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
        Spacer(Modifier.height(48.dp))
        Button(
            onClick = {
                viewModel.logout()
                onLogoutClick()
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Color(0xFFB30000)),
            modifier = Modifier.fillMaxWidth(0.7f).height(50.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color(0xFFB30000))
        ) {
            Icon(Icons.Default.ExitToApp, null)
            Spacer(Modifier.width(8.dp))
            Text("CERRAR SESIÓN", fontWeight = FontWeight.Bold)
        }
    }
}