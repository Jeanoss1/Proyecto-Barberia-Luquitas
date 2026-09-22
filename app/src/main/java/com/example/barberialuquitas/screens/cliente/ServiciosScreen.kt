package com.example.barberialuquitas.screens.cliente

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.barberialuquitas.data.Servicio
import com.example.barberialuquitas.data.ServicioRepository

private val DarkBackground = Color(0xFF151515)
private val CardBackground = Color(0xFF252525)
private val GoldAccent = Color(0xFFE5B94E)
private val TextGray = Color(0xFFA0A0A0)

@Composable
fun ServiciosScreen(servicioRepository: ServicioRepository = remember { ServicioRepository() }) {
    val servicios by servicioRepository.observarServicios().collectAsState(initial = null)

    Column(modifier = Modifier.fillMaxSize().background(DarkBackground)) {
        Text(
            "Nuestros Servicios",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(20.dp)
        )

        when {
            servicios == null -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = GoldAccent)
            }
            servicios!!.isEmpty() -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Aún no hay servicios disponibles.", color = TextGray, fontSize = 14.sp)
            }
            else -> LazyColumn(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(servicios!!, key = { it.id }) { servicio ->
                    ServicioCard(servicio)
                }
            }
        }
    }
}

@Composable
private fun ServicioCard(servicio: Servicio) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(servicio.nombre, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(servicio.descripcion, color = TextGray, fontSize = 13.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text("S/ ${servicio.precio}", color = GoldAccent, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}
