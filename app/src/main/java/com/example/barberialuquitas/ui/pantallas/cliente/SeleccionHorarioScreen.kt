package com.example.barberialuquitas.ui.pantallas.cliente

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.barberialuquitas.data.modelo.Barbero
import com.example.barberialuquitas.data.modelo.Servicio
import com.example.barberialuquitas.data.repositorio.BarberiaRepository
import com.example.barberialuquitas.data.repositorio.BarberoRepository
import com.example.barberialuquitas.data.repositorio.CitaRepository
import com.example.barberialuquitas.data.repositorio.ServicioRepository
import com.example.barberialuquitas.ui.componentes.BloqueHorarioChip
import com.example.barberialuquitas.ui.componentes.EncabezadoConAtras
import com.example.barberialuquitas.ui.componentes.MensajeError
import com.example.barberialuquitas.ui.componentes.MensajeVacio
import com.example.barberialuquitas.ui.componentes.PantallaCargando
import com.example.barberialuquitas.ui.componentes.coloresCalendarioBarberia
import com.example.barberialuquitas.ui.navegacion.Rutas
import com.example.barberialuquitas.ui.theme.CardBackground
import com.example.barberialuquitas.ui.theme.DarkBackground
import com.example.barberialuquitas.ui.theme.GoldAccent
import com.example.barberialuquitas.ui.theme.TextGray
import com.example.barberialuquitas.util.HorarioAtencion
import com.example.barberialuquitas.util.bloquesNecesarios
import com.example.barberialuquitas.util.calcularBloques
import com.example.barberialuquitas.util.fechaDesdeMillisUtc
import com.example.barberialuquitas.util.horarioDeAtencion
import com.example.barberialuquitas.util.millisUtcDesdeFecha
import java.time.LocalDate
import java.time.LocalDateTime

private data class DatosHorario(val barbero: Barbero, val servicio: Servicio, val horario: HorarioAtencion)

// RF06: calendario con los bloques libres dentro del horario de la barbería (RF17) y del barbero.
@Composable
fun SeleccionHorarioScreen(
    navController: NavController,
    servicioId: String,
    barberoId: String,
    barberoRepository: BarberoRepository = remember { BarberoRepository() },
    servicioRepository: ServicioRepository = remember { ServicioRepository() },
    barberiaRepository: BarberiaRepository = remember { BarberiaRepository() }
) {
    var intento by remember { mutableIntStateOf(0) }
    var resultado by remember { mutableStateOf<Result<DatosHorario>?>(null) }

    LaunchedEffect(barberoId, servicioId, intento) {
        resultado = null
        resultado = runCatching {
            val barbero = barberoRepository.obtenerBarbero(barberoId).getOrThrow()
            val barberia = barberiaRepository.obtenerBarberia(barbero.barberiaId).getOrThrow()
            DatosHorario(
                barbero = barbero,
                servicio = servicioRepository.obtenerServicio(servicioId).getOrThrow(),
                horario = horarioDeAtencion(barberia, barbero)
            )
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(DarkBackground)) {
        EncabezadoConAtras("Elige fecha y hora") { navController.popBackStack() }

        val datos = resultado?.getOrNull()
        when {
            resultado == null -> PantallaCargando(Modifier.fillMaxSize())
            datos == null -> MensajeError(
                mensaje = resultado?.exceptionOrNull()?.message ?: "Ocurrió un error.",
                alReintentar = { intento++ },
                modifier = Modifier.fillMaxSize()
            )
            datos.horario.primeraFechaReservable(LocalDate.now()) == null ->
                MensajeVacio("${datos.barbero.nombre} no tiene días de atención disponibles.", modifier = Modifier.fillMaxSize())
            else -> CalendarioDelBarbero(datos.barbero, datos.servicio, datos.horario) { fecha, hora ->
                navController.navigate(Rutas.confirmacionReserva(servicioId, barberoId, fecha, hora))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ColumnScope.CalendarioDelBarbero(
    barbero: Barbero,
    servicio: Servicio,
    horario: HorarioAtencion,
    alContinuar: (fecha: String, hora: String) -> Unit
) {
    val hoy = remember { LocalDate.now() }
    val fechasPermitidas = remember(horario) {
        object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long) =
                horario.esFechaReservable(fechaDesdeMillisUtc(utcTimeMillis), hoy)

            override fun isSelectableYear(year: Int) = year >= hoy.year
        }
    }
    val estadoCalendario = rememberDatePickerState(
        initialSelectedDateMillis = horario.primeraFechaReservable(hoy)?.let(::millisUtcDesdeFecha),
        yearRange = hoy.year..hoy.year + 1,
        selectableDates = fechasPermitidas
    )
    val fecha = estadoCalendario.selectedDateMillis?.let(::fechaDesdeMillisUtc)
    var horaSeleccionada by remember(fecha) { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)
    ) {
        Text(
            "Barbero: ${barbero.nombre} · Atiende de ${horario.horaInicio} a ${horario.horaFin}\n" +
                "${servicio.nombre} · ${servicio.duracionMinutos} min",
            color = TextGray,
            fontSize = 14.sp,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Card(
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            shape = RoundedCornerShape(16.dp)
        ) {
            DatePicker(
                state = estadoCalendario,
                title = null,
                headline = null,
                showModeToggle = false,
                colors = coloresCalendarioBarberia()
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text("Horarios", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        if (fecha == null) {
            MensajeVacio("Selecciona una fecha para ver los horarios.", modifier = Modifier.fillMaxWidth())
        } else {
            BloquesDelDia(
                barbero.id,
                horario,
                fecha,
                bloquesNecesarios(servicio.duracionMinutos),
                horaSeleccionada,
                alSeleccionar = { horaSeleccionada = it }
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
    }

    Button(
        onClick = { if (fecha != null) horaSeleccionada?.let { alContinuar(fecha.toString(), it) } },
        enabled = fecha != null && horaSeleccionada != null,
        modifier = Modifier.fillMaxWidth().padding(16.dp).height(50.dp),
        colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text("Continuar", color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun BloquesDelDia(
    barberoId: String,
    horario: HorarioAtencion,
    fecha: LocalDate,
    bloquesPorCita: Int,
    horaSeleccionada: String?,
    alSeleccionar: (String?) -> Unit,
    citaRepository: CitaRepository = remember { CitaRepository() }
) {
    var intento by remember { mutableIntStateOf(0) }
    val resultado by remember(barberoId, fecha, intento) {
        citaRepository.observarHorasOcupadas(barberoId, fecha.toString())
    }.collectAsState(initial = null)

    val horasOcupadas = resultado?.getOrNull()
    when {
        resultado == null -> PantallaCargando(Modifier.fillMaxWidth().padding(24.dp))
        horasOcupadas == null -> MensajeError(
            mensaje = resultado?.exceptionOrNull()?.message ?: "Ocurrió un error.",
            alReintentar = { intento++ },
            modifier = Modifier.fillMaxWidth()
        )
        else -> {
            val bloques = calcularBloques(
                horario.horaInicio,
                horario.horaFin,
                horasOcupadas,
                fecha,
                LocalDateTime.now(),
                bloquesPorCita
            )
            LaunchedEffect(bloques, horaSeleccionada) {
                if (bloques.none { it.hora == horaSeleccionada && it.disponible }) alSeleccionar(null)
            }
            if (bloques.none { it.disponible }) {
                MensajeVacio("No quedan horarios libres para este día.", modifier = Modifier.fillMaxWidth())
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                bloques.forEach { bloque ->
                    BloqueHorarioChip(
                        bloque = bloque,
                        seleccionado = bloque.hora == horaSeleccionada,
                        alPulsar = { alSeleccionar(bloque.hora) }
                    )
                }
            }
        }
    }
}
