package com.ahd.notebk.ui.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.ahd.notebk.R
import com.ahd.notebk.data.local.AppSettings

@Composable
fun NoteBkDrawer(
    currentRoute: String?,
    settings: AppSettings,
    onNavigate: (String) -> Unit
) {
    ModalDrawerSheet {
        Column(Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Image(painterResource(R.drawable.brand_logo), "شعار التطبيق", Modifier.size(84.dp))
            Text("noteBK", style = MaterialTheme.typography.headlineSmall)
            Text(settings.ownerName.ifBlank { settings.workerName }.ifBlank { "المستخدم" })
            Text(settings.shopName, style = MaterialTheme.typography.bodySmall)
        }
        HorizontalDivider()
        val items = listOf(
            Triple(AppRoutes.HOME, "الرئيسية", Icons.Default.Home),
            Triple(AppRoutes.NUMERIC, "الدفتر / التسجيل العددي", Icons.Default.MenuBook),
            Triple(AppRoutes.INDIVIDUAL, "السجل الفردي", Icons.Default.Person),
            Triple(AppRoutes.STATISTICS, "الإحصائيات", Icons.Default.Analytics),
            Triple(AppRoutes.REPORT, "التقارير", Icons.Default.Description),
            Triple(AppRoutes.SHOPS, "المحلات", Icons.Default.Store),
            Triple(AppRoutes.SETTINGS, "الإعدادات", Icons.Default.Settings)
        )
        items.forEach { (route, label, icon) ->
            NavigationDrawerItem(
                icon = { Icon(icon, null) },
                label = { Text(label) },
                selected = currentRoute == route,
                onClick = { onNavigate(route) },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
        }
        HorizontalDivider(Modifier.padding(vertical = 8.dp))
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Logout, null) },
            label = { Text("تسجيل الخروج") },
            selected = false,
            onClick = { onNavigate(AppRoutes.HOME) },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )
    }
}
