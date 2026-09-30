package com.ahd.notebk.ui.screen.ledger.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.size
import com.ahd.notebk.domain.model.TailorRecord

@Composable
fun RecordActions(record: TailorRecord, onEdit: (TailorRecord) -> Unit, onDelete: (TailorRecord) -> Unit) {
    Row {
        IconButton(onClick = { onEdit(record) }, modifier = androidx.compose.ui.Modifier.size(36.dp)) {
            Icon(Icons.Default.Edit, contentDescription = "تعديل")
        }
        IconButton(onClick = { onDelete(record) }, modifier = androidx.compose.ui.Modifier.size(36.dp)) {
            Icon(Icons.Default.Delete, contentDescription = "حذف")
        }
    }
}
