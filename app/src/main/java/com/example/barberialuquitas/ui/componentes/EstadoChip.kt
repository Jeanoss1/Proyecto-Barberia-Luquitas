package com.example.barberialuquitas.ui.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.barberialuquitas.data.modelo.EstadosCita
import com.example.barberialuquitas.ui.theme.ErrorRed
import com.example.barberialuquitas.ui.theme.GoldAccent
import com.example.barberialuquitas.ui.theme.InfoBlue
import com.example.barberialuquitas.ui.theme.SuccessGreen
import com.example.barberialuquitas.ui.theme.TextGray

@Composable
fun EstadoChip(estado: String) {
    val color = colorDeEstado(estado)
    Text(
        EstadosCita.etiqueta(estado),
        color = color,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}

fun colorDeEstado(estado: String): Color = when (estado) {
    EstadosCita.RESERVADA -> GoldAccent
    EstadosCita.ATENDIDA, EstadosCita.COMPLETADA -> SuccessGreen
    EstadosCita.CANCELADA, EstadosCita.NO_ASISTIO -> ErrorRed
    EstadosCita.POR_CONFIRMAR -> InfoBlue
    else -> TextGray
}

@Preview
@Composable
private fun EstadoChipPreview() {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        EstadoChip(EstadosCita.RESERVADA)
        EstadoChip(EstadosCita.POR_CONFIRMAR)
        EstadoChip(EstadosCita.ATENDIDA)
        EstadoChip(EstadosCita.NO_ASISTIO)
        EstadoChip(EstadosCita.CANCELADA)
    }
}
