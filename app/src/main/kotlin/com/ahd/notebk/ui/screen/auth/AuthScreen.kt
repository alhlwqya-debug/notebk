package com.ahd.notebk.ui.screen.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ahd.notebk.data.cloud.AuthResult
import com.ahd.notebk.data.cloud.SupabaseAuthRepository
import com.ahd.notebk.ui.components.AppBackground
import com.ahd.notebk.ui.components.AppBorder
import com.ahd.notebk.ui.components.AppMuted
import com.ahd.notebk.ui.components.AppPrimarySoft
import com.ahd.notebk.ui.components.AppSurface
import com.ahd.notebk.ui.components.AppSurfaceAlt
import com.ahd.notebk.ui.components.AppText
import com.ahd.notebk.ui.components.BrandLogo
import com.ahd.notebk.ui.components.NotebkBrand
import com.ahd.notebk.ui.components.NotebkBrandPurple
import com.ahd.notebk.ui.components.NotebkWordmark
import kotlinx.coroutines.launch

private val HeroStart = Color(0xFF16052E)
private val HeroEnd = Color(0xFF4B16A8)

@Composable
fun AuthScreen(
    onContinue: () -> Unit,
    allowBack: Boolean = false,
    onBack: (() -> Unit)? = null
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val repository = remember { SupabaseAuthRepository(context.applicationContext) }
    val prefs = remember { context.getSharedPreferences("add_paper_user", android.content.Context.MODE_PRIVATE) }
    var activeSession by remember { mutableStateOf(repository.savedSession()) }
    var guestMode by remember { mutableStateOf(prefs.getBoolean("guest_mode", false)) }
    val scope = rememberCoroutineScope()

    var registerMode by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf(activeSession?.label.orEmpty()) }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var success by remember { mutableStateOf(false) }
    var showAccountTools by remember { mutableStateOf(false) }
    var newPassword by remember { mutableStateOf("") }
    var confirmNewPassword by remember { mutableStateOf("") }
    var confirmDeleteAccount by remember { mutableStateOf(false) }

    fun finishGuest() {
        repository.clearSession()
        activeSession = null
        guestMode = true
        prefs.edit()
            .putBoolean("guest_mode", true)
            .putString("user_name", "ضيف")
            .putString("user_email", "")
            .apply()
        onContinue()
    }

    fun finishSession(result: AuthResult.SignedIn) {
        activeSession = result.session
        guestMode = false
        prefs.edit()
            .putBoolean("guest_mode", false)
            .putString("user_email", result.session.label)
            .putString("user_name", name.trim().ifBlank { result.session.label.substringBefore('@') })
            .apply()
        onContinue()
    }

    Box(
        Modifier.fillMaxSize().background(AppBackground)
    ) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).safeDrawingPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                Modifier.fillMaxWidth().height(285.dp)
                    .background(Brush.verticalGradient(listOf(HeroStart, HeroEnd)))
            ) {
                if (allowBack && onBack != null) {
                    IconButton(onClick = onBack, modifier = Modifier.padding(8.dp).align(Alignment.TopStart)) {
                        Icon(Icons.Default.ArrowBack, "رجوع", tint = Color.White)
                    }
                }
                Column(
                    Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Surface(
                        modifier = Modifier.size(88.dp).clip(RoundedCornerShape(24.dp)),
                        color = Color.White.copy(alpha = .10f),
                        tonalElevation = 0.dp
                    ) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { BrandLogo(Modifier.size(72.dp)) } }
                    Spacer(Modifier.height(12.dp))
                    NotebkWordmark(fontSize = 36.sp, normalColor = Color.White)
                    Text(NotebkBrand.TAGLINE, color = Color.White.copy(alpha = .78f), fontSize = 13.sp)
                    Spacer(Modifier.height(10.dp))
                    Text(
                        if (registerMode) "أنشئ حسابك واحفظ بياناتك بأمان" else "ادخل إلى سجلاتك من أي جهاز",
                        color = Color.White.copy(alpha = .70f), fontSize = 12.sp, textAlign = TextAlign.Center
                    )
                }
            }

            Column(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (activeSession != null || guestMode) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = AppPrimarySoft),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AppBorder)
                    ) {
                        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Surface(Modifier.size(44.dp), shape = CircleShape, color = NotebkBrandPurple) {
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Icon(if (activeSession != null) Icons.Default.CloudDone else Icons.Default.WifiOff, null, tint = Color.White)
                                }
                            }
                            Spacer(Modifier.size(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(if (activeSession != null) "الحساب متصل" else "تستخدم notebk كضيف", fontWeight = FontWeight.Bold, color = AppText)
                                Text(activeSession?.label ?: "البيانات محفوظة على هذا الجهاز", fontSize = 11.sp, color = AppMuted)
                            }
                            if (activeSession != null) {
                                IconButton(onClick = {
                                    busy = true
                                    scope.launch {
                                        repository.signOut()
                                        busy = false
                                        activeSession = null
                                        guestMode = false
                                        prefs.edit().putBoolean("guest_mode", false).remove("user_email").apply()
                                        message = "تم تسجيل الخروج من هذا الجهاز. بياناتك المحلية لم تُحذف."
                                        success = true
                                    }
                                }) {
                                    Icon(Icons.Default.Logout, "تسجيل الخروج", tint = NotebkBrandPurple)
                                }
                            }
                        }
                    }
                }

                if (activeSession != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = AppSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AppBorder)
                    ) {
                        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("إدارة الحساب", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = AppText)
                            Text("البريد المسجل: ${activeSession?.label.orEmpty()}", color = AppMuted, fontSize = 12.sp)
                            OutlinedButton(onClick = { showAccountTools = !showAccountTools }, modifier = Modifier.fillMaxWidth()) {
                                Text(if (showAccountTools) "إخفاء إعدادات الأمان" else "تغيير كلمة المرور")
                            }
                            if (showAccountTools) {
                                OutlinedTextField(newPassword, { newPassword = it }, label = { Text("كلمة المرور الجديدة") }, visualTransformation = PasswordVisualTransformation(), singleLine = true, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(confirmNewPassword, { confirmNewPassword = it }, label = { Text("تأكيد كلمة المرور الجديدة") }, visualTransformation = PasswordVisualTransformation(), singleLine = true, modifier = Modifier.fillMaxWidth())
                                Button(
                                    onClick = {
                                        if (newPassword != confirmNewPassword) {
                                            message = "كلمتا المرور الجديدتان غير متطابقتين."
                                            success = false
                                        } else {
                                            busy = true
                                            scope.launch {
                                                val result = repository.changePassword(newPassword)
                                                busy = false
                                                when (result) {
                                                    is AuthResult.SignedIn -> { newPassword = ""; confirmNewPassword = ""; message = "تم تغيير كلمة المرور بنجاح."; success = true }
                                                    is AuthResult.Failure -> { message = result.message; success = false }
                                                    else -> { message = "تعذر تغيير كلمة المرور."; success = false }
                                                }
                                            }
                                        }
                                    },
                                    enabled = !busy && newPassword.length >= 6 && confirmNewPassword.isNotBlank(),
                                    modifier = Modifier.fillMaxWidth()
                                ) { Text("حفظ كلمة المرور الجديدة") }
                                OutlinedButton(onClick = { confirmDeleteAccount = true }, enabled = !busy, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                                    Text("حذف حسابي نهائيًا")
                                }
                                Text("حذف الحساب نهائي ولا يمكن التراجع عنه. يتطلب حذف الحساب السحابي تفعيل دالة حذف آمنة على الخادم.", color = AppMuted, fontSize = 11.sp, lineHeight = 16.sp)
                            }
                        }
                    }
                } else if (!guestMode) {
                    TextButton(
                        onClick = {
                            busy = true
                            scope.launch {
                                val result = repository.requestPasswordReset(email)
                                busy = false
                                when (result) {
                                    is AuthResult.ConfirmationEmailSent -> { message = "إذا كان البريد مسجلًا، فستصلك رسالة لإعادة تعيين كلمة المرور."; success = true }
                                    is AuthResult.Failure -> { message = result.message; success = false }
                                    else -> { message = "تعذر طلب إعادة تعيين كلمة المرور."; success = false }
                                }
                            }
                        },
                        enabled = !busy && email.isNotBlank(),
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("نسيت كلمة المرور؟ إرسال رابط الاستعادة") }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = AppSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AppBorder),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ModeButton("تسجيل الدخول", !registerMode, Icons.Default.Login) { registerMode = false; message = null; success = false }
                            ModeButton("حساب جديد", registerMode, Icons.Default.PersonAdd) { registerMode = true; message = null; success = false }
                        }
                        Spacer(Modifier.height(4.dp))

                        if (registerMode) {
                            OutlinedTextField(name, { name = it }, label = { Text("الاسم") }, leadingIcon = { Icon(Icons.Default.Person, null) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                        }
                        OutlinedTextField(email, { email = it }, label = { Text("البريد الإلكتروني") }, leadingIcon = { Icon(Icons.Default.Person, null) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(
                            password, { password = it }, label = { Text("كلمة المرور") }, leadingIcon = { Icon(Icons.Default.Lock, null) }, singleLine = true,
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = { IconButton(onClick = { passwordVisible = !passwordVisible }) { Icon(if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, null) } },
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (registerMode) {
                            OutlinedTextField(
                                confirmPassword, { confirmPassword = it }, label = { Text("تأكيد كلمة المرور") }, leadingIcon = { Icon(Icons.Default.Lock, null) }, singleLine = true,
                                visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth()
                            )
                        }

                        if (!message.isNullOrBlank()) {
                            Text(message!!, color = if (success) Color(0xFF237A35) else MaterialTheme.colorScheme.error, fontSize = 12.sp, lineHeight = 18.sp)
                        }

                        Button(
                            onClick = {
                                if (registerMode && password != confirmPassword) { message = "كلمتا المرور غير متطابقتين."; return@Button }
                                busy = true; message = null
                                scope.launch {
                                    val result = if (registerMode) repository.signUpWithPassword(email, password, name) else repository.signInWithPassword(email, password)
                                    busy = false
                                    when (result) {
                                        is AuthResult.SignedIn -> finishSession(result)
                                        is AuthResult.EmailConfirmationRequired -> { success = true; message = "تم إنشاء الحساب. افتح رسالة تأكيد البريد ثم سجّل الدخول."; registerMode = false }
                                        is AuthResult.ConfirmationEmailSent -> { success = true; message = "أُعيد إرسال رسالة التأكيد." }
                                        is AuthResult.Failure -> { success = false; message = result.message }
                                    }
                                }
                            },
                            enabled = !busy,
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(15.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NotebkBrandPurple),
                            contentPadding = PaddingValues(12.dp)
                        ) {
                            if (busy) CircularProgressIndicator(Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
                            else Text(if (registerMode) "إنشاء الحساب والمتابعة" else "تسجيل الدخول", fontWeight = FontWeight.Bold)
                        }

                        TextButton(onClick = { finishGuest() }, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Default.WifiOff, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.size(6.dp))
                            Text("الدخول بدون حساب")
                        }
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = AppSurfaceAlt)
                ) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lock, null, tint = NotebkBrandPurple)
                        Spacer(Modifier.size(10.dp))
                        Text("يمكنك استخدام notebk بدون حساب. عند تسجيل الدخول لاحقًا يمكن ربط حسابك بالمزامنة السحابية.", fontSize = 11.sp, color = AppMuted, lineHeight = 17.sp)
                    }
                }

                Text("AHD SOFTWARE PARTNER", fontSize = 10.sp, color = AppMuted, letterSpacing = 1.3.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                Text(NotebkBrand.CREDIT, fontSize = 10.sp, color = AppMuted, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            }
        }
    }

    if (confirmDeleteAccount) {
        AlertDialog(
            onDismissRequest = { confirmDeleteAccount = false },
            title = { Text("تأكيد حذف الحساب") },
            text = { Text("هل أنت متأكد من حذف حسابك السحابي نهائيًا؟ لن تُحذف السجلات المحلية من هذا الجهاز تلقائيًا.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmDeleteAccount = false
                    busy = true
                    scope.launch {
                        val result = repository.deleteAccount()
                        busy = false
                        when (result) {
                            is AuthResult.SignedIn -> {
                                activeSession = null
                                guestMode = false
                                prefs.edit().putBoolean("guest_mode", false).remove("user_email").apply()
                                message = "تم حذف الحساب السحابي."
                                success = true
                            }
                            is AuthResult.Failure -> { message = result.message; success = false }
                            else -> { message = "تعذر حذف الحساب."; success = false }
                        }
                    }
                }) { Text("تأكيد الحذف", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { confirmDeleteAccount = false }) { Text("إلغاء") } }
        )
    }
}

@Composable
private fun RowScope.ModeButton(title: String, selected: Boolean, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(13.dp),
        colors = if (selected) ButtonDefaults.outlinedButtonColors(containerColor = AppPrimarySoft, contentColor = NotebkBrandPurple) else ButtonDefaults.outlinedButtonColors(contentColor = AppMuted)
    ) {
        Icon(icon, null, modifier = Modifier.size(18.dp)); Spacer(Modifier.size(5.dp)); Text(title, fontSize = 12.sp)
    }
}
