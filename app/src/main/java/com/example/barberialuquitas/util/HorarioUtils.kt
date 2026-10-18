package com.example.barberialuquitas.util

import com.example.barberialuquitas.data.Cita
import com.example.barberialuquitas.data.EstadosCita
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

const val DURACION_BLOQUE_MINUTOS = 30

// Debe coincidir con el máximo que aceptan las reglas de Firestore (4 bloques = 2 horas).
const val MAX_BLOQUES_POR_CITA = 4

private const val DIAS_A_BUSCAR_FECHA_LIBRE = 31L

val LOCALE_ES: Locale = Locale.forLanguageTag("es-PE")

private val FORMATO_HORA: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
private val FORMATO_FECHA_LEGIBLE: DateTimeFormatter = DateTimeFormatter.ofPattern("EEEE d 'de' MMMM, yyyy", LOCALE_ES)

data class BloqueHorario(val hora: String, val disponible: Boolean)

data class CitasSeparadas(val proximas: List<Cita>, val historial: List<Cita>)

fun bloquesNecesarios(duracionMinutos: Int): Int =
    ((duracionMinutos + DURACION_BLOQUE_MINUTOS - 1) / DURACION_BLOQUE_MINUTOS).coerceIn(1, MAX_BLOQUES_POR_CITA)

// Un inicio solo está disponible si todos los bloques que ocupa el servicio están libres y caben antes del cierre.
fun calcularBloques(
    horaInicio: String,
    horaFin: String,
    horasOcupadas: Set<String>,
    fecha: LocalDate,
    ahora: LocalDateTime,
    bloquesPorCita: Int = 1
): List<BloqueHorario> {
    val horas = generarHorasDelDia(horaInicio, horaFin).map { it.format(FORMATO_HORA) }
    return horas.mapIndexed { indice, texto ->
        val bloquesDeLaCita = horas.drop(indice).take(bloquesPorCita)
        BloqueHorario(
            hora = texto,
            disponible = bloquesDeLaCita.size == bloquesPorCita &&
                bloquesDeLaCita.none { it in horasOcupadas } &&
                esFechaHoraFutura(fecha.toString(), texto, ahora)
        )
    }
}

fun bloquesDesde(hora: String, cantidad: Int): List<String> {
    val inicio = parsearHora(hora) ?: return emptyList()
    return (0 until cantidad).map { inicio.plusMinutes(it.toLong() * DURACION_BLOQUE_MINUTOS).format(FORMATO_HORA) }
}

fun horaDeTermino(hora: String, duracionMinutos: Int): String =
    parsearHora(hora)?.plusMinutes(duracionMinutos.toLong())?.format(FORMATO_HORA) ?: hora

fun generarHorasDelDia(horaInicio: String, horaFin: String): List<LocalTime> {
    val inicio = parsearHora(horaInicio) ?: return emptyList()
    val fin = parsearHora(horaFin) ?: return emptyList()
    val inicioEnMinutos = inicio.toSecondOfDay() / 60
    val ultimoInicioPosible = fin.toSecondOfDay() / 60 - DURACION_BLOQUE_MINUTOS
    return (inicioEnMinutos..ultimoInicioPosible step DURACION_BLOQUE_MINUTOS)
        .map { minutos -> LocalTime.of(minutos / 60, minutos % 60) }
}

fun esDiaLaborable(fecha: LocalDate, diasLaborables: List<Int>): Boolean =
    fecha.dayOfWeek.value in diasLaborables

fun esFechaReservable(fecha: LocalDate, diasLaborables: List<Int>, hoy: LocalDate): Boolean =
    !fecha.isBefore(hoy) && esDiaLaborable(fecha, diasLaborables)

fun primeraFechaReservable(diasLaborables: List<Int>, hoy: LocalDate): LocalDate? =
    (0 until DIAS_A_BUSCAR_FECHA_LIBRE)
        .map { hoy.plusDays(it) }
        .firstOrNull { esDiaLaborable(it, diasLaborables) }

fun esFechaHoraFutura(fecha: String, hora: String, ahora: LocalDateTime): Boolean {
    val dia = parsearFecha(fecha) ?: return false
    val horaInicio = parsearHora(hora) ?: return false
    return dia.atTime(horaInicio).isAfter(ahora)
}

fun separarCitas(citas: List<Cita>, ahora: LocalDateTime): CitasSeparadas {
    val (proximas, historial) = citas.partition { cita ->
        cita.estado == EstadosCita.RESERVADA && esFechaHoraFutura(cita.fecha, cita.hora, ahora)
    }
    return CitasSeparadas(
        proximas = proximas.sortedWith(compareBy({ it.fecha }, { it.hora })),
        historial = historial.sortedWith(compareByDescending<Cita> { it.fecha }.thenByDescending { it.hora })
    )
}

// Una cita reservada cuyo servicio ya terminó se muestra como completada; el documento en Firestore no se modifica.
fun estadoVisible(cita: Cita, ahora: LocalDateTime): String {
    if (cita.estado != EstadosCita.RESERVADA) return cita.estado
    val dia = parsearFecha(cita.fecha) ?: return cita.estado
    val horaInicio = parsearHora(cita.hora) ?: return cita.estado
    val fin = dia.atTime(horaInicio).plusMinutes(cita.duracionMinutos.toLong())
    return if (ahora.isBefore(fin)) cita.estado else EstadosCita.COMPLETADA
}

fun describirDiasLaborables(diasLaborables: List<Int>): String =
    diasLaborables.sorted()
        .filter { it in 1..7 }
        .joinToString(", ") { dia ->
            DayOfWeek.of(dia).getDisplayName(TextStyle.SHORT, LOCALE_ES).replaceFirstChar { it.uppercase() }
        }

fun formatearFechaLegible(fecha: String): String =
    parsearFecha(fecha)?.format(FORMATO_FECHA_LEGIBLE)?.replaceFirstChar { it.uppercase() } ?: fecha

fun fechaDesdeMillisUtc(millis: Long): LocalDate =
    Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()

fun millisUtcDesdeFecha(fecha: LocalDate): Long =
    fecha.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

fun parsearFecha(fecha: String): LocalDate? = runCatching { LocalDate.parse(fecha) }.getOrNull()

private fun parsearHora(hora: String): LocalTime? = runCatching { LocalTime.parse(hora) }.getOrNull()
