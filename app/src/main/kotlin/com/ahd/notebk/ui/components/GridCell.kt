package com.ahd.notebk.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RowScope.GridCell(
    text: String,
    weight: Float,
    isHeader: Boolean = false,
    color: Color = Color.Unspecified,
    fontWeight: FontWeight = FontWeight.Normal
) {
    Box(
        modifier = Modifier
            .weight(weight)
            .border(0.5.dp, Color(0xFFCBD5E1))
            .padding(vertical = 9.dp, horizontal = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = if (isHeader) 11.sp else 12.sp,
            fontWeight = if (isHeader) FontWeight.Bold else fontWeight,
            color = if (color == Color.Unspecified) Color(0xFF1E293B) else color,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}
