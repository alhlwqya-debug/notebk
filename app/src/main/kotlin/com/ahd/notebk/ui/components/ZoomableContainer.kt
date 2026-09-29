package com.ahd.notebk.ui.components

import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

@Composable
fun ZoomableContainer(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var scale by remember { mutableStateOf(1f) }
    val clampedScale = scale.coerceIn(0.70f, 1.40f)

    Box(
        modifier = modifier.pointerInput(Unit) {
            detectTransformGestures { _, _, zoom, _ ->
                scale = (scale * zoom).coerceIn(0.70f, 1.40f)
            }
        }
    ) {
        Box(
            modifier = Modifier.graphicsLayer {
                scaleX = clampedScale
                scaleY = clampedScale
                transformOrigin = TransformOrigin(0f, 0f)
            }
        ) {
            content()
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FloatingActionButton(
                onClick = { scale = (scale - 0.10f).coerceIn(0.70f, 1.40f) },
                modifier = Modifier.size(42.dp)
            ) {
                Icon(Icons.Default.Remove, contentDescription = "تصغير")
            }

            Surface(tonalElevation = 3.dp) {
                Text(
                    text = "${(clampedScale * 100).toInt()}%",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                )
            }

            FloatingActionButton(
                onClick = { scale = (scale + 0.10f).coerceIn(0.70f, 1.40f) },
                modifier = Modifier.size(42.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "تكبير")
            }

            FloatingActionButton(
                onClick = { scale = 1f },
                modifier = Modifier.size(42.dp)
            ) {
                Icon(Icons.Default.RestartAlt, contentDescription = "إعادة الحجم")
            }
        }
    }
}
