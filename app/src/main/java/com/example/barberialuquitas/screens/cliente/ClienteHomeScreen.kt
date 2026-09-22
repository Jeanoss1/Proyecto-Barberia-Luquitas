package com.example.barberialuquitas.screens.cliente

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.barberialuquitas.data.AuthRepository
import com.example.barberialuquitas.data.Usuario

private val DarkBackground = Color(0xFF151515)
private val CardBackground = Color(0xFF252525)
private val GoldAccent = Color(0xFFE5B94E)
private val TextGray = Color(0xFFA0A0A0)

private enum class PestanaCliente { SERVICIOS, PERFIL }

@Composable
fun ClienteHomeScreen(navController: NavController, authRepository: AuthRepository = remember { AuthRepository() }) {
    var pestanaActual by remember { mutableStateOf(PestanaCliente.SERVICIOS) }
    var usuario by remember { mutableStateOf<Usuario?>(null) }

    LaunchedEffect(Unit) {
        authRepository.obtenerUsuarioActual().onSuccess { usuario = it }
    }

    fun cerrarSesion() {
        authRepository.cerrarSesion()
        navController.navigate("login") { popUpTo(0) }
    }

    Scaffold(
        containerColor = DarkBackground,
        bottomBar = {
            NavigationBar(containerColor = CardBackground) {
                NavigationBarItem(
                    selected = pestanaActual == PestanaCliente.SERVICIOS,
                    onClick = { pestanaActual = PestanaCliente.SERVICIOS },
                    icon = { Icon(Icons.Default.ContentCut, contentDescription = null) },
                    label = { Text("Servicios", fontSize = 12.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = GoldAccent,
                        indicatorColor = GoldAccent,
                        unselectedIconColor = TextGray,
                        unselectedTextColor = TextGray
                    )
                )
                NavigationBarItem(
                    selected = pestanaActual == PestanaCliente.PERFIL,
                    onClick = { pestanaActual = PestanaCliente.PERFIL },
                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
                    label = { Text("Mi Perfil", fontSize = 12.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = GoldAccent,
                        indicatorColor = GoldAccent,
                        unselectedIconColor = TextGray,
                        unselectedTextColor = TextGray
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when (pestanaActual) {
                PestanaCliente.SERVICIOS -> ServiciosScreen()
                PestanaCliente.PERFIL -> {
                    val usuarioActual = usuario
                    if (usuarioActual == null) {
                        Box(modifier = Modifier.fillMaxSize().background(DarkBackground), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = GoldAccent)
                        }
                    } else {
                        PerfilScreen(usuario = usuarioActual, alCerrarSesion = ::cerrarSesion)
                    }
                }
            }
        }
    }
}
