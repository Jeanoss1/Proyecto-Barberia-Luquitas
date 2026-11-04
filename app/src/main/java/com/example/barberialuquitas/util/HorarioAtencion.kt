package com.example.barberialuquitas.util

import com.example.barberialuquitas.data.modelo.Barberia
import com.example.barberialuquitas.data.modelo.Barbero
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

// Horario en el que realmente se puede reservar con un barbero: la parte común entre el horario
// de la barbería (RF17) y el del barbero, sin los días no laborables de la barbería.
data class HorarioAtencion(
    val horaInicio: String,
    val horaFin: String,
    val diasLaborables: List<Int>,
    val diasNoLaborables: Set<String> = emptySet()
) {
    fun esFechaReservable(fecha: LocalDate, hoy: LocalDate): Boolean =
        esFechaReservable(fecha, diasLaborables, hoy) && fecha.toString() !in diasNoLaborables

    fun primeraFechaReservable(hoy: LocalDate): LocalDate? =
        (0 until DIAS_A_BUSCAR_FECHA_LIBRE).map { hoy.plusDays(it) }.firstOrNull { esFechaReservable(it, hoy) }
}

fun horarioDeAtencion(barberia: Barberia, barbero: Barbero): HorarioAtencion = HorarioAtencion(
    horaInicio = maxOf(barberia.horaApertura, barbero.horaInicio),
    horaFin = minOf(barberia.horaCierre, barbero.horaFin),
    diasLaborables = barbero.diasLaborables.filter { it in barberia.diasLaborables }.sorted(),
    diasNoLaborables = barberia.diasNoLaborables.toSet()
)

// "Abierto ahora" en la lista de barberías.
fun estaAbierta(barberia: Barberia, ahora: LocalDateTime): Boolean {
    val hoy = ahora.toLocalDate()
    val apertura = parsearHora(barberia.horaApertura) ?: return false
    val cierre = parsearHora(barberia.horaCierre) ?: return false
    val hora = ahora.toLocalTime()
    return barberia.activa &&
        esDiaLaborable(hoy, barberia.diasLaborables) &&
        hoy.toString() !in barberia.diasNoLaborables &&
        !hora.isBefore(apertura) && hora.isBefore(cierre)
}

// Horas que el administrador puede elegir para abrir o cerrar: cada 30 minutos de 06:00 a 23:30.
fun opcionesDeHora(): List<String> =
    (6 * 60..23 * 60 + 30 step DURACION_BLOQUE_MINUTOS).map { LocalTime.of(it / 60, it % 60).toString() }
