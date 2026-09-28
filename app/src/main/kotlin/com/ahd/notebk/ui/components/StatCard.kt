package com.ahd.notebk.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun StatCard(title: String, value: String, modifier: Modifier = Modifier, valueColor: Color = Color(0xFF0F172A)) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC))) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(title, fontSize = 10.sp, color = Color(0xFF64748B))
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = valueColor)
        }
    }
}
