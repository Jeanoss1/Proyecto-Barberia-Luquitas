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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.barberialuquitas.screens.AdminHomeScreen
import com.example.barberialuquitas.screens.LoginScreen
import com.example.barberialuquitas.screens.RegisterScreen
import com.example.barberialuquitas.screens.SuccessScreen
import com.example.barberialuquitas.screens.cliente.ClienteHomeScreen
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
                        NavHost(navController = navController, startDestination = "login") {
                            composable("login") { LoginScreen(navController) }
                            composable("register") { RegisterScreen(navController) }
                            composable("success") { SuccessScreen(navController) }
                            composable("home_cliente") { ClienteHomeScreen(navController) }
                            composable("home_admin") { AdminHomeScreen(navController) }
                        }
                    }
                }
            }
        }
    }
}
