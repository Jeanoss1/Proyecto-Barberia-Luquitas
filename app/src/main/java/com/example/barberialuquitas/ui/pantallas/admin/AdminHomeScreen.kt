package com.example.barberialuquitas.ui.pantallas.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.barberialuquitas.data.modelo.Barberia
import com.example.barberialuquitas.data.modelo.EstadosCita
import com.example.barberialuquitas.data.modelo.Usuario
import com.example.barberialuquitas.data.repositorio.AuthRepository
import com.example.barberialuquitas.data.repositorio.BarberiaRepository
import com.example.barberialuquitas.data.repositorio.CitaRepository
import com.example.barberialuquitas.ui.componentes.CitaCard
import com.example.barberialuquitas.ui.componentes.ContenidoSegunResultado
import com.example.barberialuquitas.ui.componentes.MensajeVacio
import com.example.barberialuquitas.ui.componentes.TarjetaDato
import com.example.barberialuquitas.ui.componentes.TituloSeccion
import com.example.barberialuquitas.ui.navegacion.Rutas
import com.example.barberialuquitas.ui.theme.CardBackground
import com.example.barberialuquitas.ui.theme.DarkBackground
import com.example.barberialuquitas.ui.theme.ErrorRed
import com.example.barberialuquitas.ui.theme.GoldAccent
import com.example.barberialuquitas.ui.theme.InfoBlue
import com.example.barberialuquitas.ui.theme.SuccessGreen
import com.example.barberialuquitas.ui.theme.TextGray
import com.example.barberialuquitas.util.estadoVisible
import com.example.barberialuquitas.util.formatearFechaLegible
import com.example.barberialuquitas.util.resumirCitas
import java.time.LocalDate
import java.time.LocalDateTime

// RF12: panel principal del administrador con las citas de hoy, sus totales y el acceso a cada módulo.
@Composable
fun AdminHomeScreen(navController: NavController, authRepository: AuthRepository = remember { AuthRepository() }) {
    AccesoAdministrador(navController, authRepository) { administrador ->
        Dashboard(
            administrador = administrador,
            alNavegar = { navController.navigate(it) },
            alCerrarSesion = {
                authRepository.cerrarSesion()
                navController.navigate(Rutas.LOGIN) { popUpTo(0) }
            }
        )
    }
}

@Composable
private fun Dashboard(
    administrador: Usuario,
    alNavegar: (String) -> Unit,
    alCerrarSesion: () -> Unit,
    barberiaRepository: BarberiaRepository = remember { BarberiaRepository() }
) {
    val barberia by remember(administrador.barberiaId) {
        barberiaRepository.observarBarberia(administrador.barberiaId)
    }.collectAsState(initial = null)
    var avisoMigracion by remember { mutableStateOf<String?>(null) }

    // La barbería original adopta una sola vez los datos creados antes de soportar varias barberías.
    val datosBarberia = barberia?.getOrNull()
    LaunchedEffect(datosBarberia?.migrada) {
        if (datosBarberia != null && !datosBarberia.migrada) {
            avisoMigracion = "Actualizando los datos anteriores de la barbería…"
            avisoMigracion = barberiaRepository.adoptarDatosAntiguos(datosBarberia).fold(
                onSuccess = { null },
                onFailure = { "${it.message} Vuelve a abrir el panel para reintentar." }
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text("Hola, ${administrador.nombre.ifBlank { "administrador" }}", color = TextGray, fontSize = 14.sp)
        Text(
            datosBarberia?.nombre ?: "Panel de administrador",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Text(formatearFechaLegible(LocalDate.now().toString()), color = GoldAccent, fontSize = 13.sp)
        avisoMigracion?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(it, color = InfoBlue, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(20.dp))
        ResumenDeHoy(administrador.barberiaId, alVerAgenda = { alNavegar(Rutas.AGENDA_ADMIN) })

        Spacer(modifier = Modifier.height(24.dp))
        TituloSeccion("Gestión")
        Spacer(modifier = Modifier.height(12.dp))
        MenuAdministrador(datosBarberia, alNavegar)

        Spacer(modifier = Modifier.height(24.dp))
        OutlinedButton(
            onClick = alCerrarSesion,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            border = BorderStroke(1.dp, GoldAccent),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Cerrar sesión", color = GoldAccent, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ResumenDeHoy(
    barberiaId: String,
    alVerAgenda: () -> Unit,
    citaRepository: CitaRepository = remember { CitaRepository() }
) {
    val hoy = remember { LocalDate.now().toString() }
    var intento by remember { mutableIntStateOf(0) }
    val resultado by remember(barberiaId, intento) {
        citaRepository.observarAgendaDelDia(barberiaId, hoy)
    }.collectAsState(initial = null)

    ContenidoSegunResultado(resultado, alReintentar = { intento++ }, modifier = Modifier.fillMaxWidth()) { citas ->
        val ahora = LocalDateTime.now()
        val resumen = resumirCitas(citas, ahora)
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TarjetaDato("Citas de hoy", "${resumen.total - resumen.canceladas}", Modifier.weight(1f))
                TarjetaDato("Ingresos de hoy", "S/ ${resumen.ingresos}", Modifier.weight(1f), color = GoldAccent)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TarjetaDato("Pendientes", "${resumen.pendientes + resumen.porConfirmar}", Modifier.weight(1f))
                TarjetaDato("Atendidas", "${resumen.atendidas}", Modifier.weight(1f), color = SuccessGreen)
                TarjetaDato("No asistió", "${resumen.noAsistio}", Modifier.weight(1f), color = ErrorRed)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        TituloSeccion("Próximas citas de hoy")
        Spacer(modifier = Modifier.height(8.dp))
        // Reservadas que aún no terminan; las demás se gestionan desde la agenda.
        val proximas = citas.filter { estadoVisible(it, ahora) == EstadosCita.RESERVADA }.take(MAX_PROXIMAS)
        if (proximas.isEmpty()) {
            MensajeVacio("No quedan citas pendientes para hoy.", modifier = Modifier.fillMaxWidth())
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                proximas.forEach { CitaCard(it, mostrarCliente = true) }
            }
        }
        if (citas.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = alVerAgenda,
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, GoldAccent),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Gestionar citas del día", color = GoldAccent, fontWeight = FontWeight.Bold)
            }
        }
    }
}

private const val MAX_PROXIMAS = 3

private data class OpcionMenu(val titulo: String, val descripcion: String, val icono: ImageVector, val ruta: String)

@Composable
private fun MenuAdministrador(barberia: Barberia?, alNavegar: (String) -> Unit) {
    val opciones = listOf(
        OpcionMenu("Agenda general", "Citas por día y su estado", Icons.Default.CalendarMonth, Rutas.AGENDA_ADMIN),
        OpcionMenu("Servicios", "Agregar, editar y desactivar", Icons.Default.ContentCut, Rutas.SERVICIOS_ADMIN),
        OpcionMenu("Barberos", "Registrar, editar, alta y baja", Icons.Default.People, Rutas.BARBEROS_ADMIN),
        OpcionMenu(
            "Horario de atención",
            barberia?.let { "${it.horaApertura} - ${it.horaCierre} · datos de la barbería" } ?: "Apertura, cierre y feriados",
            Icons.Default.Schedule,
            Rutas.HORARIO_ADMIN
        ),
        OpcionMenu("Reporte de ingresos", "Por día, semana o mes", Icons.Default.BarChart, Rutas.REPORTE_ADMIN)
    )
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        opciones.forEach { opcion ->
            Card(
                onClick = { alNavegar(opcion.ruta) },
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(opcion.icono, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(26.dp))
                    Column(modifier = Modifier.padding(start = 14.dp)) {
                        Text(opcion.titulo, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text(opcion.descripcion, color = TextGray, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
