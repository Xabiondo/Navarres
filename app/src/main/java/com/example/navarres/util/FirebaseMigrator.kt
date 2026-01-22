package com.example.navarres.util

import android.content.Context
import android.location.Geocoder
import android.util.Log
import com.example.navarres.model.RestauranteRaw
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import java.util.Locale

object FirebaseMigration {

    suspend fun ejecutarMigracionUnica(context: Context, records: List<RestauranteRaw>) {
        val db = FirebaseFirestore.getInstance()
        val geocoder = Geocoder(context, Locale.getDefault())

        records.forEachIndexed { index, rest ->
            // Geocoding
            val address = try {
                geocoder.getFromLocationName("${rest.DIRECCION}, ${rest.LOCALIDAD}, Navarra, España", 1)
            } catch (e: Exception) { null }

            val lat = address?.getOrNull(0)?.latitude ?: 0.0
            val lng = address?.getOrNull(0)?.longitude ?: 0.0

            // Mapeo limpio
            val restauranteMap = hashMapOf(
                "nombre" to rest.NOMBRE,
                "direccion" to rest.DIRECCION,
                "localidad" to rest.LOCALIDAD,
                "municipio" to rest.MUNICIPIO,
                "categoria" to rest.CATEGORIA,
                "especialidades" to (rest.Especialidad?.split(",")?.map { it.trim() } ?: listOf<String>()),
                "latitud" to lat,
                "longitud" to lng
            )

            // Subida con COD_INSCRIPCION como ID
            db.collection("restaurantes").document(rest.COD_INSCRIPCION).set(restauranteMap)

            if (index % 10 == 0) {
                Log.d("MIGRACION", "Procesado $index de ${records.size}: ${rest.NOMBRE}")
                delay(200)
            }
        }
    }
}