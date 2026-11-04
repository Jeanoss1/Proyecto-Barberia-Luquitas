package com.example.barberialuquitas.ui.componentes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.barberialuquitas.ui.theme.CardBackground
import com.example.barberialuquitas.ui.theme.GoldAccent
import com.example.barberialuquitas.ui.theme.TextGray
import com.example.barberialuquitas.util.opcionesDeHora

// Piezas reutilizables para los formularios y paneles del administrador.

@Composable
fun TituloSeccion(texto: String, modifier: Modifier = Modifier) {
    Text(texto, color = GoldAccent, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = modifier)
}

@Composable
fun EtiquetaCampo(texto: String) {
    Text(texto, color = TextGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
}

@Composable
fun BotonPrincipal(
    texto: String,
    alPulsar: () -> Unit,
    modifier: Modifier = Modifier,
    cargando: Boolean = false,
    habilitado: Boolean = true
) {
    Button(
        onClick = alPulsar,
        enabled = habilitado && !cargando,
        modifier = modifier.fillMaxWidth().height(50.dp),
        colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
        shape = RoundedCornerShape(8.dp)
    ) {
        if (cargando) {
            CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(20.dp))
        } else {
            Text(texto, color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// Interruptor con título y explicación, p. ej. "Activo · Visible en el catálogo".
@Composable
fun FilaInterruptor(titulo: String, descripcion: String, activo: Boolean, alCambiar: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.weight(1f)) {
            Text(titulo, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Text(descripcion, color = TextGray, fontSize = 12.sp)
        }
        InterruptorActivo(activo, alCambiar)
    }
}

@Composable
fun InterruptorActivo(activo: Boolean, alCambiar: (Boolean) -> Unit, habilitado: Boolean = true) {
    Switch(
        checked = activo,
        onCheckedChange = alCambiar,
        enabled = habilitado,
        colors = SwitchDefaults.colors(
            checkedThumbColor = Color.Black,
            checkedTrackColor = GoldAccent,
            uncheckedThumbColor = TextGray,
            uncheckedTrackColor = CardBackground
        )
    )
}

private val DIAS_CORTOS = listOf(1 to "Lun", 2 to "Mar", 3 to "Mié", 4 to "Jue", 5 to "Vie", 6 to "Sáb", 7 to "Dom")

// Días de atención (1 = lunes ... 7 = domingo).
@Composable
fun SelectorDiasSemana(seleccionados: List<Int>, alCambiar: (List<Int>) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        DIAS_CORTOS.forEach { (dia, nombre) ->
            val seleccionado = dia in seleccionados
            FilterChip(
                selected = seleccionado,
                onClick = { alCambiar(if (seleccionado) seleccionados - dia else (seleccionados + dia).sorted()) },
                label = { Text(nombre, fontWeight = FontWeight.Bold) },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = CardBackground,
                    labelColor = TextGray,
                    selectedContainerColor = GoldAccent,
                    selectedLabelColor = Color.Black
                )
            )
        }
    }
}

// Lista desplegable sencilla.
@Composable
fun <T> SelectorDesplegable(
    etiqueta: String,
    seleccionado: T,
    opciones: List<T>,
    texto: (T) -> String,
    alElegir: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    var abierto by remember { mutableStateOf(false) }
    Column(modifier = modifier) {
        EtiquetaCampo(etiqueta)
        Spacer(modifier = Modifier.height(4.dp))
        Box {
            OutlinedButton(
                onClick = { abierto = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = CardBackground, contentColor = Color.White),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(texto(seleccionado), modifier = Modifier.weight(1f))
                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = GoldAccent)
            }
            DropdownMenu(
                expanded = abierto,
                onDismissRequest = { abierto = false },
                modifier = Modifier.heightIn(max = 320.dp)
            ) {
                opciones.forEach { opcion ->
                    DropdownMenuItem(
                        text = { Text(texto(opcion)) },
                        onClick = {
                            alElegir(opcion)
                            abierto = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SelectorHora(etiqueta: String, hora: String, alElegir: (String) -> Unit, modifier: Modifier = Modifier) {
    SelectorDesplegable(etiqueta, hora, opcionesDeHora(), texto = { it }, alElegir = alElegir, modifier = modifier)
}

// Indicador numérico para el dashboard y el reporte.
@Composable
fun TarjetaDato(titulo: String, valor: String, modifier: Modifier = Modifier, color: Color = Color.White) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(titulo, color = TextGray, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(valor, color = color, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
    }
}
