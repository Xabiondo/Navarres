package com.example.navarres.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.navarres.viewmodel.ConfigViewModel

@Composable
fun ConfigScreen(viewModel: ConfigViewModel) {
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val fontScale by viewModel.fontScale.collectAsState()
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val strings by viewModel.uiStrings.collectAsState() // Observa los textos

    var showLanguageMenu by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = strings["title"] ?: "Configuración",
            style = MaterialTheme.typography.headlineMedium
        )

        HorizontalDivider()

        // --- APARIENCIA ---
        Text(
            text = (strings["appearance"] ?: "Apariencia").uppercase(),
            style = MaterialTheme.typography.labelLarge,
            color = Color.Gray
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(strings["dark_mode"] ?: "Modo Oscuro")
            Switch(
                checked = isDarkMode,
                onCheckedChange = { viewModel.toggleDarkMode(it) }
            )
        }

        Column {
            Text("${strings["font_size"] ?: "Tamaño"}: ${(fontScale * 100).toInt()}%")
            Slider(
                value = fontScale,
                onValueChange = { viewModel.updateFontScale(it) },
                valueRange = 0.8f..1.5f,
                steps = 5
            )
        }

        // --- IDIOMA ---
        Text(
            text = (strings["language"] ?: "Idioma").uppercase(),
            style = MaterialTheme.typography.labelLarge,
            color = Color.Gray
        )

        Box {
            OutlinedCard(
                modifier = Modifier.fillMaxWidth().clickable { showLanguageMenu = true }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(selectedLanguage)
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
            }
            DropdownMenu(
                expanded = showLanguageMenu,
                onDismissRequest = { showLanguageMenu = false }
            ) {
                listOf("Español", "English", "Euskara").forEach { lang ->
                    DropdownMenuItem(
                        text = { Text(lang) },
                        onClick = {
                            viewModel.updateLanguage(lang)
                            showLanguageMenu = false
                        }
                    )
                }
            }
        }
    }
}