package com.ahd.notebk.ui.screen.control

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ahd.notebk.ui.components.AppBorder
import com.ahd.notebk.ui.components.AppMuted
import com.ahd.notebk.ui.components.AppPrimarySoft
import com.ahd.notebk.ui.components.AppSurface
import com.ahd.notebk.ui.components.NotebkBrand
import com.ahd.notebk.ui.components.NotebkBrandPurple
import com.ahd.notebk.ui.components.NotebkPage

private data class Feature(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val action: () -> Unit
)

@Composable
fun NotebkFeatureHub(
    onBack: () -> Unit,
    onDailyWork: () -> Unit,
    onMonths: () -> Unit,
    onPieces: () -> Unit,
    onReports: () -> Unit,
    onStats: () -> Unit,
    onProfile: () -> Unit,
    onShopSettings: () -> Unit
) {
    val features = listOf(
        Feature("تسجيل عمل اليوم", "إضافة ومراجعة حركة اليوم", Icons.Default.Today, onDailyWork),
        Feature("الأشهر", "فتح الشهر وإدارة سجلاته", Icons.Default.CalendarMonth, onMonths),
        Feature("إدارة القطع", "عرض وإدارة القطع والأسعار", Icons.Default.Category, onPieces),
        Feature("التقارير والطباعة", "التقارير الشهرية وملفات PDF", Icons.Default.Description, onReports),
        Feature("الإحصائيات", "ملخصات ومؤشرات العمل", Icons.Default.Assessment, onStats),
        Feature("الملف الشخصي", "بيانات المستخدم", Icons.Default.Person, onProfile),
        Feature("إعدادات المحل", "بيانات المحل وطريقة التسجيل", Icons.Default.Storefront, onShopSettings)
    )

    NotebkPage("مكونات ${NotebkBrand.APP_NAME}", "كل وظائف التطبيق في مكان واحد", onBack) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(features, key = { it.title }) { feature ->
                Card(
                    onClick = feature.action,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = AppSurface),
                    border = BorderStroke(1.dp, AppBorder)
                ) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = AppPrimarySoft,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.padding(12.dp).size(46.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(feature.icon, contentDescription = null, tint = NotebkBrandPurple, modifier = Modifier.size(25.dp))
                            }
                        }
                        Spacer(Modifier.width(2.dp))
                        Column(Modifier.weight(1f).padding(horizontal = 12.dp, vertical = 12.dp)) {
                            Text(feature.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(feature.description, fontSize = 11.sp, color = AppMuted)
                        }
                    }
                }
            }
        }
    }
}
