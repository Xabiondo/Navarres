package com.example.navarres.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.navarres.viewmodel.ConfigViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigScreen(viewModel: ConfigViewModel) {
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val fontScale by viewModel.fontScale.collectAsState()
    val currentLanguage by viewModel.currentLanguage.collectAsState()
    val uiStrings by viewModel.uiStrings.collectAsState()

    var expanded by remember { mutableStateOf(false) }

    fun t(key: String, default: String): String = uiStrings[key] ?: default

    val fontOptions = listOf(
        0.85f to t("size_small", "Pequeño"),
        1.0f to t("size_medium", "Mediano"),
        1.15f to t("size_large", "Grande")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = t("nav_config", "Configuración"),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        HorizontalDivider()

        // Tarjeta Modo Oscuro
        Card(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(t("dark_mode", "Modo Oscuro"))
                Switch(
                    checked = isDarkMode,
                    onCheckedChange = { viewModel.toggleDarkMode() }
                )
            }
        }

        // Tarjeta Tamaño Fuente
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = t("font_size", "Tamaño de fuente"),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    val currentLabel = fontOptions.find { it.first == fontScale }?.second ?: t("size_medium", "Mediano")

                    OutlinedTextField(
                        value = currentLabel,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        fontOptions.forEach { (scale, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    viewModel.updateFontScale(scale)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // Selección de Idioma
        Text(text = t("nav_lang", "Idioma"), style = MaterialTheme.typography.titleMedium)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val languages = listOf("es" to "Español", "en" to "English", "eu" to "Euskara")
            languages.forEach { (code, label) ->
                FilterChip(
                    selected = currentLanguage == code,
                    onClick = { viewModel.updateLanguage(code) },
                    label = { Text(label) }
                )
            }
        }
    }
}