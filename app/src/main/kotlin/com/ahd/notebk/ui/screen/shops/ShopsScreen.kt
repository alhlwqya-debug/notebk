package com.ahd.notebk.ui.screen.shops

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddBusiness
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ahd.notebk.data.local.Shop
import com.ahd.notebk.ui.components.BrandTopBar

@Composable
fun ShopsScreen(viewModel: ShopViewModel) {
    val shops by viewModel.shops.collectAsState()
    val selectedId by viewModel.selectedShopId.collectAsState()
    var showCreate by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            BrandTopBar(
                title = "المحلات والفروع",
                trailing = {
                    IconButton(onClick = { showCreate = true }) {
                        Icon(Icons.Default.AddBusiness, contentDescription = "إنشاء محل")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showCreate = true },
                icon = { Icon(Icons.Default.AddBusiness, null) },
                text = { Text("إنشاء محل") }
            )
        }
    ) { padding ->
        if (shops.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("جارٍ تجهيز قائمة المحلات...")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(
                        "اختر المحل النشط الذي سيظهر في رأس شاشات التسجيل.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                    )
                }
                items(shops, key = { it.id }) { shop ->
                    ShopCard(
                        shop = shop,
                        selected = shop.id == selectedId,
                        onSelect = { viewModel.selectShop(shop.id) },
                        onDelete = { viewModel.deleteShop(shop.id) }
                    )
                }
            }
        }
    }

    if (showCreate) {
        CreateShopDialog(
            onDismiss = { showCreate = false },
            onCreate = { name, owner, phone, address ->
                viewModel.createShop(name, owner, phone, address)
                showCreate = false
            }
        )
    }
}

@Composable
private fun ShopCard(shop: Shop, selected: Boolean, onSelect: () -> Unit, onDelete: () -> Unit) {
    Card(
        onClick = onSelect,
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Store, null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(shop.name, fontWeight = FontWeight.Bold)
                if (shop.ownerName.isNotBlank()) Text("المالك: \${shop.ownerName}", style = MaterialTheme.typography.bodySmall)
                if (shop.phone.isNotBlank()) Text(shop.phone, style = MaterialTheme.typography.bodySmall)
                if (selected) Text("المحل النشط", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium)
            }
            if (selected) {
                Text("✓", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            } else {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "حذف المحل")
                }
            }
        }
    }
}

@Composable
private fun CreateShopDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var owner by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("إنشاء محل / فرع جديد") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("اسم المحل *") }, singleLine = true)
                OutlinedTextField(owner, { owner = it }, label = { Text("اسم المالك") }, singleLine = true)
                OutlinedTextField(phone, { phone = it }, label = { Text("رقم الهاتف") }, singleLine = true)
                OutlinedTextField(address, { address = it }, label = { Text("العنوان") }, singleLine = true)
            }
        },
        confirmButton = {
            Button(onClick = { onCreate(name, owner, phone, address) }, enabled = name.isNotBlank()) {
                Text("إنشاء")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}
