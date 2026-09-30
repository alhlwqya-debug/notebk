package com.ahd.notebk.ui.screen.individual.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PersonSelector(people: List<String>, selected: String?, showUnassigned: Boolean, onSelect: (String?) -> Unit) {
    LazyRow(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(horizontal = 2.dp)) {
        item { FilterChip(selected = selected == null, onClick = { onSelect(null) }, label = { Text("الكل") }) }
        items(people) { person ->
            FilterChip(selected = selected == person, onClick = { onSelect(person) }, label = { Text(person) }, leadingIcon = { Icon(Icons.Default.Person, null) })
        }
        if (showUnassigned) item { FilterChip(selected = selected == "__UNASSIGNED__", onClick = { onSelect("__UNASSIGNED__") }, label = { Text("غير محدد") }) }
    }
}
