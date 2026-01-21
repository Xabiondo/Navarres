package com.example.navarres.view

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.navarres.viewmodel.ConfigViewModel

@Composable
fun ConfigScreen(viewModel: ConfigViewModel) {
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val fontScale by viewModel.fontScale.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "Configuración",
            style = MaterialTheme.typography.headlineMedium
        )

        HorizontalDivider()

        // Control de Modo Oscuro
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Modo Oscuro")
            Switch(
                checked = isDarkMode,
                onCheckedChange = { viewModel.toggleDarkMode(it) }
            )
        }

        // Control de Tamaño de Fuente
        Column {
            Text("Tamaño de fuente: ${(fontScale * 100).toInt()}%")
            Slider(
                value = fontScale,
                onValueChange = { viewModel.updateFontScale(it) },
                valueRange = 0.8f..1.5f,
                steps = 5
            )
        }
    }
}