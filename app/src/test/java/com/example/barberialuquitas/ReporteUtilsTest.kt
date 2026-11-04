package com.example.barberialuquitas

import com.example.barberialuquitas.data.modelo.Cita
import com.example.barberialuquitas.data.modelo.EstadosCita
import com.example.barberialuquitas.util.FilaIngreso
import com.example.barberialuquitas.util.PeriodoReporte
import com.example.barberialuquitas.util.RangoFechas
import com.example.barberialuquitas.util.ingresosAgrupados
import com.example.barberialuquitas.util.moverPeriodo
import com.example.barberialuquitas.util.rangoDelPeriodo
import com.example.barberialuquitas.util.resumirCitas
import java.time.LocalDate
import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Test

class ReporteUtilsTest {

    private val miercoles = LocalDate.of(2026, 10, 21)

    @Test
    fun rangosDeDiaSemanaYMes() {
        assertEquals(RangoFechas(miercoles, miercoles), rangoDelPeriodo(PeriodoReporte.DIA, miercoles))
        assertEquals(
            RangoFechas(LocalDate.of(2026, 10, 19), LocalDate.of(2026, 10, 25)),
            rangoDelPeriodo(PeriodoReporte.SEMANA, miercoles)
        )
        assertEquals(
            RangoFechas(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31)),
            rangoDelPeriodo(PeriodoReporte.MES, miercoles)
        )
    }

    @Test
    fun moverPeriodoAvanzaSegunElTipo() {
        assertEquals(LocalDate.of(2026, 10, 20), moverPeriodo(PeriodoReporte.DIA, miercoles, -1))
        assertEquals(LocalDate.of(2026, 10, 28), moverPeriodo(PeriodoReporte.SEMANA, miercoles, 1))
        assertEquals(LocalDate.of(2026, 9, 21), moverPeriodo(PeriodoReporte.MES, miercoles, -1))
    }

    private val citas = listOf(
        Cita(id = "1", barberoNombre = "Ana", servicioNombre = "Corte", precio = 25, fecha = "2026-10-21", hora = "09:00", estado = EstadosCita.ATENDIDA),
        // "completada" (segunda entrega) cuenta como atendida.
        Cita(id = "2", barberoNombre = "Beto", servicioNombre = "Barba", precio = 15, fecha = "2026-10-21", hora = "10:00", estado = EstadosCita.COMPLETADA),
        Cita(id = "3", barberoNombre = "Ana", servicioNombre = "Corte", precio = 25, fecha = "2026-10-21", hora = "11:00", estado = EstadosCita.NO_ASISTIO),
        Cita(id = "4", barberoNombre = "Ana", servicioNombre = "Corte", precio = 25, fecha = "2026-10-21", hora = "12:00", estado = EstadosCita.CANCELADA),
        // Reservada que ya pasó: queda por confirmar y no suma.
        Cita(id = "5", barberoNombre = "Ana", servicioNombre = "Corte", precio = 25, fecha = "2026-10-21", hora = "13:00"),
        Cita(id = "6", barberoNombre = "Beto", servicioNombre = "Corte", precio = 25, fecha = "2026-10-21", hora = "18:00")
    )

    @Test
    fun soloLasCitasAtendidasSumanIngresos() {
        val resumen = resumirCitas(citas, LocalDateTime.of(2026, 10, 21, 15, 0))
        assertEquals(6, resumen.total)
        assertEquals(2, resumen.atendidas)
        assertEquals(1, resumen.noAsistio)
        assertEquals(1, resumen.canceladas)
        assertEquals(1, resumen.porConfirmar)
        assertEquals(1, resumen.pendientes)
        assertEquals(40L, resumen.ingresos)
        assertEquals(20L, resumen.ticketPromedio)
    }

    @Test
    fun ingresosPorBarberoOrdenadosDeMayorAMenor() {
        assertEquals(
            listOf(FilaIngreso("Ana", 1, 25), FilaIngreso("Beto", 1, 15)),
            ingresosAgrupados(citas) { it.barberoNombre }
        )
    }
}
