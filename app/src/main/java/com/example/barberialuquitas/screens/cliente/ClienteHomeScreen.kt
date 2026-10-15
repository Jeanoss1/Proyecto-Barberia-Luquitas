package com.example.barberialuquitas.screens.cliente

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.barberialuquitas.components.PantallaCargando
import com.example.barberialuquitas.data.AuthRepository
import com.example.barberialuquitas.data.Usuario
import com.example.barberialuquitas.navigation.Rutas
import com.example.barberialuquitas.ui.theme.CardBackground
import com.example.barberialuquitas.ui.theme.DarkBackground
import com.example.barberialuquitas.ui.theme.GoldAccent
import com.example.barberialuquitas.ui.theme.TextGray

enum class PestanaCliente { SERVICIOS, MIS_CITAS, PERFIL }

@Composable
fun ClienteHomeScreen(
    navController: NavController,
    pestanaInicial: String? = null,
    authRepository: AuthRepository = remember { AuthRepository() }
) {
    var pestanaActual by rememberSaveable {
        mutableStateOf(PestanaCliente.entries.firstOrNull { it.name == pestanaInicial } ?: PestanaCliente.SERVICIOS)
    }
    var usuario by remember { mutableStateOf<Usuario?>(null) }

    LaunchedEffect(Unit) {
        authRepository.obtenerUsuarioActual().onSuccess { usuario = it }
    }

    fun cerrarSesion() {
        authRepository.cerrarSesion()
        navController.navigate(Rutas.LOGIN) { popUpTo(0) }
    }

    Scaffold(
        containerColor = DarkBackground,
        bottomBar = {
            NavigationBar(containerColor = CardBackground) {
                PestanaItem("Servicios", Icons.Default.ContentCut, pestanaActual == PestanaCliente.SERVICIOS) {
                    pestanaActual = PestanaCliente.SERVICIOS
                }
                PestanaItem("Mis citas", Icons.Default.CalendarMonth, pestanaActual == PestanaCliente.MIS_CITAS) {
                    pestanaActual = PestanaCliente.MIS_CITAS
                }
                PestanaItem("Mi Perfil", Icons.Default.Person, pestanaActual == PestanaCliente.PERFIL) {
                    pestanaActual = PestanaCliente.PERFIL
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            val usuarioActual = usuario
            when {
                pestanaActual == PestanaCliente.SERVICIOS -> ServiciosScreen(
                    alReservar = { servicio -> navController.navigate(Rutas.seleccionBarbero(servicio.id)) }
                )
                usuarioActual == null -> PantallaCargando(Modifier.fillMaxSize().background(DarkBackground))
                pestanaActual == PestanaCliente.MIS_CITAS -> MisCitasScreen(clienteId = usuarioActual.uid)
                else -> PerfilScreen(usuario = usuarioActual, alCerrarSesion = ::cerrarSesion)
            }
        }
    }
}

@Composable
private fun RowScope.PestanaItem(etiqueta: String, icono: ImageVector, seleccionada: Boolean, alPulsar: () -> Unit) {
    NavigationBarItem(
        selected = seleccionada,
        onClick = alPulsar,
        icon = { Icon(icono, contentDescription = null) },
        label = { Text(etiqueta, fontSize = 12.sp) },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = Color.Black,
            selectedTextColor = GoldAccent,
            indicatorColor = GoldAccent,
            unselectedIconColor = TextGray,
            unselectedTextColor = TextGray
        )
    )
}
