package com.example.barberialuquitas.ui.pantallas.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.barberialuquitas.data.modelo.Barberia
import com.example.barberialuquitas.data.modelo.Usuario
import com.example.barberialuquitas.data.repositorio.BarberiaRepository
import com.example.barberialuquitas.ui.componentes.BotonPrincipal
import com.example.barberialuquitas.ui.componentes.CustomTextField
import com.example.barberialuquitas.ui.theme.DarkBackground
import com.example.barberialuquitas.ui.theme.ErrorRed
import com.example.barberialuquitas.ui.theme.GoldAccent
import com.example.barberialuquitas.ui.theme.TextGray
import com.example.barberialuquitas.util.soloDigitos
import kotlinx.coroutines.launch

// Primer ingreso de un administrador: registra su barbería. El horario se ajusta después en RF17.
@Composable
fun RegistrarBarberiaScreen(
    administrador: Usuario,
    alRegistrar: () -> Unit,
    alCerrarSesion: () -> Unit,
    barberiaRepository: BarberiaRepository = remember { BarberiaRepository() }
) {
    var nombre by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var nombreError by remember { mutableStateOf<String?>(null) }
    var mensajeError by remember { mutableStateOf<String?>(null) }
    var guardando by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun registrar() {
        nombreError = if (nombre.isBlank()) "Ingresa el nombre de la barbería" else null
        if (nombreError != null) return
        guardando = true
        mensajeError = null
        scope.launch {
            barberiaRepository.registrarBarberia(
                Barberia(nombre = nombre, direccion = direccion, telefono = telefono),
                administrador.uid
            )
                .onSuccess { alRegistrar() }
                .onFailure { mensajeError = it.message }
            guardando = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Text("Registra tu barbería", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Hola ${administrador.nombre}. Antes de empezar, cuéntanos qué barbería administras. " +
                "Luego podrás configurar su horario, servicios y barberos.",
            color = TextGray,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(24.dp))

        CustomTextField(
            "NOMBRE DE LA BARBERÍA", "Ej. Barbería Luquitas", nombre, { nombre = it }, Icons.Default.Storefront,
            errorText = nombreError
        )
        Spacer(modifier = Modifier.height(16.dp))
        CustomTextField("DIRECCIÓN", "Av. Principal 123", direccion, { direccion = it }, Icons.Default.LocationOn)
        Spacer(modifier = Modifier.height(16.dp))
        CustomTextField(
            "TELÉFONO", "987654321", telefono, { telefono = it.take(9) }, Icons.Default.Phone,
            KeyboardType.Phone, filtro = ::soloDigitos
        )

        mensajeError?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Text(it, color = ErrorRed, fontSize = 13.sp)
        }
        Spacer(modifier = Modifier.height(32.dp))
        BotonPrincipal("Registrar barbería", alPulsar = ::registrar, cargando = guardando)
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = alCerrarSesion, modifier = Modifier.fillMaxWidth()) {
            Text("Cerrar sesión", color = GoldAccent)
        }
    }
}
