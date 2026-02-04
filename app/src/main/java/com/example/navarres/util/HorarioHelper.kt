package com.example.navarres.util

import android.util.Log
import com.example.navarres.model.data.OpenStatus
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object HorarioHelper {

    fun getStatus(horarios: Map<String, String>): OpenStatus {
        val hoyKey = getDiaActualKey()
        val horarioRaw = horarios[hoyKey]



        if (horarioRaw.isNullOrBlank()) return OpenStatus.UNKNOWN


        val texto = horarioRaw.lowercase().trim()
            .replace(".", "")
            .replace(Regex("\\s+"), "")
            .replace("p.m", "pm")
            .replace("a.m", "am")


        if (texto.contains("cerrado") || texto.contains("closed")) return OpenStatus.CLOSED
        if (texto.contains("abierto") || texto.contains("24h")) return OpenStatus.OPEN

        try {
            // 2. Separar turnos (por coma)
            // Ej: "11am-5pm,8pm-1am"
            val turnos = texto.split(",")
            val ahora = LocalTime.now()

            for (turno in turnos) {
                if (turno.isNotBlank() && isCurrentTimeInTurn(turno, ahora)) {
                    return OpenStatus.OPEN
                }
            }

            // Si pasamos todos los turnos y no encajamos en ninguno -> CERRADO
            return OpenStatus.CLOSED

        } catch (e: Exception) {
            Log.e("HorarioHelper", "Error parseando: $horarioRaw", e)
            return OpenStatus.UNKNOWN
        }
    }

    private fun isCurrentTimeInTurn(rango: String, ahora: LocalTime): Boolean {
        // 3. Separar rango usando CUALQUIER tipo de guion
        val partes = rango.split(Regex("[-–—]"))

        if (partes.size != 2) return false

        val rawInicio = partes[0]
        val rawFin = partes[1]

        // 4. Inteligencia de sufijos (para casos como "1-5pm")
        // Si el inicio no tiene am/pm, le prestamos el del final
        val suffixFin = if (rawFin.contains("pm")) "pm" else if (rawFin.contains("am")) "am" else ""
        val suffixInicio = if (rawInicio.contains("pm")) "pm" else if (rawInicio.contains("am")) "am" else ""

        // Si inicio no tiene sufijo, usamos el del final
        val contextoInicio = if(suffixInicio.isEmpty()) suffixFin else ""

        val inicio = parseTime(rawInicio, contextoInicio) ?: return false
        val fin = parseTime(rawFin, "") ?: return false

        // Lógica de cruce de horas
        return if (inicio.isBefore(fin)) {
            // Horario normal (ej: 13:00 a 17:00)
            ahora.isAfter(inicio) && ahora.isBefore(fin)
        } else {
            // Horario nocturno que cruza medianoche (ej: 20:00 a 02:00)
            // Es abierto si es > 20:00  O BIEN  < 02:00
            ahora.isAfter(inicio) || ahora.isBefore(fin)
        }
    }

    private fun parseTime(raw: String, suffixContext: String): LocalTime? {
        try {
            var horaStr = raw

            // Detectar si es PM:
            // 1. Si pone "pm" explícito
            // 2. O si no pone nada, pero el contexto dice "pm" (ej: "1" en "1-5pm")
            val isPm = horaStr.contains("pm") || (suffixContext == "pm" && !horaStr.contains("am"))
            val isAm = horaStr.contains("am") || (suffixContext == "am" && !horaStr.contains("pm"))

            // Quitamos letras para dejar solo números y dos puntos
            horaStr = horaStr.replace(Regex("[a-z]"), "")

            var h = 0
            var m = 0

            if (horaStr.contains(":")) {
                val split = horaStr.split(":")
                h = split[0].toInt()
                m = split[1].toInt()
            } else {
                h = horaStr.toInt()
            }

            // Conversión a 24h
            if (isPm && h < 12) h += 12
            if (isAm && h == 12) h = 0 // 12 am es medianoche (00:00)

            return LocalTime.of(h, m)
        } catch (e: Exception) {
            return null
        }
    }

    private fun getDiaActualKey(): String {
        // Mapeo manual para asegurar que coincide con tus claves de Firestore
        return when (LocalDate.now().dayOfWeek) {
            DayOfWeek.MONDAY -> "lunes"
            DayOfWeek.TUESDAY -> "martes"
            DayOfWeek.WEDNESDAY -> "miercoles"
            DayOfWeek.THURSDAY -> "jueves"
            DayOfWeek.FRIDAY -> "viernes"
            DayOfWeek.SATURDAY -> "sabado"
            DayOfWeek.SUNDAY -> "domingo"
            else -> "lunes"
        }
    }
}