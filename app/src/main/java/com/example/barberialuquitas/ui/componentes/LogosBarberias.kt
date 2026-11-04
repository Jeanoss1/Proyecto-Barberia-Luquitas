package com.example.barberialuquitas.ui.componentes

import androidx.annotation.DrawableRes
import com.example.barberialuquitas.R
import com.example.barberialuquitas.data.modelo.Barberia

// Logos de barberías que vienen dentro de la app (res/drawable-nodpi). Se usan cuando la barbería
// no tiene "fotoUrl". Para agregar otro: copia el PNG a drawable-nodpi y añade una línea aquí.
// (logo_barberchota es el logo de la app, no de una barbería: se usa en login y registro).
@DrawableRes
fun logoLocalDe(barberia: Barberia): Int? {
    val clave = (barberia.id + barberia.nombre).lowercase().filter { it.isLetter() }
    return when {
        "luquitas" in clave || "lukitas" in clave -> R.drawable.logo_barberia
        else -> null
    }
}
