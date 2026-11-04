package com.example.barberialuquitas.ui.pantallas.cliente

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.barberialuquitas.data.repositorio.AuthRepository
import com.example.barberialuquitas.ui.componentes.CustomTextField
import com.example.barberialuquitas.ui.componentes.PantallaCargando
import com.example.barberialuquitas.ui.theme.DarkBackground
import com.example.barberialuquitas.ui.theme.ErrorRed
import com.example.barberialuquitas.ui.theme.GoldAccent
import com.example.barberialuquitas.ui.theme.TextGray
import com.example.barberialuquitas.util.soloDigitos
import com.example.barberialuquitas.util.soloLetras
import kotlinx.coroutines.launch

@Composable
fun EditarDatosScreen(
    navController: NavController,
    authRepository: AuthRepository = remember { AuthRepository() }
) {
    var nombre by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(true) }
    var guardando by remember { mutableStateOf(false) }
    var nombreError by remember { mutableStateOf<String?>(null) }
    var telefonoError by remember { mutableStateOf<String?>(null) }
    var mensajeError by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        authRepository.obtenerUsuarioActual()
            .onSuccess {
                nombre = it.nombre
                telefono = it.telefono
                correo = it.correo
            }
            .onFailure { mensajeError = it.message }
        cargando = false
    }

    fun validar(): Boolean {
        nombreError = if (nombre.isBlank()) "Ingresa tu nombre completo" else null
        telefonoError = when {
            telefono.isBlank() -> "Ingresa tu teléfono"
            telefono.length < 9 -> "El teléfono debe tener 9 dígitos"
            else -> null
        }
        return nombreError == null && telefonoError == null
    }

    fun guardar() {
        if (!validar()) return
        guardando = true
        mensajeError = null
        scope.launch {
            authRepository.actualizarDatos(nombre.trim(), telefono)
                .onSuccess { navController.popBackStack() }
                .onFailure { mensajeError = it.message }
            guardando = false
        }
    }

    if (cargando) {
        PantallaCargando(Modifier.fillMaxSize().background(DarkBackground))
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
            }
            Text("Editar datos", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))

        CustomTextField(
            "NOMBRE COMPLETO", "Ej. Juan Pérez", nombre, { nombre = it }, Icons.Default.Person,
            errorText = nombreError, filtro = ::soloLetras
        )
        Spacer(modifier = Modifier.height(16.dp))
        CustomTextField(
            "TELÉFONO", "987654321", telefono, { telefono = it.take(9) }, Icons.Default.Phone,
            KeyboardType.Phone, errorText = telefonoError, filtro = ::soloDigitos
        )
        Spacer(modifier = Modifier.height(16.dp))

        // El correo solo se muestra: cambiarlo requiere reautenticación en Firebase Auth
        Text("CORREO ELECTRÓNICO", color = TextGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Email, contentDescription = null, tint = TextGray, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(correo, color = TextGray, fontSize = 15.sp)
        }

        mensajeError?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Text(it, color = ErrorRed, fontSize = 13.sp)
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = ::guardar,
            enabled = !guardando,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
            shape = RoundedCornerShape(8.dp)
        ) {
            if (guardando) {
                CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(20.dp))
            } else {
                Text("Guardar cambios", color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
