package com.example.barberialuquitas.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import com.example.barberialuquitas.ui.theme.CardBackground
import com.example.barberialuquitas.ui.theme.GoldAccent
import com.example.barberialuquitas.ui.theme.TextGray
import com.example.barberialuquitas.util.BloqueHorario

@Composable
fun BloqueHorarioChip(bloque: BloqueHorario, seleccionado: Boolean, alPulsar: () -> Unit) {
    FilterChip(
        selected = seleccionado,
        onClick = alPulsar,
        enabled = bloque.disponible,
        label = {
            Text(
                bloque.hora,
                fontWeight = FontWeight.Bold,
                textDecoration = if (bloque.disponible) null else TextDecoration.LineThrough
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            containerColor = CardBackground,
            labelColor = Color.White,
            selectedContainerColor = GoldAccent,
            selectedLabelColor = Color.Black,
            disabledContainerColor = CardBackground.copy(alpha = 0.4f),
            disabledLabelColor = TextGray.copy(alpha = 0.5f)
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = bloque.disponible,
            selected = seleccionado,
            borderColor = GoldAccent.copy(alpha = 0.5f),
            selectedBorderColor = GoldAccent,
            disabledBorderColor = Color.Transparent
        )
    )
}

@Preview
@Composable
private fun BloqueHorarioChipPreview() {
    Row {
        BloqueHorarioChip(BloqueHorario("09:00", disponible = true), seleccionado = false, alPulsar = {})
        BloqueHorarioChip(BloqueHorario("09:30", disponible = true), seleccionado = true, alPulsar = {})
        BloqueHorarioChip(BloqueHorario("10:00", disponible = false), seleccionado = false, alPulsar = {})
    }
}
