package com.example.barberialuquitas.ui.pantallas.cliente

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.barberialuquitas.data.modelo.Servicio
import com.example.barberialuquitas.data.repositorio.BarberoRepository
import com.example.barberialuquitas.data.repositorio.ServicioRepository
import com.example.barberialuquitas.ui.componentes.BarberoCard
import com.example.barberialuquitas.ui.componentes.ContenidoSegunResultado
import com.example.barberialuquitas.ui.componentes.EncabezadoConAtras
import com.example.barberialuquitas.ui.navegacion.Rutas
import com.example.barberialuquitas.ui.theme.DarkBackground
import com.example.barberialuquitas.ui.theme.TextGray

// RF05: barberos activos de la barbería a la que pertenece el servicio elegido.
@Composable
fun SeleccionBarberoScreen(
    navController: NavController,
    servicioId: String,
    servicioRepository: ServicioRepository = remember { ServicioRepository() }
) {
    var intento by remember { mutableIntStateOf(0) }
    var servicio by remember { mutableStateOf<Result<Servicio>?>(null) }

    LaunchedEffect(servicioId, intento) {
        servicio = null
        servicio = servicioRepository.obtenerServicio(servicioId)
    }

    Column(modifier = Modifier.fillMaxSize().background(DarkBackground)) {
        EncabezadoConAtras("Elige tu barbero") { navController.popBackStack() }
        ContenidoSegunResultado(servicio, alReintentar = { intento++ }, modifier = Modifier.fillMaxSize()) {
            BarberosDelServicio(it) { barberoId ->
                navController.navigate(Rutas.seleccionHorario(servicioId, barberoId))
            }
        }
    }
}

@Composable
private fun BarberosDelServicio(
    servicio: Servicio,
    barberoRepository: BarberoRepository = remember { BarberoRepository() },
    alElegir: (barberoId: String) -> Unit
) {
    var intento by remember { mutableIntStateOf(0) }
    val resultado by remember(servicio.barberiaId, intento) {
        barberoRepository.observarBarberosActivos(servicio.barberiaId)
    }.collectAsState(initial = null)

    Column {
        Text(
            "Servicio: ${servicio.nombre} · S/ ${servicio.precio} · ${servicio.duracionMinutos} min",
            color = TextGray,
            fontSize = 14.sp,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        ContenidoSegunResultado(
            resultado = resultado,
            alReintentar = { intento++ },
            modifier = Modifier.fillMaxSize(),
            mensajeVacio = "No hay barberos disponibles en este momento."
        ) { barberos ->
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(barberos, key = { it.id }) { barbero ->
                    BarberoCard(barbero, alElegir = { alElegir(barbero.id) })
                }
            }
        }
    }
}
