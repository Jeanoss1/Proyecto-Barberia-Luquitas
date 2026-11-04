package com.example.barberialuquitas.ui.pantallas.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.barberialuquitas.data.modelo.Barbero
import com.example.barberialuquitas.data.repositorio.BarberoRepository
import com.example.barberialuquitas.ui.componentes.BarberoCard
import com.example.barberialuquitas.ui.componentes.ContenidoSegunResultado
import com.example.barberialuquitas.ui.componentes.InterruptorActivo
import com.example.barberialuquitas.ui.navegacion.Rutas
import com.example.barberialuquitas.ui.theme.ErrorRed
import com.example.barberialuquitas.ui.theme.TextGray
import kotlinx.coroutines.launch

// RF14: barberos de la barbería. Tocar uno lo edita; el interruptor lo da de alta o de baja.
// Solo los activos aparecen al reservar (RF05).
@Composable
fun GestionBarberosScreen(navController: NavController) {
    PantallaAdministrador(
        navController,
        titulo = "Barberos",
        botonFlotante = { BotonFlotanteAgregar("Nuevo barbero") { navController.navigate(Rutas.editarBarbero()) } }
    ) { administrador ->
        ListaDeBarberos(
            barberiaId = administrador.barberiaId,
            alEditar = { navController.navigate(Rutas.editarBarbero(it.id)) }
        )
    }
}

@Composable
private fun ListaDeBarberos(
    barberiaId: String,
    alEditar: (Barbero) -> Unit,
    barberoRepository: BarberoRepository = remember { BarberoRepository() }
) {
    var intento by remember { mutableIntStateOf(0) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val resultado by remember(barberiaId, intento) {
        barberoRepository.observarBarberosDeBarberia(barberiaId)
    }.collectAsState(initial = null)

    Text(
        "Al dar de baja a un barbero deja de aparecer para reservar; sus citas ya reservadas se mantienen.",
        color = TextGray,
        fontSize = 12.sp,
        modifier = Modifier.padding(horizontal = 20.dp)
    )
    error?.let { Text(it, color = ErrorRed, fontSize = 13.sp, modifier = Modifier.padding(horizontal = 20.dp)) }
    ContenidoSegunResultado(
        resultado = resultado,
        alReintentar = { intento++ },
        modifier = Modifier.fillMaxSize(),
        mensajeVacio = "Aún no registras barberos. Pulsa \"Nuevo barbero\" para agregar el primero."
    ) { barberos ->
        LazyColumn(
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(barberos, key = { it.id }) { barbero ->
                BarberoCard(
                    barbero = barbero,
                    alElegir = { alEditar(barbero) },
                    accion = {
                        InterruptorActivo(barbero.activo, alCambiar = { activo ->
                            error = null
                            scope.launch {
                                barberoRepository.cambiarActivo(barbero.id, activo).onFailure { error = it.message }
                            }
                        })
                    }
                )
            }
        }
    }
}
