package com.ladablocker.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.ladablocker.ui.screens.AjustesScreen
import com.ladablocker.ui.screens.HistorialScreen
import com.ladablocker.ui.screens.LadasScreen
import com.ladablocker.ui.screens.NumerosScreen
import com.ladablocker.ui.screens.RevisarScreen

private data class Tab(
    val label: String,
    val icon: ImageVector
)

private val tabs = listOf(
    Tab("Ladas", Icons.Filled.Numbers),
    Tab("Números", Icons.Filled.List),
    Tab("Revisar", Icons.Filled.FactCheck),
    Tab("Historial", Icons.Filled.History),
    Tab("Ajustes", Icons.Filled.Settings),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LadaBlockerApp(vm: AppViewModel) {
    val state by vm.state.collectAsState()
    var selected by rememberSaveable { mutableIntStateOf(0) }
    val titles = listOf("Ladas a bloquear", "Números y listas", "Revisar listas", "Historial", "Ajustes")
    val pending = state.pendingReview

    Scaffold(
        topBar = { androidx.compose.material3.TopAppBar(title = { Text(titles[selected]) }) },
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selected == index,
                        onClick = { selected = index },
                        icon = {
                            if (index == 2) {
                                androidx.compose.material3.BadgedBox(badge = {
                                    if (pending > 0) Badge { Text(pending.toString()) }
                                }) { Icon(tab.icon, contentDescription = tab.label) }
                            } else {
                                Icon(tab.icon, contentDescription = tab.label)
                            }
                        },
                        label = { Text(tab.label) }
                    )
                }
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when (selected) {
                0 -> LadasScreen(state, vm)
                1 -> NumerosScreen(state, vm)
                2 -> RevisarScreen(state, vm)
                3 -> HistorialScreen(state, vm)
                4 -> AjustesScreen(state, vm)
            }
        }
    }
}