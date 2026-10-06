package com.ahd.notebk.ui.screen.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ahd.notebk.R
import com.ahd.notebk.ui.components.NotebkBrand
import com.ahd.notebk.ui.components.NotebkWordmarkImage
import kotlinx.coroutines.delay

private val SplashBackground = Color(0xFF070019)
private val SplashPurple = Color(0xFF5B1BFF)
private val SplashViolet = Color(0xFFB94CFF)

/**
 * Official notebk welcome/splash composition.
 * Assets are deliberately separated: app icon, wordmark, and AHD developer identity.
 */
@Composable
fun SplashScreen(onFinished: () -> Unit) {
    var started by remember { mutableStateOf(false) }
    val logoAlpha by animateFloatAsState(if (started) 1f else 0f, tween(650), label = "logoAlpha")
    val logoScale by animateFloatAsState(if (started) 1f else 0.76f, tween(900, easing = FastOutSlowInEasing), label = "logoScale")
    val wordmarkAlpha by animateFloatAsState(if (started) 1f else 0f, tween(700, 220), label = "wordmarkAlpha")
    val wordmarkScale by animateFloatAsState(if (started) 1f else 0.88f, tween(750, 220, easing = FastOutSlowInEasing), label = "wordmarkScale")
    val textAlpha by animateFloatAsState(if (started) 1f else 0f, tween(650, 420), label = "textAlpha")
    val footerAlpha by animateFloatAsState(if (started) 1f else 0f, tween(700, 700), label = "footerAlpha")
    val pulse = remember { Animatable(0.98f) }
    val glow = remember { Animatable(0.28f) }

    LaunchedEffect(Unit) {
        started = true
        while (true) {
            pulse.animateTo(1.025f, tween(1500, easing = FastOutSlowInEasing))
            pulse.animateTo(0.98f, tween(1500, easing = FastOutSlowInEasing))
        }
    }
    LaunchedEffect(Unit) {
        while (true) {
            glow.animateTo(0.62f, tween(1600, easing = FastOutSlowInEasing))
            glow.animateTo(0.28f, tween(1600, easing = FastOutSlowInEasing))
        }
    }
    LaunchedEffect(Unit) {
        delay(3000)
        onFinished()
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF281078), SplashBackground, Color(0xFF020008)),
                    radius = 1200f
                )
            )
            .padding(horizontal = 22.dp)
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            drawArc(
                color = SplashPurple.copy(alpha = 0.30f),
                startAngle = 205f,
                sweepAngle = 105f,
                useCenter = false,
                topLeft = androidx.compose.ui.geometry.Offset(-w * 0.40f, -h * 0.12f),
                size = androidx.compose.ui.geometry.Size(w * 1.02f, h * 0.50f),
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )
            drawArc(
                color = SplashViolet.copy(alpha = 0.22f),
                startAngle = 20f,
                sweepAngle = 120f,
                useCenter = false,
                topLeft = androidx.compose.ui.geometry.Offset(w * 0.42f, h * 0.70f),
                size = androidx.compose.ui.geometry.Size(w * 0.84f, h * 0.44f),
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            )
            drawCircle(
                color = SplashPurple.copy(alpha = glow.value * 0.18f),
                radius = w * 0.34f,
                center = androidx.compose.ui.geometry.Offset(w / 2f, h * 0.40f)
            )
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = NotebkBrand.STORY,
                color = Color.White.copy(alpha = 0.94f),
                fontSize = 21.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.alpha(textAlpha)
            )

            Spacer(Modifier.height(30.dp))

            Box(contentAlignment = Alignment.Center) {
                Box(
                    Modifier
                        .size(246.dp)
                        .shadow(
                            28.dp,
                            RoundedCornerShape(46.dp),
                            ambientColor = SplashPurple.copy(alpha = glow.value),
                            spotColor = SplashPurple.copy(alpha = glow.value)
                        )
                        .background(
                            Brush.radialGradient(listOf(Color(0xFF6D2BFF), Color(0xFF2B087A), Color(0xFF16004B))),
                            RoundedCornerShape(46.dp)
                        )
                        .alpha(logoAlpha)
                )
                Image(
                    painter = painterResource(R.drawable.brand_logo),
                    contentDescription = NotebkBrand.APP_NAME,
                    modifier = Modifier.size(235.dp).scale(logoScale * pulse.value).alpha(logoAlpha),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(Modifier.height(20.dp))

            // The wordmark is an actual image asset, not a normal TextView/Text composable.
            NotebkWordmarkImage(
                Modifier
                    .width(235.dp)
                    .scale(wordmarkScale)
                    .alpha(wordmarkAlpha)
            )

            Spacer(Modifier.height(4.dp))
            Text(
                text = NotebkBrand.TAGLINE,
                color = Color.White.copy(alpha = 0.90f),
                fontSize = 19.sp,
                modifier = Modifier.alpha(textAlpha)
            )

            Spacer(Modifier.height(22.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(9.dp),
                modifier = Modifier.alpha(textAlpha)
            ) {
                Box(Modifier.width(62.dp).height(7.dp).background(SplashViolet, RoundedCornerShape(10.dp)))
                repeat(4) {
                    Box(Modifier.size(9.dp).background(SplashPurple.copy(alpha = 0.9f), RoundedCornerShape(50)))
                }
            }

            Spacer(Modifier.height(44.dp))
            Image(
                painter = painterResource(R.drawable.developer_logo),
                contentDescription = NotebkBrand.DEVELOPER_NAME,
                modifier = Modifier.size(width = 72.dp, height = 52.dp).alpha(footerAlpha),
                contentScale = ContentScale.Fit
            )
            Text(
                text = NotebkBrand.DEVELOPER_NAME,
                color = Color.White.copy(alpha = 0.94f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 3.sp,
                modifier = Modifier.alpha(footerAlpha)
            )
            Spacer(Modifier.height(5.dp))
            Text(
                text = NotebkBrand.CREDIT,
                color = Color.White.copy(alpha = 0.90f),
                fontSize = 14.sp,
                modifier = Modifier.alpha(footerAlpha)
            )
        }
    }
}
