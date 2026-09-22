package com.example.barberialuquitas.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
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
import com.example.barberialuquitas.util.esPasswordValida
import com.example.barberialuquitas.util.obtenerGoogleIdToken
import com.example.barberialuquitas.util.soloDigitos
import com.example.barberialuquitas.util.soloLetras
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(navController: NavController, authRepository: AuthRepository = remember { AuthRepository() }) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }
    var mensajeGeneral by remember { mutableStateOf<String?>(null) }
    var cargando by remember { mutableStateOf(false) }
    var cargandoGoogle by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    fun registrarseConGoogle() {
        mensajeGeneral = null
        cargandoGoogle = true
        scope.launch {
            try {
                val idToken = obtenerGoogleIdToken(context)
                val resultado = authRepository.iniciarSesionConGoogle(idToken)
                cargandoGoogle = false
                resultado
                    .onSuccess { usuario ->
                        val destino = if (usuario.rol == "administrador") "home_admin" else "home_cliente"
                        navController.navigate(destino) { popUpTo(0) }
                    }
                    .onFailure { mensajeGeneral = it.message }
            } catch (_: GetCredentialCancellationException) {
                cargandoGoogle = false
            } catch (_: Exception) {
                cargandoGoogle = false
                mensajeGeneral = "No se pudo registrar con Google."
            }
        }
    }

    val darkBackground = Color(0xFF151515)
    val formBackground = Color(0xFF252525)
    val goldAccent = Color(0xFFE5B94E)
    val textGray = Color(0xFFA0A0A0)

    fun validar(): Boolean {
        nameError = if (fullName.isBlank()) "Ingresa tu nombre completo" else null
        emailError = when {
            email.isBlank() -> "Ingresa tu correo electrónico"
            !esCorreoValido(email) -> "Ingresa un correo válido"
            else -> null
        }
        phoneError = when {
            phone.isBlank() -> "Ingresa tu teléfono"
            phone.length < 9 -> "El teléfono debe tener 9 dígitos"
            else -> null
        }
        passwordError = when {
            password.isBlank() -> "Ingresa una contraseña"
            !esPasswordValida(password) -> "Debe tener al menos 6 caracteres"
            else -> null
        }
        confirmPasswordError = when {
            confirmPassword.isBlank() -> "Repite tu contraseña"
            confirmPassword != password -> "Las contraseñas no coinciden"
            else -> null
        }
        return listOf(nameError, emailError, phoneError, passwordError, confirmPasswordError).all { it == null }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(darkBackground)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Image(
            painter = painterResource(id = R.drawable.logo_barberia),
            contentDescription = "Logo",
            modifier = Modifier.size(130.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("Crear Cuenta", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Únete a la experiencia de grooming premium.", color = textGray, fontSize = 14.sp)

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = formBackground),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                CustomTextField("NOMBRE COMPLETO", "Ej. Juan Pérez", fullName, { fullName = it }, Icons.Default.Person, errorText = nameError, filtro = ::soloLetras)
                Spacer(modifier = Modifier.height(16.dp))
                CustomTextField("CORREO ELECTRÓNICO", "juan@ejemplo.com", email, { email = it }, Icons.Default.Email, KeyboardType.Email, errorText = emailError)
                Spacer(modifier = Modifier.height(16.dp))
                CustomTextField("TELÉFONO", "987654321", phone, { phone = it }, Icons.Default.Phone, KeyboardType.Phone, errorText = phoneError, filtro = ::soloDigitos)
                Spacer(modifier = Modifier.height(16.dp))
                PasswordField(value = password, onValueChange = { password = it }, errorText = passwordError)
                Spacer(modifier = Modifier.height(16.dp))
                PasswordField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = "REPETIR CONTRASEÑA",
                    errorText = confirmPasswordError
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
                                val resultado = authRepository.registrar(fullName, email, phone, password)
                                cargando = false
                                resultado
                                    .onSuccess {
                                        navController.navigate("success") {
                                            popUpTo("register") { inclusive = true }
                                        }
                                    }
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Registrarse", color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.Black)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                DivisorConTexto("O")
                Spacer(modifier = Modifier.height(20.dp))
                GoogleSignInButton(onClick = { registrarseConGoogle() }, cargando = cargandoGoogle)

                Spacer(modifier = Modifier.height(20.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    Text("¿Ya tienes una cuenta? ", color = textGray, fontSize = 14.sp)
                    Text(
                        "Inicia Sesión",
                        color = goldAccent,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { navController.navigate("login") }
                    )
                }
            }
        }
    }
}
