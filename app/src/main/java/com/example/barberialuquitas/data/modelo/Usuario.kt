package com.example.barberialuquitas.data.modelo

data class Usuario(
    val uid: String = "",
    val nombre: String = "",
    val correo: String = "",
    val telefono: String = "",
    val rol: String = Roles.CLIENTE
)
