package com.example.barberialuquitas.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.barberialuquitas.data.Cita
import com.example.barberialuquitas.ui.theme.CardBackground
import com.example.barberialuquitas.ui.theme.ErrorRed
import com.example.barberialuquitas.ui.theme.GoldAccent
import com.example.barberialuquitas.ui.theme.TextGray
import com.example.barberialuquitas.util.estadoVisible
import com.example.barberialuquitas.util.formatearFechaLegible
import com.example.barberialuquitas.util.horaDeTermino
import java.time.LocalDateTime

@Composable
fun CitaCard(cita: Cita, mostrarCliente: Boolean = false, alCancelar: (() -> Unit)? = null) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    cita.servicioNombre,
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                EstadoChip(estadoVisible(cita, LocalDateTime.now()))
            }
            Spacer(modifier = Modifier.height(10.dp))
            if (mostrarCliente) DatoCita(Icons.Default.Person, "Cliente: ${cita.clienteNombre}")
            DatoCita(Icons.Default.ContentCut, "Barbero: ${cita.barberoNombre}")
            DatoCita(Icons.Default.CalendarMonth, formatearFechaLegible(cita.fecha))
            DatoCita(Icons.Default.Schedule, "${cita.hora} - ${horaDeTermino(cita.hora, cita.duracionMinutos)}")
            Spacer(modifier = Modifier.height(4.dp))
            Text("S/ ${cita.precio}", color = GoldAccent, fontSize = 15.sp, fontWeight = FontWeight.Bold)

            if (alCancelar != null) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = alCancelar,
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, ErrorRed),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Cancelar", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun DatoCita(icono: ImageVector, texto: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
        Icon(icono, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(texto, color = TextGray, fontSize = 13.sp)
    }
}

@Preview
@Composable
private fun CitaCardPreview() {
    CitaCard(
        cita = Cita(
            id = "1",
            servicioNombre = "Corte clásico",
            precio = 25,
            barberoNombre = "Luquitas",
            fecha = "2026-10-20",
            hora = "10:30"
        ),
        alCancelar = {}
    )
}
