package com.ahd.notebk.ui.screen.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DisplaySettings
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ahd.notebk.domain.model.RegistrationMode
import com.ahd.notebk.domain.model.ShopRecord
import com.ahd.notebk.ui.components.NotebkPage
import com.ahd.notebk.ui.components.NotebkPrimaryAction
import com.ahd.notebk.ui.components.NotebkSectionCard
import com.ahd.notebk.ui.components.NotebkSecondaryAction
import com.ahd.notebk.ui.components.NotebkWordmark
import com.ahd.notebk.ui.components.AppText
import com.ahd.notebk.ui.components.AppMuted
import com.ahd.notebk.notification.WorkReminderScheduler

@Composable
fun ProfilePage(
    userName: String, userPhone: String, userEmail: String, userShop: String, userImagePath: String,
    onUserNameChange: (String) -> Unit, onUserPhoneChange: (String) -> Unit,
    onUserEmailChange: (String) -> Unit, onUserShopChange: (String) -> Unit,
    onSelectImage: () -> Unit, onSave: () -> Unit, onAccountAuth: () -> Unit, onBack: () -> Unit
) {
    val hasContact = userPhone.isNotBlank() || userEmail.isNotBlank()
    val context = androidx.compose.ui.platform.LocalContext.current
    val profileStore = androidx.compose.runtime.remember { com.ahd.notebk.data.local.NotebkSettingsStore(context) }
    var jobTitle by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(profileStore.getString("user_job_title")) }
    var bio by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(profileStore.getString("user_bio")) }
    var phoneAlt by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(profileStore.getString("user_phone_alt")) }
    var address by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(profileStore.getString("user_address")) }
    var whatsapp by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(profileStore.getString("user_whatsapp")) }
    var whatsappEnabled by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(profileStore.getBoolean("whatsapp_enabled", false)) }
    var registrationNumber by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(profileStore.getString("user_registration_number")) }
    var taxNumber by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(profileStore.getString("user_tax_number")) }
    var commercialRegister by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(profileStore.getString("user_commercial_register")) }
    var businessAddress by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(profileStore.getString("business_address")) }
    var facebook by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(profileStore.getString("social_facebook")) }
    var instagram by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(profileStore.getString("social_instagram")) }
    var twitter by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(profileStore.getString("social_twitter")) }
    var tiktok by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(profileStore.getString("social_tiktok")) }
    var telegram by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(profileStore.getString("social_telegram")) }
    var website by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(profileStore.getString("social_website")) }
    fun saveExtendedProfile() {
        profileStore.putString("user_job_title", jobTitle); profileStore.putString("user_bio", bio)
        profileStore.putString("user_phone_alt", phoneAlt); profileStore.putString("user_address", address)
        profileStore.putBoolean("whatsapp_enabled", whatsappEnabled); profileStore.putString("user_whatsapp", whatsapp)
        profileStore.putString("user_registration_number", registrationNumber); profileStore.putString("user_tax_number", taxNumber)
        profileStore.putString("user_commercial_register", commercialRegister); profileStore.putString("business_address", businessAddress)
        profileStore.putString("social_facebook", facebook); profileStore.putString("social_instagram", instagram)
        profileStore.putString("social_twitter", twitter); profileStore.putString("social_tiktok", tiktok)
        profileStore.putString("social_telegram", telegram); profileStore.putString("social_website", website)
    }
    NotebkPage("الملف الشخصي", "بيانات المستخدم والهوية المستخدمة في notebk", onBack) {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                NotebkSectionCard("الحساب", "معلوماتك الأساسية وحالة بيانات الاتصال") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.Person, null, modifier = Modifier.size(48.dp), tint = com.ahd.notebk.ui.components.NotebkBrandPurple)
                        Column(Modifier.weight(1f)) {
                            Text(
                                if (userName.isBlank()) "لم يتم تحديد الاسم" else userName,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                if (hasContact) "بيانات الاتصال متوفرة" else "أضف رقم الهاتف أو البريد الإلكتروني",
                                fontSize = 12.sp,
                                color = AppMuted
                            )
                        }
                    }
                    Spacer(Modifier.size(8.dp))
                    NotebkSecondaryAction("تسجيل الدخول / إدارة الحساب", onAccountAuth)
                }
            }
            item {
                NotebkSectionCard("البيانات الشخصية", "هذه البيانات يمكن أن تظهر في التقارير وملفات المشاركة") {
                    OutlinedTextField(
                        value = userName,
                        onValueChange = onUserNameChange,
                        label = { Text("الاسم الكامل *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = userPhone,
                        onValueChange = onUserPhoneChange,
                        label = { Text("رقم الهاتف") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = userEmail,
                        onValueChange = onUserEmailChange,
                        label = { Text("البريد الإلكتروني") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    NotebkSecondaryAction("تغيير الصورة الشخصية", onSelectImage)
                    Text(
                        if (userImagePath.isBlank()) "لم يتم اختيار صورة شخصية" else "تم حفظ الصورة الشخصية على الجهاز",
                        fontSize = 11.sp, color = AppMuted
                    )
                }
            }
            item {
                NotebkSectionCard("هوية التقارير", "اسم المحل هنا يحافظ على توافق التقارير القديمة. إعدادات المحل الأساسية موجودة في قسم المحل") {
                    OutlinedTextField(
                        value = userShop,
                        onValueChange = onUserShopChange,
                        label = { Text("اسم المحل في التقارير") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            item {
                NotebkSectionCard("📷 الأساسيات وبيانات الاتصال", "بيانات إضافية محفوظة محليًا وتستخدم عند تجهيز الملف والتقارير") {
                    OutlinedTextField(jobTitle,{jobTitle=it},label={Text("المسمى الوظيفي")},singleLine=true,modifier=Modifier.fillMaxWidth())
                    OutlinedTextField(bio,{bio=it},label={Text("نبذة قصيرة")},modifier=Modifier.fillMaxWidth())
                    OutlinedTextField(phoneAlt,{phoneAlt=it},label={Text("رقم هاتف إضافي")},singleLine=true,modifier=Modifier.fillMaxWidth())
                    OutlinedTextField(address,{address=it},label={Text("الموقع / العنوان")},singleLine=true,modifier=Modifier.fillMaxWidth())
                    Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){Text("WhatsApp للتقرير",Modifier.weight(1f));Switch(whatsappEnabled,{whatsappEnabled=it})}
                    if(whatsappEnabled) OutlinedTextField(whatsapp,{whatsapp=it},label={Text("رقم WhatsApp")},singleLine=true,modifier=Modifier.fillMaxWidth())
                }
            }
            item {
                NotebkSectionCard("🏪 معلومات العمل", "بيانات إضافية مستقلة عن اسم المحل الحالي") {
                    OutlinedTextField(registrationNumber,{registrationNumber=it},label={Text("رقم التسجيل / الترخيص")},singleLine=true,modifier=Modifier.fillMaxWidth())
                    OutlinedTextField(taxNumber,{taxNumber=it},label={Text("الرقم الضريبي")},singleLine=true,modifier=Modifier.fillMaxWidth())
                    OutlinedTextField(commercialRegister,{commercialRegister=it},label={Text("رقم السجل التجاري")},singleLine=true,modifier=Modifier.fillMaxWidth())
                    OutlinedTextField(businessAddress,{businessAddress=it},label={Text("العنوان التجاري")},singleLine=true,modifier=Modifier.fillMaxWidth())
                }
            }
            item {
                NotebkSectionCard("🌐 حسابات التواصل", "يمكن استخدامها لاحقًا في رأس أو تذييل PDF") {
                    OutlinedTextField(facebook,{facebook=it},label={Text("Facebook")},singleLine=true,modifier=Modifier.fillMaxWidth())
                    OutlinedTextField(instagram,{instagram=it},label={Text("Instagram")},singleLine=true,modifier=Modifier.fillMaxWidth())
                    OutlinedTextField(twitter,{twitter=it},label={Text("Twitter / X")},singleLine=true,modifier=Modifier.fillMaxWidth())
                    OutlinedTextField(tiktok,{tiktok=it},label={Text("TikTok")},singleLine=true,modifier=Modifier.fillMaxWidth())
                    OutlinedTextField(telegram,{telegram=it},label={Text("Telegram")},singleLine=true,modifier=Modifier.fillMaxWidth())
                    OutlinedTextField(website,{website=it},label={Text("الموقع الإلكتروني")},singleLine=true,modifier=Modifier.fillMaxWidth())
                }
            }
            item {
                NotebkSectionCard("حفظ البيانات الموسعة", "تُحفظ تلقائيًا عند الضغط على حفظ الملف الشخصي") {
                    NotebkSecondaryAction("حفظ البيانات الإضافية", ::saveExtendedProfile)
                }
            }
            item {
                NotebkSectionCard("هوية notebk", "الهوية الرسمية للتطبيق والجهة المطورة") {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        NotebkWordmark(fontSize = 22.sp, normalColor = AppText)
                        Spacer(Modifier.weight(1f))
                        Text("AHD DEV TEAM", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = AppMuted)
                    }
                    Spacer(Modifier.size(6.dp))
                    Text("كل سجل .. يروي قصة", fontSize = 11.sp, color = AppMuted)
                }
            }
            item { NotebkPrimaryAction("حفظ جميع بيانات الملف", { saveExtendedProfile(); onSave() }) }
        }
    }
}

@Composable
fun ShopSettingsPage(shop: ShopRecord, onSave: (String,String,RegistrationMode)->Unit, onBack: ()->Unit) {
    varName(shop.name, onSave, shop, onBack)
}

@Composable
private fun varName(initial:String, onSave:(String,String,RegistrationMode)->Unit, shop:ShopRecord, onBack:()->Unit) {
    var name by androidx.compose.runtime.remember(shop.id) { androidx.compose.runtime.mutableStateOf(initial) }
    var registration by androidx.compose.runtime.remember(shop.id) { androidx.compose.runtime.mutableStateOf(shop.registrationNumber) }
    var mode by androidx.compose.runtime.remember(shop.id) { androidx.compose.runtime.mutableStateOf(shop.registrationMode) }
    NotebkPage("إعدادات المحل", "إعدادات المحل الحالي فقط", onBack) {
        LazyColumn(verticalArrangement=Arrangement.spacedBy(12.dp)) {
            item { NotebkSectionCard("هوية المحل") {
                OutlinedTextField(name,{name=it},label={Text("اسم المحل")},singleLine=true,modifier=Modifier.fillMaxWidth())
                OutlinedTextField(registration,{registration=it},label={Text("رقم المحل / التسجيل")},singleLine=true,modifier=Modifier.fillMaxWidth())
            } }
            item { NotebkSectionCard("طريقة تسجيل العمل", "يمكن تغييرها دون حذف السجلات الحالية") {
                ModeRow("تسجيل عددي", "جدول الأيام والكميات والمصروفات", mode==RegistrationMode.NUMERIC){mode=RegistrationMode.NUMERIC}
                ModeRow("تسجيل فردي", "حسب الزبون ورقم الصفحة", mode==RegistrationMode.INDIVIDUAL){mode=RegistrationMode.INDIVIDUAL}
            } }
            item { NotebkPrimaryAction("حفظ إعدادات المحل", { onSave(name.trim(),registration.trim(),mode) }) }
        }
    }
}

@Composable private fun ModeRow(title:String, desc:String, selected:Boolean, onClick:()->Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical=5.dp), verticalAlignment=Alignment.CenterVertically) {
        RadioButton(selected,onClick); Column(Modifier.weight(1f)){Text(title,fontWeight=FontWeight.Bold);Text(desc,fontSize=11.sp,color=AppMuted)}
    }
}

@Composable
fun RegistrationSettingsPage(currentMode:RegistrationMode,onSave:(RegistrationMode)->Unit,onSecurity:()->Unit,onBack:()->Unit) {
    var mode by androidx.compose.runtime.remember(currentMode){androidx.compose.runtime.mutableStateOf(currentMode)}
    NotebkPage("طريقة تسجيل العمل", "تحديد طريقة تسجيل المحل الحالي", onBack) {
        NotebkSectionCard("الطريقة الحالية") {
            ModeRow("تسجيل عددي","جدول بالأيام والكميات والمصروف والمجموع",mode==RegistrationMode.NUMERIC){mode=RegistrationMode.NUMERIC}
            ModeRow("تسجيل فردي","سجل بأسماء الزبائن وأرقام الصفحات والكميات",mode==RegistrationMode.INDIVIDUAL){mode=RegistrationMode.INDIVIDUAL}
            NotebkPrimaryAction("حفظ الطريقة",{onSave(mode)})
        }
        NotebkSectionCard("الحماية") {
            Icon(Icons.Default.Security,null,modifier=Modifier.size(30.dp))
            NotebkSecondaryAction("الحماية والنسخ الاحتياطي",onSecurity)
        }
    }
}

@Composable
fun AppSettingsPage(
    reminderEnabled: Boolean,
    darkTheme: Boolean,
    onReminderChange: (Boolean) -> Unit,
    onDarkThemeChange: (Boolean) -> Unit,
    onSecurity: () -> Unit,
    onSystemSettings: () -> Unit,
    onAccountAuth: () -> Unit,
    onHelp: () -> Unit,
    onBack: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val store = androidx.compose.runtime.remember { com.ahd.notebk.data.local.NotebkSettingsStore(context) }
    var search by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }
    var expanded by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(setOf("general", "notifications")) }
    var currency by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(store.getString("currency_code", "YER")) }
    var symbol by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(store.getString("currency_symbol", "ريال")) }
    var decimalPlaces by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(store.getInt("decimal_places", 0)) }
    var dateFormat by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(store.getString("date_format", "yyyy/MM/dd")) }
    var timeFormat by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(store.getString("time_format", "24h")) }
    var reportLanguage by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(store.getString("report_language", "ar")) }
    var fontScale by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(store.getFloat("font_scale", 1f)) }
    var density by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(store.getString("density", "normal")) }
    var showSearch by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(store.getBoolean("show_search", true)) }
    var showFab by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(store.getBoolean("show_fab", true)) }
    var showBottomNav by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(store.getBoolean("show_bottom_nav", true)) }
    var animations by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(store.getBoolean("animations_enabled", true)) }
    var autoBackup by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(store.getBoolean("auto_backup", false)) }
    var backupWifiOnly by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(store.getBoolean("backup_wifi_only", true)) }
    var cloudSync by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(store.getBoolean("cloud_sync", false)) }
    var lockBackground by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(store.getBoolean("lock_on_background", false)) }
    var hideAmounts by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(store.getBoolean("hide_amounts", false)) }
    var screenshotBlock by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(store.getBoolean("prevent_screenshot", false)) }
    var defaultPage by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(store.getString("default_page", "home")) }

    fun toggle(id: String) { expanded = if (expanded.contains(id)) expanded - id else expanded + id }
    fun match(vararg words: String) = search.isBlank() || words.any { it.contains(search.trim(), ignoreCase = true) }

    NotebkPage("إعدادات التطبيق", "نظام إعدادات مركزي — ابحث عن أي إعداد من أكثر من 400 خيار مخطط له", onBack) {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                OutlinedTextField(
                    value = search,
                    onValueChange = { search = it },
                    label = { Text("بحث داخل الإعدادات") },
                    placeholder = { Text("مثال: العملة، PDF، PIN، الإشعارات…") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (match("عام", "اللغة", "عملة", "التاريخ", "المظهر")) item {
                SettingsGroup("general", "🌍 الإعدادات العامة", "اللغة والمنطقة والعملة والتاريخ والمظهر", expanded, ::toggle) {
                    SettingsChoice("اللغة", "العربية", store.getString("language", "ar")) { store.putString("language", "ar") }
                    SettingsChoice("الاتجاه", "RTL", store.getString("direction", "rtl")) { store.putString("direction", "rtl") }
                    SettingsChoice("لغة التقارير", "العربية", reportLanguage) { reportLanguage = "ar"; store.putString("report_language", "ar") }
                    SettingsChoice("العملة", "ريال يمني (YER)", currency) { currency = "YER"; symbol = "ريال"; store.putString("currency_code", "YER"); store.putString("currency_symbol", "ريال") }
                    OutlinedTextField(symbol, { symbol = it; store.putString("currency_symbol", it) }, label={Text("رمز العملة")}, singleLine=true, modifier=Modifier.fillMaxWidth())
                    SettingsChoice("موضع رمز العملة", "بعد المبلغ", store.getString("currency_position", "after")) { store.putString("currency_position", "after") }
                    SettingsChoice("الخانات العشرية", decimalPlaces.toString(), decimalPlaces.toString()) { decimalPlaces = if (decimalPlaces >= 2) 0 else decimalPlaces + 1; store.putInt("decimal_places", decimalPlaces) }
                    SettingsChoice("صيغة التاريخ", dateFormat, dateFormat) { dateFormat = if (dateFormat == "yyyy/MM/dd") "dd/MM/yyyy" else "yyyy/MM/dd"; store.putString("date_format", dateFormat) }
                    SettingsChoice("صيغة الوقت", timeFormat, timeFormat) { timeFormat = if (timeFormat == "24h") "12h" else "24h"; store.putString("time_format", timeFormat) }
                    SettingsChoice("كثافة العرض", density, density) { density = if (density == "normal") "comfortable" else "normal"; store.putString("density", density) }
                    Row(Modifier.fillMaxWidth(), verticalAlignment=Alignment.CenterVertically) { Text("الوضع الداكن", Modifier.weight(1f)); Switch(darkTheme, onDarkThemeChange) }
                    Row(Modifier.fillMaxWidth(), verticalAlignment=Alignment.CenterVertically) { Text("الحركة والتأثيرات", Modifier.weight(1f)); Switch(animations, { animations=it; store.putBoolean("animations_enabled", it) }) }
                    Text("حجم الخط: ${(fontScale*100).toInt()}%", fontSize=12.sp)
                    androidx.compose.material3.Slider(value=fontScale, onValueChange={fontScale=it;store.putFloat("font_scale",it)}, valueRange=0.9f..1.3f, steps=3)
                    NotebkSecondaryAction("إعادة تعيين الإعدادات العامة", { store.resetGeneral(); currency="YER"; symbol="ريال"; decimalPlaces=0; dateFormat="yyyy/MM/dd"; timeFormat="24h"; reportLanguage="ar"; fontScale=1f; density="normal"; animations=true; showSearch=true; showFab=true; showBottomNav=true; cloudSync=false; lockBackground=false; hideAmounts=false; screenshotBlock=false; defaultPage="home"; onDarkThemeChange(false) })
                }
            }

            if (match("إشعارات", "تذكير", "نسخ احتياطي")) item {
                SettingsGroup("notifications", "🔔 الإشعارات والتذكيرات", "كل ما يتعلق بالتنبيهات الدورية", expanded, ::toggle) {
                    SettingsSwitch("تذكير تسجيل العمل", reminderEnabled, onReminderChange, "يُستخدم نظام التذكير الحالي في التطبيق")
                    SettingsChoice("وقت التذكير", store.getString("reminder_time", "20:00"), store.getString("reminder_time", "20:00")) { val next = if (store.getString("reminder_time","20:00")=="20:00") "18:00" else "20:00"; store.putString("reminder_time", next); if (reminderEnabled) WorkReminderScheduler.schedule(context) }
                    SettingsSwitch("صوت الإشعار", store.getBoolean("notification_sound", true), { store.putBoolean("notification_sound", it) })
                    SettingsSwitch("اهتزاز", store.getBoolean("notification_vibrate", true), { store.putBoolean("notification_vibrate", it) })
                    SettingsSwitch("إشعار عند النسخ الاحتياطي", store.getBoolean("backup_notification", true), { store.putBoolean("backup_notification", it) })
                    SettingsSwitch("نسخ احتياطي تلقائي", autoBackup, { autoBackup=it;store.putBoolean("auto_backup",it) })
                    SettingsSwitch("Wi‑Fi فقط", backupWifiOnly, { backupWifiOnly=it;store.putBoolean("backup_wifi_only",it) })
                }
            }

            if (match("تقارير", "طباعة", "PDF")) item {
                SettingsGroup("reports", "🖨️ الطباعة والتقارير", "الإعدادات التي يعتمد عليها نظام التقارير عند توفر الخيار في مولد PDF", expanded, ::toggle) {
                    SettingsChoice("حجم الورق", store.getString("paper_size","A4"), store.getString("paper_size","A4")) { store.putString("paper_size", if(store.getString("paper_size","A4")=="A4") "A5" else "A4") }
                    SettingsChoice("اتجاه الورق", store.getString("paper_orientation","portrait"), store.getString("paper_orientation","portrait")) { store.putString("paper_orientation", if(store.getString("paper_orientation","portrait")=="portrait") "landscape" else "portrait") }
                    SettingsSwitch("إظهار الهيدر", store.getBoolean("report_header",true), {store.putBoolean("report_header",it)})
                    SettingsSwitch("إظهار التذييل", store.getBoolean("report_footer",true), {store.putBoolean("report_footer",it)})
                    SettingsSwitch("إظهار الشعار", store.getBoolean("report_logo",true), {store.putBoolean("report_logo",it)})
                    SettingsSwitch("إظهار بيانات المحل", store.getBoolean("report_shop_data",true), {store.putBoolean("report_shop_data",it)})
                    SettingsSwitch("ترقيم الصفحات", store.getBoolean("report_page_numbers",true), {store.putBoolean("report_page_numbers",it)})
                    SettingsSwitch("إظهار الرسم البياني", store.getBoolean("report_chart",true), {store.putBoolean("report_chart",it)})
                    SettingsSwitch("إظهار الشهور الفارغة", store.getBoolean("report_empty_months",false), {store.putBoolean("report_empty_months",it)})
                    SettingsSwitch("ضغط التقرير", store.getBoolean("report_compress",true), {store.putBoolean("report_compress",it)})
                    OutlinedTextField(store.getString("report_title","التقرير الشهري"), {store.putString("report_title",it)}, label={Text("عنوان التقرير")}, singleLine=true, modifier=Modifier.fillMaxWidth())
                    OutlinedTextField(store.getString("report_footer_text",""), {store.putString("report_footer_text",it)}, label={Text("نص التذييل")}, modifier=Modifier.fillMaxWidth())
                }
            }

            if (match("بيانات", "نسخ", "استيراد", "تصدير", "مزامنة")) item {
                SettingsGroup("data", "💾 البيانات والنسخ الاحتياطي", "إدارة النسخ والاستيراد والتصدير والمزامنة", expanded, ::toggle) {
                    SettingsSwitch("المزامنة السحابية", cloudSync, {cloudSync=it;store.putBoolean("cloud_sync",it)})
                    SettingsChoice("دورية المزامنة", store.getString("sync_frequency","manual"), store.getString("sync_frequency","manual")) { store.putString("sync_frequency", if(store.getString("sync_frequency","manual")=="manual") "daily" else "manual") }
                    SettingsChoice("عدد النسخ المحتفظ بها", store.getInt("backup_retention",10).toString(), store.getInt("backup_retention",10).toString()) { store.putInt("backup_retention", if(store.getInt("backup_retention",10)>=30) 5 else store.getInt("backup_retention",10)+5) }
                    NotebkSecondaryAction("إدارة النسخ الاحتياطي والحماية", onSecurity)
                    Text("التصدير والاستيراد الفعليان يستخدمان نظام النسخ الموجود في التطبيق، ولا يتم حذف قاعدة البيانات تلقائيًا.", fontSize=11.sp, color=AppMuted)
                }
            }

            if (match("أمان", "خصوصية", "PIN", "قفل")) item {
                SettingsGroup("security", "🔒 الأمان والخصوصية", "خيارات حماية الشاشة والبيانات", expanded, ::toggle) {
                    SettingsSwitch("القفل عند العودة للخلفية", lockBackground, {lockBackground=it;store.putBoolean("lock_on_background",it)})
                    SettingsSwitch("إخفاء المبالغ", hideAmounts, {hideAmounts=it;store.putBoolean("hide_amounts",it)})
                    SettingsSwitch("منع تصوير الشاشة", screenshotBlock, {screenshotBlock=it;store.putBoolean("prevent_screenshot",it)})
                    NotebkSecondaryAction("فتح إعدادات PIN والنسخ الاحتياطي", onSecurity)
                    Text("لا نعرض خيارات بصمة أو تشفير قاعدة البيانات هنا ما لم يكن التنفيذ الفعلي متاحًا، حتى لا تكون الإعدادات شكلية.", fontSize=11.sp, color=AppMuted)
                }
            }

            if (match("صفحات", "رئيسية", "FAB", "Drawer", "تنقل")) item {
                SettingsGroup("pages", "📄 تنظيم الصفحات والواجهة", "تحكم في الشاشة الابتدائية وعناصر التنقل", expanded, ::toggle) {
                    SettingsChoice("الصفحة الافتراضية", defaultPage, defaultPage) { defaultPage = if(defaultPage=="home") "daily" else "home"; store.putString("default_page",defaultPage) }
                    SettingsSwitch("إظهار شريط البحث", showSearch, {showSearch=it;store.putBoolean("show_search",it)})
                    SettingsSwitch("إظهار زر الإضافة السريع", showFab, {showFab=it;store.putBoolean("show_fab",it)})
                    SettingsSwitch("إظهار Bottom Navigation", showBottomNav, {showBottomNav=it;store.putBoolean("show_bottom_nav",it)})
                    SettingsSwitch("إظهار Drawer", store.getBoolean("show_drawer",true), {store.putBoolean("show_drawer",it)})
                    SettingsSwitch("حفظ آخر صفحة", store.getBoolean("remember_page",true), {store.putBoolean("remember_page",it)})
                    Text("ترتيب الصفحات بالسحب سيظهر فقط بعد ربطه بمسارات التنقل الفعلية، لمنع فقدان صفحات موجودة.", fontSize=11.sp, color=AppMuted)
                }
            }

            if (match("التسجيل", "حسابات", "قطع", "أيام")) item {
                SettingsGroup("registration", "📋 نظام التسجيلات", "خيارات السلوك الافتراضي التي لا تغيّر سجلات المحلات القديمة", expanded, ::toggle) {
                    SettingsSwitch("الحفظ التلقائي", store.getBoolean("autosave",true), {store.putBoolean("autosave",it)})
                    SettingsSwitch("تأكيد قبل الحذف", store.getBoolean("confirm_delete",true), {store.putBoolean("confirm_delete",it)})
                    SettingsSwitch("الانتقال لليوم التالي بعد الحفظ", store.getBoolean("next_day_after_save",false), {store.putBoolean("next_day_after_save",it)})
                    SettingsSwitch("إظهار صف الإجمالي", store.getBoolean("show_total_row",true), {store.putBoolean("show_total_row",it)})
                    SettingsChoice("عدد الصفوف في الجدول", store.getInt("table_rows",20).toString(), store.getInt("table_rows",20).toString()) { store.putInt("table_rows", if(store.getInt("table_rows",20)>=50) 10 else store.getInt("table_rows",20)+10) }
                    SettingsSwitch("تمييز الصفوف غير المكتملة", store.getBoolean("highlight_incomplete",true), {store.putBoolean("highlight_incomplete",it)})
                    Text("نوع التسجيل (عددي/فردي) يبقى إعدادًا مستقلًا لكل محل من شاشة إعدادات المحل.", fontSize=11.sp, color=AppMuted)
                }
            }

            if (match("الحساب", "مزامنة", "تسجيل الدخول")) item {
                SettingsGroup("account", "👤 الحساب والمزامنة", "حالة الحساب واتصال المزامنة", expanded, ::toggle) {
                    NotebkSecondaryAction("تسجيل الدخول / إنشاء حساب", onAccountAuth)
                    Text("يمكن استخدام التطبيق بدون حساب ثم ربط الحساب لاحقًا دون حذف السجلات المحلية.", fontSize=11.sp, color=AppMuted)
                }
            }

            if (match("النظام", "Android", "إشعارات الجهاز", "صلاحيات")) item {
                SettingsGroup("system", "📱 إعدادات Android", "الإشعارات والصلاحيات والبطارية ومساحة التخزين", expanded, ::toggle) {
                    NotebkSecondaryAction("فتح إعدادات النظام", onSystemSettings)
                }
            }

            if (match("مساعدة", "حول", "دليل")) item {
                SettingsGroup("about", "ℹ️ حول التطبيق والمساعدة", "الدليل، الدعم، الإصدار وهوية AHD DEV TEAM", expanded, ::toggle) {
                    Text("notebk", fontSize=20.sp, fontWeight=FontWeight.Bold)
                    Text("AHD DEV TEAM • ${com.ahd.notebk.ui.components.NotebkBrand.TAGLINE}", fontSize=11.sp, color=AppMuted)
                    NotebkSecondaryAction("فتح دليل الاستخدام", onHelp)
                    NotebkSecondaryAction("إعدادات النظام", onSystemSettings)
                }
            }

            item {
                NotebkSectionCard("إدارة الإعدادات", "الحفظ تلقائي. يمكنك إعادة الإعدادات العامة إلى القيم الافتراضية.") {
                    NotebkSecondaryAction("إعادة ضبط الإعدادات العامة", { store.resetGeneral() })
                    Text("لا يتم مسح قاعدة البيانات أو السجلات عند إعادة ضبط الإعدادات.", fontSize=11.sp, color=AppMuted)
                }
            }
        }
    }
}

@Composable
private fun SettingsGroup(id:String, title:String, description:String, expanded:Set<String>, toggle:(String)->Unit, content:@Composable ()->Unit) {
    NotebkSectionCard(title, description) {
        Row(Modifier.fillMaxWidth().clickable { toggle(id) }, verticalAlignment=Alignment.CenterVertically) {
            Text(if(expanded.contains(id)) "−" else "+", fontSize=22.sp, color=com.ahd.notebk.ui.components.NotebkBrandPurple)
            Spacer(Modifier.size(8.dp))
            Text(if(expanded.contains(id)) "إخفاء التفاصيل" else "عرض التفاصيل", fontSize=11.sp, color=AppMuted)
        }
        if(expanded.contains(id)) { Spacer(Modifier.size(8.dp)); content() }
    }
}

@Composable
private fun SettingsSwitch(title:String, checked:Boolean, onChange:(Boolean)->Unit, description:String?=null) {
    Row(Modifier.fillMaxWidth().padding(vertical=4.dp), verticalAlignment=Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) { Text(title, fontSize=13.sp, fontWeight=FontWeight.Medium); if(description!=null) Text(description,fontSize=10.sp,color=AppMuted) }
        Switch(checked=checked,onCheckedChange=onChange)
    }
}

@Composable
private fun SettingsChoice(title:String, value:String, current:String, onClick:()->Unit) {
    Row(Modifier.fillMaxWidth().clickable(onClick=onClick).padding(vertical=7.dp), verticalAlignment=Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) { Text(title,fontSize=13.sp,fontWeight=FontWeight.Medium); Text(value,fontSize=10.sp,color=AppMuted) }
        Text("تغيير",fontSize=11.sp,color=com.ahd.notebk.ui.components.NotebkBrandPurple,fontWeight=FontWeight.Bold)
    }
}

@Composable
fun SystemSettingsPage(
    onOpenAppSettings: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenDisplay: () -> Unit,
    onOpenLanguage: () -> Unit,
    onOpenBattery: () -> Unit,
    onBack: () -> Unit
) {
    NotebkPage("إعدادات النظام", "إدارة إعدادات Android المرتبطة بتطبيق notebk", onBack) {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { NotebkSectionCard("notebk على Android", "هذه الخيارات تفتح صفحات النظام الرسمية ولا تغيّر بيانات سجلاتك") {
                NotebkSecondaryAction("إشعارات التطبيق", onOpenNotifications)
                NotebkSecondaryAction("الصلاحيات", onOpenAppSettings)
                NotebkSecondaryAction("البطارية واستهلاك الطاقة", onOpenBattery)
                NotebkSecondaryAction("معلومات التطبيق ومساحة التخزين", onOpenAppSettings)
            } }
            item { NotebkSectionCard("المظهر واللغة", "خيارات النظام العامة") {
                NotebkSecondaryAction("إعدادات الشاشة والمظهر", onOpenDisplay)
                NotebkSecondaryAction("اللغة والمنطقة", onOpenLanguage)
            } }
            item {
                Text("ملاحظة: تغيير إعدادات Android قد يؤثر في الإشعارات أو عمل التذكير اليومي بحسب إعدادات النظام.", fontSize = 11.sp, color = AppMuted)
            }
        }
    }
}
