package com.example.barberialuquitas.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.LaunchedEffect
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
import com.example.barberialuquitas.components.CitaCard
import com.example.barberialuquitas.components.EncabezadoConAtras
import com.example.barberialuquitas.components.MensajeError
import com.example.barberialuquitas.components.MensajeVacio
import com.example.barberialuquitas.components.PantallaCargando
import com.example.barberialuquitas.components.coloresCalendarioBarberia
import com.example.barberialuquitas.data.AuthRepository
import com.example.barberialuquitas.data.CitaRepository
import com.example.barberialuquitas.data.Roles
import com.example.barberialuquitas.navigation.Rutas
import com.example.barberialuquitas.ui.theme.CardBackground
import com.example.barberialuquitas.ui.theme.DarkBackground
import com.example.barberialuquitas.ui.theme.GoldAccent
import com.example.barberialuquitas.ui.theme.TextGray
import com.example.barberialuquitas.util.fechaDesdeMillisUtc
import com.example.barberialuquitas.util.formatearFechaLegible
import com.example.barberialuquitas.util.millisUtcDesdeFecha
import com.example.barberialuquitas.util.parsearFecha
import java.time.LocalDate

@Composable
fun AgendaAdminScreen(navController: NavController, authRepository: AuthRepository = remember { AuthRepository() }) {
    var esAdministrador by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val usuario = authRepository.obtenerUsuarioActual().getOrNull()
        when {
            usuario == null -> navController.navigate(Rutas.LOGIN) { popUpTo(0) }
            usuario.rol != Roles.ADMINISTRADOR -> navController.navigate(Rutas.homeCliente()) { popUpTo(0) }
            else -> esAdministrador = true
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(DarkBackground)) {
        EncabezadoConAtras("Agenda general") { navController.popBackStack() }
        if (esAdministrador) AgendaDelDia() else PantallaCargando()
    }
}

@Composable
private fun AgendaDelDia(citaRepository: CitaRepository = remember { CitaRepository() }) {
    var fechaTexto by rememberSaveable { mutableStateOf(LocalDate.now().toString()) }
    val fecha = parsearFecha(fechaTexto) ?: LocalDate.now()
    var intento by remember { mutableIntStateOf(0) }
    val resultado by remember(fechaTexto, intento) {
        citaRepository.observarAgendaDelDia(fechaTexto)
    }.collectAsState(initial = null)

    SelectorDeFecha(fecha, alCambiar = { fechaTexto = it.toString() })

    val turnos = resultado?.getOrNull()
    when {
        resultado == null -> PantallaCargando()
        turnos == null -> MensajeError(
            mensaje = resultado?.exceptionOrNull()?.message ?: "Ocurrió un error.",
            alReintentar = { intento++ }
        )
        turnos.isEmpty() -> MensajeVacio("No hay turnos reservados para este día.")
        else -> {
            Text(
                "${turnos.size} turno(s) reservado(s)",
                color = TextGray,
                fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(turnos, key = { it.id }) { cita -> CitaCard(cita, mostrarCliente = true) }
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
