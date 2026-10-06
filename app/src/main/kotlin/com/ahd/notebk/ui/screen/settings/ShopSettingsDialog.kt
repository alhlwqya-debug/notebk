package com.ahd.notebk.ui.screen.settings

import com.ahd.notebk.domain.model.RegistrationMode
import com.ahd.notebk.domain.model.ShopRecord
import com.ahd.notebk.ui.components.Purple

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun ShopSettingsDialog(
    shop: ShopRecord,
    onDismiss: () -> Unit,
    onSave: (name: String, registrationNumber: String, mode: RegistrationMode) -> Unit
) {
    var name by remember(shop.id) { mutableStateOf(shop.name) }
    var registrationNumber by remember(shop.id) { mutableStateOf(shop.registrationNumber) }
    var mode by remember(shop.id) { mutableStateOf(shop.registrationMode) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("إعدادات المحل", fontWeight = FontWeight.Bold, color = Purple) },
        text = {
            Column(Modifier.fillMaxWidth()) {
                Text("تعديل بيانات المحل الحالي فقط.", fontSize = 12.sp, color = Color.Gray)
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    singleLine = true,
                    label = { Text("اسم المحل") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = registrationNumber,
                    onValueChange = { registrationNumber = it },
                    singleLine = true,
                    label = { Text("رقم المحل / التسجيل") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))
                Text("طريقة تسجيل العمل", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                ShopModeOption("تسجيل عددي", "جدول الأيام والكميات والمصروفات.", mode == RegistrationMode.NUMERIC) { mode = RegistrationMode.NUMERIC }
                ShopModeOption("تسجيل فردي", "تسجيل العمل حسب الزبون ورقم الصفحة.", mode == RegistrationMode.INDIVIDUAL) { mode = RegistrationMode.INDIVIDUAL }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name.trim(), registrationNumber.trim(), mode) },
                enabled = name.trim().isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = Purple)
            ) { Text("حفظ التعديلات") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}

@Composable
private fun ShopModeOption(
    title: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text(description, fontSize = 10.sp, color = Color.Gray)
        }
    }
}
