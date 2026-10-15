package com.example.barberialuquitas.data

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
    val estado: String = EstadosCita.RESERVADA
)
