package com.ahd.notebk.ui.screen.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import com.ahd.notebk.ui.components.AppMuted
import com.ahd.notebk.ui.components.AppPrimarySoft
import com.ahd.notebk.ui.components.AppSettingItem
import com.ahd.notebk.ui.components.AppSurface
import com.ahd.notebk.ui.components.AppText
import com.ahd.notebk.ui.components.NotebkBrandPurple
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun AppSettingsDialog(
    reminderEnabled: Boolean,
    onReminderChange: (Boolean) -> Unit,
    onSecurity: () -> Unit,
    onHelp: () -> Unit,
    onProfile: () -> Unit,
    onShopSettings: () -> Unit,
    onRegistrationSettings: () -> Unit,
    onLedger: () -> Unit,
    onDismiss: () -> Unit
) {
    val primary = NotebkBrandPurple
    val text = AppText
    val muted = AppMuted

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = null,
                        tint = primary,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "إعدادات التطبيق",
                        color = text,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(5.dp))
                Text(
                    "تحكم مركزي في التطبيق ومكوناته. إعدادات كل محل تبقى مستقلة.",
                    color = muted,
                    fontSize = 12.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                SettingsSectionTitle("التنبيهات")
                SettingsSwitchRow(
                    icon = Icons.Default.Notifications,
                    title = "تذكير تسجيل العمل",
                    description = "تنبيه يومي عند عدم تسجيل عمل اليوم.",
                    checked = reminderEnabled,
                    onCheckedChange = onReminderChange
                )

                Spacer(Modifier.height(8.dp))
                HorizontalDivider(color = com.ahd.notebk.ui.components.AppBorder)
                Spacer(Modifier.height(8.dp))

                SettingsSectionTitle("مكونات التطبيق")
                SettingsRow(
                    Icons.Default.Person,
                    "الملف الشخصي",
                    "الاسم ووسائل التواصل والصورة",
                    onProfile
                )
                SettingsRow(
                    Icons.Default.Storefront,
                    "إعدادات المحل",
                    "اسم المحل ورقم التسجيل",
                    onShopSettings
                )
                SettingsRow(
                    Icons.Default.Tune,
                    "طريقة تسجيل العمل",
                    "عددي أو فردي للمحل الحالي",
                    onRegistrationSettings
                )
                SettingsRow(
                    Icons.Default.Book,
                    "الدفتر والسجلات",
                    "الأشهر والأيام والقطع والمصروفات",
                    onLedger
                )
                SettingsRow(
                    Icons.Default.Assessment,
                    "التقارير والإحصائيات",
                    "مراجعة النتائج والطباعة وPDF",
                    onLedger
                )

                Spacer(Modifier.height(8.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(Modifier.height(8.dp))

                SettingsSectionTitle("البيانات والحماية")
                SettingsRow(
                    Icons.Default.Security,
                    "الحماية والنسخ الاحتياطي",
                    "قفل التطبيق وإنشاء واستعادة النسخ",
                    onSecurity
                )
                SettingsRow(
                    Icons.Default.Backup,
                    "إدارة البيانات",
                    "احتفظ بنسخة خارج الهاتف قبل الحذف أو النقل",
                    onSecurity
                )

                Spacer(Modifier.height(8.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(Modifier.height(8.dp))

                SettingsSectionTitle("المساعدة")
                SettingsRow(
                    Icons.Default.HelpOutline,
                    "دليل الاستخدام",
                    "شرح المكونات وطريقة العمل خطوة بخطوة",
                    onHelp
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("إغلاق", color = primary, fontWeight = FontWeight.SemiBold)
            }
        }
    )
}

@Composable
private fun SettingsSectionTitle(title: String) {
    Text(
        title,
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    AppSettingItem(
        title = title,
        description = description,
        icon = icon,
        onClick = onClick
    )
}

@Composable
private fun SettingsSwitchRow(
    icon: ImageVector,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    androidx.compose.material3.Card(
        modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = AppSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, com.ahd.notebk.ui.components.AppBorder),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = androidx.compose.ui.Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            androidx.compose.material3.Surface(
                color = AppPrimarySoft,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                modifier = androidx.compose.ui.Modifier.size(44.dp)
            ) {
                androidx.compose.foundation.layout.Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = NotebkBrandPurple, modifier = androidx.compose.ui.Modifier.size(24.dp))
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AppText)
                Spacer(Modifier.height(2.dp))
                Text(description, fontSize = 10.sp, color = AppMuted)
            }
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}
