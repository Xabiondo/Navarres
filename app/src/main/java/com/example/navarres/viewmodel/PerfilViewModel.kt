package com.example.navarres.viewmodel

import androidx.lifecycle.ViewModel
import com.example.navarres.model.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class PerfilViewModel(private val authRepository: AuthRepository) : ViewModel() {
    private val _userEmail = MutableStateFlow(authRepository.getCurrentUser()?.email ?: "Usuario")
    val userEmail = _userEmail.asStateFlow()

    fun logout() {
        authRepository.logout()
    }
}