package com.jorge.p2pchat.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ChatListScreen(onOpenChat: (String) -> Unit, onOpenSettings: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chats") },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "Ajustes")
                    }
                }
            )
        }
    ) { padding ->
        // Placeholder — se conecta a Room + StateFlow en el siguiente paso del roadmap
        Box(Modifier.fillMaxSize().padding(padding)) {
            Text(
                "Aún no hay chats. Cuando agreguemos el cache con Room, esta lista se llena sola.",
                modifier = Modifier.padding(24.dp)
            )
        }
    }
}
