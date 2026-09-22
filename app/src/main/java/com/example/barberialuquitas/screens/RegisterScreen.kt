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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.barberialuquitas.R
import com.example.barberialuquitas.components.CustomTextField
import com.example.barberialuquitas.components.PasswordField
import com.example.barberialuquitas.data.AuthRepository
import com.example.barberialuquitas.util.esCorreoValido
import com.example.barberialuquitas.util.esPasswordValida
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(navController: NavController, authRepository: AuthRepository = remember { AuthRepository() }) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var mensajeGeneral by remember { mutableStateOf<String?>(null) }
    var cargando by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

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
        phoneError = if (phone.isBlank()) "Ingresa tu teléfono" else null
        passwordError = when {
            password.isBlank() -> "Ingresa una contraseña"
            !esPasswordValida(password) -> "Debe tener al menos 6 caracteres"
            else -> null
        }
        return listOf(nameError, emailError, phoneError, passwordError).all { it == null }
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
                CustomTextField("NOMBRE COMPLETO", "Ej. Juan Pérez", fullName, { fullName = it }, Icons.Default.Person, errorText = nameError)
                Spacer(modifier = Modifier.height(16.dp))
                CustomTextField("CORREO ELECTRÓNICO", "juan@ejemplo.com", email, { email = it }, Icons.Default.Email, KeyboardType.Email, errorText = emailError)
                Spacer(modifier = Modifier.height(16.dp))
                CustomTextField("TELÉFONO", "+51 987 654 321", phone, { phone = it }, Icons.Default.Phone, KeyboardType.Phone, errorText = phoneError)
                Spacer(modifier = Modifier.height(16.dp))
                PasswordField(value = password, onValueChange = { password = it }, errorText = passwordError)

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
                    enabled = !cargando,
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
