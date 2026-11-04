package com.example.barberialuquitas.ui.navegacion

import android.net.Uri

// Nombres de las rutas de navegación. Las que reciben datos tienen una función para armarlas.
object Rutas {
    // ── Autenticación ──
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val SUCCESS = "success"

    // ── Argumentos ──
    const val ARG_PESTANA = "pestana"
    const val ARG_SERVICIO_ID = "servicioId"
    const val ARG_BARBERO_ID = "barberoId"
    const val ARG_FECHA = "fecha"
    const val ARG_HORA = "hora"

    // Valor de ARG_SERVICIO_ID / ARG_BARBERO_ID para abrir el formulario vacío.
    const val NUEVO = "nuevo"

    // ── Cliente ──
    const val HOME_CLIENTE = "home_cliente?$ARG_PESTANA={$ARG_PESTANA}"
    const val EDITAR_DATOS = "editar_datos"
    const val SELECCION_BARBERO = "seleccion_barbero/{$ARG_SERVICIO_ID}"
    const val SELECCION_HORARIO = "seleccion_horario/{$ARG_SERVICIO_ID}/{$ARG_BARBERO_ID}"
    const val CONFIRMACION_RESERVA = "confirmacion_reserva/{$ARG_SERVICIO_ID}/{$ARG_BARBERO_ID}/{$ARG_FECHA}/{$ARG_HORA}"
    const val RESERVA_EXITOSA = "reserva_exitosa/{$ARG_FECHA}/{$ARG_HORA}"

    fun homeCliente(pestana: String? = null) =
        if (pestana == null) "home_cliente" else "home_cliente?$ARG_PESTANA=${Uri.encode(pestana)}"

    fun seleccionBarbero(servicioId: String) = "seleccion_barbero/${Uri.encode(servicioId)}"

    fun seleccionHorario(servicioId: String, barberoId: String) =
        "seleccion_horario/${Uri.encode(servicioId)}/${Uri.encode(barberoId)}"

    fun confirmacionReserva(servicioId: String, barberoId: String, fecha: String, hora: String) =
        "confirmacion_reserva/${Uri.encode(servicioId)}/${Uri.encode(barberoId)}/${Uri.encode(fecha)}/${Uri.encode(hora)}"

    fun reservaExitosa(fecha: String, hora: String) = "reserva_exitosa/${Uri.encode(fecha)}/${Uri.encode(hora)}"

    // ── Administrador ──
    const val HOME_ADMIN = "home_admin"                      // RF12 Dashboard
    const val AGENDA_ADMIN = "agenda_admin"                  // RF15 + RF16
    const val SERVICIOS_ADMIN = "servicios_admin"            // RF13
    const val EDITAR_SERVICIO = "editar_servicio/{$ARG_SERVICIO_ID}"
    const val BARBEROS_ADMIN = "barberos_admin"              // RF14
    const val EDITAR_BARBERO = "editar_barbero/{$ARG_BARBERO_ID}"
    const val HORARIO_ADMIN = "horario_admin"                // RF17
    const val REPORTE_ADMIN = "reporte_admin"                // RF19

    fun editarServicio(servicioId: String = NUEVO) = "editar_servicio/${Uri.encode(servicioId)}"

    fun editarBarbero(barberoId: String = NUEVO) = "editar_barbero/${Uri.encode(barberoId)}"
}
