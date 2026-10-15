package com.example.barberialuquitas.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.barberialuquitas.data.Barbero
import com.example.barberialuquitas.ui.theme.CardBackground
import com.example.barberialuquitas.ui.theme.DarkBackground
import com.example.barberialuquitas.ui.theme.GoldAccent
import com.example.barberialuquitas.ui.theme.TextGray
import com.example.barberialuquitas.util.describirDiasLaborables

@Composable
fun BarberoCard(barbero: Barbero, alElegir: () -> Unit) {
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
            FotoBarbero(barbero)
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
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = TextGray)
        }
    }
}

@Composable
private fun FotoBarbero(barbero: Barbero) {
    val modificador = Modifier.size(56.dp).clip(CircleShape)
    if (barbero.fotoUrl != null) {
        AsyncImage(
            model = barbero.fotoUrl,
            contentDescription = "Foto de ${barbero.nombre}",
            contentScale = ContentScale.Crop,
            modifier = modificador.background(DarkBackground)
        )
    } else {
        Box(modifier = modificador.background(DarkBackground), contentAlignment = Alignment.Center) {
            Text(
                barbero.nombre.take(1).uppercase(),
                color = GoldAccent,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
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
