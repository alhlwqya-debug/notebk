package com.ahd.notebk.ui.screen.settings

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.ahd.notebk.data.cloud.AuthResult
import com.ahd.notebk.data.cloud.SupabaseAuthRepository
import com.ahd.notebk.ui.components.AppMuted
import com.ahd.notebk.ui.components.AppText
import kotlinx.coroutines.launch

/**
 * إدارة الحساب محصورة في الإعدادات، ولا تُعرض داخل واجهة المصادقة.
 * تسجيل الخروج محلي مضمون حتى عند انقطاع الشبكة؛ حذف الحساب يتطلب دالة خادم آمنة.
 */
@Composable
fun AccountSecurityDialog(
    onDismiss: () -> Unit,
    onOpenAuth: () -> Unit,
    onSignedOut: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { SupabaseAuthRepository(context.applicationContext) }
    val session = remember { repository.savedSession() }
    val profile = remember {
        com.ahd.notebk.data.local.AccountDataScope.profilePreferences(context)
    }
    val guestMode = remember {
        context.getSharedPreferences("add_paper_user", Context.MODE_PRIVATE)
            .getBoolean("guest_mode", false)
    }
    val scope = rememberCoroutineScope()
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var messageIsError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { if (!busy) onDismiss() },
        icon = { androidx.compose.material3.Icon(Icons.Default.AccountCircle, contentDescription = null) },
        title = { Text("الحساب والأمان", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (session == null) {
                    Text(
                        if (guestMode) "أنت تستخدم التطبيق كضيف. سجّل الدخول أو أنشئ حسابًا لتفعيل مزايا الحساب السحابي."
                        else "لا يوجد حساب مسجل الدخول على هذا الجهاز.",
                        color = AppText
                    )
                    OutlinedButton(
                        onClick = onOpenAuth,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !busy
                    ) { Text("تسجيل الدخول / إنشاء حساب") }
                } else {
                    Text("معلومات الحساب", fontWeight = FontWeight.SemiBold, color = AppText)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.material3.Icon(Icons.Default.Email, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.size(8.dp))
                        Text(session.label, color = AppText)
                    }
                    Text("معرّف الحساب: " + session.userId.take(8) + "…", color = AppMuted, style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(4.dp))
                    Text("تغيير كلمة المرور", fontWeight = FontWeight.SemiBold, color = AppText)
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("كلمة المرور الجديدة") },
                        leadingIcon = { androidx.compose.material3.Icon(Icons.Default.Lock, null) },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        enabled = !busy,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("تأكيد كلمة المرور") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        enabled = !busy,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = {
                            if (newPassword != confirmPassword) {
                                message = "كلمتا المرور غير متطابقتين."
                                messageIsError = true
                            } else {
                                busy = true
                                message = null
                                scope.launch {
                                    when (val result = repository.changePassword(newPassword)) {
                                        is AuthResult.SignedIn -> {
                                            message = "تم تغيير كلمة المرور بنجاح."
                                            messageIsError = false
                                            newPassword = ""
                                            confirmPassword = ""
                                        }
                                        is AuthResult.Failure -> {
                                            message = result.message
                                            messageIsError = true
                                        }
                                        else -> {
                                            message = "تعذر تغيير كلمة المرور."
                                            messageIsError = true
                                        }
                                    }
                                    busy = false
                                }
                            }
                        },
                        enabled = !busy && newPassword.length >= 6 && confirmPassword.isNotBlank(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (busy) CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        else Text("حفظ كلمة المرور")
                    }
                    OutlinedButton(
                        onClick = {
                            busy = true
                            scope.launch {
                                val result = repository.signOut()
                                busy = false
                                // Clear only auth metadata; preserve local ledger records.
                                context.getSharedPreferences("add_paper_user", Context.MODE_PRIVATE)
                                    .edit().putBoolean("guest_mode", false).remove("user_email").apply()
                                profile.edit().remove("user_email").apply()
                                when (result) {
                                    is AuthResult.Failure -> {
                                        message = result.message
                                        messageIsError = true
                                        // The repository guarantees local credentials are cleared.
                                        onSignedOut()
                                    }
                                    else -> onSignedOut()
                                }
                            }
                        },
                        enabled = !busy,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        androidx.compose.material3.Icon(Icons.AutoMirrored.Filled.Logout, null)
                        Spacer(Modifier.size(8.dp))
                        Text("تسجيل الخروج")
                    }
                    OutlinedButton(
                        onClick = { showDeleteConfirmation = true },
                        enabled = !busy,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        androidx.compose.material3.Icon(Icons.Default.DeleteForever, null, tint = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.size(8.dp))
                        Text("حذف الحساب نهائيًا", color = MaterialTheme.colorScheme.error)
                    }
                    Text(
                        "حذف الحساب يحتاج إلى دالة حذف آمنة منشورة في Supabase. لن تُحذف سجلاتك المحلية تلقائيًا.",
                        color = AppMuted,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                message?.let {
                    Text(it, color = if (messageIsError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss, enabled = !busy) { Text("إغلاق") }
        }
    )

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { if (!busy) showDeleteConfirmation = false },
            title = { Text("تأكيد حذف الحساب") },
            text = { Text("هل أنت متأكد من حذف الحساب السحابي نهائيًا؟ لا يمكن التراجع عن هذه العملية، ولن تُحذف السجلات المحلية تلقائيًا.") },
            confirmButton = {
                TextButton(
                    enabled = !busy,
                    onClick = {
                        showDeleteConfirmation = false
                        busy = true
                        scope.launch {
                            when (val result = repository.deleteAccount()) {
                                is AuthResult.SignedIn -> {
                                    context.getSharedPreferences("add_paper_user", Context.MODE_PRIVATE)
                                        .edit().putBoolean("guest_mode", false).remove("user_email").apply()
                                    profile.edit().remove("user_email").apply()
                                    busy = false
                                    onSignedOut()
                                }
                                is AuthResult.Failure -> {
                                    message = result.message
                                    messageIsError = true
                                    busy = false
                                }
                                else -> {
                                    message = "تعذر حذف الحساب."
                                    messageIsError = true
                                    busy = false
                                }
                            }
                        }
                    }
                ) { Text("تأكيد الحذف", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }, enabled = !busy) { Text("إلغاء") }
            }
        )
    }
}
