package com.example.barberialuquitas.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val GoldAccent = Color(0xFFE5B94E)
private val TextGray = Color(0xFFA0A0A0)
private val ErrorRed = Color(0xFFE57373)

@Composable
fun CustomTextField(
    label: String,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text,
    errorText: String? = null,
    filtro: ((String) -> String)? = null
) {
    Column {
        Text(text = label, color = TextGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = { nuevoValor -> onValueChange(filtro?.invoke(nuevoValor) ?: nuevoValor) },
            placeholder = { Text(placeholder, color = Color.Gray) },
            leadingIcon = { Icon(icon, contentDescription = null, tint = Color.Gray) },
            isError = errorText != null,
            supportingText = errorText?.let { { Text(it, color = ErrorRed) } },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GoldAccent, unfocusedBorderColor = Color.Transparent,
                errorBorderColor = ErrorRed,
                focusedContainerColor = Color.White, unfocusedContainerColor = Color.White,
                errorContainerColor = Color.White,
                focusedTextColor = Color.Black, unfocusedTextColor = Color.Black
            ),
            shape = RoundedCornerShape(8.dp), singleLine = true
        )
    }
}

@Composable
fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String = "CONTRASEÑA",
    errorText: String? = null
) {
    var passwordVisible by remember { mutableStateOf(false) }

    Column {
        Text(text = label, color = TextGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text("••••••••", color = Color.Gray) },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Gray) },
            trailingIcon = {
                val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, contentDescription = null, tint = Color.Gray)
                }
            },
            isError = errorText != null,
            supportingText = errorText?.let { { Text(it, color = ErrorRed) } },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GoldAccent, unfocusedBorderColor = Color.Transparent,
                errorBorderColor = ErrorRed,
                focusedContainerColor = Color.White, unfocusedContainerColor = Color.White,
                errorContainerColor = Color.White,
                focusedTextColor = Color.Black, unfocusedTextColor = Color.Black
            ),
            shape = RoundedCornerShape(8.dp), singleLine = true
        )
    }
}

@Composable
fun DivisorConTexto(texto: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = TextGray.copy(alpha = 0.3f))
        Text(texto, color = TextGray, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 12.dp))
        HorizontalDivider(modifier = Modifier.weight(1f), color = TextGray.copy(alpha = 0.3f))
    }
}

@Composable
fun GoogleSignInButton(onClick: () -> Unit, cargando: Boolean = false) {
    OutlinedButton(
        onClick = onClick,
        enabled = !cargando,
        modifier = Modifier.fillMaxWidth().height(50.dp),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White, contentColor = Color.Black),
        border = BorderStroke(1.dp, Color.LightGray),
        shape = RoundedCornerShape(8.dp)
    ) {
        if (cargando) {
            CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(20.dp))
        } else {
            Box(modifier = Modifier.size(20.dp), contentAlignment = Alignment.Center) {
                Text("G", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = GoldAccent)
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text("Continuar con Google", fontSize = 15.sp, fontWeight = FontWeight.Medium)
        }
    }
}
