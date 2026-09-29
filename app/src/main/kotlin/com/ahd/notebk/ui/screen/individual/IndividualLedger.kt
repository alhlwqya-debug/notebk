package com.ahd.notebk.ui.screen.individual

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ahd.notebk.domain.model.TailorRecord
import com.ahd.notebk.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IndividualLedger(records: List<TailorRecord>, onBack: () -> Unit) {
    Scaffold(topBar = { TopAppBar(title = { Text("السجل الفردي") }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "رجوع") } }) }) { padding ->
        LazyColumn(Modifier.fillMaxSize().background(LightBackground).padding(padding), contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(records, key = { it.id }) { record ->
                Card(Modifier.fillMaxWidth(), shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Row { Icon(Icons.Default.Person, null, tint = PrimaryPurple); Spacer(Modifier.width(8.dp)); Text(record.note.ifBlank { "بدون اسم" }, fontWeight = FontWeight.Bold) }
                            Text("#${record.id}", color = TextSecondary)
                        }
                        Spacer(Modifier.height(10.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("القطع: ${record.itemQuantity}")
                            Text("الإنتاج: %.0f".format(record.credit), color = SecondaryGreen)
                            Text("المصروف: %.0f".format(record.debit), color = ErrorRed)
                        }
                        Text("الرصيد: %.0f".format(record.balance), color = PrimaryPurple, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
                    }
                }
            }
        }
    }
}
