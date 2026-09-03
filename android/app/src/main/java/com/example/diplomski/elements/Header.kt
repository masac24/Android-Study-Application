package com.example.diplomski.elements

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.diplomski.ui.theme.TextSecondary

@Composable
fun Header(label : String, profile: ()->Unit, notifications: () -> Unit, messageCount: Int) {
    Row(horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth().weight(0.8f),
            horizontalAlignment = Alignment.Start) {
            Text(
                label,
                fontWeight = FontWeight.Bold,
                fontSize = 35.sp,
                modifier = Modifier.padding(16.dp),
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
            )
        }

        Row(Modifier.fillMaxWidth().weight(0.2f), horizontalArrangement = Arrangement.End) {
            Icon(Icons.Filled.Person, null,
                modifier = Modifier.clickable(
                    onClick = {profile()}
                ).size(35.dp).padding(end = 8.dp))
            BadgedBox(
                badge = {
                    if (messageCount > 0) {
                        Badge { Text(messageCount.toString())}
                    }
                },
                modifier = Modifier.padding(end = 12.dp)
            ) {
                Icon(
                    Icons.Filled.Notifications,
                    contentDescription = null,
                    modifier = Modifier.size(30.dp).clickable { notifications() }
                )
            }
        }

    }
}

@Composable
fun SubHeader(label: String){
    Row(horizontalArrangement = Arrangement.Start,
        modifier = Modifier.fillMaxWidth()) {
        Spacer(Modifier.height(10.dp))
        Text(label,
        fontWeight = FontWeight.Light,
        color = TextSecondary,
        fontSize = 15.sp,
        modifier = Modifier.padding(8.dp))

    }
}