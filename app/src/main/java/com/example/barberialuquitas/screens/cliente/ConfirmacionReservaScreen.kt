package com.example.barberialuquitas.screens.cliente

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.barberialuquitas.components.EncabezadoConAtras
import com.example.barberialuquitas.components.MensajeError
import com.example.barberialuquitas.components.PantallaCargando
import com.example.barberialuquitas.data.AuthRepository
import com.example.barberialuquitas.data.Barbero
import com.example.barberialuquitas.data.BarberoRepository
import com.example.barberialuquitas.data.Cita
import com.example.barberialuquitas.data.CitaRepository
import com.example.barberialuquitas.data.HorarioOcupadoException
import com.example.barberialuquitas.data.Servicio
import com.example.barberialuquitas.data.ServicioRepository
import com.example.barberialuquitas.data.Usuario
import com.example.barberialuquitas.navigation.Rutas
import com.example.barberialuquitas.ui.theme.CardBackground
import com.example.barberialuquitas.ui.theme.DarkBackground
import com.example.barberialuquitas.ui.theme.ErrorRed
import com.example.barberialuquitas.ui.theme.GoldAccent
import com.example.barberialuquitas.ui.theme.TextGray
import com.example.barberialuquitas.util.bloquesDesde
import com.example.barberialuquitas.util.bloquesNecesarios
import com.example.barberialuquitas.util.esFechaHoraFutura
import com.example.barberialuquitas.util.formatearFechaLegible
import com.example.barberialuquitas.util.horaDeTermino
import kotlinx.coroutines.launch
import java.time.LocalDateTime

private data class DatosReserva(val servicio: Servicio, val barbero: Barbero, val usuario: Usuario)

@Composable
fun ConfirmacionReservaScreen(
    navController: NavController,
    servicioId: String,
    barberoId: String,
    fecha: String,
    hora: String,
    servicioRepository: ServicioRepository = remember { ServicioRepository() },
    barberoRepository: BarberoRepository = remember { BarberoRepository() },
    authRepository: AuthRepository = remember { AuthRepository() },
    citaRepository: CitaRepository = remember { CitaRepository() }
) {
    var intento by remember { mutableIntStateOf(0) }
    var datos by remember { mutableStateOf<Result<DatosReserva>?>(null) }
    var guardando by remember { mutableStateOf(false) }
    var mensajeError by remember { mutableStateOf<String?>(null) }
    var horarioNoDisponible by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(intento) {
        datos = null
        datos = runCatching {
            DatosReserva(
                servicio = servicioRepository.obtenerServicio(servicioId).getOrThrow(),
                barbero = barberoRepository.obtenerBarbero(barberoId).getOrThrow(),
                usuario = authRepository.obtenerUsuarioActual().getOrThrow()
            )
        }
    }

    fun confirmar(reserva: DatosReserva) {
        if (!esFechaHoraFutura(fecha, hora, LocalDateTime.now())) {
            horarioNoDisponible = true
            return
        }
        mensajeError = null
        guardando = true
        scope.launch {
            val resultado = citaRepository.reservarCita(
                Cita(
                    clienteId = reserva.usuario.uid,
                    clienteNombre = reserva.usuario.nombre,
                    servicioId = reserva.servicio.id,
                    servicioNombre = reserva.servicio.nombre,
                    precio = reserva.servicio.precio,
                    barberoId = reserva.barbero.id,
                    barberoNombre = reserva.barbero.nombre,
                    fecha = fecha,
                    hora = hora,
                    duracionMinutos = reserva.servicio.duracionMinutos,
                    bloques = bloquesDesde(hora, bloquesNecesarios(reserva.servicio.duracionMinutos))
                )
            )
            guardando = false
            resultado
                .onSuccess {
                    navController.navigate(Rutas.reservaExitosa(fecha, hora)) {
                        popUpTo(Rutas.HOME_CLIENTE)
                    }
                }
                .onFailure { error ->
                    if (error is HorarioOcupadoException) horarioNoDisponible = true else mensajeError = error.message
                }
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(DarkBackground)) {
        EncabezadoConAtras("Confirma tu reserva") { navController.popBackStack() }

        val reserva = datos?.getOrNull()
        when {
            datos == null -> PantallaCargando(Modifier.fillMaxSize())
            reserva == null -> MensajeError(
                mensaje = datos?.exceptionOrNull()?.message ?: "Ocurrió un error.",
                alReintentar = { intento++ },
                modifier = Modifier.fillMaxSize()
            )
            else -> Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
                ResumenReserva(reserva.servicio, reserva.barbero.nombre, fecha, hora)
                if (mensajeError != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(mensajeError!!, color = ErrorRed, fontSize = 13.sp)
                }
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = { confirmar(reserva) },
                    enabled = !guardando,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (guardando) {
                        CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(20.dp))
                    } else {
                        Text("Confirmar reserva", color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (horarioNoDisponible) {
        AlertDialog(
            onDismissRequest = {},
            containerColor = CardBackground,
            titleContentColor = Color.White,
            textContentColor = TextGray,
            title = { Text("Horario no disponible") },
            text = { Text("El bloque de las $hora ya no está disponible. Elige otro horario.") },
            confirmButton = {
                TextButton(onClick = {
                    horarioNoDisponible = false
                    navController.popBackStack()
                }) {
                    Text("Elegir otro horario", color = GoldAccent, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
private fun ResumenReserva(servicio: Servicio, barberoNombre: String, fecha: String, hora: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            FilaResumen("Servicio", servicio.nombre)
            FilaResumen("Barbero", barberoNombre)
            FilaResumen("Fecha", formatearFechaLegible(fecha))
            FilaResumen("Hora", "$hora - ${horaDeTermino(hora, servicio.duracionMinutos)}")
            FilaResumen("Duración", "${servicio.duracionMinutos} min")
            HorizontalDivider(color = TextGray.copy(alpha = 0.3f))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Total", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text("S/ ${servicio.precio}", color = GoldAccent, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun FilaResumen(etiqueta: String, valor: String) {
    Column {
        Text(etiqueta, color = TextGray, fontSize = 12.sp)
        Text(valor, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium)
    }
}

@Preview
@Composable
private fun ResumenReservaPreview() {
    ResumenReserva(
        servicio = Servicio(id = "1", nombre = "Corte clásico", precio = 25, duracionMinutos = 45),
        barberoNombre = "Luquitas",
        fecha = "2026-10-20",
        hora = "10:30"
    )
}
