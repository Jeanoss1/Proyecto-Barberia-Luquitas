package com.example.barberialuquitas.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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

@Composable
fun ClienteHomeScreen(navController: NavController, authRepository: AuthRepository = remember { AuthRepository() }) {
    PantallaBienvenida(
        titulo = "Área de cliente",
        subtitulo = "Aquí verás el catálogo de servicios y tus reservas próximamente.",
        navController = navController,
        authRepository = authRepository
    )
}

@Composable
fun AdminHomeScreen(navController: NavController, authRepository: AuthRepository = remember { AuthRepository() }) {
    PantallaBienvenida(
        titulo = "Panel de administrador",
        subtitulo = "Panel de control en construcción.",
        navController = navController,
        authRepository = authRepository
    )
}

@Composable
private fun PantallaBienvenida(
    titulo: String,
    subtitulo: String,
    navController: NavController,
    authRepository: AuthRepository
) {
    val darkBackground = Color(0xFF151515)
    val goldAccent = Color(0xFFE5B94E)
    val textGray = Color(0xFFA0A0A0)

    Column(
        modifier = Modifier.fillMaxSize().background(darkBackground).padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(titulo, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        Text(subtitulo, color = textGray, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                authRepository.cerrarSesion()
                navController.navigate("login") {
                    popUpTo(0)
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = goldAccent),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Cerrar sesión", color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}
