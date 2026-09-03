package com.example.diplomski.elements

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.diplomski.ui.theme.TextSecondary

@SuppressLint("DefaultLocale")
@Composable
fun SessionDuration(
    setDuration: (d: Int) -> Unit
) {
    var hours by remember { mutableIntStateOf(0) }
    var minutes by remember { mutableIntStateOf(0) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Column (horizontalAlignment = Alignment.CenterHorizontally) {
            IconButton(onClick = { if (hours < 23) hours++
                setDuration(hours * 60 + minutes)
            }) {
                Icon(Icons.Default.KeyboardArrowUp, contentDescription = null)
            }
            Text(String.format("%02d", hours), fontSize = 32.sp, fontWeight = FontWeight.Medium)
            IconButton(onClick = { if (hours > 0) hours--
                setDuration(hours * 60 + minutes)}) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
            }
            Text("h", fontSize = 12.sp, color = TextSecondary)
        }

        Text(":", fontSize = 32.sp, fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 8.dp))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            IconButton(onClick = { if (minutes < 59) minutes++ else { minutes = 0; hours++ }
                setDuration(hours * 60 + minutes)
            }) {
                Icon(Icons.Default.KeyboardArrowUp, contentDescription = null)
            }
            Text(String.format("%02d", minutes), fontSize = 32.sp, fontWeight = FontWeight.Medium)
            IconButton(onClick = { if (minutes > 0) minutes-- else if (hours > 0) { minutes = 59; hours-- }
                setDuration(hours * 60 + minutes)}) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
            }
            Text("min", fontSize = 12.sp, color = TextSecondary)
        }
    }
}

