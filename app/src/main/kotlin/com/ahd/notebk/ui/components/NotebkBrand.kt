package com.ahd.notebk.ui.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.height
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ahd.notebk.R

/**
 * Canonical notebk identity.
 * Latin brand assets stay LTR even when the application UI is RTL.
 */
object NotebkBrand {
    const val APP_NAME = "notebk"
    const val DEVELOPER_NAME = "AHD DEV TEAM"
    const val TAGLINE = "إدارة سجلاتك بكل سهولة"
    const val CREDIT = "عمل المهندس أحمد عبدالودود الدبعي"
    const val STORY = "كل سجل .. يروي قصة"
}

/** Official notebk wordmark asset, separated from the notebook icon. */
@Composable
fun NotebkWordmarkImage(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(R.drawable.notebk_wordmark),
        contentDescription = NotebkBrand.APP_NAME,
        modifier = modifier,
        contentScale = ContentScale.Fit
    )
}

/** Official notebook + AHD application mark. */
@Composable
fun BrandLogo(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(R.drawable.brand_logo),
        contentDescription = NotebkBrand.APP_NAME,
        modifier = modifier,
        contentScale = ContentScale.Fit
    )
}

/** Official AHD developer mark. */
@Composable
fun AhdDeveloperMark(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(R.drawable.developer_logo),
        contentDescription = NotebkBrand.DEVELOPER_NAME,
        modifier = modifier,
        contentScale = ContentScale.Fit
    )
}

fun brandifyText(
    text: String,
    color: androidx.compose.ui.graphics.Color? = null
): AnnotatedString {
    val marker = NotebkBrand.APP_NAME
    val index = text.indexOf(marker)
    if (index < 0) return AnnotatedString(text)
    return buildAnnotatedString {
        append(text.substring(0, index))
        withStyle(
            SpanStyle(
                fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold,
                color = color ?: androidx.compose.ui.graphics.Color.Unspecified
            )
        ) {
            append(marker)
        }
        append(text.substring(index + marker.length))
    }
}

/**
 * Reusable official notebk wordmark for compact UI surfaces.
 * The asset is used instead of plain text so the branded purple `b` is preserved.
 */
@Composable
fun NotebkWordmark(
    modifier: Modifier = Modifier,
    fontSize: androidx.compose.ui.unit.TextUnit = 22.sp,
    normalColor: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color.White
) {
    Image(
        painter = painterResource(R.drawable.notebk_wordmark),
        contentDescription = NotebkBrand.APP_NAME,
        modifier = modifier.height(fontSize.value.dp * 0.95f),
        contentScale = ContentScale.Fit
    )
}
