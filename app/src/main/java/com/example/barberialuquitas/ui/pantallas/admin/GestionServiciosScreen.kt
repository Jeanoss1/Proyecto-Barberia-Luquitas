package com.example.barberialuquitas.ui.pantallas.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.barberialuquitas.data.modelo.Servicio
import com.example.barberialuquitas.data.repositorio.ServicioRepository
import com.example.barberialuquitas.ui.componentes.ContenidoSegunResultado
import com.example.barberialuquitas.ui.componentes.FotoOInicial
import com.example.barberialuquitas.ui.componentes.InterruptorActivo
import com.example.barberialuquitas.ui.navegacion.Rutas
import com.example.barberialuquitas.ui.theme.CardBackground
import com.example.barberialuquitas.ui.theme.ErrorRed
import com.example.barberialuquitas.ui.theme.GoldAccent
import com.example.barberialuquitas.ui.theme.TextGray
import kotlinx.coroutines.launch

// RF13: lista de servicios de la barbería. Tocar uno lo edita; el interruptor lo activa o desactiva.
// Los cambios se ven al instante en el catálogo del cliente porque ambos escuchan Firestore en tiempo real.
@Composable
fun GestionServiciosScreen(navController: NavController) {
    PantallaAdministrador(
        navController,
        titulo = "Servicios",
        botonFlotante = { BotonFlotanteAgregar("Nuevo servicio") { navController.navigate(Rutas.editarServicio()) } }
    ) { administrador ->
        ListaDeServicios(
            barberiaId = administrador.barberiaId,
            alEditar = { navController.navigate(Rutas.editarServicio(it.id)) }
        )
    }
}

@Composable
private fun ListaDeServicios(
    barberiaId: String,
    alEditar: (Servicio) -> Unit,
    servicioRepository: ServicioRepository = remember { ServicioRepository() }
) {
    var intento by remember { mutableIntStateOf(0) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val resultado by remember(barberiaId, intento) {
        servicioRepository.observarServiciosDeBarberia(barberiaId)
    }.collectAsState(initial = null)

    error?.let { Text(it, color = ErrorRed, fontSize = 13.sp, modifier = Modifier.padding(horizontal = 20.dp)) }
    ContenidoSegunResultado(
        resultado = resultado,
        alReintentar = { intento++ },
        modifier = Modifier.fillMaxSize(),
        mensajeVacio = "Aún no registras servicios. Pulsa \"Nuevo servicio\" para agregar el primero."
    ) { servicios ->
        LazyColumn(
            // Espacio extra abajo para que el botón flotante no tape el último servicio.
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(servicios, key = { it.id }) { servicio ->
                FilaServicio(
                    servicio = servicio,
                    alEditar = { alEditar(servicio) },
                    alCambiarActivo = { activo ->
                        error = null
                        scope.launch {
                            servicioRepository.cambiarActivo(servicio.id, activo).onFailure { error = it.message }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun FilaServicio(servicio: Servicio, alEditar: () -> Unit, alCambiarActivo: (Boolean) -> Unit) {
    Card(
        onClick = alEditar,
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            FotoOInicial(servicio.fotoUrl, servicio.nombre, tamano = 48.dp, forma = RoundedCornerShape(10.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    servicio.nombre,
                    color = if (servicio.activo) Color.White else TextGray,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text("S/ ${servicio.precio} · ${servicio.duracionMinutos} min", color = GoldAccent, fontSize = 13.sp)
                Text(
                    if (servicio.activo) "Visible en el catálogo" else "Desactivado",
                    color = TextGray,
                    fontSize = 12.sp
                )
            }
            InterruptorActivo(servicio.activo, alCambiarActivo)
        }
    }
}
