package com.ahd.notebk.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ahd.notebk.R

/** Shared notebk visual language. Feature/data logic stays outside this file. */
// Canonical palette used by every screen, including the work/ledger pages.
val AppBackground = Color(0xFF070019)
val AppSurface = Color(0xFF12072E)
val AppSurfaceAlt = Color(0xFF190B3E)
val AppPrimarySoft = Color(0xFF2B1166)
val AppBorder = Color(0xFF5630A2)
val AppText = Color(0xFFF9F5FF)
val AppMuted = Color(0xFFC8B9DF)
val Purple = Color(0xFF9C4DFF)
val Green = Color(0xFF39D98A)
val Blue = Color(0xFF58A6FF)
val Red = Color(0xFFFF6B8A)
val Orange = Color(0xFFFFB52E)
val CardWorkBg = Color(0xFF103A31)
val CardExpBg = Color(0xFF3A1728)
val CardNetBg = Color(0xFF172C4A)
val AppButtonShape = RoundedCornerShape(14.dp)
val AppRadiusSmall = 12.dp
val AppRadiusMedium = 16.dp
val AppRadiusLarge = 20.dp
val AppContentPadding = 16.dp
val AppItemSpacing = 10.dp
val AppControlHeight = 48.dp
val PageBg = AppBackground
val Yellow = Color(0xFFFDE48B)
val LightBlue = Color(0xFFC9D4EA)
val HeaderBlue = Color(0xFFD8E1F3)
val NotebkBrandPurple = Purple
val AppGradient = Brush.linearGradient(listOf(Color(0xFF17004A), Color(0xFF070019), Color(0xFF020008)))

@Composable
fun NotebkScreenBackground(content: @Composable BoxScope.() -> Unit) {
    Box(Modifier.fillMaxSize().background(AppGradient)) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            drawArc(Color(0xFF5B1BFF).copy(alpha = 0.28f), 205f, 105f, false,
                androidx.compose.ui.geometry.Offset(-w * 0.40f, -h * 0.12f),
                androidx.compose.ui.geometry.Size(w * 1.02f, h * 0.50f),
                style = Stroke(2.5.dp.toPx(), cap = StrokeCap.Round))
            drawArc(Color(0xFFB94CFF).copy(alpha = 0.20f), 20f, 120f, false,
                androidx.compose.ui.geometry.Offset(w * 0.42f, h * 0.70f),
                androidx.compose.ui.geometry.Size(w * 0.84f, h * 0.44f),
                style = Stroke(2.dp.toPx(), cap = StrokeCap.Round))
        }
        content()
    }
}

@Composable
fun NotebkBrandHeader(
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = AppPrimarySoft,
        shape = RoundedCornerShape(if (compact) 18.dp else 22.dp),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = if (compact) 12.dp else 16.dp,
                vertical = if (compact) 9.dp else 12.dp
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.brand_logo),
                contentDescription = NotebkBrand.APP_NAME,
                modifier = Modifier.size(if (compact) 48.dp else 58.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                NotebkWordmark(
                    fontSize = if (compact) 18.sp else 21.sp,
                    normalColor = AppText
                )
                Text(
                    NotebkBrand.DEVELOPER_NAME,
                    color = AppMuted,
                    fontSize = if (compact) 9.sp else 10.sp,
                    letterSpacing = 1.2.sp
                )
            }
        }
    }
}

@Composable
fun NotebkPage(
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .safeDrawingPadding()
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = AppSurface,
            shape = RoundedCornerShape(AppRadiusLarge),
            tonalElevation = 0.dp,
            shadowElevation = 1.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onBack != null) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "رجوع", tint = NotebkBrandPurple)
                    }
                }
                Column(Modifier.weight(1f).padding(horizontal = 6.dp)) {
                    Text(brandifyText(title, AppText), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AppText)
                    if (subtitle != null) Text(brandifyText(subtitle, AppMuted), fontSize = 11.sp, color = AppMuted)
                }
                Image(
                    painter = painterResource(R.drawable.brand_logo),
                    contentDescription = NotebkBrand.APP_NAME,
                    modifier = Modifier.size(44.dp)
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 2.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content
        )
    }
}

@Composable
fun NotebkSectionCard(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppRadiusMedium),
        colors = CardDefaults.cardColors(containerColor = AppSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, AppBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(brandifyText(title, AppText), fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = AppText)
            if (subtitle != null) Text(brandifyText(subtitle, AppMuted), fontSize = 11.sp, color = AppMuted)
            content()
        }
    }
}

@Composable
fun NotebkPrimaryAction(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = NotebkBrandPurple)
    ) { Text(text, fontWeight = FontWeight.SemiBold) }
}

@Composable
fun NotebkSecondaryAction(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = NotebkBrandPurple)
    ) { Text(text) }
}

@Composable
fun NotebkStatCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, AppBorder)
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(title, fontSize = 11.sp, color = AppMuted)
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = AppText)
        }
    }
}

@Composable
fun AppTopBar(
    title: String,
    subtitle: String? = null,
    onMenu: (() -> Unit)? = null,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    leadingContentDescription: String? = null,
    onLeading: (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = AppPrimarySoft,
        shape = RoundedCornerShape(22.dp),
        tonalElevation = 0.dp,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            when {
                onLeading != null && leadingIcon != null -> {
                    IconButton(onClick = onLeading) {
                        Icon(leadingIcon, contentDescription = leadingContentDescription, tint = NotebkBrandPurple)
                    }
                }
                onMenu != null -> {
                    IconButton(onClick = onMenu) {
                        Icon(Icons.Default.Menu, contentDescription = "القائمة", tint = NotebkBrandPurple)
                    }
                }
            }
            Column(Modifier.weight(1f).padding(horizontal = 6.dp)) {
                Text(brandifyText(title, AppText), color = AppText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                if (!subtitle.isNullOrBlank()) Text(brandifyText(subtitle, AppMuted), color = AppMuted, fontSize = 10.sp)
            }
            Image(
                painter = painterResource(R.drawable.brand_logo),
                contentDescription = NotebkBrand.APP_NAME,
                modifier = Modifier.size(46.dp)
            )
            trailingContent?.invoke()
        }
    }
}

@Composable
fun AppSectionCard(title: String, subtitle: String? = null, content: @Composable () -> Unit) =
    NotebkSectionCard(title, subtitle, content = { content() })

@Composable
fun AppSettingItem(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppSurfaceAlt),
        border = androidx.compose.foundation.BorderStroke(1.dp, AppBorder)
    ) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(
                color = AppPrimarySoft,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = NotebkBrandPurple, modifier = Modifier.size(25.dp))
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, color = AppText)
                Text(brandifyText(description, AppMuted), fontSize = 11.sp, color = AppMuted)
            }
        }
    }
}
