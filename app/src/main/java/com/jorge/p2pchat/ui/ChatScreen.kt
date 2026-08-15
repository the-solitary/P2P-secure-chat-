package com.jorge.p2pchat.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ChatScreen(chatId: String, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(chatId) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        // Placeholder — aquí va la lista de mensajes (LazyColumn) + input de texto/audio/multimedia
        Box(Modifier.fillMaxSize().padding(padding)) {
            Text("Chat con $chatId — mensajes en construcción", modifier = Modifier.padding(24.dp))
        }
    }
}
