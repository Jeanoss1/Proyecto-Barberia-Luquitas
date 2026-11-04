package com.example.barberialuquitas

import com.example.barberialuquitas.data.modelo.Barberia
import com.example.barberialuquitas.data.modelo.Barbero
import com.example.barberialuquitas.util.estaAbierta
import com.example.barberialuquitas.util.horarioDeAtencion
import com.example.barberialuquitas.util.opcionesDeHora
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HorarioAtencionTest {

    private val lunes = LocalDate.of(2026, 10, 19)

    private val barberia = Barberia(
        id = "b1",
        horaApertura = "10:00",
        horaCierre = "19:00",
        diasLaborables = listOf(1, 2, 3, 4, 5),
        diasNoLaborables = listOf("2026-10-20")
    )

    private val barbero = Barbero(
        id = "x",
        horaInicio = "09:00",
        horaFin = "18:00",
        diasLaborables = listOf(1, 2, 6)
    )

    @Test
    fun seUsaLaParteComunEntreBarberiaYBarbero() {
        val horario = horarioDeAtencion(barberia, barbero)
        assertEquals("10:00", horario.horaInicio)
        assertEquals("18:00", horario.horaFin)
        // El sábado lo trabaja el barbero pero la barbería está cerrada.
        assertEquals(listOf(1, 2), horario.diasLaborables)
    }

    @Test
    fun losDiasNoLaborablesDeLaBarberiaNoSePuedenReservar() {
        val horario = horarioDeAtencion(barberia, barbero)
        assertTrue(horario.esFechaReservable(lunes, lunes))
        assertFalse(horario.esFechaReservable(lunes.plusDays(1), lunes))
        // Martes feriado → el siguiente día común es el lunes de la otra semana.
        assertEquals(lunes.plusDays(7), horario.primeraFechaReservable(lunes.plusDays(1)))
    }

    @Test
    fun abiertoSoloDentroDelHorarioYEnDiaLaborable() {
        assertTrue(estaAbierta(barberia, lunes.atTime(10, 0)))
        assertFalse(estaAbierta(barberia, lunes.atTime(9, 59)))
        assertFalse(estaAbierta(barberia, lunes.atTime(19, 0)))
        assertFalse(estaAbierta(barberia, lunes.plusDays(1).atTime(12, 0)))
        assertFalse(estaAbierta(barberia.copy(activa = false), lunes.atTime(12, 0)))
    }

    @Test
    fun opcionesDeHoraVanCadaMediaHora() {
        val opciones = opcionesDeHora()
        assertEquals("06:00", opciones.first())
        assertEquals("06:30", opciones[1])
        assertEquals("23:30", opciones.last())
    }
}
