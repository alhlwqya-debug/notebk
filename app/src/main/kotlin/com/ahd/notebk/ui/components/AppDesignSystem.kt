package com.ahd.notebk.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
// Snapshot-backed palette keeps custom Compose components readable in dark mode too.
var AppBackground by mutableStateOf(Color(0xFFFFFFFF))
    private set
var AppSurface by mutableStateOf(Color(0xFFFFFFFF))
    private set
var AppSurfaceAlt by mutableStateOf(Color(0xFFF8F6FC))
    private set
var AppPrimarySoft by mutableStateOf(Color(0xFFF0E7FF))
    private set
var AppBorder by mutableStateOf(Color(0xFFD8C8EF))
    private set
var AppText by mutableStateOf(Color(0xFF211B2B))
    private set
var AppMuted by mutableStateOf(Color(0xFF756C80))
    private set
val Purple = Color(0xFF7C3AED)
val Green = Color(0xFF22B573)
val Blue = Color(0xFF3B82D0)
val Red = Color(0xFFE2556B)
val Orange = Color(0xFFE7A51C)
var CardWorkBg by mutableStateOf(Color(0xFFEAF8F1))
    private set
var CardExpBg by mutableStateOf(Color(0xFFFFEEF1))
    private set
var CardNetBg by mutableStateOf(Color(0xFFEDF5FF))
    private set
val AppButtonShape = RoundedCornerShape(14.dp)
val AppRadiusSmall = 12.dp
val AppRadiusMedium = 16.dp
val AppRadiusLarge = 20.dp
val AppContentPadding = 16.dp
val AppItemSpacing = 10.dp
val AppControlHeight = 48.dp
val PageBg: Color get() = AppBackground
val Yellow = Color(0xFFFDE48B)
val LightBlue = Color(0xFFC9D4EA)
val HeaderBlue = Color(0xFFD8E1F3)
val NotebkBrandPurple = Purple
val AppGradient: Brush get() = Brush.linearGradient(listOf(AppBackground, AppSurfaceAlt))

/** Update the shared component palette whenever the app theme changes. */
fun setNotebkPalette(dark: Boolean) {
    AppBackground = if (dark) Color(0xFF121018) else Color(0xFFFFFFFF)
    AppSurface = if (dark) Color(0xFF1D1926) else Color(0xFFFFFFFF)
    AppSurfaceAlt = if (dark) Color(0xFF262132) else Color(0xFFF8F6FC)
    AppPrimarySoft = if (dark) Color(0xFF33224A) else Color(0xFFF0E7FF)
    AppBorder = if (dark) Color(0xFF493B58) else Color(0xFFD8C8EF)
    AppText = if (dark) Color(0xFFF3EDF9) else Color(0xFF211B2B)
    AppMuted = if (dark) Color(0xFFB7AFC3) else Color(0xFF756C80)
    CardWorkBg = if (dark) Color(0xFF173328) else Color(0xFFEAF8F1)
    CardExpBg = if (dark) Color(0xFF3A2229) else Color(0xFFFFEEF1)
    CardNetBg = if (dark) Color(0xFF1E2C40) else Color(0xFFEDF5FF)
}

/**
 * Shared responsive canvas:
 * - Phones keep the full available width.
 * - Tablets, foldables and desktop-sized windows use a centered readable content width.
 * - Background still fills the entire window, independent of the content width.
 */
@Composable
fun NotebkScreenBackground(content: @Composable BoxScope.() -> Unit) {
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize().background(AppGradient)
    ) {
        val contentWidth = if (maxWidth >= 840.dp) 760.dp else maxWidth
        Box(
            modifier = Modifier
                .width(contentWidth)
                .fillMaxHeight()
                .align(Alignment.TopCenter)
        ) {
            content()
        }
    }
}

@Composable
fun NotebkBrandTitle(
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    NotebkWordmark(
        modifier = modifier,
        fontSize = if (compact) 18.sp else 22.sp,
        normalColor = AppText
    )
}

@Composable
private fun NotebkTitleOrText(
    title: String,
    modifier: Modifier = Modifier
) {
    if (title == NotebkBrand.APP_NAME) {
        NotebkBrandTitle(modifier)
    } else {
        Text(
            brandifyText(title, AppText),
            modifier = modifier,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = AppText
        )
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
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .safeDrawingPadding()
    ) {
        val pageWidth = if (maxWidth >= 840.dp) 760.dp else maxWidth
        Column(
            modifier = Modifier
                .width(pageWidth)
                .fillMaxHeight()
                .align(Alignment.TopCenter)
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع", tint = NotebkBrandPurple)
                    }
                }
                Column(Modifier.weight(1f).padding(horizontal = 6.dp)) {
                    NotebkTitleOrText(title, Modifier.fillMaxWidth())
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
                NotebkTitleOrText(title)
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
