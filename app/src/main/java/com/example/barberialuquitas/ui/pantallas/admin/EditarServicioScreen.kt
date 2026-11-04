package com.example.barberialuquitas.ui.pantallas.admin

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Payments
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
import com.example.barberialuquitas.data.modelo.Servicio
import com.example.barberialuquitas.data.repositorio.ServicioRepository
import com.example.barberialuquitas.ui.componentes.BotonPrincipal
import com.example.barberialuquitas.ui.componentes.ContenidoSegunResultado
import com.example.barberialuquitas.ui.componentes.CustomTextField
import com.example.barberialuquitas.ui.componentes.FilaInterruptor
import com.example.barberialuquitas.ui.componentes.FotoOInicial
import com.example.barberialuquitas.ui.componentes.SelectorDesplegable
import com.example.barberialuquitas.ui.navegacion.Rutas
import com.example.barberialuquitas.ui.theme.ErrorRed
import com.example.barberialuquitas.ui.theme.TextGray
import com.example.barberialuquitas.util.DURACION_BLOQUE_MINUTOS
import com.example.barberialuquitas.util.MAX_BLOQUES_POR_CITA
import com.example.barberialuquitas.util.soloDigitos
import kotlinx.coroutines.launch

// Duraciones permitidas: de 15 en 15 minutos hasta el máximo que aceptan las reglas (4 bloques = 2 horas).
private val DURACIONES = (15..MAX_BLOQUES_POR_CITA * DURACION_BLOQUE_MINUTOS step 15).toList()

// RF13: agregar (servicioId = Rutas.NUEVO) o editar un servicio.
@Composable
fun EditarServicioScreen(
    navController: NavController,
    servicioId: String,
    servicioRepository: ServicioRepository = remember { ServicioRepository() }
) {
    val esNuevo = servicioId == Rutas.NUEVO
    PantallaAdministrador(navController, titulo = if (esNuevo) "Nuevo servicio" else "Editar servicio") { administrador ->
        var intento by remember { mutableIntStateOf(0) }
        var servicio by remember { mutableStateOf<Result<Servicio>?>(null) }

        LaunchedEffect(servicioId, intento) {
            servicio = if (esNuevo) {
                Result.success(Servicio(barberiaId = administrador.barberiaId))
            } else {
                servicioRepository.obtenerServicio(servicioId)
            }
        }

        ContenidoSegunResultado(servicio, alReintentar = { intento++ }, modifier = Modifier.fillMaxSize()) { inicial ->
            FormularioServicio(inicial, servicioRepository, alGuardar = { navController.popBackStack() })
        }
    }
}

@Composable
private fun FormularioServicio(inicial: Servicio, servicioRepository: ServicioRepository, alGuardar: () -> Unit) {
    var nombre by remember { mutableStateOf(inicial.nombre) }
    var descripcion by remember { mutableStateOf(inicial.descripcion) }
    var precio by remember { mutableStateOf(if (inicial.id.isBlank()) "" else inicial.precio.toString()) }
    var duracion by remember { mutableIntStateOf(inicial.duracionMinutos.coerceIn(DURACIONES.first(), DURACIONES.last())) }
    var fotoUrl by remember { mutableStateOf(inicial.fotoUrl.orEmpty()) }
    var activo by remember { mutableStateOf(inicial.activo) }

    var nombreError by remember { mutableStateOf<String?>(null) }
    var precioError by remember { mutableStateOf<String?>(null) }
    var mensajeError by remember { mutableStateOf<String?>(null) }
    var guardando by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun guardar() {
        nombreError = if (nombre.isBlank()) "Ingresa el nombre del servicio" else null
        precioError = if (precio.toLongOrNull() == null) "Ingresa el precio en soles" else null
        if (nombreError != null || precioError != null) return
        guardando = true
        mensajeError = null
        scope.launch {
            servicioRepository.guardarServicio(
                inicial.copy(
                    nombre = nombre,
                    descripcion = descripcion,
                    precio = precio.toLong(),
                    duracionMinutos = duracion,
                    fotoUrl = fotoUrl.ifBlank { null },
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
            "NOMBRE", "Ej. Corte clásico", nombre, { nombre = it }, Icons.Default.ContentCut, errorText = nombreError
        )
        Spacer(modifier = Modifier.height(16.dp))
        CustomTextField(
            "DESCRIPCIÓN", "Ej. Corte con tijera y máquina", descripcion, { descripcion = it },
            Icons.AutoMirrored.Filled.Notes
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row {
            Column(modifier = Modifier.weight(1f)) {
                CustomTextField(
                    "PRECIO (S/)", "25", precio, { precio = it.take(5) }, Icons.Default.Payments,
                    KeyboardType.Number, errorText = precioError, filtro = ::soloDigitos
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            SelectorDesplegable(
                etiqueta = "DURACIÓN",
                seleccionado = duracion,
                opciones = DURACIONES,
                texto = { "$it min" },
                alElegir = { duracion = it },
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        CustomTextField(
            "FOTO (URL)", "https://…/corte.jpg", fotoUrl, { fotoUrl = it.trim() }, Icons.Default.Image,
            KeyboardType.Uri
        )
        if (fotoUrl.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                FotoOInicial(fotoUrl, nombre.ifBlank { "?" }, tamano = 72.dp, forma = RoundedCornerShape(12.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text("Vista previa", color = TextGray, fontSize = 12.sp)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        FilaInterruptor(
            titulo = "Activo",
            descripcion = if (activo) "Los clientes pueden verlo y reservarlo." else "Oculto en el catálogo.",
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
