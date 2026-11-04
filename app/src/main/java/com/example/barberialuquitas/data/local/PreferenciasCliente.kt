package com.example.barberialuquitas.data.local

import android.content.Context
import androidx.core.content.edit

// Recuerda en el teléfono la barbería que eligió el cliente (archivo de preferencias, semana 5).
class PreferenciasCliente(context: Context) {
    private val preferencias = context.getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE)

    var barberiaSeleccionada: String?
        get() = preferencias.getString(CLAVE_BARBERIA, null)
        set(valor) = preferencias.edit { putString(CLAVE_BARBERIA, valor) }

    private companion object {
        const val ARCHIVO = "preferencias_cliente"
        const val CLAVE_BARBERIA = "barberia_id"
    }
}
