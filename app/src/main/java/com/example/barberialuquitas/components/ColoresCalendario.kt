package com.example.barberialuquitas.components

import androidx.compose.material3.DatePickerColors
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.barberialuquitas.ui.theme.CardBackground
import com.example.barberialuquitas.ui.theme.GoldAccent
import com.example.barberialuquitas.ui.theme.TextGray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun coloresCalendarioBarberia(): DatePickerColors = DatePickerDefaults.colors(
    containerColor = CardBackground,
    titleContentColor = TextGray,
    headlineContentColor = Color.White,
    weekdayContentColor = TextGray,
    subheadContentColor = TextGray,
    navigationContentColor = Color.White,
    yearContentColor = Color.White,
    disabledYearContentColor = TextGray.copy(alpha = 0.35f),
    currentYearContentColor = GoldAccent,
    selectedYearContentColor = Color.Black,
    selectedYearContainerColor = GoldAccent,
    dayContentColor = Color.White,
    disabledDayContentColor = TextGray.copy(alpha = 0.35f),
    selectedDayContentColor = Color.Black,
    selectedDayContainerColor = GoldAccent,
    todayContentColor = GoldAccent,
    todayDateBorderColor = GoldAccent,
    dividerColor = TextGray.copy(alpha = 0.3f)
)
