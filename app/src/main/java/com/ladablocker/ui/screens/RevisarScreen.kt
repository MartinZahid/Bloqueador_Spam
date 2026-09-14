package com.ladablocker.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ladablocker.data.AppState
import com.ladablocker.data.Categoria
import com.ladablocker.data.CommunityNumber
import com.ladablocker.ui.AppViewModel

@Composable
fun RevisarScreen(state: AppState, vm: AppViewModel) {
    val pendingByCat = Categoria.entries.map { cat ->
        cat to state.community.filter { it.category == cat && !it.active }
    }
    val total = pendingByCat.sumOf { it.second.size }

    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        item {
            Spacer(Modifier.height(12.dp))
            Text(
                if (total == 0) "No hay números pendientes de revisión."
                else "$total números de la comunidad esperan tu visto bueno.",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(8.dp))
        }
        pendingByCat.forEach { (cat, list) ->
            if (list.isNotEmpty()) {
                item {
                    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            cat.label,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f)
                        )
                        Button(onClick = { vm.setCommunityCategory(cat, true) }) {
                            Icon(Icons.Filled.Check, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text("Activar ${list.size}")
                        }
                    }
                    HorizontalDivider()
                }
                items(list, key = { "${it.category.name}|${it.number}" }) { item ->
                    PendingRow(item, vm)
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun PendingRow(item: CommunityNumber, vm: AppViewModel) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(item.number, style = MaterialTheme.typography.bodyLarge)
            Text(
                if (item.mask) "Prefijo · " + item.source else item.source,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        OutlinedButton(onClick = {
            vm.acceptCommunity(item.number, item.category)
        }) {
            Icon(Icons.Filled.Check, contentDescription = "Bloquear")
            Spacer(Modifier.width(4.dp))
            Text("Bloquear")
        }
        IconButton(onClick = { vm.removeCommunity(item.number, item.category) }) {
            Icon(Icons.Filled.Close, contentDescription = "Descartar")
        }
    }
}