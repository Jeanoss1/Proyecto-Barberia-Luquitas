package com.example.barberialuquitas.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.barberialuquitas.components.PantallaCargando
import com.example.barberialuquitas.data.AuthRepository
import com.example.barberialuquitas.data.Roles
import com.example.barberialuquitas.navigation.Rutas
import com.example.barberialuquitas.ui.theme.DarkBackground

// Muestra el contenido solo si el usuario actual es administrador; si no, lo envía al login o a su inicio.
@Composable
fun AccesoAdministrador(
    navController: NavController,
    authRepository: AuthRepository = remember { AuthRepository() },
    contenido: @Composable () -> Unit
) {
    var esAdministrador by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val usuario = authRepository.obtenerUsuarioActual().getOrNull()
        when {
            usuario == null -> navController.navigate(Rutas.LOGIN) { popUpTo(0) }
            usuario.rol != Roles.ADMINISTRADOR -> navController.navigate(Rutas.homeCliente()) { popUpTo(0) }
            else -> esAdministrador = true
        }
    }

    if (esAdministrador) contenido() else PantallaCargando(Modifier.fillMaxSize().background(DarkBackground))
}
