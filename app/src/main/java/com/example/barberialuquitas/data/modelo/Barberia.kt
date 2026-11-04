package com.example.barberialuquitas.data.modelo

// Una barbería registrada en la app. Su horario (RF17) limita el de todos sus barberos.
data class Barberia(
    val id: String = "",
    val nombre: String = "",
    val direccion: String = "",
    val telefono: String = "",
    val fotoUrl: String? = null,
    val activa: Boolean = true,
    val horaApertura: String = "09:00",
    val horaCierre: String = "20:00",
    // 1 = lunes ... 7 = domingo
    val diasLaborables: List<Int> = listOf(1, 2, 3, 4, 5, 6),
    // Fechas "yyyy-MM-dd" en las que no se atiende (feriados, vacaciones).
    val diasNoLaborables: List<String> = emptyList(),
    // Posición en la lista que ve el cliente: menor número aparece primero.
    val orden: Int = ORDEN_POR_DEFECTO,
    // true cuando ya adoptó los datos antiguos sin barbería (solo importa para la barbería original).
    val migrada: Boolean = true
)

const val ORDEN_POR_DEFECTO = 100
