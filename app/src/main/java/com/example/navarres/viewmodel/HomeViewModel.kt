package com.example.navarres.viewmodel

import androidx.lifecycle.ViewModel
import com.example.navarres.model.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class HomeViewModel(
    val authRepository: AuthRepository
) : ViewModel() {

    private val _currentUserEmail = MutableStateFlow(authRepository.getCurrentUser()?.email ?: "Usuario")
    val currentUserEmail = _currentUserEmail.asStateFlow()

    private val _isLoggedOut = MutableStateFlow(false)
    val isLoggedOut = _isLoggedOut.asStateFlow()

    // Estado para la pestaña actual
    private val _selectedTab = MutableStateFlow("restaurantes")
    val selectedTab = _selectedTab.asStateFlow()

    fun selectTab(route: String) {
        _selectedTab.value = route
    }

    fun logout() {
        authRepository.logout()
        _isLoggedOut.value = true
    }
}