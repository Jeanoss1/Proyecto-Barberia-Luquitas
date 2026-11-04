package com.example.barberialuquitas.ui.pantallas.admin

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.barberialuquitas.data.modelo.Barbero
import com.example.barberialuquitas.data.repositorio.BarberoRepository
import com.example.barberialuquitas.ui.componentes.BotonPrincipal
import com.example.barberialuquitas.ui.componentes.ContenidoSegunResultado
import com.example.barberialuquitas.ui.componentes.CustomTextField
import com.example.barberialuquitas.ui.componentes.EtiquetaCampo
import com.example.barberialuquitas.ui.componentes.FilaInterruptor
import com.example.barberialuquitas.ui.componentes.FotoOInicial
import com.example.barberialuquitas.ui.componentes.SelectorDiasSemana
import com.example.barberialuquitas.ui.componentes.SelectorHora
import com.example.barberialuquitas.ui.navegacion.Rutas
import com.example.barberialuquitas.ui.theme.ErrorRed
import com.example.barberialuquitas.ui.theme.TextGray
import com.example.barberialuquitas.util.soloLetras
import kotlinx.coroutines.launch

// RF14: registrar (barberoId = Rutas.NUEVO) o editar un barbero.
@Composable
fun EditarBarberoScreen(
    navController: NavController,
    barberoId: String,
    barberoRepository: BarberoRepository = remember { BarberoRepository() }
) {
    val esNuevo = barberoId == Rutas.NUEVO
    PantallaAdministrador(navController, titulo = if (esNuevo) "Nuevo barbero" else "Editar barbero") { administrador ->
        var intento by remember { mutableIntStateOf(0) }
        var barbero by remember { mutableStateOf<Result<Barbero>?>(null) }

        LaunchedEffect(barberoId, intento) {
            barbero = if (esNuevo) {
                Result.success(
                    Barbero(barberiaId = administrador.barberiaId, activo = true, diasLaborables = listOf(1, 2, 3, 4, 5, 6))
                )
            } else {
                barberoRepository.obtenerBarbero(barberoId)
            }
        }

        ContenidoSegunResultado(barbero, alReintentar = { intento++ }, modifier = Modifier.fillMaxSize()) { inicial ->
            FormularioBarbero(inicial, barberoRepository, alGuardar = { navController.popBackStack() })
        }
    }
}

@Composable
private fun FormularioBarbero(inicial: Barbero, barberoRepository: BarberoRepository, alGuardar: () -> Unit) {
    var nombre by remember { mutableStateOf(inicial.nombre) }
    var especialidad by remember { mutableStateOf(inicial.especialidad) }
    var fotoUrl by remember { mutableStateOf(inicial.fotoUrl.orEmpty()) }
    var horaInicio by remember { mutableStateOf(inicial.horaInicio) }
    var horaFin by remember { mutableStateOf(inicial.horaFin) }
    var dias by remember { mutableStateOf(inicial.diasLaborables) }
    var activo by remember { mutableStateOf(inicial.activo) }

    var nombreError by remember { mutableStateOf<String?>(null) }
    var mensajeError by remember { mutableStateOf<String?>(null) }
    var guardando by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun guardar() {
        nombreError = if (nombre.isBlank()) "Ingresa el nombre del barbero" else null
        mensajeError = when {
            horaInicio >= horaFin -> "La hora de salida debe ser posterior a la de entrada."
            dias.isEmpty() -> "Elige al menos un día de atención."
            else -> null
        }
        if (nombreError != null || mensajeError != null) return
        guardando = true
        scope.launch {
            barberoRepository.guardarBarbero(
                inicial.copy(
                    nombre = nombre,
                    especialidad = especialidad,
                    fotoUrl = fotoUrl.ifBlank { null },
                    horaInicio = horaInicio,
                    horaFin = horaFin,
                    diasLaborables = dias,
                    activo = activo
                )
            )
                .onSuccess { alGuardar() }
                .onFailure { mensajeError = it.message }
            guardando = false
        }
    }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
        CustomTextField(
            "NOMBRE", "Ej. Luquitas", nombre, { nombre = it }, Icons.Default.Person,
            errorText = nombreError, filtro = ::soloLetras
        )
        Spacer(modifier = Modifier.height(16.dp))
        CustomTextField("ESPECIALIDAD", "Ej. Fade y diseño", especialidad, { especialidad = it }, Icons.Default.Star)
        Spacer(modifier = Modifier.height(16.dp))
        CustomTextField(
            "FOTO (URL)", "https://…/barbero.jpg", fotoUrl, { fotoUrl = it.trim() }, Icons.Default.Image,
            KeyboardType.Uri
        )
        if (fotoUrl.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                FotoOInicial(fotoUrl, nombre.ifBlank { "?" }, tamano = 64.dp)
                Spacer(modifier = Modifier.width(12.dp))
                Text("Vista previa", color = TextGray, fontSize = 12.sp)
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        EtiquetaCampo("DÍAS QUE ATIENDE")
        Spacer(modifier = Modifier.height(4.dp))
        SelectorDiasSemana(dias, alCambiar = { dias = it })
        Spacer(modifier = Modifier.height(16.dp))
        Row {
            SelectorHora("ENTRADA", horaInicio, alElegir = { horaInicio = it }, modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(12.dp))
            SelectorHora("SALIDA", horaFin, alElegir = { horaFin = it }, modifier = Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            "Solo se podrá reservar dentro del horario de atención de la barbería.",
            color = TextGray,
            fontSize = 12.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        FilaInterruptor(
            titulo = "Activo",
            descripcion = if (activo) "Aparece para reservar." else "Dado de baja: no aparece para reservar.",
            activo = activo,
            alCambiar = { activo = it }
        )

        mensajeError?.let {
            Spacer(modifier = Modifier.height(12.dp))
            Text(it, color = ErrorRed, fontSize = 13.sp)
        }
        Spacer(modifier = Modifier.height(24.dp))
        BotonPrincipal("Guardar", alPulsar = ::guardar, cargando = guardando)
        Spacer(modifier = Modifier.height(24.dp))
    }
}
