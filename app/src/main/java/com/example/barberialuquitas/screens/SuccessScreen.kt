package com.example.barberialuquitas.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.barberialuquitas.R

@Composable
fun SuccessScreen(navController: NavController) {
    val darkBackground = Color(0xFF151515)
    val goldAccent = Color(0xFFE5B94E)
    val textGray = Color(0xFFA0A0A0)

    Column(
        modifier = Modifier.fillMaxSize().background(darkBackground).padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(painter = painterResource(id = R.drawable.logo_barberia), contentDescription = "Logo", modifier = Modifier.size(130.dp))
        Spacer(modifier = Modifier.height(48.dp))
        
        Image(painter = painterResource(id = R.drawable.check_dorado), contentDescription = "Check", modifier = Modifier.size(80.dp))
        Spacer(modifier = Modifier.height(24.dp))

        Text("¡Cuenta creada con éxito!", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(16.dp))
        
        Text("Bienvenido al club. Ya puedes explorar nuestros servicios y reservar tu primera cita.", color = textGray, fontSize = 16.sp, textAlign = TextAlign.Center, lineHeight = 22.sp)
        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = {
                navController.navigate("home_cliente") {
                    popUpTo(0)
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = goldAccent),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Comenzar", color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}
