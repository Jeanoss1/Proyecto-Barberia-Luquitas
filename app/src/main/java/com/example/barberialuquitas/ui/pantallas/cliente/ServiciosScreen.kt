package com.example.barberialuquitas.ui.pantallas.cliente

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.barberialuquitas.data.local.PreferenciasCliente
import com.example.barberialuquitas.data.modelo.Barberia
import com.example.barberialuquitas.data.modelo.Servicio
import com.example.barberialuquitas.data.repositorio.BarberiaRepository
import com.example.barberialuquitas.data.repositorio.ServicioRepository
import com.example.barberialuquitas.ui.componentes.ContenidoSegunResultado
import com.example.barberialuquitas.ui.componentes.FotoOInicial
import com.example.barberialuquitas.ui.componentes.MensajeError
import com.example.barberialuquitas.ui.componentes.PantallaCargando
import com.example.barberialuquitas.ui.theme.CardBackground
import com.example.barberialuquitas.ui.theme.DarkBackground
import com.example.barberialuquitas.ui.theme.GoldAccent
import com.example.barberialuquitas.ui.theme.TextGray
import com.example.barberialuquitas.util.estaAbierta
import java.time.LocalDateTime

// Pestaña "Servicios": primero se elige la barbería (se recuerda en el teléfono) y luego su catálogo (RF04).
@Composable
fun ServiciosScreen(alReservar: (Servicio) -> Unit) {
    val contexto = LocalContext.current
    val preferencias = remember { PreferenciasCliente(contexto) }
    var barberiaId by remember { mutableStateOf(preferencias.barberiaSeleccionada) }
    var cambiando by remember { mutableStateOf(false) }

    val idActual = barberiaId
    if (idActual == null || cambiando) {
        ElegirBarberiaContenido(alElegir = { barberia ->
            preferencias.barberiaSeleccionada = barberia.id
            barberiaId = barberia.id
            cambiando = false
        })
    } else {
        BarberiaElegida(
            barberiaId = idActual,
            // Si la barbería guardada ya no existe o se desactivó, se vuelve a elegir.
            alNoEstarDisponible = {
                preferencias.barberiaSeleccionada = null
                barberiaId = null
            },
            alCambiarBarberia = { cambiando = true },
            alReservar = alReservar
        )
    }
}

@Composable
private fun BarberiaElegida(
    barberiaId: String,
    alNoEstarDisponible: () -> Unit,
    alCambiarBarberia: () -> Unit,
    alReservar: (Servicio) -> Unit,
    barberiaRepository: BarberiaRepository = remember { BarberiaRepository() }
) {
    var intento by remember { mutableIntStateOf(0) }
    val resultado by remember(barberiaId, intento) { barberiaRepository.observarBarberia(barberiaId) }.collectAsState(initial = null)
    val barberia = resultado?.getOrNull()
    val disponible = barberia != null && barberia.activa

    LaunchedEffect(resultado) {
        if (resultado?.isSuccess == true && !disponible) alNoEstarDisponible()
    }

    val modificador = Modifier.fillMaxSize().background(DarkBackground)
    when {
        resultado?.isFailure == true -> MensajeError(
            mensaje = resultado?.exceptionOrNull()?.message ?: "Ocurrió un error.",
            alReintentar = { intento++ },
            modifier = modificador
        )
        barberia == null || !disponible -> PantallaCargando(modificador)
        else -> CatalogoDeBarberia(barberia, alCambiarBarberia, alReservar)
    }
}

@Composable
private fun CatalogoDeBarberia(
    barberia: Barberia,
    alCambiarBarberia: () -> Unit,
    alReservar: (Servicio) -> Unit,
    servicioRepository: ServicioRepository = remember { ServicioRepository() }
) {
    var intento by remember { mutableIntStateOf(0) }
    val resultado by remember(barberia.id, intento) {
        servicioRepository.observarServiciosActivos(barberia.id)
    }.collectAsState(initial = null)

    Column(modifier = Modifier.fillMaxSize().background(DarkBackground)) {
        EncabezadoBarberia(barberia, alCambiarBarberia)
        Text(
            "Nuestros Servicios",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
        )
        ContenidoSegunResultado(
            resultado = resultado,
            alReintentar = { intento++ },
            modifier = Modifier.fillMaxSize(),
            mensajeVacio = "Esta barbería aún no tiene servicios disponibles."
        ) { servicios ->
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(servicios, key = { it.id }) { servicio ->
                    ServicioCard(servicio, alReservar = { alReservar(servicio) })
                }
            }
        }
    }
}

@Composable
private fun EncabezadoBarberia(barberia: Barberia, alCambiar: () -> Unit) {
    val abierta = estaAbierta(barberia, LocalDateTime.now())
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            FotoOInicial(barberia.fotoUrl, barberia.nombre, tamano = 44.dp, forma = RoundedCornerShape(10.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(barberia.nombre, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(
                    "${if (abierta) "Abierto" else "Cerrado"} · ${barberia.horaApertura} - ${barberia.horaCierre}",
                    color = TextGray,
                    fontSize = 12.sp
                )
            }
            TextButton(onClick = alCambiar) {
                Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = GoldAccent)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Cambiar", color = GoldAccent, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ServicioCard(servicio: Servicio, alReservar: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (servicio.fotoUrl != null) {
                    FotoOInicial(servicio.fotoUrl, servicio.nombre, tamano = 64.dp, forma = RoundedCornerShape(12.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(servicio.nombre, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(servicio.descripcion, color = TextGray, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("${servicio.duracionMinutos} min", color = GoldAccent, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text("S/ ${servicio.precio}", color = GoldAccent, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = alReservar,
                modifier = Modifier.fillMaxWidth().height(42.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Reservar", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}

