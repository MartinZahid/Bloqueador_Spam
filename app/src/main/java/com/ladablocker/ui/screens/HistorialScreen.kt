package com.ladablocker.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoNotDisturb
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ladablocker.data.AppState
import com.ladablocker.data.BlockedCall
import com.ladablocker.ui.AppViewModel
import com.ladablocker.util.Normalizer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistorialScreen(state: AppState, vm: AppViewModel) {
    if (state.history.isEmpty()) {
        Column(Modifier.fillMaxSize().padding(24.dp)) {
            Text("Aún no hay llamadas bloqueadas.", style = MaterialTheme.typography.bodyLarge)
            Text(
                "Recuerda activar LadaBlocker como 'ID de llamada y spam' en Ajustes para que empiece a filtrar.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        item {
            Row(Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "${state.history.size} llamadas bloqueadas",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { vm.clearHistory() }) {
                    Icon(Icons.Filled.Delete, contentDescription = "Vaciar historial")
                }
            }
            HorizontalDivider()
        }
        items(state.history, key = { "${it.time}-${it.number}" }) { call ->
            HistRow(call, state, vm)
            HorizontalDivider()
        }
    }
}

@Composable
private fun HistRow(call: BlockedCall, state: AppState, vm: AppViewModel) {
    val national = Normalizer.normalizeToNational(call.number)
    val inExactos = national.length == 10 && national in state.exactos
    val inBlanca = national.length >= 7 && national in state.blanca

    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(call.number, style = MaterialTheme.typography.bodyLarge)
            Text(
                call.rule,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (national.length == 10) {
            if (!inExactos) {
                IconButton(onClick = { vm.addExact(national) }) {
                    Icon(Icons.Filled.DoNotDisturb, contentDescription = "Bloquear siempre")
                }
            }
            if (!inBlanca) {
                IconButton(onClick = { vm.addBlanca(national) }) {
                    Icon(Icons.Filled.PersonAdd, contentDescription = "No bloquear este número")
                }
            }
        }
        Text(
            SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(Date(call.time)),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}