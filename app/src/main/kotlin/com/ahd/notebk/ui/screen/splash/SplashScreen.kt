package com.ahd.notebk.ui.screen.splash

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ahd.notebk.R
import kotlinx.coroutines.delay

private val SplashBackground = Color(0xFF090024)
private val SplashPurple = Color(0xFF5B1BFF)
private val SplashViolet = Color(0xFFB94CFF)
private val SplashCyan = Color(0xFF00D9FF)

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    var started by remember { mutableStateOf(false) }

    val logoAlpha by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(650, easing = FastOutSlowInEasing),
        label = "logoAlpha"
    )
    val logoScale by animateFloatAsState(
        targetValue = if (started) 1f else 0.72f,
        animationSpec = tween(950, easing = FastOutSlowInEasing),
        label = "logoScale"
    )
    val titleAlpha by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(750, delayMillis = 250),
        label = "titleAlpha"
    )
    val titleScale by animateFloatAsState(
        targetValue = if (started) 1f else 0.82f,
        animationSpec = tween(800, delayMillis = 250, easing = FastOutSlowInEasing),
        label = "titleScale"
    )
    val subtitleAlpha by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(700, delayMillis = 450),
        label = "subtitleAlpha"
    )
    val footerAlpha by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(700, delayMillis = 700),
        label = "footerAlpha"
    )

    val infinite = rememberInfiniteTransition(label = "brandPulse")
    val pulse by infinite.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.035f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logoPulse"
    )
    val glow by infinite.animateFloat(
        initialValue = 0.32f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    LaunchedEffect(Unit) {
        started = true
        delay(2700)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF25106F), SplashBackground, Color(0xFF03000D)),
                    radius = 1000f
                )
            )
    ) {
        Canvas(Modifier.fillMaxSize()) {
            // Decorative flowing neon curves matching the approved identity.
            val w = size.width
            val h = size.height
            drawArc(
                color = SplashPurple.copy(alpha = 0.32f),
                startAngle = 205f,
                sweepAngle = 105f,
                useCenter = false,
                topLeft = androidx.compose.ui.geometry.Offset(-w * 0.38f, -h * 0.12f),
                size = androidx.compose.ui.geometry.Size(w * 0.98f, h * 0.48f),
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )
            drawArc(
                color = SplashViolet.copy(alpha = 0.22f),
                startAngle = 20f,
                sweepAngle = 120f,
                useCenter = false,
                topLeft = androidx.compose.ui.geometry.Offset(w * 0.42f, h * 0.70f),
                size = androidx.compose.ui.geometry.Size(w * 0.82f, h * 0.44f),
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            )
            drawCircle(
                color = SplashPurple.copy(alpha = glow * 0.18f),
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
                text = "كل سجل .. يروي قصة",
                color = Color.White.copy(alpha = 0.92f),
                fontSize = 21.sp,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.alpha(subtitleAlpha)
            )

            Spacer(Modifier.height(32.dp))

            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(215.dp)
                        .shadow(26.dp, RoundedCornerShape(46.dp), ambientColor = SplashPurple.copy(alpha = glow), spotColor = SplashPurple.copy(alpha = glow))
                        .background(
                            Brush.radialGradient(
                                listOf(Color(0xFF6D2BFF), Color(0xFF2B087A), Color(0xFF16004B))
                            ),
                            RoundedCornerShape(46.dp)
                        )
                        .alpha(logoAlpha)
                )
                Image(
                    painter = painterResource(R.drawable.brand_logo),
                    contentDescription = "شعار notebk",
                    modifier = Modifier
                        .size(205.dp)
                        .scale(logoScale * pulse)
                        .alpha(logoAlpha),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(Modifier.height(22.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .scale(titleScale)
                    .alpha(titleAlpha)
            ) {
                Text("note", color = Color.White, fontSize = 54.sp, fontWeight = FontWeight.ExtraBold)
                Text(
                    "b",
                    style = TextStyle(
                        brush = Brush.verticalGradient(listOf(Color(0xFFFF7BFF), SplashViolet, SplashPurple))
                    ),
                    fontSize = 54.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text("k", color = Color.White, fontSize = 54.sp, fontWeight = FontWeight.ExtraBold)
            }

            Spacer(Modifier.height(5.dp))

            Text(
                text = "إدارة سجلاتك بكل سهولة",
                color = Color.White.copy(alpha = 0.88f),
                fontSize = 19.sp,
                modifier = Modifier.alpha(subtitleAlpha)
            )

            Spacer(Modifier.height(24.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(9.dp),
                modifier = Modifier.alpha(subtitleAlpha)
            ) {
                Box(Modifier.width(62.dp).height(7.dp).background(SplashViolet, RoundedCornerShape(10.dp)))
                repeat(4) {
                    Box(Modifier.size(9.dp).background(SplashPurple.copy(alpha = 0.9f), RoundedCornerShape(50)))
                }
            }

            Spacer(Modifier.height(48.dp))

            Image(
                painter = painterResource(R.drawable.brand_logo),
                contentDescription = "AHD Dev Team",
                modifier = Modifier
                    .size(62.dp)
                    .alpha(footerAlpha)
                    .scale(pulse),
                contentScale = ContentScale.Fit
            )
            Text(
                "AHD DEV TEAM",
                color = Color.White.copy(alpha = 0.92f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 3.sp,
                modifier = Modifier.alpha(footerAlpha)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "عمل المهندس أحمد عبدالودود الدبعي",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 14.sp,
                modifier = Modifier.alpha(footerAlpha)
            )
        }
    }
}
