package com.example.barberialuquitas.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.navigation.NavController
import com.example.barberialuquitas.R
import com.example.barberialuquitas.components.CustomTextField
import com.example.barberialuquitas.components.DivisorConTexto
import com.example.barberialuquitas.components.GoogleSignInButton
import com.example.barberialuquitas.components.PasswordField
import com.example.barberialuquitas.data.AuthRepository
import com.example.barberialuquitas.util.esCorreoValido
import com.example.barberialuquitas.util.obtenerGoogleIdToken
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(navController: NavController, authRepository: AuthRepository = remember { AuthRepository() }) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var mensajeGeneral by remember { mutableStateOf<String?>(null) }
    var cargando by remember { mutableStateOf(false) }
    var cargandoGoogle by remember { mutableStateOf(false) }
    var mostrarRecuperacion by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    fun irADestino(rol: String) {
        val destino = if (rol == "administrador") "home_admin" else "home_cliente"
        navController.navigate(destino) { popUpTo("login") { inclusive = true } }
    }

    fun iniciarSesionConGoogle() {
        mensajeGeneral = null
        cargandoGoogle = true
        scope.launch {
            try {
                val idToken = obtenerGoogleIdToken(context)
                val resultado = authRepository.iniciarSesionConGoogle(idToken)
                cargandoGoogle = false
                resultado.onSuccess { irADestino(it.rol) }.onFailure { mensajeGeneral = it.message }
            } catch (_: GetCredentialCancellationException) {
                cargandoGoogle = false
            } catch (_: Exception) {
                cargandoGoogle = false
                mensajeGeneral = "No se pudo iniciar sesión con Google."
            }
        }
    }

    val darkBackground = Color(0xFF151515)
    val formBackground = Color(0xFF252525)
    val goldAccent = Color(0xFFE5B94E)
    val textGray = Color(0xFFA0A0A0)

    fun validar(): Boolean {
        emailError = if (email.isBlank()) "Ingresa tu correo electrónico" else null
        passwordError = if (password.isBlank()) "Ingresa tu contraseña" else null
        return emailError == null && passwordError == null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(darkBackground)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        Image(
            painter = painterResource(id = R.drawable.logo_barberia),
            contentDescription = "Logo",
            modifier = Modifier.size(130.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("Bienvenido de vuelta", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Inicia sesión para continuar.", color = textGray, fontSize = 14.sp)

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = formBackground),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                CustomTextField("CORREO ELECTRÓNICO", "juan@ejemplo.com", email, { email = it }, Icons.Default.Email, KeyboardType.Email, errorText = emailError)
                Spacer(modifier = Modifier.height(16.dp))
                PasswordField(value = password, onValueChange = { password = it }, errorText = passwordError)

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "¿Olvidaste tu contraseña?",
                    color = goldAccent,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { mostrarRecuperacion = true }
                )

                if (mensajeGeneral != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(mensajeGeneral!!, color = Color(0xFFE57373), fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        mensajeGeneral = null
                        if (validar()) {
                            cargando = true
                            scope.launch {
                                val resultado = authRepository.iniciarSesion(email, password)
                                cargando = false
                                resultado
                                    .onSuccess { irADestino(it.rol) }
                                    .onFailure { mensajeGeneral = it.message }
                            }
                        }
                    },
                    enabled = !cargando && !cargandoGoogle,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = goldAccent),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (cargando) {
                        CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(20.dp))
                    } else {
                        Text("Iniciar Sesión", color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                DivisorConTexto("O")
                Spacer(modifier = Modifier.height(20.dp))
                GoogleSignInButton(onClick = { iniciarSesionConGoogle() }, cargando = cargandoGoogle)

                Spacer(modifier = Modifier.height(20.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    Text("¿No tienes una cuenta? ", color = textGray, fontSize = 14.sp)
                    Text(
                        "Regístrate",
                        color = goldAccent,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { navController.navigate("register") }
                    )
                }
            }
        }
    }

    if (mostrarRecuperacion) {
        RecuperarPasswordDialog(
            authRepository = authRepository,
            onDismiss = { mostrarRecuperacion = false }
        )
    }
}

@Composable
private fun RecuperarPasswordDialog(authRepository: AuthRepository, onDismiss: () -> Unit) {
    var correo by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var mensajeExito by remember { mutableStateOf<String?>(null) }
    var enviando by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Recuperar contraseña") },
        text = {
            Column {
                Text(
                    "Ingresa tu correo y te enviaremos un enlace para restablecer tu contraseña.",
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = correo,
                    onValueChange = { correo = it },
                    placeholder = { Text("juan@ejemplo.com") },
                    isError = error != null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (error != null) {
                    Text(error!!, color = Color(0xFFE57373), fontSize = 12.sp)
                }
                if (mensajeExito != null) {
                    Text(mensajeExito!!, color = Color(0xFF81C784), fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !enviando,
                onClick = {
                    if (!esCorreoValido(correo)) {
                        error = "Ingresa un correo válido"
                    } else {
                        error = null
                        enviando = true
                        scope.launch {
                            val resultado = authRepository.enviarCorreoRecuperacion(correo)
                            enviando = false
                            resultado
                                .onSuccess { mensajeExito = "Enlace enviado. Revisa tu correo." }
                                .onFailure { error = it.message }
                        }
                    }
                }
            ) { Text("Enviar enlace") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cerrar") }
        }
    )
}
