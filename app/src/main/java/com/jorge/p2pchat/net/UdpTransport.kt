package com.jorge.p2pchat.net

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

/**
 * Paso 1 del roadmap: transporte UDP crudo.
 * Sin STUN/hole-punching todavía — pensado para probar el handshake
 * Noise entre dos dispositivos en la MISMA red local primero.
 * El paso 2 (STUN + hole punching) se agrega encima de esto sin
 * tocar esta clase, solo cambiando qué IP:puerto se usa como destino.
 */
class UdpTransport(private val localPort: Int) {

    private var socket: DatagramSocket? = null
    val incoming = Channel<ReceivedPacket>(capacity = Channel.UNLIMITED)

    data class ReceivedPacket(val data: ByteArray, val fromAddress: InetAddress, val fromPort: Int)

    fun start(scope: CoroutineScope) {
        socket = DatagramSocket(localPort)
        scope.launch(Dispatchers.IO) {
            val buffer = ByteArray(2048) // suficiente para mensajes de texto + overhead de cifrado
            while (true) {
                val packet = DatagramPacket(buffer, buffer.size)
                socket?.receive(packet) ?: break
                val data = packet.data.copyOfRange(0, packet.length)
                incoming.trySend(ReceivedPacket(data, packet.address, packet.port))
            }
        }
    }

    suspend fun send(data: ByteArray, address: InetAddress, port: Int) {
        kotlinx.coroutines.withContext(Dispatchers.IO) {
            val packet = DatagramPacket(data, data.size, address, port)
            socket?.send(packet)
        }
    }

    fun close() {
        socket?.close()
    }
}
