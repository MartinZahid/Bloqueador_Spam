package com.ladablocker.ui.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.unit.dp
import com.ladablocker.data.AppState
import com.ladablocker.data.Categoria
import com.ladablocker.data.ListsImporter
import com.ladablocker.ui.AppViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun NumerosScreen(state: AppState, vm: AppViewModel) {
    var exactInput by remember { mutableStateOf("") }
    var blancaInput by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf(Categoria.COBRANZA) }
    var showPaste by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val fileLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                val text = withContext(Dispatchers.IO) { readText(context, it) }
                val numbers = ListsImporter.parse(text)
                vm.importNumbers(numbers, categoria, it.lastPathSegment ?: "import", active = false)
            }
        }
    }

    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        item {
            Spacer(Modifier.height(8.dp))
            Text("Números exactos", style = MaterialTheme.typography.titleMedium)
            Text(
                "Estos se bloquean aunque no estén en una lada.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = exactInput,
                    onValueChange = { exactInput = it },
                    label = { Text("Número de 10 dígitos") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                Button(onClick = { vm.addExact(exactInput); exactInput = "" }) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                }
            }
            state.exactos.forEach { n ->
                Row(
                    Modifier.fillMaxWidth().padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(n, Modifier.weight(1f))
                    IconButton(onClick = { vm.removeExact(n) }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Quitar")
                    }
                }
            }
            HorizontalDivider(Modifier.padding(vertical = 12.dp))

            Text("Lista blanca (nunca bloquear)", style = MaterialTheme.typography.titleMedium)
            Text(
                "Los números aquí se respetan aunque estén en una lada o base.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = blancaInput,
                    onValueChange = { blancaInput = it },
                    label = { Text("Número para excepción") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                Button(onClick = { vm.addBlanca(blancaInput); blancaInput = "" }) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                }
            }
            state.blanca.forEach { n ->
                Row(
                    Modifier.fillMaxWidth().padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(n, Modifier.weight(1f))
                    IconButton(onClick = { vm.removeBlanca(n) }) {
                        Icon(Icons.Filled.Close, contentDescription = "Quitar")
                    }
                }
            }
            HorizontalDivider(Modifier.padding(vertical = 12.dp))

            Text("Bases comunitarias", style = MaterialTheme.typography.titleMedium)
            Text(
                "Importa o pega listas públicas de números reportados. Cargas nuevas quedan en 'Revisar' y las activas se bloquean al instante.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Categoria.entries.forEach { cat ->
                    FilterChip(
                        selected = categoria == cat,
                        onClick = { categoria = cat },
                        label = { Text(cat.label) }
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = {
                    fileLauncher.launch(arrayOf("text/*", "text/plain", "text/comma-separated-values", "application/octet-stream", "text/x-vcard"))
                }) {
                    Icon(Icons.Filled.UploadFile, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Archivo .txt/.csv")
                }
                OutlinedButton(onClick = { showPaste = true }) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Pegar lista")
                }
            }
            Spacer(Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(Modifier.padding(12.dp)) {
                    Text("Comunidad activa (bloquea):", style = MaterialTheme.typography.labelMedium)
                    Categoria.entries.forEach { cat ->
                        val activos = state.community.count { it.category == cat && it.active }
                        val pendientes = state.community.count { it.category == cat && !it.active }
                        Row(Modifier.fillMaxWidth()) {
                            Text(cat.label, Modifier.weight(1f))
                            Text("$activos activos · $pendientes pendientes")
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Nueva lista desde URL:", style = MaterialTheme.typography.labelMedium)
                        Text(
                            "(se agrega en Ajustes)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }

    if (showPaste) {
        var pasteText by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showPaste = false },
            title = { Text("Pegar lista - ${categoria.label}") },
            text = {
                OutlinedTextField(
                    value = pasteText,
                    onValueChange = { pasteText = it },
                    label = { Text("Un número por línea") },
                    minLines = 6,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val numbers = ListsImporter.parse(pasteText)
                    vm.importNumbers(numbers, categoria, "pegado", active = false)
                    showPaste = false
                }) { Text("Cargar para revisión") }
            },
            dismissButton = { TextButton(onClick = { showPaste = false }) { Text("Cancelar") } }
        )
    }
}

private fun readText(context: Context, uri: Uri): String =
    context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() } ?: ""