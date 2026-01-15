package com.example.navarres

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import com.example.navarres.model.repository.AuthRepository
import com.example.navarres.ui.theme.NavarresTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        val repo = AuthRepository()


        lifecycleScope.launch {
            println("--- INICIANDO PRUEBA DE FIREBASE ---")

            // 1. Intentamos registrar un usuario inventado
            val resultado = repo.register("prueba1@ejemplo.com", "123456")

            if (resultado.isSuccess) {
                println("✅ ÉXITO TOTAL: Usuario creado con ID: ${resultado.getOrNull()?.uid}")
            } else {
                println("❌ FALLO: ${resultado.exceptionOrNull()?.message}")
                // Si falla porque ya existe, probamos el login
                println("--- Intentando Login en su lugar ---")
                val login = repo.login("prueba1@ejemplo.com", "123456")
                if (login.isSuccess) println("✅ LOGIN OK") else println("❌ LOGIN FALLÓ TAMBIÉN")
            }
        }
        // ---------------------------------------

        enableEdgeToEdge()
        setContent {
            // ... tu código de Compose ...
        }
    }
}