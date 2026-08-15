package com.jorge.p2pchat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.goterl.lazysodium.SodiumAndroid
import com.jorge.p2pchat.crypto.NoiseXX
import com.jorge.p2pchat.net.UdpTransport
import kotlinx.coroutines.launch
import java.net.InetAddress

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sodium = SodiumAndroid()
        val transport = UdpTransport(localPort = 45454)
        transport.start(lifecycleScope)

        setContent {
            MaterialTheme {
                var peerIp by remember { mutableStateOf("") }
                var log by remember { mutableStateOf("Listo. Ingresa la IP del otro dispositivo.") }
                val scope = rememberCoroutineScope()

                Column(Modifier.fillMaxSize().padding(24.dp)) {
                    Text("P2P Secure Chat — prueba de handshake", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = peerIp,
                        onValueChange = { peerIp = it },
                        label = { Text("IP del otro dispositivo") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = {
                        scope.launch {
                            try {
                                val noise = NoiseXX(sodium)
                                val staticKeys = noise.generateKeyPair()
                                noise.init(staticKeys)

                                val msg1 = noise.writeMessage1()
                                transport.send(msg1, InetAddress.getByName(peerIp), 45454)
                                log = "Mensaje 1 (e) enviado. Esperando respuesta..."
                            } catch (e: Exception) {
                                log = "Error: ${e.message}"
                            }
                        }
                    }, modifier = Modifier.fillMaxWidth()) {
                        Text("Iniciar handshake")
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(log)
                }
            }
        }
    }
}
