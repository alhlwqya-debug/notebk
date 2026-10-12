package com.ahd.notebk.ui.screen.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ahd.notebk.ui.components.AppSectionCard
import com.ahd.notebk.ui.components.NotebkBrandHeader
import com.ahd.notebk.ui.components.AppSettingItem
import com.ahd.notebk.ui.components.AppTopBar
import com.ahd.notebk.ui.components.NotebkBrand

private data class ControlItem(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val action: () -> Unit
)

@Composable
fun ControlCenterScreen(
    shopName: String,
    reminderEnabled: Boolean,
    onBack: () -> Unit,
    onProfile: () -> Unit,
    onShopSettings: () -> Unit,
    onRegistrationSettings: () -> Unit,
    onDailyWork: () -> Unit,
    onLedger: () -> Unit,
    onMonths: () -> Unit,
    onPieces: () -> Unit,
    onReports: () -> Unit,
    onStatistics: () -> Unit,
    onSecurity: () -> Unit,
    onAccountAuth: () -> Unit,
    onCloudSync: () -> Unit,
    onAppSettings: () -> Unit
) {
    val primary = com.ahd.notebk.ui.components.NotebkBrandPurple

    val workItems = listOf(
        ControlItem("تسجيل عمل اليوم", "إضافة كميات ومصروفات اليوم بسرعة", Icons.Default.Today, onDailyWork),
        ControlItem("الدفتر والسجلات", "مراجعة الأيام وتعديل البيانات", Icons.Default.Book, onLedger),
        ControlItem("الأشهر", "إضافة الأشهر ونسخها وإدارتها", Icons.Default.CalendarMonth, onMonths),
        ControlItem("إدارة القطع", "إضافة القطع وتعديلها وترتيبها", Icons.Default.Category, onPieces)
    )

    val reportItems = listOf(
        ControlItem("التقارير", "تجهيز التقارير والطباعة والمشاركة", Icons.Default.Description, onReports),
        ControlItem("الإحصائيات", "مؤشرات الإيرادات والمصروفات والصافي", Icons.Default.Assessment, onStatistics)
    )

    val accountItems = listOf(
        ControlItem("الملف الشخصي", "بيانات المستخدم والصورة وبيانات التقارير", Icons.Default.Person, onProfile),
        ControlItem("إعدادات المحل", "هوية المحل ورقم التسجيل وطريقة الحساب", Icons.Default.Storefront, onShopSettings),
        ControlItem("طريقة تسجيل العمل", "عددي أو فردي حسب طبيعة المحل", Icons.Default.Tune, onRegistrationSettings),
        ControlItem("الحساب والأمان", "معلومات الحساب وتغيير كلمة المرور وتسجيل الخروج", Icons.Default.AccountCircle, onAccountAuth)
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 10.dp, end = 10.dp, top = 8.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            AppTopBar(
                title = "مركز التحكم",
                subtitle = if (shopName.isBlank()) NotebkBrand.APP_NAME else shopName,
                onMenu = null,
                leadingIcon = Icons.Default.ChevronLeft,
                leadingContentDescription = "رجوع",
                onLeading = onBack,
                trailingContent = {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = null,
                        tint = com.ahd.notebk.ui.components.AppText,
                        modifier = Modifier.size(24.dp)
                    )
                }
            )
            Spacer(Modifier.height(4.dp))
        }

        item {
            NotebkBrandHeader()
        }
        item {
            AppSectionCard(
                title = "إدارة التطبيق",
                subtitle = "مكان واحد للوصول إلى كل مكونات notebk وإعداداتها"
            ) {
                Text(
                    "تم تصميم هذا القسم ليكون مركز التحكم الرئيسي بدل تجميع الإعدادات داخل نوافذ متفرقة.",
                    color = com.ahd.notebk.ui.components.AppMuted,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }
        }

        item { SectionTitle("العمل والسجلات") }
        items(workItems, key = { it.title }) { item -> SettingCard(item, primary) }

        item { SectionTitle("التقارير والتحليل") }
        items(reportItems, key = { it.title }) { item -> SettingCard(item, primary) }

        item { SectionTitle("الحساب والمحل") }
        items(accountItems, key = { it.title }) { item -> SettingCard(item, primary) }

        item { SectionTitle("النظام والحماية") }
        item {
            AppSettingItem(
                title = "إعدادات التطبيق",
                description = "التنبيهات والتفضيلات العامة ومكونات التطبيق",
                icon = Icons.Default.Settings,
                onClick = onAppSettings
            )
        }
        item {
            AppSettingItem(
                title = "المزامنة السحابية",
                description = "تفعيل المزامنة ومتابعة حالتها وإجراء مزامنة الآن",
                icon = Icons.Default.Backup,
                onClick = onCloudSync
            )
        }
        item {
            AppSettingItem(
                title = "الحماية والنسخ الاحتياطي",
                description = if (reminderEnabled) "النسخ الاحتياطي والحماية — التذكير اليومي مفعل" else "النسخ الاحتياطي والحماية وإدارة بيانات التطبيق",
                icon = Icons.Default.Security,
                onClick = onSecurity
            )
        }
        item {
            AppSettingItem(
                title = "معلومات notebk",
                description = "${NotebkBrand.APP_NAME} • ${NotebkBrand.DEVELOPER_NAME}",
                icon = Icons.Default.Backup,
                onClick = onBack
            )
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
        color = MaterialTheme.colorScheme.primary,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun SettingCard(item: ControlItem, primary: androidx.compose.ui.graphics.Color) {
    AppSettingItem(
        title = item.title,
        description = item.description,
        icon = item.icon,
        onClick = item.action
    )
}
