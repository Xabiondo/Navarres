package com.example.navarres.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BrightnessAuto
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.navarres.viewmodel.AppThemeMode
import com.example.navarres.viewmodel.ConfigViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigScreen(viewModel: ConfigViewModel) {
    // 1. Recogemos el estado del MODO (Enum), no el booleano antiguo
    val currentThemeMode by viewModel.themeMode.collectAsState()
    val fontScale by viewModel.fontScale.collectAsState()
    val currentLanguage by viewModel.currentLanguage.collectAsState()
    val uiStrings by viewModel.uiStrings.collectAsState()

    var expandedFontMenu by remember { mutableStateOf(false) }

    // Función auxiliar para traducir
    fun t(key: String, default: String): String = uiStrings[key] ?: default

    // Opciones de fuente
    val fontOptions = listOf(
        0.85f to t("size_small", "Pequeño"),
        1.0f to t("size_medium", "Mediano"),
        1.15f to t("size_large", "Grande")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = t("nav_config", "Configuración"),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )

        HorizontalDivider()

        // --- SECCIÓN TEMA (NUEVO SISTEMA 3 OPCIONES) ---
        Text(t("section_appearance", "Apariencia"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                // Opción: Sistema
                ThemeSelectionRow(
                    label = t("theme_system", "Automático (Sistema)"),
                    icon = Icons.Outlined.BrightnessAuto,
                    isSelected = currentThemeMode == AppThemeMode.SYSTEM,
                    onClick = { viewModel.setThemeMode(AppThemeMode.SYSTEM) }
                )
                // Opción: Claro
                ThemeSelectionRow(
                    label = t("theme_light", "Modo Claro"),
                    icon = Icons.Outlined.LightMode,
                    isSelected = currentThemeMode == AppThemeMode.LIGHT,
                    onClick = { viewModel.setThemeMode(AppThemeMode.LIGHT) }
                )
                // Opción: Oscuro
                ThemeSelectionRow(
                    label = t("theme_dark", "Modo Oscuro"),
                    icon = Icons.Outlined.DarkMode,
                    isSelected = currentThemeMode == AppThemeMode.DARK,
                    onClick = { viewModel.setThemeMode(AppThemeMode.DARK) }
                )
            }
        }

        // --- SECCIÓN TAMAÑO DE FUENTE ---
        Text(t("font_size", "Tamaño de fuente"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                ExposedDropdownMenuBox(
                    expanded = expandedFontMenu,
                    onExpandedChange = { expandedFontMenu = !expandedFontMenu }
                ) {
                    val currentLabel = fontOptions.find { it.first == fontScale }?.second ?: t("size_medium", "Mediano")

                    OutlinedTextField(
                        value = currentLabel,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedFontMenu) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    ExposedDropdownMenu(
                        expanded = expandedFontMenu,
                        onDismissRequest = { expandedFontMenu = false }
                    ) {
                        fontOptions.forEach { (scale, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    viewModel.updateFontScale(scale)
                                    expandedFontMenu = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // --- SECCIÓN IDIOMA ---
        Text(t("nav_lang", "Idioma"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val languages = listOf("es" to "Español", "en" to "English", "eu" to "Euskara")
            languages.forEach { (code, label) ->
                FilterChip(
                    selected = currentLanguage == code,
                    onClick = { viewModel.updateLanguage(code) },
                    label = { Text(label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }
    }
}

// COMPONENTE AUXILIAR PARA LAS FILAS DEL TEMA (RADIO BUTTONS)
@Composable
fun ThemeSelectionRow(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(16.dp))
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge
        )
        RadioButton(
            selected = isSelected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
        )
    }
}