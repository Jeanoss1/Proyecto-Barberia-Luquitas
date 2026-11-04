package com.example.barberialuquitas.ui.pantallas.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.barberialuquitas.data.modelo.Cita
import com.example.barberialuquitas.data.modelo.EstadosCita
import com.example.barberialuquitas.data.repositorio.CitaRepository
import com.example.barberialuquitas.ui.theme.CardBackground
import com.example.barberialuquitas.ui.theme.ErrorRed
import com.example.barberialuquitas.ui.theme.GoldAccent
import com.example.barberialuquitas.ui.theme.SuccessGreen
import com.example.barberialuquitas.ui.theme.TextGray
import com.example.barberialuquitas.util.formatearFechaLegible
import com.example.barberialuquitas.util.yaEmpezo
import java.time.LocalDateTime
import kotlinx.coroutines.launch

// RF16: botones para cambiar el estado de una cita. El cliente ve el cambio al instante en su historial.
// - Reservada y aún no empieza → se puede cancelar (libera el horario).
// - Ya empezó → atendida o no asistió (y se puede corregir entre ambos).
// - Cancelada → no se puede cambiar.
@Composable
fun AccionesEstadoCita(cita: Cita, citaRepository: CitaRepository = remember { CitaRepository() }) {
    var procesando by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var confirmarCancelacion by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val empezo = yaEmpezo(cita, LocalDateTime.now())

    fun ejecutar(accion: suspend () -> Result<Unit>) {
        procesando = true
        error = null
        scope.launch {
            accion().onFailure { error = it.message }
            procesando = false
        }
    }

    if (cita.estado == EstadosCita.CANCELADA) return

    Column {
        if (procesando) {
            CircularProgressIndicator(color = GoldAccent, modifier = Modifier.size(24.dp))
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                if (empezo) {
                    if (!EstadosCita.esAtendida(cita.estado)) {
                        BotonEstado("Atendida", SuccessGreen, Modifier.weight(1f)) {
                            ejecutar { citaRepository.cambiarEstado(cita, EstadosCita.ATENDIDA) }
                        }
                    }
                    if (cita.estado != EstadosCita.NO_ASISTIO) {
                        BotonEstado("No asistió", ErrorRed, Modifier.weight(1f)) {
                            ejecutar { citaRepository.cambiarEstado(cita, EstadosCita.NO_ASISTIO) }
                        }
                    }
                } else if (cita.estado == EstadosCita.RESERVADA) {
                    BotonEstado("Cancelar cita", ErrorRed, Modifier.weight(1f)) { confirmarCancelacion = true }
                }
            }
        }
        error?.let {
            Spacer(modifier = Modifier.height(6.dp))
            Text(it, color = ErrorRed, fontSize = 12.sp)
        }
    }

    if (confirmarCancelacion) {
        AlertDialog(
            onDismissRequest = { confirmarCancelacion = false },
            containerColor = CardBackground,
            titleContentColor = Color.White,
            textContentColor = TextGray,
            title = { Text("Cancelar cita") },
            text = {
                Text(
                    "¿Cancelar la cita de ${cita.clienteNombre} (${cita.servicioNombre}) el " +
                        "${formatearFechaLegible(cita.fecha)} a las ${cita.hora}? El horario quedará libre."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    confirmarCancelacion = false
                    ejecutar { citaRepository.cancelarCitaComoAdministrador(cita) }
                }) {
                    Text("Sí, cancelar", color = ErrorRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmarCancelacion = false }) { Text("Volver", color = GoldAccent) }
            }
        )
    }
}

@Composable
private fun BotonEstado(texto: String, color: Color, modifier: Modifier, alPulsar: () -> Unit) {
    OutlinedButton(
        onClick = alPulsar,
        modifier = modifier,
        border = BorderStroke(1.dp, color),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = color),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(texto, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}
