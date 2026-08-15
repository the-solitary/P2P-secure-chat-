package com.jorge.p2pchat.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jorge.p2pchat.settings.SettingsStore
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(settingsStore: SettingsStore) {
    val scope = rememberCoroutineScope()
    val isDark by settingsStore.isDarkTheme.collectAsState(initial = false)
    val shareCount by settingsStore.shareActivityCount.collectAsState(initial = false)

    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Text("Ajustes", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(24.dp))

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text("Modo oscuro")
            }
            Switch(checked = isDark, onCheckedChange = {
                scope.launch { settingsStore.setDarkTheme(it) }
            })
        }

        Spacer(Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(Modifier.height(16.dp))

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text("Mostrar cantidad de chats/grupos al administrador")
                Text(
                    "Solo el número, nunca el contenido. Apagado por defecto.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Switch(checked = shareCount, onCheckedChange = {
                scope.launch { settingsStore.setShareActivityCount(it) }
            })
        }
    }
}
