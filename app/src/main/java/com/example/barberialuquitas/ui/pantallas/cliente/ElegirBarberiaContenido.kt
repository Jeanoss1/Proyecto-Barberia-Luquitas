package com.example.barberialuquitas.ui.pantallas.cliente

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.barberialuquitas.data.modelo.Barberia
import com.example.barberialuquitas.data.repositorio.BarberiaRepository
import com.example.barberialuquitas.ui.componentes.ContenidoSegunResultado
import com.example.barberialuquitas.ui.componentes.FotoOInicial
import com.example.barberialuquitas.ui.componentes.logoLocalDe
import com.example.barberialuquitas.ui.theme.CardBackground
import com.example.barberialuquitas.ui.theme.DarkBackground
import com.example.barberialuquitas.ui.theme.ErrorRed
import com.example.barberialuquitas.ui.theme.GoldAccent
import com.example.barberialuquitas.ui.theme.SuccessGreen
import com.example.barberialuquitas.ui.theme.TextGray
import com.example.barberialuquitas.util.describirDiasLaborables
import com.example.barberialuquitas.util.estaAbierta
import java.time.LocalDateTime

// Lista de barberías activas para que el cliente elija dónde atenderse.
@Composable
fun ElegirBarberiaContenido(
    alElegir: (Barberia) -> Unit,
    barberiaRepository: BarberiaRepository = remember { BarberiaRepository() }
) {
    var intento by remember { mutableIntStateOf(0) }
    val resultado by remember(intento) { barberiaRepository.observarBarberiasActivas() }.collectAsState(initial = null)

    Column(modifier = Modifier.fillMaxSize().background(DarkBackground)) {
        Text(
            "Elige tu barbería",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp)
        )
        Text(
            "Compara horarios y reserva donde prefieras.",
            color = TextGray,
            fontSize = 14.sp,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
        )

        ContenidoSegunResultado(
            resultado = resultado,
            alReintentar = { intento++ },
            modifier = Modifier.fillMaxSize(),
            mensajeVacio = "Aún no hay barberías disponibles."
        ) { barberias ->
            val ahora = LocalDateTime.now()
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(barberias, key = { it.id }) { barberia ->
                    BarberiaCard(barberia, abierta = estaAbierta(barberia, ahora), alElegir = { alElegir(barberia) })
                }
            }
        }
    }
}

@Composable
fun BarberiaCard(barberia: Barberia, abierta: Boolean, alElegir: () -> Unit) {
    Card(
        onClick = alElegir,
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            FotoOInicial(barberia.fotoUrl, barberia.nombre, tamano = 64.dp, forma = CircleShape, logoLocal = logoLocalDe(barberia))
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(barberia.nombre, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                if (barberia.direccion.isNotBlank()) {
                    Text(barberia.direccion, color = TextGray, fontSize = 13.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "${describirDiasLaborables(barberia.diasLaborables)} · ${barberia.horaApertura} - ${barberia.horaCierre}",
                    color = GoldAccent,
                    fontSize = 12.sp
                )
            }
            Text(
                if (abierta) "Abierto" else "Cerrado",
                color = if (abierta) SuccessGreen else ErrorRed,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Preview
@Composable
private fun BarberiaCardPreview() {
    BarberiaCard(
        Barberia(id = "1", nombre = "Barbería Luquitas", direccion = "Av. Principal 123"),
        abierta = true,
        alElegir = {}
    )
}
