package com.example.barberialuquitas.util

import com.example.barberialuquitas.data.modelo.Cita
import com.example.barberialuquitas.data.modelo.EstadosCita
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

// Cálculos del dashboard (RF12) y del reporte de ingresos (RF19). Solo las citas atendidas suman ingresos.

enum class PeriodoReporte(val etiqueta: String) { DIA("Día"), SEMANA("Semana"), MES("Mes") }

data class RangoFechas(val desde: LocalDate, val hasta: LocalDate)

// La semana va de lunes a domingo; el mes, del primer al último día.
fun rangoDelPeriodo(periodo: PeriodoReporte, referencia: LocalDate): RangoFechas = when (periodo) {
    PeriodoReporte.DIA -> RangoFechas(referencia, referencia)
    PeriodoReporte.SEMANA -> RangoFechas(
        referencia.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)),
        referencia.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
    )
    PeriodoReporte.MES -> RangoFechas(
        referencia.withDayOfMonth(1),
        referencia.with(TemporalAdjusters.lastDayOfMonth())
    )
}

fun moverPeriodo(periodo: PeriodoReporte, referencia: LocalDate, pasos: Long): LocalDate = when (periodo) {
    PeriodoReporte.DIA -> referencia.plusDays(pasos)
    PeriodoReporte.SEMANA -> referencia.plusWeeks(pasos)
    PeriodoReporte.MES -> referencia.plusMonths(pasos)
}

private val FORMATO_DIA_CORTO = DateTimeFormatter.ofPattern("d MMM", LOCALE_ES)
private val FORMATO_MES = DateTimeFormatter.ofPattern("MMMM yyyy", LOCALE_ES)

fun describirPeriodo(periodo: PeriodoReporte, rango: RangoFechas): String = when (periodo) {
    PeriodoReporte.DIA -> formatearFechaLegible(rango.desde.toString())
    PeriodoReporte.SEMANA -> "${rango.desde.format(FORMATO_DIA_CORTO)} - ${rango.hasta.format(FORMATO_DIA_CORTO)}"
    PeriodoReporte.MES -> rango.desde.format(FORMATO_MES).replaceFirstChar { it.uppercase() }
}

data class ResumenCitas(
    val total: Int = 0,
    val pendientes: Int = 0,
    val porConfirmar: Int = 0,
    val atendidas: Int = 0,
    val noAsistio: Int = 0,
    val canceladas: Int = 0,
    val ingresos: Long = 0
) {
    // Ingreso promedio por cita atendida.
    val ticketPromedio: Long get() = if (atendidas == 0) 0 else ingresos / atendidas
}

fun resumirCitas(citas: List<Cita>, ahora: LocalDateTime): ResumenCitas {
    val estados = citas.map { estadoVisible(it, ahora) }
    return ResumenCitas(
        total = citas.size,
        pendientes = estados.count { it == EstadosCita.RESERVADA },
        porConfirmar = estados.count { it == EstadosCita.POR_CONFIRMAR },
        atendidas = estados.count(EstadosCita::esAtendida),
        noAsistio = estados.count { it == EstadosCita.NO_ASISTIO },
        canceladas = estados.count { it == EstadosCita.CANCELADA },
        ingresos = citas.filter { EstadosCita.esAtendida(it.estado) }.sumOf { it.precio }
    )
}

data class FilaIngreso(val nombre: String, val citas: Int, val ingresos: Long)

// Ingresos agrupados (por barbero, por servicio o por fecha), de mayor a menor.
fun ingresosAgrupados(citas: List<Cita>, clave: (Cita) -> String): List<FilaIngreso> =
    citas.filter { EstadosCita.esAtendida(it.estado) }
        .groupBy(clave)
        .map { (nombre, grupo) -> FilaIngreso(nombre, grupo.size, grupo.sumOf { it.precio }) }
        .sortedWith(compareByDescending<FilaIngreso> { it.ingresos }.thenBy { it.nombre })
