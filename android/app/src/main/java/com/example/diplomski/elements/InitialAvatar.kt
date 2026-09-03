package com.example.diplomski.elements

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun InitialAvatar(name: String, size: Int = 22) {
    val initial = name.firstOrNull()?.uppercaseChar()?.toString() ?: "?"

    val (bg, textColor) = when (initial.firstOrNull()?.code?.rem(4)) {
        0 -> Color(0xFFE1F5EE) to Color(0xFF0F6E56)
        1 -> Color(0xFFEEEDFE) to Color(0xFF534AB7)
        2 -> Color(0xFFFAEEDA) to Color(0xFF854F0B)
        else -> Color(0xFFFBEAF0) to Color(0xFF993556)
    }

    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(bg),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initial,
            fontSize = (size * 0.45).sp,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
}