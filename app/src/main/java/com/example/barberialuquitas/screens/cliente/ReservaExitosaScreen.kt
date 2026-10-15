package com.example.barberialuquitas.screens.cliente

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.barberialuquitas.R
import com.example.barberialuquitas.navigation.Rutas
import com.example.barberialuquitas.ui.theme.DarkBackground
import com.example.barberialuquitas.ui.theme.GoldAccent
import com.example.barberialuquitas.ui.theme.TextGray
import com.example.barberialuquitas.util.formatearFechaLegible

@Composable
fun ReservaExitosaScreen(navController: NavController, fecha: String, hora: String) {
    Column(
        modifier = Modifier.fillMaxSize().background(DarkBackground).padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(painter = painterResource(id = R.drawable.check_dorado), contentDescription = "Check", modifier = Modifier.size(80.dp))
        Spacer(modifier = Modifier.height(24.dp))
        Text("¡Reserva confirmada!", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Te esperamos el ${formatearFechaLegible(fecha)} a las $hora.",
            color = TextGray,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
        Spacer(modifier = Modifier.height(48.dp))
        Button(
            onClick = {
                navController.navigate(Rutas.homeCliente(PestanaCliente.MIS_CITAS.name)) {
                    popUpTo(Rutas.HOME_CLIENTE) { inclusive = true }
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Ver mis citas", color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(
            onClick = { navController.popBackStack(Rutas.HOME_CLIENTE, inclusive = false) },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            border = BorderStroke(1.dp, GoldAccent),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Volver al inicio", color = GoldAccent, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}
