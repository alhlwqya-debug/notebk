package com.ahd.notebk.ui.navigation

import com.ahd.notebk.data.report.LocalProfileImage
import com.ahd.notebk.domain.model.ShopRecord
import com.ahd.notebk.ui.components.*

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.ahd.notebk.data.cloud.AppUpdateRepository

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
internal fun DrawerContent(
    modifier: Modifier,
    shops: List<ShopRecord>,
    selectedShopId: Long?,
    userName: String,
    userImagePath: String,
    onClose: () -> Unit,
    onSelectShop: (Long) -> Unit,
    onHome: () -> Unit,
    onLedger: () -> Unit,
    onDailyWork: () -> Unit,
    onMonths: () -> Unit,
    onPieces: () -> Unit,
    onReports: () -> Unit,
    onAddShop: () -> Unit,
    onUserProfile: () -> Unit,
    onStatistics: () -> Unit,
    onShopSettings: (Long) -> Unit,
    onAppSettings: () -> Unit,
    onAbout: () -> Unit,
    onHelp: () -> Unit,
    onDeleteShop: () -> Unit
) {
    val currentShop = shops.firstOrNull { it.id == selectedShopId }
    val primary = NotebkBrandPurple
    val surface = AppSurface
    val text = AppText
    val muted = AppMuted
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Surface(
            modifier = modifier.fillMaxHeight(),
            color = surface
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 20.dp,
                    end = 20.dp,
                    top = 12.dp,
                    bottom = 28.dp
                ),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(AppPrimarySoft)
                            .padding(horizontal = 10.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BrandLogo(modifier = Modifier.size(42.dp))
                        Spacer(Modifier.width(9.dp))
                        Column(Modifier.weight(1f)) {
                            NotebkWordmark(fontSize = 16.sp, normalColor = AppText)
                            Text(NotebkBrand.DEVELOPER_NAME, fontSize = 9.sp, color = AppMuted)
                        }
                        IconButton(onClick = onClose, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = text)
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                Icons.Default.Menu,
                                contentDescription = null,
                                tint = primary,
                                modifier = Modifier.size(25.dp)
                            )
                            Text(
                                "القائمة",
                                fontSize = 21.sp,
                                fontWeight = FontWeight.Bold,
                                color = text
                            )
                        }

                        IconButton(
                            onClick = onClose,
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(AppSurfaceAlt)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "إغلاق",
                                tint = text,
                                modifier = Modifier.size(21.dp)
                            )
                        }
                    }

                    HorizontalDivider(
                        color = AppBorder,
                        modifier = Modifier.padding(top = 2.dp, bottom = 16.dp)
                    )
                }

                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = AppSurfaceAlt,
                        tonalElevation = 1.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(surface),
                                contentAlignment = Alignment.Center
                            ) {
                                if (userImagePath.isNotBlank()) {
                                    LocalProfileImage(
                                        path = userImagePath,
                                        contentDescription = "الصورة الشخصية",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = null,
                                        tint = primary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }

                            Spacer(Modifier.width(12.dp))

                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.Start
                            ) {
                                Text(
                                    if (userName.isBlank()) "المستخدم" else userName,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = text,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(Modifier.height(3.dp))
                                Text(
                                    currentShop?.name?.takeIf { it.isNotBlank() }
                                        ?: "لم يتم اختيار محل",
                                    fontSize = 13.sp,
                                    color = muted,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(18.dp))
                }

                item {
                    DrawerSectionTitle("الرئيسية")
                }

                item {
                    DrawerNavItem(
                        text = "الرئيسية",
                        icon = Icons.Default.Home,
                        selected = true,
                        onClick = onHome
                    )
                }

                item {
                    DrawerNavItem(
                        text = "الدفتر والسجلات",
                        icon = Icons.Default.Book,
                        selected = false,
                        onClick = onLedger
                    )
                }

                item {
                    DrawerNavItem(
                        text = "تسجيل العمل اليومي",
                        icon = Icons.Default.Today,
                        selected = false,
                        onClick = onDailyWork
                    )
                }

                item {
                    DrawerNavItem(
                        text = "الأشهر",
                        icon = Icons.Default.CalendarMonth,
                        selected = false,
                        onClick = onMonths
                    )
                }

                item {
                    DrawerNavItem(
                        text = "إدارة القطع",
                        icon = Icons.Default.Category,
                        selected = false,
                        onClick = onPieces
                    )
                }

                item {
                    DrawerNavItem(
                        text = "التقارير",
                        icon = Icons.Default.Description,
                        selected = false,
                        onClick = onReports
                    )
                }

                item {
                    Spacer(Modifier.height(18.dp))
                    DrawerSectionTitle("المحلات")
                }

                if (shops.isEmpty()) {
                    item {
                        Text(
                            "لا توجد محلات مضافة",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 14.dp),
                            color = muted,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    items(
                        items = shops,
                        key = { it.id }
                    ) { shop ->
                        DrawerShopItem(
                            shop = shop,
                            selected = shop.id == selectedShopId,
                            onSelect = { onSelectShop(shop.id) },
                            onSettings = { onShopSettings(shop.id) }
                        )
                    }
                }

                item {
                    DrawerActionItem(
                        text = "إضافة محل جديد",
                        icon = Icons.Default.Add,
                        tint = primary,
                        onClick = onAddShop
                    )

                    if (selectedShopId != null) {
                        DrawerActionItem(
                            text = "حذف المحل الحالي",
                            icon = Icons.Default.Delete,
                            tint = Red,
                            onClick = onDeleteShop
                        )
                    }

                    Spacer(Modifier.height(18.dp))
                    DrawerSectionTitle("الحساب والتطبيق")
                }

                item {
                    DrawerNavItem(
                        text = "ملفي الشخصي",
                        icon = Icons.Default.Person,
                        selected = false,
                        onClick = onUserProfile
                    )
                }

                item {
                    DrawerNavItem(
                        text = "إعدادات التطبيق",
                        icon = Icons.Default.Settings,
                        selected = false,
                        onClick = onAppSettings
                    )
                }

                item {
                    DrawerNavItem(
                        text = "الإحصائيات",
                        icon = Icons.Default.Assessment,
                        selected = false,
                        onClick = onStatistics
                    )
                }

                item {
                    Spacer(Modifier.height(18.dp))
                    DrawerSectionTitle("متجر AHD")
                }

                item {
                    DrawerNavItem(
                        text = "متجرنا - المزيد من التطبيقات",
                        icon = Icons.Default.Storefront,
                        selected = false,
                        onClick = {
                            onClose()
                            runCatching {
                                context.startActivity(
                                    Intent(Intent.ACTION_VIEW, Uri.parse("https://ahdstoer.vercel.app"))
                                )
                            }.onFailure {
                                Toast.makeText(context, "تعذر فتح متجر AHD", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }

                item {
                    DrawerNavItem(
                        text = "التحقق من تحديث التطبيق",
                        icon = Icons.Default.Update,
                        selected = false,
                        onClick = {
                            onClose()
                            Toast.makeText(context, "جارٍ التحقق من وجود تحديث...", Toast.LENGTH_SHORT).show()
                            coroutineScope.launch {
                                when (val result = AppUpdateRepository.checkManually(context)) {
                                    is AppUpdateRepository.ManualCheckResult.UpdateAvailable -> {
                                        Toast.makeText(context, "يتوفر تحديث جديد؛ سيتم فتح صفحة التطبيق في متجر AHD", Toast.LENGTH_LONG).show()
                                        runCatching {
                                            context.startActivity(
                                                Intent(Intent.ACTION_VIEW, Uri.parse(result.detailsUrl))
                                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            )
                                        }.onFailure {
                                            Toast.makeText(context, "تعذر فتح صفحة التحديث", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                    AppUpdateRepository.ManualCheckResult.UpToDate ->
                                        Toast.makeText(context, "تطبيقك محدّث إلى آخر إصدار منشور", Toast.LENGTH_LONG).show()
                                    AppUpdateRepository.ManualCheckResult.NotPublished ->
                                        Toast.makeText(context, "لم يتم نشر إصدار التطبيق في متجر AHD بعد", Toast.LENGTH_LONG).show()
                                    AppUpdateRepository.ManualCheckResult.Failed ->
                                        Toast.makeText(context, "تعذر التحقق الآن؛ تحقق من اتصال الإنترنت وحاول مجددًا", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    )
                }

                item {
                    Spacer(Modifier.height(18.dp))
                    DrawerSectionTitle("المساعدة")
                }

                item {
                    DrawerNavItem(
                        text = "دليل الاستخدام",
                        icon = Icons.Default.HelpOutline,
                        selected = false,
                        onClick = onHelp
                    )
                }

                item {
                    DrawerNavItem(
                        text = "من نحن",
                        icon = Icons.Default.Info,
                        selected = false,
                        onClick = onAbout
                    )
                }

                item {
                    Spacer(Modifier.height(18.dp))
                    HorizontalDivider(
                        color = AppBorder
                    )
                    Spacer(Modifier.height(12.dp))
                    NotebkWordmark(
                        modifier = Modifier.fillMaxWidth(),
                        fontSize = 12.sp,
                        normalColor = muted
                    )
                    Text(
                        NotebkBrand.DEVELOPER_NAME,
                        modifier = Modifier.fillMaxWidth(),
                        fontSize = 10.sp,
                        color = muted,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        NotebkBrand.CREDIT,
                        modifier = Modifier.fillMaxWidth(),
                        fontSize = 10.sp,
                        color = muted,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun DrawerSectionTitle(title: String) {
    Text(
        title,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 7.dp),
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = AppMuted
    )
}

@Composable
private fun DrawerNavItem(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    val primary = NotebkBrandPurple
    val itemColor = if (selected) primary else AppText
    val background = if (selected) {
        primary.copy(alpha = 0.12f)
    } else {
        Color.Transparent
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(background)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = itemColor,
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.width(14.dp))
        Text(
            text,
            modifier = Modifier.weight(1f),
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = itemColor
        )
    }
}

@Composable
private fun DrawerShopItem(
    shop: ShopRecord,
    selected: Boolean,
    onSelect: () -> Unit,
    onSettings: () -> Unit
) {
    val primary = NotebkBrandPurple
    val text = AppText
    val muted = AppMuted

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (selected) primary.copy(alpha = 0.10f)
                else Color.Transparent
            )
            .clickable(onClick = onSelect)
            .padding(start = 12.dp, end = 4.dp, top = 9.dp, bottom = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(
                    if (selected) primary.copy(alpha = 0.14f)
                    else MaterialTheme.colorScheme.surfaceVariant
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Storefront,
                contentDescription = null,
                tint = if (selected) primary else muted,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(Modifier.width(11.dp))

        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                shop.name,
                fontSize = 14.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (selected) {
                Spacer(Modifier.height(2.dp))
                Text(
                    "المحل الحالي",
                    fontSize = 11.sp,
                    color = primary
                )
            }
        }

        IconButton(
            onClick = onSettings,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                Icons.Default.Settings,
                contentDescription = "إعدادات ${shop.name}",
                tint = muted,
                modifier = Modifier.size(19.dp)
            )
        }
    }
}

@Composable
private fun DrawerActionItem(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(21.dp)
        )
        Spacer(Modifier.width(14.dp))
        Text(
            text,
            modifier = Modifier.weight(1f),
            fontSize = 14.sp,
            color = tint,
            fontWeight = FontWeight.Medium
        )
    }
}
