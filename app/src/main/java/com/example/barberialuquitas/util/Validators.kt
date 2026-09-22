package com.example.barberialuquitas.util

import android.util.Patterns

fun esCorreoValido(correo: String): Boolean =
    Patterns.EMAIL_ADDRESS.matcher(correo).matches()

fun esPasswordValida(password: String): Boolean =
    password.length >= 6
