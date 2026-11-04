package com.example.barberialuquitas.ui.pantallas.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.barberialuquitas.data.modelo.Barberia
import com.example.barberialuquitas.data.repositorio.BarberiaRepository
import com.example.barberialuquitas.ui.componentes.BotonPrincipal
import com.example.barberialuquitas.ui.componentes.ContenidoSegunResultado
import com.example.barberialuquitas.ui.componentes.CustomTextField
import com.example.barberialuquitas.ui.componentes.EtiquetaCampo
import com.example.barberialuquitas.ui.componentes.FilaInterruptor
import com.example.barberialuquitas.ui.componentes.SelectorDiasSemana
import com.example.barberialuquitas.ui.componentes.SelectorHora
import com.example.barberialuquitas.ui.componentes.TituloSeccion
import com.example.barberialuquitas.ui.componentes.coloresCalendarioBarberia
import com.example.barberialuquitas.ui.theme.CardBackground
import com.example.barberialuquitas.ui.theme.ErrorRed
import com.example.barberialuquitas.ui.theme.GoldAccent
import com.example.barberialuquitas.ui.theme.SuccessGreen
import com.example.barberialuquitas.ui.theme.TextGray
import com.example.barberialuquitas.util.fechaDesdeMillisUtc
import com.example.barberialuquitas.util.formatearFechaLegible
import com.example.barberialuquitas.util.millisUtcDesdeFecha
import com.example.barberialuquitas.util.soloDigitos
import java.time.LocalDate
import kotlinx.coroutines.launch

// RF17: datos de la barbería y su horario de atención (apertura, cierre, días y días no laborables).
// La disponibilidad que ve el cliente (RF06) se calcula con esta configuración y Firestore la vuelve a validar.
@Composable
fun HorarioAtencionScreen(
    navController: NavController,
    barberiaRepository: BarberiaRepository = remember { BarberiaRepository() }
) {
    PantallaAdministrador(navController, titulo = "Mi barbería y horario") { administrador ->
        var intento by remember { mutableIntStateOf(0) }
        // Se lee una sola vez para que el formulario no se reinicie mientras el administrador escribe.
        val resultado by remember(administrador.barberiaId, intento) {
            barberiaRepository.observarBarberia(administrador.barberiaId)
        }.collectAsState(initial = null)
        var inicial by remember { mutableStateOf<Barberia?>(null) }
        LaunchedEffect(resultado) {
            if (inicial == null) inicial = resultado?.getOrNull()
        }

        val barberia = inicial
        if (barberia != null) {
            FormularioHorario(barberia, barberiaRepository)
        } else {
            ContenidoSegunResultado(resultado, alReintentar = { intento++ }, modifier = Modifier.fillMaxSize()) {}
        }
    }
}

@Composable
private fun FormularioHorario(inicial: Barberia, barberiaRepository: BarberiaRepository) {
    var nombre by remember { mutableStateOf(inicial.nombre) }
    var direccion by remember { mutableStateOf(inicial.direccion) }
    var telefono by remember { mutableStateOf(inicial.telefono) }
    var fotoUrl by remember { mutableStateOf(inicial.fotoUrl.orEmpty()) }
    var activa by remember { mutableStateOf(inicial.activa) }
    var apertura by remember { mutableStateOf(inicial.horaApertura) }
    var cierre by remember { mutableStateOf(inicial.horaCierre) }
    var dias by remember { mutableStateOf(inicial.diasLaborables) }
    var noLaborables by remember { mutableStateOf(inicial.diasNoLaborables) }

    var mensajeError by remember { mutableStateOf<String?>(null) }
    var guardado by remember { mutableStateOf(false) }
    var guardando by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun guardar() {
        mensajeError = when {
            nombre.isBlank() -> "Ingresa el nombre de la barbería."
            apertura >= cierre -> "La hora de cierre debe ser posterior a la de apertura."
            dias.isEmpty() -> "Elige al menos un día de atención."
            else -> null
        }
        if (mensajeError != null) return
        guardando = true
        guardado = false
        scope.launch {
            barberiaRepository.guardarBarberia(
                inicial.copy(
                    nombre = nombre,
                    direccion = direccion,
                    telefono = telefono,
                    fotoUrl = fotoUrl.ifBlank { null },
                    activa = activa,
                    horaApertura = apertura,
                    horaCierre = cierre,
                    diasLaborables = dias,
                    diasNoLaborables = noLaborables
                )
            )
                .onSuccess { guardado = true }
                .onFailure { mensajeError = it.message }
            guardando = false
        }
    }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
        TituloSeccion("Datos de la barbería")
        Spacer(modifier = Modifier.height(12.dp))
        CustomTextField("NOMBRE", "Ej. Barbería Luquitas", nombre, { nombre = it }, Icons.Default.Storefront)
        Spacer(modifier = Modifier.height(12.dp))
        CustomTextField("DIRECCIÓN", "Av. Principal 123", direccion, { direccion = it }, Icons.Default.LocationOn)
        Spacer(modifier = Modifier.height(12.dp))
        CustomTextField(
            "TELÉFONO", "987654321", telefono, { telefono = it.take(9) }, Icons.Default.Phone,
            KeyboardType.Phone, filtro = ::soloDigitos
        )
        Spacer(modifier = Modifier.height(12.dp))
        CustomTextField(
            "FOTO O LOGO (URL)", "https://…/logo.png", fotoUrl, { fotoUrl = it.trim() }, Icons.Default.Image,
            KeyboardType.Uri
        )
        Spacer(modifier = Modifier.height(12.dp))
        FilaInterruptor(
            titulo = "Visible para clientes",
            descripcion = if (activa) "Aparece en la lista de barberías." else "Oculta: nadie puede reservar.",
            activo = activa,
            alCambiar = { activa = it }
        )

        Spacer(modifier = Modifier.height(28.dp))
        TituloSeccion("Horario de atención")
        Spacer(modifier = Modifier.height(12.dp))
        Row {
            SelectorHora("APERTURA", apertura, alElegir = { apertura = it }, modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(12.dp))
            SelectorHora("CIERRE", cierre, alElegir = { cierre = it }, modifier = Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(16.dp))
        EtiquetaCampo("DÍAS DE ATENCIÓN")
        Spacer(modifier = Modifier.height(4.dp))
        SelectorDiasSemana(dias, alCambiar = { dias = it })

        Spacer(modifier = Modifier.height(28.dp))
        TituloSeccion("Días no laborables")
        Text("Feriados o vacaciones: ese día nadie podrá reservar.", color = TextGray, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(8.dp))
        DiasNoLaborables(noLaborables, alCambiar = { noLaborables = it })

        mensajeError?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Text(it, color = ErrorRed, fontSize = 13.sp)
        }
        if (guardado) {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Cambios guardados.", color = SuccessGreen, fontSize = 13.sp)
        }
        Spacer(modifier = Modifier.height(24.dp))
        BotonPrincipal("Guardar cambios", alPulsar = ::guardar, cargando = guardando)
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DiasNoLaborables(fechas: List<String>, alCambiar: (List<String>) -> Unit) {
    var mostrarCalendario by remember { mutableStateOf(false) }
    val hoy = remember { LocalDate.now() }
    // Las fechas que ya pasaron no se muestran (siguen guardadas pero ya no afectan reservas).
    val proximas = fechas.filter { it >= hoy.toString() }

    if (proximas.isEmpty()) {
        Text("No hay días no laborables programados.", color = TextGray, fontSize = 13.sp)
    }
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        proximas.forEach { fecha ->
            InputChip(
                selected = false,
                onClick = { alCambiar(fechas - fecha) },
                label = { Text(formatearFechaLegible(fecha), fontSize = 12.sp) },
                trailingIcon = {
                    Icon(Icons.Default.Close, contentDescription = "Quitar", modifier = Modifier.size(16.dp))
                },
                colors = InputChipDefaults.inputChipColors(
                    containerColor = CardBackground,
                    labelColor = Color.White,
                    trailingIconColor = ErrorRed
                )
            )
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
    OutlinedButton(
        onClick = { mostrarCalendario = true },
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = CardBackground, contentColor = GoldAccent),
        shape = RoundedCornerShape(8.dp)
    ) {
        Icon(Icons.Default.EventBusy, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Agregar día no laborable", fontWeight = FontWeight.Bold)
    }

    if (mostrarCalendario) {
        val estado = rememberDatePickerState(
            initialSelectedDateMillis = millisUtcDesdeFecha(hoy),
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long) = !fechaDesdeMillisUtc(utcTimeMillis).isBefore(hoy)
                override fun isSelectableYear(year: Int) = year >= hoy.year
            }
        )
        DatePickerDialog(
            onDismissRequest = { mostrarCalendario = false },
            colors = coloresCalendarioBarberia(),
            confirmButton = {
                TextButton(onClick = {
                    estado.selectedDateMillis?.let { millis ->
                        alCambiar((fechas + fechaDesdeMillisUtc(millis).toString()).distinct().sorted())
                    }
                    mostrarCalendario = false
                }) {
                    Text("Agregar", color = GoldAccent, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarCalendario = false }) { Text("Cancelar", color = TextGray) }
            }
        ) {
            DatePicker(state = estado, colors = coloresCalendarioBarberia())
        }
    }
}
