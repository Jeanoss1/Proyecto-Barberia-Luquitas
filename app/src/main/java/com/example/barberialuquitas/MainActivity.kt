package com.example.barberialuquitas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.barberialuquitas.navigation.Rutas
import com.example.barberialuquitas.screens.AdminHomeScreen
import com.example.barberialuquitas.screens.LoginScreen
import com.example.barberialuquitas.screens.RegisterScreen
import com.example.barberialuquitas.screens.admin.AgendaAdminScreen
import com.example.barberialuquitas.screens.SuccessScreen
import com.example.barberialuquitas.screens.cliente.ClienteHomeScreen
import com.example.barberialuquitas.screens.cliente.ConfirmacionReservaScreen
import com.example.barberialuquitas.screens.cliente.ReservaExitosaScreen
import com.example.barberialuquitas.screens.cliente.SeleccionBarberoScreen
import com.example.barberialuquitas.screens.cliente.SeleccionHorarioScreen
import com.example.barberialuquitas.ui.theme.BarberiaLuquitasTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BarberiaLuquitasTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        val navController = rememberNavController()
                        NavHost(navController = navController, startDestination = Rutas.LOGIN) {
                            composable(Rutas.LOGIN) { LoginScreen(navController) }
                            composable(Rutas.REGISTER) { RegisterScreen(navController) }
                            composable(Rutas.SUCCESS) { SuccessScreen(navController) }
                            composable(
                                Rutas.HOME_CLIENTE,
                                arguments = listOf(navArgument(Rutas.ARG_PESTANA) { type = NavType.StringType; nullable = true })
                            ) { entrada ->
                                ClienteHomeScreen(navController, pestanaInicial = entrada.arguments?.getString(Rutas.ARG_PESTANA))
                            }
                            composable(Rutas.HOME_ADMIN) { AdminHomeScreen(navController) }
                            composable(Rutas.AGENDA_ADMIN) { AgendaAdminScreen(navController) }
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
                        }
                    }
                }
            }
        }
    }
}

private fun NavBackStackEntry.argumento(nombre: String): String = arguments?.getString(nombre).orEmpty()
