package com.example.barberialuquitas.navigation

import android.net.Uri

object Rutas {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val SUCCESS = "success"
    const val HOME_ADMIN = "home_admin"
    const val AGENDA_ADMIN = "agenda_admin"

    const val ARG_PESTANA = "pestana"
    const val ARG_SERVICIO_ID = "servicioId"
    const val ARG_BARBERO_ID = "barberoId"
    const val ARG_FECHA = "fecha"
    const val ARG_HORA = "hora"

    const val HOME_CLIENTE = "home_cliente?$ARG_PESTANA={$ARG_PESTANA}"
    const val SELECCION_BARBERO = "seleccion_barbero/{$ARG_SERVICIO_ID}"
    const val SELECCION_HORARIO = "seleccion_horario/{$ARG_SERVICIO_ID}/{$ARG_BARBERO_ID}"
    const val RESERVA_EXITOSA = "reserva_exitosa/{$ARG_FECHA}/{$ARG_HORA}"
    const val CONFIRMACION_RESERVA = "confirmacion_reserva/{$ARG_SERVICIO_ID}/{$ARG_BARBERO_ID}/{$ARG_FECHA}/{$ARG_HORA}"

    fun homeCliente(pestana: String? = null) =
        if (pestana == null) "home_cliente" else "home_cliente?$ARG_PESTANA=${Uri.encode(pestana)}"

    fun seleccionBarbero(servicioId: String) = "seleccion_barbero/${Uri.encode(servicioId)}"

    fun seleccionHorario(servicioId: String, barberoId: String) =
        "seleccion_horario/${Uri.encode(servicioId)}/${Uri.encode(barberoId)}"

    fun confirmacionReserva(servicioId: String, barberoId: String, fecha: String, hora: String) =
        "confirmacion_reserva/${Uri.encode(servicioId)}/${Uri.encode(barberoId)}/${Uri.encode(fecha)}/${Uri.encode(hora)}"

    fun reservaExitosa(fecha: String, hora: String) = "reserva_exitosa/${Uri.encode(fecha)}/${Uri.encode(hora)}"
}
