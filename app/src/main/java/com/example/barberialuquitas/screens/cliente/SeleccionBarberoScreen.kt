package com.example.barberialuquitas.screens.cliente

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
import com.example.barberialuquitas.components.BarberoCard
import com.example.barberialuquitas.components.EncabezadoConAtras
import com.example.barberialuquitas.components.MensajeError
import com.example.barberialuquitas.components.MensajeVacio
import com.example.barberialuquitas.components.PantallaCargando
import com.example.barberialuquitas.data.BarberoRepository
import com.example.barberialuquitas.data.Servicio
import com.example.barberialuquitas.data.ServicioRepository
import com.example.barberialuquitas.navigation.Rutas
import com.example.barberialuquitas.ui.theme.DarkBackground
import com.example.barberialuquitas.ui.theme.TextGray

@Composable
fun SeleccionBarberoScreen(
    navController: NavController,
    servicioId: String,
    barberoRepository: BarberoRepository = remember { BarberoRepository() },
    servicioRepository: ServicioRepository = remember { ServicioRepository() }
) {
    var intento by remember { mutableIntStateOf(0) }
    val resultado by remember(intento) { barberoRepository.observarBarberos() }.collectAsState(initial = null)
    var servicio by remember { mutableStateOf<Servicio?>(null) }

    LaunchedEffect(servicioId) {
        servicioRepository.obtenerServicio(servicioId).onSuccess { servicio = it }
    }

    Column(modifier = Modifier.fillMaxSize().background(DarkBackground)) {
        EncabezadoConAtras("Elige tu barbero") { navController.popBackStack() }
        servicio?.let {
            Text(
                "Servicio: ${it.nombre} · S/ ${it.precio}",
                color = TextGray,
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }

        val barberos = resultado?.getOrNull()
        when {
            resultado == null -> PantallaCargando()
            barberos == null -> MensajeError(
                mensaje = resultado?.exceptionOrNull()?.message ?: "Ocurrió un error.",
                alReintentar = { intento++ }
            )
            barberos.isEmpty() -> MensajeVacio("No hay barberos disponibles en este momento.")
            else -> LazyColumn(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(barberos, key = { it.id }) { barbero ->
                    BarberoCard(barbero) {
                        navController.navigate(Rutas.seleccionHorario(servicioId, barbero.id))
                    }
                }
            }
        }
    }
}
