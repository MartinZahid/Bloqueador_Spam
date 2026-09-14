package com.ladablocker.ui.screens

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.ladablocker.data.AppState
import com.ladablocker.ui.AppViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AjustesScreen(state: AppState, vm: AppViewModel) {
    val context = LocalContext.current

    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) vm.reload()
    }

    fun requestContactPermission() {
        permLauncher.launch(Manifest.permission.READ_CONTACTS)
    }

    fun openCallScreeningSettings() {
        try {
            context.startActivity(Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "Abre manualmente Ajustes > Apps > Apps predeterminadas", Toast.LENGTH_LONG).show()
        }
    }

    fun openBatterySettings() {
        val intent = Intent(
            Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
            Uri.parse("package:${context.packageName}")
        )
        runCatching { context.startActivity(intent) }
            .onFailure {
                Toast.makeText(context, "Abre: Ajustes > Batería > LadaBlocker > Sin restricción", Toast.LENGTH_LONG).show()
            }
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(8.dp))

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Row(
                Modifier.padding(14.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        if (state.masterEnabled) "Bloqueo activado" else "Bloqueo en pausa",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        if (state.masterEnabled) "Todas las reglas están aplicando."
                        else "No se bloqueará ninguna llamada hasta reactivar.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(checked = state.masterEnabled, onCheckedChange = { vm.setMaster(it) })
            }
        }

        Spacer(Modifier.height(12.dp))

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Info, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Activar el filtro (obligatorio)", style = MaterialTheme.typography.titleMedium)
                }
                Spacer(Modifier.height(8.dp))
                Text("1. Entra a la pantalla de 'ID de llamada y spam' y elige LadaBlocker.", style = MaterialTheme.typography.bodyMedium)
                Button(onClick = { openCallScreeningSettings() }, modifier = Modifier.fillMaxWidth()) {
                    Text("Abrir 'ID de llamada y spam'")
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "En Samsung: busca ahí 'LadaBlocker' y selecciónala. Sin este paso, LadaBlocker NO puede revisar ninguna llamada.",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Solo una app a la vez puede ser 'ID de llamada y spam'. Si usas Truecaller u otra, quítale el rol ahí antes de activar LadaBlocker.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                Text("2. Quita la restricción de batería (para que no la cierren).", style = MaterialTheme.typography.bodyMedium)
                OutlinedButton(onClick = { openBatterySettings() }, modifier = Modifier.fillMaxWidth()) {
                    Text("Batería: permitir uso sin restricción")
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    "3. (Opcional) Si activas la lista blanca de contactos, se pedirá acceso a contactos.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        Card {
            Column(Modifier.padding(14.dp)) {
                Text("Opciones de bloqueo", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Bloquear números ocultos/desconocidos", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "Números sin identificador (comunes en cobranza).",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(checked = state.unknownBlock, onCheckedChange = { vm.setUnknownBlock(it) })
                }
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Nunca bloquear a mis contactos", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            if (state.contactsPermission) "Usa ${state.contacts.size} números de tu agenda."
                            else "Requiere permiso de contactos.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = state.contactsBlanca,
                        onCheckedChange = { enabled ->
                            vm.setContactsBlanca(enabled)
                            if (enabled && !state.contactsPermission) requestContactPermission()
                        }
                    )
                }
                Spacer(Modifier.height(8.dp))
            }
        }

        Spacer(Modifier.height(12.dp))

        Card {
            Column(Modifier.padding(14.dp)) {
                Text("Listas en la nube (comunidad)", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Se actualizan cada semana, solo en Wi-Fi, y las novedades quedan en la pestaña Revisar (sin notificaciones).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    if (state.lastUpdate > 0) "Última actualización: " +
                        SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(Date(state.lastUpdate))
                    else "Aún no se actualiza automáticamente (agrega al menos una URL).",
                    style = MaterialTheme.typography.labelMedium
                )

                var newUrl by remember { mutableStateOf("") }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = newUrl,
                        onValueChange = { newUrl = it },
                        label = { Text("URL de lista .txt/.csv") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(6.dp))
                    IconButton(onClick = {
                        vm.addUrl(newUrl)
                        newUrl = ""
                    }) {
                        Icon(Icons.Filled.Add, contentDescription = "Agregar URL")
                    }
                }
                state.updateUrls.forEach { url ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Link, contentDescription = null)
                        Text(url, Modifier.weight(1f).padding(start = 6.dp), style = MaterialTheme.typography.bodySmall)
                        IconButton(onClick = { vm.removeUrl(url) }) {
                            Icon(Icons.Filled.Close, contentDescription = "Quitar URL")
                        }
                    }
                }
                OutlinedButton(onClick = { vm.updateNow() }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Filled.Refresh, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Comprobar actualizaciones ahora")
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    "Ideas de fuentes: listas públicas de números reportados (NoCall 'Cobros'), máscaras SpamBlacklistMX, o cualquier lista tuya alojada.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        Card {
            Column(Modifier.padding(14.dp)) {
                Text("Modo sigilo / bajo consumo", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text(
                    "LadaBlocker no deja notificaciones, no corre en segundo plano y no gasta batería: se despierta solo al llegar una llamada, decide en microsegundos y se duerme. Si Samsung la llegara a suspender mucho, ábrela de vez en cuando o vuelve a tocar el paso 2 de la activación.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}