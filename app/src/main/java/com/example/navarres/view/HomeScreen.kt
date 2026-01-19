package com.example.navarres.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.navarres.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onLogoutSuccess: () -> Unit
) {
    val email by viewModel.currentUserEmail.collectAsState()
    val isLoggedOut by viewModel.isLoggedOut.collectAsState()

    LaunchedEffect(isLoggedOut) {
        if (isLoggedOut) {
            onLogoutSuccess()
        }
    }

    HomeContent(
        userEmail = email,
        onLogoutClick = viewModel::logout
    )
}

@Composable
fun HomeContent(
    userEmail: String,
    onLogoutClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFDFCF0)),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.Restaurant, contentDescription = null, modifier = Modifier.size(100.dp), tint = Color(0xFF2E7D32))
        Spacer(modifier = Modifier.height(24.dp))
        Text("¡Buen provecho!", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = Color(0xFFB30000))
        Text("Explora los sabores del Reyno", style = MaterialTheme.typography.bodyLarge, color = Color.DarkGray)
        Spacer(modifier = Modifier.height(16.dp))

        Text("Conectado como: $userEmail", style = MaterialTheme.typography.bodySmall, color = Color.Gray)

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onLogoutClick,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Color(0xFFB30000)),
            modifier = Modifier.padding(horizontal = 32.dp).height(50.dp).fillMaxWidth(0.7f),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color(0xFFB30000))
        ) {
            Icon(Icons.Default.ExitToApp, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("ABANDONAR LA MESA", fontWeight = FontWeight.Bold)
        }
    }
}