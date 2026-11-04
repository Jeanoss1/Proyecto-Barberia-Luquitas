package com.example.barberialuquitas.ui.pantallas.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.barberialuquitas.data.modelo.EstadosCita
import com.example.barberialuquitas.data.repositorio.CitaRepository
import com.example.barberialuquitas.ui.componentes.CitaCard
import com.example.barberialuquitas.ui.componentes.ContenidoSegunResultado
import com.example.barberialuquitas.ui.componentes.coloresCalendarioBarberia
import com.example.barberialuquitas.ui.theme.CardBackground
import com.example.barberialuquitas.ui.theme.GoldAccent
import com.example.barberialuquitas.ui.theme.TextGray
import com.example.barberialuquitas.util.fechaDesdeMillisUtc
import com.example.barberialuquitas.util.formatearFechaLegible
import com.example.barberialuquitas.util.millisUtcDesdeFecha
import com.example.barberialuquitas.util.parsearFecha
import java.time.LocalDate

// RF15: agenda de la barbería por día, con cliente y barbero. RF16: cada cita trae sus botones de estado.
@Composable
fun AgendaAdminScreen(navController: NavController) {
    PantallaAdministrador(navController, titulo = "Agenda general") { administrador ->
        AgendaDelDia(administrador.barberiaId)
    }
}

@Composable
private fun AgendaDelDia(barberiaId: String, citaRepository: CitaRepository = remember { CitaRepository() }) {
    var fechaTexto by rememberSaveable { mutableStateOf(LocalDate.now().toString()) }
    val fecha = parsearFecha(fechaTexto) ?: LocalDate.now()
    var intento by remember { mutableIntStateOf(0) }
    val resultado by remember(barberiaId, fechaTexto, intento) {
        citaRepository.observarAgendaDelDia(barberiaId, fechaTexto)
    }.collectAsState(initial = null)

    SelectorDeFecha(fecha, alCambiar = { fechaTexto = it.toString() })

    ContenidoSegunResultado(
        resultado = resultado,
        alReintentar = { intento++ },
        modifier = Modifier.fillMaxSize(),
        mensajeVacio = "No hay turnos reservados para este día."
    ) { turnos ->
        val canceladas = turnos.count { it.estado == EstadosCita.CANCELADA }
        Text(
            "${turnos.size - canceladas} turno(s) · $canceladas cancelada(s)",
            color = TextGray,
            fontSize = 13.sp,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Las canceladas van al final para que la agenda muestre primero lo que sí se atiende.
            items(turnos.sortedBy { it.estado == EstadosCita.CANCELADA }, key = { it.id }) { cita ->
                CitaCard(cita, mostrarCliente = true, acciones = { AccionesEstadoCita(cita, citaRepository) })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectorDeFecha(fecha: LocalDate, alCambiar: (LocalDate) -> Unit) {
    var mostrarCalendario by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { alCambiar(fecha.minusDays(1)) }) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Día anterior", tint = GoldAccent)
        }
        OutlinedButton(
            onClick = { mostrarCalendario = true },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = CardBackground, contentColor = GoldAccent),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(Icons.Default.CalendarMonth, contentDescription = null)
            Text(
                formatearFechaLegible(fecha.toString()),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        IconButton(onClick = { alCambiar(fecha.plusDays(1)) }) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Día siguiente", tint = GoldAccent)
        }
    }

    if (mostrarCalendario) {
        val estadoCalendario = rememberDatePickerState(initialSelectedDateMillis = millisUtcDesdeFecha(fecha))
        DatePickerDialog(
            onDismissRequest = { mostrarCalendario = false },
            colors = coloresCalendarioBarberia(),
            confirmButton = {
                TextButton(onClick = {
                    estadoCalendario.selectedDateMillis?.let { alCambiar(fechaDesdeMillisUtc(it)) }
                    mostrarCalendario = false
                }) {
                    Text("Aceptar", color = GoldAccent, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarCalendario = false }) {
                    Text("Cancelar", color = TextGray)
                }
            }
        ) {
            DatePicker(state = estadoCalendario, colors = coloresCalendarioBarberia())
        }
    }
}
