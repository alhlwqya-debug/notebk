package com.ahd.notebk.ui.screen.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ahd.notebk.data.local.LedgerDatabase
import com.ahd.notebk.domain.model.MonthBundle
import com.ahd.notebk.domain.model.RegistrationMode
import com.ahd.notebk.domain.model.ShopRecord
import com.ahd.notebk.ui.components.AppBorder
import com.ahd.notebk.ui.components.AppMuted
import com.ahd.notebk.ui.components.AppPrimarySoft
import com.ahd.notebk.ui.components.AppSurface
import com.ahd.notebk.ui.components.AppSurfaceAlt
import com.ahd.notebk.ui.components.AppText
import com.ahd.notebk.ui.components.AppTopBar
import com.ahd.notebk.ui.components.NotebkBrand
import com.ahd.notebk.ui.components.Blue
import com.ahd.notebk.ui.components.CardExpBg
import com.ahd.notebk.ui.components.CardNetBg
import com.ahd.notebk.ui.components.CardWorkBg
import com.ahd.notebk.ui.components.Green
import com.ahd.notebk.ui.components.Orange
import com.ahd.notebk.ui.components.Purple
import com.ahd.notebk.ui.components.Red

private enum class ActivityFilter { ALL, INCOME, EXPENSE }

private data class DashboardActivity(
    val date: String,
    val monthName: String,
    val earned: Int,
    val expense: Int,
    val note: String
)

private data class DashboardTotals(
    val earned: Int = 0,
    val expense: Int = 0,
    val net: Int = 0,
    val pieces: Int = 0,
    val months: Int = 0,
    val days: Int = 0,
    val activities: List<DashboardActivity> = emptyList()
)

@Composable
fun HomeDashboard(
    database: LedgerDatabase,
    shops: List<ShopRecord>,
    selectedShopId: Long?,
    userName: String,
    refreshKey: Int,
    onMenu: () -> Unit,
    onOpenLedger: () -> Unit,
    onOpenDailyWork: () -> Unit,
    onAddShop: () -> Unit,
    onShopSettings: () -> Unit,
    onUserProfile: () -> Unit,
    onAppSettings: () -> Unit,
    onHelp: () -> Unit,
    onStatistics: () -> Unit
) {
    var totals by remember { mutableStateOf(DashboardTotals()) }
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf(ActivityFilter.ALL) }

    fun reload() {
        val targetShops = selectedShopId?.let { id -> shops.filter { it.id == id } } ?: shops
        val bundles = mutableListOf<MonthBundle>()
        targetShops.forEach { shop ->
            database.getMonths(shop.id).forEach { month ->
                database.loadMonthBundle(month.id)?.let(bundles::add)
            }
        }

        var earned = 0
        var expense = 0
        var net = 0
        var pieces = 0
        var days = 0
        val activities = mutableListOf<DashboardActivity>()
        bundles.forEach { bundle ->
            val shopMode = targetShops.firstOrNull { it.id == bundle.month.shopId }?.registrationMode
                ?: RegistrationMode.NUMERIC
            val bundleExpense = database.calculateMonthExpenses(bundle)
            expense += bundleExpense
            bundle.days.forEach { day ->
                val dayEarned = if (shopMode == RegistrationMode.INDIVIDUAL) {
                    val entries = database.getIndividualEntries(bundle.month.id, day.date)
                    pieces += entries.sumOf { entry -> entry.quantities.values.sum() }
                    database.calculateIndividualDayEarned(entries)
                } else {
                    pieces += day.quantities.values.sum()
                    database.calculateDayEarned(day)
                }
                earned += dayEarned
                if (day.quantities.values.any { value -> value > 0 } || day.expense > 0 || dayEarned > 0) {
                    days++
                }
                if (day.quantities.values.any { value -> value > 0 } || day.expense > 0 || dayEarned > 0) {
                    activities += DashboardActivity(
                        date = day.date,
                        monthName = bundle.month.name,
                        earned = dayEarned,
                        expense = day.expense,
                        note = day.expenseNote
                    )
                }
            }
            net += if (bundle.month.deductExpense) {
                bundle.days.sumOf { day ->
                    val dayEarned = if (shopMode == RegistrationMode.INDIVIDUAL) {
                        database.calculateIndividualDayEarned(database.getIndividualEntries(bundle.month.id, day.date))
                    } else {
                        database.calculateDayEarned(day)
                    }
                    dayEarned - day.expense
                }
            } else {
                bundle.days.sumOf { day ->
                    if (shopMode == RegistrationMode.INDIVIDUAL) {
                        database.calculateIndividualDayEarned(database.getIndividualEntries(bundle.month.id, day.date))
                    } else {
                        database.calculateDayEarned(day)
                    }
                }
            }
        }

        totals = DashboardTotals(
            earned = earned,
            expense = expense,
            net = net,
            pieces = pieces,
            months = bundles.size,
            days = days,
            activities = activities.sortedByDescending { it.date }.take(30)
        )
    }

    LaunchedEffect(refreshKey, selectedShopId, shops) { reload() }

    val filteredActivities = totals.activities.filter { activity ->
        val matchesFilter = when (filter) {
            ActivityFilter.ALL -> true
            ActivityFilter.INCOME -> activity.earned > 0
            ActivityFilter.EXPENSE -> activity.expense > 0
        }
        val q = query.trim()
        val matchesQuery = q.isBlank() || listOf(activity.date, activity.monthName, activity.note)
            .any { it.contains(q, ignoreCase = true) }
        matchesFilter && matchesQuery
    }

    Box(Modifier.fillMaxSize().background(Color(0xFFF6F4FB))) {
        Column(Modifier.fillMaxSize().padding(horizontal = 10.dp, vertical = 8.dp)) {
            AppTopBar(
                title = "الرئيسية",
                subtitle = if (userName.isBlank()) NotebkBrand.APP_NAME else "أهلاً بك، $userName",
                onMenu = onMenu,
                trailingContent = {
                    Icon(Icons.Default.Home, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                }
            )

            Spacer(Modifier.height(10.dp))

            if (shops.isEmpty()) {
                EmptyDashboard(onAddShop)
            } else {
                LazyColumn(
                    Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 20.dp)
                ) {
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(7.dp), modifier = Modifier.fillMaxWidth()) {
                            DashboardStat("الإيرادات", totals.earned, CardWorkBg, Green, Modifier.weight(1f), Icons.Default.TrendingUp)
                            DashboardStat("المصروفات", totals.expense, CardExpBg, Red, Modifier.weight(1f), Icons.Default.TrendingDown)
                        }
                        Spacer(Modifier.height(7.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(7.dp), modifier = Modifier.fillMaxWidth()) {
                            DashboardStat("الصافي", totals.net, CardNetBg, Blue, Modifier.weight(1f), Icons.Default.Wallet)
                            DashboardStat("القطع", totals.pieces, Color(0xFFFFF3E0), Orange, Modifier.weight(1f), Icons.Default.CalendarMonth)
                        }
                    }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(7.dp), modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = onOpenDailyWork,
                                modifier = Modifier.weight(1f).height(46.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Green)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(5.dp))
                                Text("تسجيل عمل اليوم", fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = onOpenLedger,
                                modifier = Modifier.weight(1f).height(46.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Purple)
                            ) { Text("فتح الدفتر", fontWeight = FontWeight.SemiBold) }
                        }
                    }
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().background(AppPrimarySoft, RoundedCornerShape(16.dp)).border(1.dp, AppBorder, RoundedCornerShape(16.dp)).padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text("المحل الحالي", color = AppMuted, fontSize = 10.sp)
                                Text(shops.firstOrNull { it.id == selectedShopId }?.name ?: "غير محدد", color = Purple, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                Text("إدارة المحل وإعداداته من هنا", color = AppMuted, fontSize = 10.sp)
                            }
                            OutlinedButton(
                                onClick = onShopSettings,
                                contentPadding = PaddingValues(horizontal = 12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Purple)
                            ) { Text("⚙ إعدادات المحل") }
                        }
                    }
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth().background(AppSurface, RoundedCornerShape(18.dp)).border(1.dp, AppBorder, RoundedCornerShape(18.dp)).padding(12.dp)
                        ) {
                            Text("اختصارات التطبيق", color = AppText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(7.dp), modifier = Modifier.fillMaxWidth()) {
                                OutlinedButton(onClick = onUserProfile, modifier = Modifier.weight(1f).height(42.dp), shape = RoundedCornerShape(12.dp), contentPadding = PaddingValues(horizontal = 4.dp)) {
                                    Text("👤 الملف الشخصي", fontSize = 11.sp)
                                }
                                OutlinedButton(onClick = onAppSettings, modifier = Modifier.weight(1f).height(42.dp), shape = RoundedCornerShape(12.dp), contentPadding = PaddingValues(horizontal = 4.dp)) {
                                    Text("⚙ إعدادات التطبيق", fontSize = 11.sp)
                                }
                            }
                            Spacer(Modifier.height(7.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(7.dp), modifier = Modifier.fillMaxWidth()) {
                                OutlinedButton(onClick = onStatistics, modifier = Modifier.weight(1f).height(42.dp), shape = RoundedCornerShape(12.dp), contentPadding = PaddingValues(horizontal = 4.dp)) {
                                    Text("📊 الإحصائيات", fontSize = 11.sp)
                                }
                                OutlinedButton(onClick = onHelp, modifier = Modifier.weight(1f).height(42.dp), shape = RoundedCornerShape(12.dp), contentPadding = PaddingValues(horizontal = 4.dp)) {
                                    Text("❓ طريقة الاستخدام", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth().background(Color(0xFFF3ECFA), RoundedCornerShape(18.dp)).border(1.dp, AppBorder, RoundedCornerShape(18.dp)).padding(12.dp)
                        ) {
                            Text("طريقة الاستخدام السريعة", color = Purple, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(7.dp))
                            Text("1  •  أضف المحل والقطع والأسعار مرة واحدة.", color = AppText, fontSize = 11.sp)
                            Text("2  •  استخدم «تسجيل عمل اليوم» لإدخال عملك بسرعة.", color = AppText, fontSize = 11.sp)
                            Text("3  •  استخدم «فتح الدفتر» للمراجعة والتعديل والطباعة.", color = AppText, fontSize = 11.sp)
                            Spacer(Modifier.height(6.dp))
                            TextButton(onClick = onHelp, modifier = Modifier.align(Alignment.End)) { Text("فتح الدليل الكامل", color = Purple) }
                        }
                    }
                    item {
                        Column(
                            Modifier.fillMaxWidth().background(AppSurface, RoundedCornerShape(18.dp)).border(1.dp, AppBorder, RoundedCornerShape(18.dp)).padding(12.dp)
                        ) {
                            Text("نظرة عامة", color = AppText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(8.dp))
                            Text("${totals.months} شهر • ${totals.days} يوم مسجل", color = AppMuted, fontSize = 11.sp)
                            Spacer(Modifier.height(10.dp))
                            OutlinedTextField(
                                value = query,
                                onValueChange = { query = it },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                                label = { Text("بحث في السجل") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                            )
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                                FilterButton("الكل", filter == ActivityFilter.ALL, { filter = ActivityFilter.ALL }, Modifier.weight(1f))
                                FilterButton("الإيرادات", filter == ActivityFilter.INCOME, { filter = ActivityFilter.INCOME }, Modifier.weight(1f))
                                FilterButton("المصروفات", filter == ActivityFilter.EXPENSE, { filter = ActivityFilter.EXPENSE }, Modifier.weight(1f))
                            }
                        }
                    }
                    item {
                        Text("آخر النشاطات", Modifier.fillMaxWidth().padding(horizontal = 4.dp), color = AppText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    if (filteredActivities.isEmpty()) {
                        item {
                            Box(Modifier.fillMaxWidth().background(AppSurfaceAlt, RoundedCornerShape(16.dp)).padding(24.dp), contentAlignment = Alignment.Center) {
                                Text("لا توجد سجلات مطابقة", color = AppMuted, fontSize = 12.sp)
                            }
                        }
                    } else {
                        items(filteredActivities, key = { "${it.date}-${it.monthName}-${it.earned}-${it.expense}" }) { activity -> ActivityRow(activity) }
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardStat(title: String, value: Int, background: Color, accent: Color, modifier: Modifier, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Column(modifier.background(background, RoundedCornerShape(17.dp)).padding(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(19.dp))
            Spacer(Modifier.width(6.dp))
            Text(title, color = AppMuted, fontSize = 11.sp)
        }
        Spacer(Modifier.height(5.dp))
        Text(value.toString(), color = accent, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
private fun FilterButton(text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(38.dp),
        shape = RoundedCornerShape(11.dp),
        contentPadding = PaddingValues(horizontal = 4.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (selected) AppPrimarySoft else Color.Transparent,
            contentColor = if (selected) Purple else AppMuted
        )
    ) { Text(text, fontSize = 10.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal) }
}

@Composable
private fun ActivityRow(activity: DashboardActivity) {
    Row(
        Modifier.fillMaxWidth().background(AppSurface, RoundedCornerShape(15.dp)).border(1.dp, AppBorder, RoundedCornerShape(15.dp)).padding(11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(activity.monthName, color = AppText, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(3.dp))
            Text(activity.date, color = AppMuted, fontSize = 10.sp)
            if (activity.note.isNotBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(activity.note, color = AppMuted, fontSize = 10.sp, maxLines = 1)
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            if (activity.earned > 0) Text("+${activity.earned}", color = Green, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            if (activity.expense > 0) Text("-${activity.expense}", color = Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun EmptyDashboard(onAddShop: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(Icons.Default.Home, contentDescription = null, tint = Purple, modifier = Modifier.size(56.dp))
        Spacer(Modifier.height(12.dp))
        Text("ابدأ بإضافة أول محل", color = AppText, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(7.dp))
        Text("سيتم الاحتفاظ بسجلاتك محلياً، ويمكنك العودة إلى الدفتر في أي وقت.", color = AppMuted, fontSize = 12.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(15.dp))
        Button(onClick = onAddShop, shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = Purple)) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(Modifier.width(6.dp))
            Text("إضافة محل")
        }
    }
}
