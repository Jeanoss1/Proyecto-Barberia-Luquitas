package com.example.barberialuquitas.data.modelo

data class Barbero(
    val id: String = "",
    val barberiaId: String = "",
    val nombre: String = "",
    val especialidad: String = "",
    val fotoUrl: String? = null,
    val activo: Boolean = false,
    val horaInicio: String = "09:00",
    val horaFin: String = "20:00",
    // 1 = lunes ... 7 = domingo
    val diasLaborables: List<Int> = emptyList()
)
