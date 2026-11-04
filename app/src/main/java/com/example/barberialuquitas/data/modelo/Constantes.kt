package com.example.barberialuquitas.data.modelo

object Colecciones {
    const val USUARIOS = "usuarios"
    const val BARBERIAS = "barberias"
    const val SERVICIOS = "servicios"
    const val BARBEROS = "barberos"
    const val CITAS = "citas"
    const val HORARIOS_OCUPADOS = "horarios_ocupados"
}

// Id de la barbería a la que pertenecen los datos creados antes de soportar varias barberías.
// Debe coincidir con barberiaOriginal() en firestore.rules.
const val ID_BARBERIA_ORIGINAL = "luquitas"

object EstadosCita {
    const val RESERVADA = "reservada"
    const val ATENDIDA = "atendida"
    const val NO_ASISTIO = "no_asistio"
    const val CANCELADA = "cancelada"

    // Estado de la segunda entrega: la app la marcaba sola al terminar el servicio. Se trata como atendida.
    const val COMPLETADA = "completada"

    // Solo para mostrar: cita reservada que ya terminó y el administrador aún no marca (no se guarda).
    const val POR_CONFIRMAR = "por_confirmar"

    fun esAtendida(estado: String) = estado == ATENDIDA || estado == COMPLETADA

    fun etiqueta(estado: String): String = when (estado) {
        RESERVADA -> "Reservada"
        ATENDIDA, COMPLETADA -> "Atendida"
        NO_ASISTIO -> "No asistió"
        CANCELADA -> "Cancelada"
        POR_CONFIRMAR -> "Por confirmar"
        else -> estado.replaceFirstChar { it.uppercase() }
    }
}

object Roles {
    const val ADMINISTRADOR = "administrador"
    const val CLIENTE = "cliente"
}
