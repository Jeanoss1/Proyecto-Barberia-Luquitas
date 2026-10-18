package com.example.barberialuquitas.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.barberialuquitas.data.AuthRepository
import com.example.barberialuquitas.navigation.Rutas
import com.example.barberialuquitas.screens.admin.AccesoAdministrador
import com.example.barberialuquitas.ui.theme.DarkBackground
import com.example.barberialuquitas.ui.theme.GoldAccent
import com.example.barberialuquitas.ui.theme.TextGray

@Composable
fun AdminHomeScreen(navController: NavController, authRepository: AuthRepository = remember { AuthRepository() }) {
    AccesoAdministrador(navController, authRepository) {
        PanelAdministrador(navController, authRepository)
    }
}

@Composable
private fun PanelAdministrador(navController: NavController, authRepository: AuthRepository) {
    Column(
        modifier = Modifier.fillMaxSize().background(DarkBackground).padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Panel de administrador", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        Text("Gestiona la agenda de la barbería.", color = TextGray, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { navController.navigate(Rutas.AGENDA_ADMIN) },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = Color.Black)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Ver agenda general", color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = {
                authRepository.cerrarSesion()
                navController.navigate(Rutas.LOGIN) { popUpTo(0) }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            border = BorderStroke(1.dp, GoldAccent),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Cerrar sesión", color = GoldAccent, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}
