package com.example.barberialuquitas.screens.cliente

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.barberialuquitas.components.CitaCard
import com.example.barberialuquitas.components.MensajeError
import com.example.barberialuquitas.components.MensajeVacio
import com.example.barberialuquitas.components.PantallaCargando
import com.example.barberialuquitas.data.Cita
import com.example.barberialuquitas.data.CitaRepository
import com.example.barberialuquitas.ui.theme.CardBackground
import com.example.barberialuquitas.ui.theme.DarkBackground
import com.example.barberialuquitas.ui.theme.ErrorRed
import com.example.barberialuquitas.ui.theme.GoldAccent
import com.example.barberialuquitas.ui.theme.TextGray
import com.example.barberialuquitas.util.formatearFechaLegible
import com.example.barberialuquitas.util.separarCitas
import kotlinx.coroutines.launch
import java.time.LocalDateTime

@Composable
fun MisCitasScreen(clienteId: String, citaRepository: CitaRepository = remember { CitaRepository() }) {
    var intento by remember { mutableIntStateOf(0) }
    var citaACancelar by remember { mutableStateOf<Cita?>(null) }
    val resultado by remember(clienteId, intento) {
        citaRepository.observarCitasDelCliente(clienteId)
    }.collectAsState(initial = null)

    Column(modifier = Modifier.fillMaxSize().background(DarkBackground)) {
        Text(
            "Mis citas",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(20.dp)
        )

        val citas = resultado?.getOrNull()
        when {
            resultado == null -> PantallaCargando(Modifier.fillMaxSize())
            citas == null -> MensajeError(
                mensaje = resultado?.exceptionOrNull()?.message ?: "Ocurrió un error.",
                alReintentar = { intento++ },
                modifier = Modifier.fillMaxSize()
            )
            citas.isEmpty() -> MensajeVacio("Aún no tienes citas. Reserva tu primer servicio.", modifier = Modifier.fillMaxSize())
            else -> {
                val separadas = separarCitas(citas, LocalDateTime.now())
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    seccionDeCitas("Próximas", separadas.proximas, "No tienes citas próximas.") { citaACancelar = it }
                    seccionDeCitas("Historial", separadas.historial, "Aún no tienes citas en tu historial.")
                }
            }
        }
    }

    citaACancelar?.let { cita ->
        CancelarCitaDialog(cita, citaRepository, alCerrar = { citaACancelar = null })
    }
}

@Composable
private fun CancelarCitaDialog(cita: Cita, citaRepository: CitaRepository, alCerrar: () -> Unit) {
    var cancelando by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = { if (!cancelando) alCerrar() },
        containerColor = CardBackground,
        titleContentColor = Color.White,
        textContentColor = TextGray,
        title = { Text("Cancelar cita") },
        text = {
            Column {
                Text(
                    "¿Seguro que quieres cancelar tu ${cita.servicioNombre} con ${cita.barberoNombre} " +
                        "el ${formatearFechaLegible(cita.fecha)} a las ${cita.hora}?"
                )
                if (error != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(error!!, color = ErrorRed, fontSize = 13.sp)
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !cancelando,
                onClick = {
                    cancelando = true
                    error = null
                    scope.launch {
                        citaRepository.cancelarCita(cita)
                            .onSuccess { alCerrar() }
                            .onFailure { error = it.message }
                        cancelando = false
                    }
                }
            ) {
                if (cancelando) {
                    CircularProgressIndicator(color = ErrorRed, modifier = Modifier.size(18.dp))
                } else {
                    Text("Sí, cancelar", color = ErrorRed, fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(enabled = !cancelando, onClick = alCerrar) {
                Text("Volver", color = GoldAccent)
            }
        }
    )
}

private fun LazyListScope.seccionDeCitas(
    titulo: String,
    citas: List<Cita>,
    mensajeVacio: String,
    alCancelar: ((Cita) -> Unit)? = null
) {
    item(key = titulo) {
        Text(titulo, color = GoldAccent, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
    if (citas.isEmpty()) {
        item(key = "$titulo-vacio") { MensajeVacio(mensajeVacio, modifier = Modifier.fillMaxWidth()) }
    }
    items(citas, key = { it.id }) { cita ->
        CitaCard(cita, alCancelar = alCancelar?.let { cancelar -> { cancelar(cita) } })
    }
}
