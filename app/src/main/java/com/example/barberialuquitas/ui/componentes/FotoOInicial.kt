package com.example.barberialuquitas.ui.componentes

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.barberialuquitas.ui.theme.DarkBackground
import com.example.barberialuquitas.ui.theme.GoldAccent

// Foto desde una URL; si no hay, el logo incluido en la app (logoLocal) o la inicial del nombre.
// Se usa para barberías, barberos y servicios.
@Composable
fun FotoOInicial(
    url: String?,
    nombre: String,
    tamano: Dp = 56.dp,
    forma: Shape = CircleShape,
    @DrawableRes logoLocal: Int? = null
) {
    val modificador = Modifier.size(tamano).clip(forma).background(DarkBackground)
    if (!url.isNullOrBlank()) {
        AsyncImage(
            model = url,
            contentDescription = "Foto de $nombre",
            contentScale = ContentScale.Crop,
            modifier = modificador
        )
    } else if (logoLocal != null) {
        Image(
            painter = painterResource(logoLocal),
            contentDescription = "Logo de $nombre",
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(tamano).clip(forma).background(Color.White)
        )
    } else {
        Box(modifier = modificador, contentAlignment = Alignment.Center) {
            Text(
                nombre.take(1).uppercase(),
                color = GoldAccent,
                fontSize = (tamano.value * 0.4f).sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
