package com.example.barberialuquitas.util

import android.util.Patterns

fun esCorreoValido(correo: String): Boolean =
    Patterns.EMAIL_ADDRESS.matcher(correo).matches()

fun esPasswordValida(password: String): Boolean =
    password.length >= 6

fun soloLetras(texto: String): String =
    texto.filter { it.isLetter() || it.isWhitespace() }

fun soloDigitos(texto: String): String =
    texto.filter { it.isDigit() }
