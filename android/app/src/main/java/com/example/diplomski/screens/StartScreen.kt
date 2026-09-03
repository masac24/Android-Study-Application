package com.example.diplomski.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.diplomski.ui.theme.PrimaryColor

@Composable
fun StartScreen() {

    Column(Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {
        Text("STUDY",
            fontSize = 100.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryColor
        )
        Text("APP",
            fontSize = 100.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryColor
        )
    }
}