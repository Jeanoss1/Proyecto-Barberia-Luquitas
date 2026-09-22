package com.example.barberialuquitas.screens.cliente

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.barberialuquitas.data.Usuario

private val DarkBackground = Color(0xFF151515)
private val CardBackground = Color(0xFF252525)
private val GoldAccent = Color(0xFFE5B94E)
private val TextGray = Color(0xFFA0A0A0)

@Composable
fun PerfilScreen(usuario: Usuario, alCerrarSesion: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().background(DarkBackground).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Box(
            modifier = Modifier.size(90.dp).background(CardBackground, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Person, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(48.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(usuario.nombre, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(usuario.rol.replaceFirstChar { it.uppercase() }, color = GoldAccent, fontSize = 13.sp)

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                DatoPerfil(Icons.Default.Email, "Correo", usuario.correo)
                Spacer(modifier = Modifier.height(16.dp))
                DatoPerfil(Icons.Default.Phone, "Teléfono", usuario.telefono)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = alCerrarSesion,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Cerrar sesión", color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun DatoPerfil(icon: ImageVector, etiqueta: String, valor: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(etiqueta, color = TextGray, fontSize = 12.sp)
            Text(valor, color = Color.White, fontSize = 15.sp)
        }
    }
}
