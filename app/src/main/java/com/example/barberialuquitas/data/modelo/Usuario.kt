package com.example.barberialuquitas.data.modelo

data class Usuario(
    val uid: String = "",
    val nombre: String = "",
    val correo: String = "",
    val telefono: String = "",
    val rol: String = Roles.CLIENTE,
    // Solo para administradores: la barbería que gestionan. Vacío si aún no registran una.
    val barberiaId: String = ""
)
