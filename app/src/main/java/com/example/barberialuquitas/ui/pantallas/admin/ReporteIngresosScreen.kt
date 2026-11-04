package com.example.barberialuquitas.ui.pantallas.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.barberialuquitas.data.modelo.Cita
import com.example.barberialuquitas.data.repositorio.CitaRepository
import com.example.barberialuquitas.ui.componentes.ContenidoSegunResultado
import com.example.barberialuquitas.ui.componentes.TarjetaDato
import com.example.barberialuquitas.ui.componentes.TituloSeccion
import com.example.barberialuquitas.ui.theme.CardBackground
import com.example.barberialuquitas.ui.theme.ErrorRed
import com.example.barberialuquitas.ui.theme.GoldAccent
import com.example.barberialuquitas.ui.theme.SuccessGreen
import com.example.barberialuquitas.ui.theme.TextGray
import com.example.barberialuquitas.util.FilaIngreso
import com.example.barberialuquitas.util.PeriodoReporte
import com.example.barberialuquitas.util.describirPeriodo
import com.example.barberialuquitas.util.formatearFechaLegible
import com.example.barberialuquitas.util.ingresosAgrupados
import com.example.barberialuquitas.util.moverPeriodo
import com.example.barberialuquitas.util.parsearFecha
import com.example.barberialuquitas.util.rangoDelPeriodo
import com.example.barberialuquitas.util.resumirCitas
import java.time.LocalDate
import java.time.LocalDateTime

// RF19: ingresos por día, semana o mes, calculados solo con las citas atendidas.
@Composable
fun ReporteIngresosScreen(navController: NavController) {
    PantallaAdministrador(navController, titulo = "Reporte de ingresos") { administrador ->
        Reporte(administrador.barberiaId)
    }
}

@Composable
private fun Reporte(barberiaId: String, citaRepository: CitaRepository = remember { CitaRepository() }) {
    var periodo by rememberSaveable { mutableStateOf(PeriodoReporte.SEMANA) }
    var referenciaTexto by rememberSaveable { mutableStateOf(LocalDate.now().toString()) }
    val referencia = parsearFecha(referenciaTexto) ?: LocalDate.now()
    val rango = rangoDelPeriodo(periodo, referencia)

    var intento by remember { mutableIntStateOf(0) }
    val resultado by remember(barberiaId, rango, intento) {
        citaRepository.observarCitasEntre(barberiaId, rango.desde.toString(), rango.hasta.toString())
    }.collectAsState(initial = null)

    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
        PeriodoReporte.entries.forEachIndexed { indice, opcion ->
            SegmentedButton(
                selected = periodo == opcion,
                onClick = { periodo = opcion },
                shape = SegmentedButtonDefaults.itemShape(indice, PeriodoReporte.entries.size),
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = GoldAccent,
                    activeContentColor = Color.Black,
                    inactiveContainerColor = CardBackground,
                    inactiveContentColor = TextGray
                )
            ) {
                Text(opcion.etiqueta, fontWeight = FontWeight.Bold)
            }
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { referenciaTexto = moverPeriodo(periodo, referencia, -1).toString() }) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Anterior", tint = GoldAccent)
        }
        Text(
            describirPeriodo(periodo, rango),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = { referenciaTexto = moverPeriodo(periodo, referencia, 1).toString() }) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Siguiente", tint = GoldAccent)
        }
    }

    ContenidoSegunResultado(resultado, alReintentar = { intento++ }, modifier = Modifier.fillMaxSize()) { citas ->
        DetalleReporte(citas, mostrarPorDia = periodo != PeriodoReporte.DIA)
    }
}

@Composable
private fun DetalleReporte(citas: List<Cita>, mostrarPorDia: Boolean) {
    val resumen = resumirCitas(citas, LocalDateTime.now())
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        TarjetaDato("Ingresos (citas atendidas)", "S/ ${resumen.ingresos}", Modifier.fillMaxWidth(), color = GoldAccent)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            TarjetaDato("Atendidas", "${resumen.atendidas}", Modifier.weight(1f), color = SuccessGreen)
            TarjetaDato("Promedio", "S/ ${resumen.ticketPromedio}", Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            TarjetaDato("No asistió", "${resumen.noAsistio}", Modifier.weight(1f), color = ErrorRed)
            TarjetaDato("Canceladas", "${resumen.canceladas}", Modifier.weight(1f), color = ErrorRed)
            TarjetaDato("Pendientes", "${resumen.pendientes + resumen.porConfirmar}", Modifier.weight(1f))
        }
        if (resumen.porConfirmar > 0) {
            Text(
                "${resumen.porConfirmar} cita(s) ya pasaron y aún no se marcan como atendidas: no suman ingresos " +
                    "hasta que las marques en la agenda.",
                color = TextGray,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(6.dp))
        TablaIngresos("Por barbero", ingresosAgrupados(citas) { it.barberoNombre })
        TablaIngresos("Por servicio", ingresosAgrupados(citas) { it.servicioNombre })
        if (mostrarPorDia) {
            TablaIngresos(
                "Por día",
                ingresosAgrupados(citas) { it.fecha }.sortedBy { it.nombre }.map { it.copy(nombre = formatearFechaLegible(it.nombre)) }
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun TablaIngresos(titulo: String, filas: List<FilaIngreso>) {
    TituloSeccion(titulo)
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)) {
            if (filas.isEmpty()) {
                Text("Sin citas atendidas en este periodo.", color = TextGray, fontSize = 13.sp, modifier = Modifier.padding(vertical = 8.dp))
            }
            filas.forEachIndexed { indice, fila ->
                if (indice > 0) HorizontalDivider(color = TextGray.copy(alpha = 0.2f))
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(fila.nombre, color = Color.White, fontSize = 14.sp)
                        Text("${fila.citas} cita(s)", color = TextGray, fontSize = 12.sp)
                    }
                    Text("S/ ${fila.ingresos}", color = GoldAccent, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
