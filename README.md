# P2P Secure Chat

App Android de mensajería P2P sin servidor central, cifrada de extremo a
extremo, pensada para chats 1:1 y grupos pequeños, con soporte para
muchos usuarios usando la app en simultáneo (cada uno maneja solo su
propio tráfico, sin cuello de botella central).

## Estado actual: Paso 1 del roadmap

Este commit inicial cubre el **paso 1**: handshake criptográfico
(Noise_XX simplificado) + transporte UDP crudo, pensado para probarse
primero entre dos dispositivos en la misma red local (sin STUN todavía).

⚠️ **`NoiseXX.kt` es un esqueleto pedagógico**, no una implementación
auditada del spec completo de Noise. Antes de manejar datos reales hay
que revisarlo contra la especificación oficial
(https://noiseprotocol.org/noise.html) o migrar a una librería que la
implemente al 100%.

## Roadmap completo

1. ✅ Handshake Noise + mensajería UDP 1:1 en red local
2. ⬜ STUN + UDP hole punching (conexión entre redes distintas, ej. México ↔ El Salvador)
3. ⬜ Foreground Service para persistencia en segundo plano
4. ⬜ Sender Keys + lógica de grupos
5. ⬜ Gossip para reenvío de mensajes cuando el destinatario está offline
6. ⬜ Cache local con Room + UI reactiva en Compose (LazyColumn + Coil para media)

## Stack

- **Lenguaje:** Kotlin
- **Cripto:** libsodium vía `lazysodium-android` (X25519, ChaCha20-Poly1305, BLAKE2b)
- **Transporte:** UDP crudo (`java.net.DatagramSocket`)
- **UI:** Jetpack Compose (ligera, sin XML)
- **Cache:** Room (SQLite) + StateFlow

## Cómo probar el skeleton actual

1. Abrir en Android Studio, sincronizar Gradle
2. Instalar en dos dispositivos conectados a la **misma red WiFi**
3. En cada uno, escribir la IP local del otro y presionar "Iniciar handshake"
4. Revisar Logcat — el mensaje 1 (`e`) debería llegar al otro dispositivo

Nota: el flujo completo del handshake (mensajes 2 y 3, y el envío de
texto cifrado) todavía no está conectado a la UI — está implementado en
`NoiseXX.kt` pero falta el loop que escucha `transport.incoming` y
completa los 3 pasos automáticamente. Ese es el siguiente pedazo a
construir.
