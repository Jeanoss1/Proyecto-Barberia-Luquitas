package com.example.barberialuquitas.ui.pantallas.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import com.example.barberialuquitas.data.modelo.Roles
import com.example.barberialuquitas.data.modelo.Usuario
import com.example.barberialuquitas.data.repositorio.AuthRepository
import com.example.barberialuquitas.ui.componentes.EncabezadoConAtras
import com.example.barberialuquitas.ui.componentes.PantallaCargando
import com.example.barberialuquitas.ui.navegacion.Rutas
import com.example.barberialuquitas.ui.theme.DarkBackground
import com.example.barberialuquitas.ui.theme.GoldAccent

// RF11: toda pantalla del administrador va dentro de este bloque.
// - Sin sesión → login. Si no es administrador → inicio del cliente.
// - Administrador sin barbería → primero registra la suya.
// - Si todo está bien, muestra el contenido con el administrador (y su barberiaId).
// Firestore vuelve a comprobar el rol y la barbería en cada lectura y escritura (firestore.rules).
@Composable
fun AccesoAdministrador(
    navController: NavController,
    authRepository: AuthRepository = remember { AuthRepository() },
    contenido: @Composable (administrador: Usuario) -> Unit
) {
    var administrador by remember { mutableStateOf<Usuario?>(null) }
    var recarga by remember { mutableIntStateOf(0) }

    LaunchedEffect(recarga) {
        val usuario = authRepository.obtenerUsuarioActual().getOrNull()
        when {
            usuario == null -> navController.navigate(Rutas.LOGIN) { popUpTo(0) }
            usuario.rol != Roles.ADMINISTRADOR -> navController.navigate(Rutas.homeCliente()) { popUpTo(0) }
            else -> administrador = usuario
        }
    }

    val admin = administrador
    when {
        admin == null -> PantallaCargando(Modifier.fillMaxSize().background(DarkBackground))
        admin.barberiaId.isBlank() -> RegistrarBarberiaScreen(
            administrador = admin,
            alRegistrar = { recarga++ },
            alCerrarSesion = {
                authRepository.cerrarSesion()
                navController.navigate(Rutas.LOGIN) { popUpTo(0) }
            }
        )
        else -> contenido(admin)
    }
}

// Estructura común de las pantallas del administrador: control de acceso, título con "volver" y,
// opcionalmente, un botón flotante (p. ej. "Nuevo servicio").
@Composable
fun PantallaAdministrador(
    navController: NavController,
    titulo: String,
    botonFlotante: (@Composable () -> Unit)? = null,
    contenido: @Composable ColumnScope.(administrador: Usuario) -> Unit
) {
    AccesoAdministrador(navController) { administrador ->
        Scaffold(
            containerColor = DarkBackground,
            // MainActivity ya aplica el espacio de las barras del sistema.
            contentWindowInsets = WindowInsets(0),
            floatingActionButton = { botonFlotante?.invoke() }
        ) { relleno ->
            Column(modifier = Modifier.fillMaxSize().padding(relleno)) {
                EncabezadoConAtras(titulo) { navController.popBackStack() }
                contenido(administrador)
            }
        }
    }
}

@Composable
fun BotonFlotanteAgregar(texto: String, alPulsar: () -> Unit) {
    ExtendedFloatingActionButton(
        onClick = alPulsar,
        containerColor = GoldAccent,
        contentColor = Color.Black,
        icon = { Icon(Icons.Default.Add, contentDescription = null) },
        text = { Text(texto, fontWeight = FontWeight.Bold) }
    )
}
