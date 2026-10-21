package com.ahd.notebk.security

import com.ahd.notebk.ui.components.Purple
import com.ahd.notebk.ui.components.Green
import com.ahd.notebk.ui.components.Blue
import com.ahd.notebk.data.local.LedgerDatabase

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import com.ahd.notebk.data.local.NotebkSettingsStore
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.MessageDigest
import java.util.Date
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

private const val SECURITY_PREFS = "app_security"
private const val PIN_HASH = "pin_hash"

internal fun hasAppPin(context: Context): Boolean =
    context.getSharedPreferences(SECURITY_PREFS, Context.MODE_PRIVATE)
        .getString(PIN_HASH, null).orEmpty().isNotBlank()

private fun hashPin(pin: String): String {
    val digest = MessageDigest.getInstance("SHA-256").digest(pin.toByteArray(Charsets.UTF_8))
    return digest.joinToString("") { "%02x".format(it) }
}

internal fun setAppPin(context: Context, pin: String) {
    context.getSharedPreferences(SECURITY_PREFS, Context.MODE_PRIVATE)
        .edit().putString(PIN_HASH, hashPin(pin)).apply()
}

internal fun clearAppPin(context: Context) {
    context.getSharedPreferences(SECURITY_PREFS, Context.MODE_PRIVATE)
        .edit().remove(PIN_HASH).apply()
}

internal fun verifyAppPin(context: Context, pin: String): Boolean =
    hashPin(pin) == context.getSharedPreferences(SECURITY_PREFS, Context.MODE_PRIVATE)
        .getString(PIN_HASH, "")

@Composable
internal fun AppLockScreen(context: Context, onUnlocked: () -> Unit) {
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    Column(
        Modifier.fillMaxWidth().padding(28.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("التطبيق مقفل", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(
            "أدخل رمز الحماية للوصول إلى الحسابات والبيانات.",
            fontSize = 12.sp,
            color = Color.Gray
        )
        Spacer(Modifier.height(18.dp))
        androidx.compose.material3.OutlinedTextField(
            value = pin,
            onValueChange = { pin = it.filter(Char::isDigit).take(8); error = "" },
            label = { Text("رمز القفل") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            modifier = Modifier.fillMaxWidth()
        )
        if (error.isNotBlank()) {
            Spacer(Modifier.height(6.dp))
            Text(error, color = Color(0xFFC62828), fontSize = 11.sp)
        }
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = {
                if (verifyAppPin(context, pin)) onUnlocked()
                else error = "رمز القفل غير صحيح"
            },
            enabled = pin.length >= 4,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Purple)
        ) { Text("فتح التطبيق") }
    }
}

@Composable
internal fun SetPinDialog(
    onDismiss: () -> Unit,
    onSaved: (String) -> Unit
) {
    var first by remember { mutableStateOf("") }
    var second by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تعيين رمز حماية", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(
                    "استخدم رمزًا من 4 إلى 8 أرقام. لا تحفظه داخل النسخة الاحتياطية كنص مكشوف.",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
                Spacer(Modifier.height(8.dp))
                androidx.compose.material3.OutlinedTextField(
                    first,
                    { first = it.filter(Char::isDigit).take(8) },
                    label = { Text("الرمز") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(7.dp))
                androidx.compose.material3.OutlinedTextField(
                    second,
                    { second = it.filter(Char::isDigit).take(8) },
                    label = { Text("تأكيد الرمز") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.fillMaxWidth()
                )
                if (error.isNotBlank()) {
                    Spacer(Modifier.height(5.dp))
                    Text(error, color = Color(0xFFC62828), fontSize = 10.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when {
                        first.length < 4 -> error = "الرمز يجب أن يكون 4 أرقام على الأقل"
                        first != second -> error = "الرمزان غير متطابقين"
                        else -> onSaved(first)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Purple)
            ) { Text("حفظ") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}

@Composable
internal fun SecuritySettingsDialog(
    context: Context,
    hasPin: Boolean,
    onDismiss: () -> Unit,
    onSetPin: () -> Unit,
    onRemovePin: () -> Unit,
    onBackup: () -> Unit,
    onRestore: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.Security,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                "الحماية والنسخ الاحتياطي",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = null,
                        tint = if (hasPin) Green else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            if (hasPin) "قفل التطبيق مفعّل" else "قفل التطبيق غير مفعّل",
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            if (hasPin) "بياناتك محمية برمز الدخول."
                            else "فعّل رمزًا من 4 إلى 8 أرقام لحماية فتح التطبيق.",
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    }
                }

                Button(
                    onClick = onBackup,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Green)
                ) {
                    Icon(Icons.Default.Backup, null)
                    Spacer(Modifier.width(8.dp))
                    Text("إنشاء نسخة احتياطية")
                }

                OutlinedButton(
                    onClick = onRestore,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Restore, null)
                    Spacer(Modifier.width(8.dp))
                    Text("استعادة نسخة احتياطية")
                }

                Text(
                    "احفظ النسخة في مكان مستقل مثل Google Drive أو ذاكرة خارجية. الاستعادة تستبدل بيانات التطبيق الحالية.",
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("إغلاق", color = MaterialTheme.colorScheme.primary)
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onSetPin) {
                    Text(if (hasPin) "تغيير القفل" else "تعيين القفل")
                }
                if (hasPin) {
                    TextButton(onClick = onRemovePin) {
                        Text("إلغاء القفل", color = Color(0xFFC62828))
                    }
                }
            }
        }
    )
}


private const val INTERNAL_BACKUP_DIR = "internal_backups"
private const val INTERNAL_BACKUP_PREFIX = "notebk_auto_"
private const val MAX_INTERNAL_BACKUPS = 10

/**
 * Creates a private automatic backup before destructive operations and from
 * the daily reminder. The backup stays inside the app's private files area
 * and never requires storage permissions or a shareable URI.
 */
internal fun createInternalAutoBackup(context: Context): Boolean {
    return try {
        val backupDir = File(context.filesDir, INTERNAL_BACKUP_DIR).apply { mkdirs() }
        if (!backupDir.isDirectory) return false

        val checkpointDb = LedgerDatabase(context)
        try {
            checkpointDb.optimizeDatabase()
            checkpointDb.writableDatabase.rawQuery("PRAGMA wal_checkpoint(FULL)", null).use { cursor ->
                if (cursor.moveToFirst()) {
                    // Consume the checkpoint result.
                }
            }
        } finally {
            checkpointDb.close()
        }

        val databaseFile = context.getDatabasePath("add_paper.db")
        if (!databaseFile.exists()) return false

        val stamp = java.text.SimpleDateFormat(
            "yyyyMMdd_HHmmss_SSS",
            java.util.Locale.US
        ).format(java.util.Date())
        val backupFile = File(backupDir, "$INTERNAL_BACKUP_PREFIX$stamp.zip")
        FileOutputStream(backupFile).use { output ->
            ZipOutputStream(output).use { zip ->
                addFileToZip(databaseFile, zip, "database/add_paper.db")

                val prefs = File(context.dataDir, "shared_prefs/add_paper_user.xml")
                if (prefs.exists()) {
                    addFileToZip(prefs, zip, "shared_prefs/add_paper_user.xml")
                }

                val securityPrefs = File(context.dataDir, "shared_prefs/app_security.xml")
                if (securityPrefs.exists()) {
                    addFileToZip(securityPrefs, zip, "shared_prefs/app_security.xml")
                }

                val profileImage = File(context.filesDir, "profile_image.jpg")
                if (profileImage.exists()) {
                    addFileToZip(profileImage, zip, "files/profile_image.jpg")
                }

                zip.putNextEntry(ZipEntry("backup_info.txt"))
                zip.write(
                    "notebk internal automatic backup\n${java.util.Date()}\n"
                        .toByteArray(Charsets.UTF_8)
                )
                zip.closeEntry()
            }
        }

        val retention = NotebkSettingsStore(context)
            .getInt("backup_retention", MAX_INTERNAL_BACKUPS)
            .coerceIn(1, 50)
        backupDir.listFiles { file ->
            file.isFile && file.name.startsWith(INTERNAL_BACKUP_PREFIX) && file.extension == "zip"
        }
            ?.sortedByDescending { it.lastModified() }
            ?.drop(retention)
            ?.forEach { it.delete() }

        true
    } catch (_: Exception) {
        false
    }
}

private fun addFileToZip(file: File, zip: ZipOutputStream, entryName: String) {
    zip.putNextEntry(ZipEntry(entryName))
    FileInputStream(file).use { it.copyTo(zip) }
    zip.closeEntry()
}

internal fun exportBackup(context: Context, uri: Uri): Boolean {
    return try {
        val db = File(context.getDatabasePath("add_paper.db").absolutePath)
        if (!db.exists()) return false
        val checkpointDb = LedgerDatabase(context)
        checkpointDb.optimizeDatabase()
        checkpointDb.writableDatabase.rawQuery("PRAGMA wal_checkpoint(FULL)", null).use { it.moveToFirst() }
        checkpointDb.close()
        val prefs = File(context.dataDir, "shared_prefs/add_paper_user.xml")
        context.contentResolver.openOutputStream(uri)?.use { output ->
            ZipOutputStream(output).use { zip ->
                zip.putNextEntry(ZipEntry("database/add_paper.db"))
                FileInputStream(db).use { it.copyTo(zip) }
                zip.closeEntry()
                if (prefs.exists()) {
                    zip.putNextEntry(ZipEntry("shared_prefs/add_paper_user.xml"))
                    FileInputStream(prefs).use { it.copyTo(zip) }
                    zip.closeEntry()
                }
                val securityPrefs = File(context.dataDir, "shared_prefs/app_security.xml")
                if (securityPrefs.exists()) {
                    zip.putNextEntry(ZipEntry("shared_prefs/app_security.xml"))
                    FileInputStream(securityPrefs).use { it.copyTo(zip) }
                    zip.closeEntry()
                }
                val profileImage = File(context.filesDir, "profile_image.jpg")
                if (profileImage.exists()) {
                    zip.putNextEntry(ZipEntry("files/profile_image.jpg"))
                    FileInputStream(profileImage).use { it.copyTo(zip) }
                    zip.closeEntry()
                }
                zip.putNextEntry(ZipEntry("backup_info.txt"))
                zip.write("notebk backup\n${Date()}\n".toByteArray())
                zip.closeEntry()
            }
        } ?: return false
        true
    } catch (_: Exception) {
        false
    }
}

internal fun restoreBackup(context: Context, uri: Uri): Boolean {
    return try {
        val tempDir = File(context.cacheDir, "restore_${System.currentTimeMillis()}").apply { mkdirs() }
        context.contentResolver.openInputStream(uri)?.use { input ->
            ZipInputStream(input).use { zip ->
                var entry = zip.nextEntry
                while (entry != null) {
                    val target = File(tempDir, entry.name)
                    if (!target.canonicalPath.startsWith(tempDir.canonicalPath + File.separator)) {
                        throw SecurityException("Invalid backup")
                    }
                    if (entry.isDirectory) target.mkdirs()
                    else {
                        target.parentFile?.mkdirs()
                        FileOutputStream(target).use { output -> zip.copyTo(output) }
                    }
                    zip.closeEntry()
                    entry = zip.nextEntry
                }
            }
        } ?: return false

        val dbSource = File(tempDir, "database/add_paper.db")
        if (!dbSource.exists()) return false

        val dbTarget = context.getDatabasePath("add_paper.db")
        dbTarget.parentFile?.mkdirs()
        FileInputStream(dbSource).use { input ->
            FileOutputStream(dbTarget).use { output -> input.copyTo(output) }
        }

        val prefsSource = File(tempDir, "shared_prefs/add_paper_user.xml")
        if (prefsSource.exists()) {
            val prefsTarget = File(context.dataDir, "shared_prefs/add_paper_user.xml")
            prefsTarget.parentFile?.mkdirs()
            FileInputStream(prefsSource).use { input ->
                FileOutputStream(prefsTarget).use { output -> input.copyTo(output) }
            }
        }

        val securitySource = File(tempDir, "shared_prefs/app_security.xml")
        if (securitySource.exists()) {
            val securityTarget = File(context.dataDir, "shared_prefs/app_security.xml")
            securityTarget.parentFile?.mkdirs()
            FileInputStream(securitySource).use { input ->
                FileOutputStream(securityTarget).use { output -> input.copyTo(output) }
            }
        }

        val imageSource = File(tempDir, "files/profile_image.jpg")
        if (imageSource.exists()) {
            val imageTarget = File(context.filesDir, "profile_image.jpg")
            FileInputStream(imageSource).use { input ->
                FileOutputStream(imageTarget).use { output -> input.copyTo(output) }
            }
        }
        tempDir.deleteRecursively()
        true
    } catch (_: Exception) {
        false
    }
}
