package com.example.barberialuquitas.data

import com.example.barberialuquitas.util.DURACION_BLOQUE_MINUTOS

data class Cita(
    val id: String = "",
    val clienteId: String = "",
    val clienteNombre: String = "",
    val servicioId: String = "",
    val servicioNombre: String = "",
    val precio: Long = 0,
    val barberoId: String = "",
    val barberoNombre: String = "",
    val fecha: String = "",
    val hora: String = "",
    val duracionMinutos: Int = DURACION_BLOQUE_MINUTOS,
    val bloques: List<String> = listOf(hora),
    val estado: String = EstadosCita.RESERVADA
)
