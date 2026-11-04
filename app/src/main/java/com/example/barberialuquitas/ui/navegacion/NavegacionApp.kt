package com.example.barberialuquitas.ui.navegacion

import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.barberialuquitas.ui.pantallas.admin.AdminHomeScreen
import com.example.barberialuquitas.ui.pantallas.admin.AgendaAdminScreen
import com.example.barberialuquitas.ui.pantallas.auth.LoginScreen
import com.example.barberialuquitas.ui.pantallas.auth.RegisterScreen
import com.example.barberialuquitas.ui.pantallas.auth.SuccessScreen
import com.example.barberialuquitas.ui.pantallas.cliente.ClienteHomeScreen
import com.example.barberialuquitas.ui.pantallas.cliente.ConfirmacionReservaScreen
import com.example.barberialuquitas.ui.pantallas.cliente.EditarDatosScreen
import com.example.barberialuquitas.ui.pantallas.cliente.ReservaExitosaScreen
import com.example.barberialuquitas.ui.pantallas.cliente.SeleccionBarberoScreen
import com.example.barberialuquitas.ui.pantallas.cliente.SeleccionHorarioScreen

// Todas las pantallas de la app y cómo se llega a cada una. Las rutas están en Rutas.kt.
@Composable
fun NavegacionApp() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Rutas.LOGIN) {

        // ── Autenticación ──
        composable(Rutas.LOGIN) { LoginScreen(navController) }
        composable(Rutas.REGISTER) { RegisterScreen(navController) }
        composable(Rutas.SUCCESS) { SuccessScreen(navController) }

        // ── Cliente ──
        composable(
            Rutas.HOME_CLIENTE,
            arguments = listOf(navArgument(Rutas.ARG_PESTANA) { type = NavType.StringType; nullable = true })
        ) { entrada ->
            ClienteHomeScreen(navController, pestanaInicial = entrada.arguments?.getString(Rutas.ARG_PESTANA))
        }
        composable(Rutas.EDITAR_DATOS) { EditarDatosScreen(navController) }
        composable(Rutas.SELECCION_BARBERO) { entrada ->
            SeleccionBarberoScreen(navController, servicioId = entrada.argumento(Rutas.ARG_SERVICIO_ID))
        }
        composable(Rutas.SELECCION_HORARIO) { entrada ->
            SeleccionHorarioScreen(
                navController,
                servicioId = entrada.argumento(Rutas.ARG_SERVICIO_ID),
                barberoId = entrada.argumento(Rutas.ARG_BARBERO_ID)
            )
        }
        composable(Rutas.CONFIRMACION_RESERVA) { entrada ->
            ConfirmacionReservaScreen(
                navController,
                servicioId = entrada.argumento(Rutas.ARG_SERVICIO_ID),
                barberoId = entrada.argumento(Rutas.ARG_BARBERO_ID),
                fecha = entrada.argumento(Rutas.ARG_FECHA),
                hora = entrada.argumento(Rutas.ARG_HORA)
            )
        }
        composable(Rutas.RESERVA_EXITOSA) { entrada ->
            ReservaExitosaScreen(
                navController,
                fecha = entrada.argumento(Rutas.ARG_FECHA),
                hora = entrada.argumento(Rutas.ARG_HORA)
            )
        }

        // ── Administrador (todas protegidas por rol con AccesoAdministrador) ──
        composable(Rutas.HOME_ADMIN) { AdminHomeScreen(navController) }
        composable(Rutas.AGENDA_ADMIN) { AgendaAdminScreen(navController) }
    }
}

private fun NavBackStackEntry.argumento(nombre: String): String = arguments?.getString(nombre).orEmpty()
