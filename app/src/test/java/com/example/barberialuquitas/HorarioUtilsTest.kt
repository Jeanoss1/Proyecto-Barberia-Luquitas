package com.example.barberialuquitas

import com.example.barberialuquitas.data.Cita
import com.example.barberialuquitas.data.EstadosCita
import com.example.barberialuquitas.util.calcularBloques
import com.example.barberialuquitas.util.esFechaReservable
import com.example.barberialuquitas.util.generarHorasDelDia
import com.example.barberialuquitas.util.primeraFechaReservable
import com.example.barberialuquitas.util.separarCitas
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

class HorarioUtilsTest {

    private val lunes = LocalDate.of(2026, 10, 19)

    @Test
    fun generaBloquesDe30MinutosSinPasarseDelCierre() {
        val horas = generarHorasDelDia("09:00", "11:00").map { it.toString() }
        assertEquals(listOf("09:00", "09:30", "10:00", "10:30"), horas)
    }

    @Test
    fun horarioInvalidoNoGeneraBloques() {
        assertTrue(generarHorasDelDia("abc", "11:00").isEmpty())
        assertTrue(generarHorasDelDia("11:00", "09:00").isEmpty())
    }

    @Test
    fun bloquesOcupadosYPasadosQuedanDeshabilitados() {
        val ahora = lunes.atTime(9, 15)
        val bloques = calcularBloques("09:00", "11:00", setOf("10:00"), lunes, ahora)
        assertEquals(
            listOf(false, true, false, true),
            bloques.map { it.disponible }
        )
    }

    @Test
    fun soloSePuedenElegirDiasLaborablesDesdeHoy() {
        val diasLunesAViernes = listOf(1, 2, 3, 4, 5)
        assertTrue(esFechaReservable(lunes, diasLunesAViernes, lunes))
        assertFalse(esFechaReservable(lunes.minusDays(3), diasLunesAViernes, lunes))
        assertFalse(esFechaReservable(lunes.plusDays(5), diasLunesAViernes, lunes))
    }

    @Test
    fun primeraFechaReservableSaltaDiasNoLaborables() {
        val sabado = lunes.minusDays(2)
        assertEquals(lunes, primeraFechaReservable(listOf(1), sabado))
        assertEquals(null, primeraFechaReservable(emptyList(), sabado))
    }

    @Test
    fun separaCitasProximasYPasadas() {
        val ahora = LocalDateTime.of(2026, 10, 19, 12, 0)
        val futura = Cita(id = "a", fecha = "2026-10-20", hora = "10:00")
        val masFutura = Cita(id = "b", fecha = "2026-10-21", hora = "09:00")
        val pasada = Cita(id = "c", fecha = "2026-10-18", hora = "10:00")
        val cancelada = Cita(id = "d", fecha = "2026-10-22", hora = "10:00", estado = EstadosCita.CANCELADA)

        val resultado = separarCitas(listOf(masFutura, pasada, cancelada, futura), ahora)

        assertEquals(listOf("a", "b"), resultado.proximas.map { it.id })
        assertEquals(listOf("d", "c"), resultado.pasadas.map { it.id })
    }
}
