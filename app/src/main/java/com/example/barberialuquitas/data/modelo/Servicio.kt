package com.example.barberialuquitas.data.modelo

import com.example.barberialuquitas.util.DURACION_BLOQUE_MINUTOS

data class Servicio(
    val id: String = "",
    val barberiaId: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val precio: Long = 0,
    val duracionMinutos: Int = DURACION_BLOQUE_MINUTOS,
    val fotoUrl: String? = null,
    val activo: Boolean = true
)
