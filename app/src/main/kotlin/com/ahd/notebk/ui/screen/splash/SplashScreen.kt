package com.ahd.notebk.ui.screen.splash

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.ahd.notebk.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val introScale = remember { Animatable(0.62f) }
    val introAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        introAlpha.animateTo(1f, tween(550))
        introScale.animateTo(1.08f, tween(650, easing = FastOutSlowInEasing))
        introScale.animateTo(1f, tween(350, easing = FastOutSlowInEasing))
    }

    val pulse = rememberInfiniteTransition(label = "logoPulse")
    val glowScale by pulse.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowScale"
    )
    val glowAlpha by pulse.animateFloat(
        initialValue = 0.08f,
        targetValue = 0.20f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )
    val logoRotation by pulse.animateFloat(
        initialValue = -1.2f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logoRotation"
    )

    LaunchedEffect(Unit) {
        delay(1800)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        // Soft purple halo behind the logo.
        Box(
            modifier = Modifier
                .size(230.dp)
                .scale(glowScale)
                .alpha(glowAlpha)
                .background(Color(0xFF6A22FF), CircleShape),
        )

        Image(
            painter = painterResource(R.drawable.splash_logo),
            contentDescription = "notebk logo",
            modifier = Modifier
                .size(190.dp)
                .scale(introScale.value)
                .rotate(logoRotation)
                .alpha(introAlpha.value)
        )
    }
}
