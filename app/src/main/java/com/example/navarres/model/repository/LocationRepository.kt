package com.example.navarres.model.repository

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.tasks.await

class LocationRepository(private val context: Context) {

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    /**
     * Obtiene la última ubicación conocida.
     * Se asume que los permisos ya han sido verificados en la UI antes de llamar a esta función.
     */
    @SuppressLint("MissingPermission")
    suspend fun getUserLocation(): Location? {
        return try {
            // await() convierte la tarea asíncrona de Google Play Services en una corrutina suspendida
            fusedLocationClient.lastLocation.await()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}