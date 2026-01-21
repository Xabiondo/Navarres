package com.example.navarres.view
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.navarres.viewmodel.FavoritosViewModel

@Composable
fun FavoritosScreen(viewModel: FavoritosViewModel) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Tus sitios favoritos")
    }
}