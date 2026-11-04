package com.example.barberialuquitas.ui.componentes

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.barberialuquitas.data.modelo.Barbero
import com.example.barberialuquitas.ui.theme.CardBackground
import com.example.barberialuquitas.ui.theme.GoldAccent
import com.example.barberialuquitas.ui.theme.TextGray
import com.example.barberialuquitas.util.describirDiasLaborables

// Tarjeta de un barbero. "accion" reemplaza la flecha (p. ej. el interruptor de alta/baja del administrador).
@Composable
fun BarberoCard(
    barbero: Barbero,
    alElegir: () -> Unit,
    accion: (@Composable () -> Unit)? = null
) {
    Card(
        onClick = alElegir,
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FotoOInicial(barbero.fotoUrl, barbero.nombre)
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(barbero.nombre, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(2.dp))
                Text(barbero.especialidad, color = GoldAccent, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "${describirDiasLaborables(barbero.diasLaborables)} · ${barbero.horaInicio} - ${barbero.horaFin}",
                    color = TextGray,
                    fontSize = 12.sp
                )
            }
            if (accion != null) {
                accion()
            } else {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = TextGray)
            }
        }
    }
}

@Preview
@Composable
private fun BarberoCardPreview() {
    BarberoCard(
        barbero = Barbero(
            id = "1",
            nombre = "Luquitas",
            especialidad = "Fade y diseño",
            activo = true,
            diasLaborables = listOf(1, 2, 3, 4, 5, 6)
        ),
        alElegir = {}
    )
}
