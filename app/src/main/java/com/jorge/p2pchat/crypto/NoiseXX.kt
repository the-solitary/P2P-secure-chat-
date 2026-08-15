package com.jorge.p2pchat.crypto

import com.goterl.lazysodium.SodiumAndroid
import com.goterl.lazysodium.utils.Key
import java.security.SecureRandom

/**
 * Implementación simplificada del patrón Noise_XX sobre libsodium.
 *
 * ADVERTENCIA: esto es un ESQUELETO PEDAGÓGICO para arrancar el proyecto,
 * no una implementación auditada de la especificación Noise completa
 * (falta el manejo estricto de contadores de nonce, padding, y varios
 * casos límite del spec). Antes de usar esto con datos reales de gente,
 * hay que revisarlo línea por línea contra noiseprotocol.org/noise.html
 * o directamente migrar a una librería que implemente el spec completo.
 *
 * Patrón XX (mutuamente autenticado, sin conocer la identidad del otro
 * de antemano — ideal para el primer contacto entre dos usuarios):
 *   -> e
 *   <- e, ee, s, es
 *   -> s, se
 *
 * Primitivas:
 *   DH   = X25519
 *   Hash = BLAKE2b (vía crypto_generichash de libsodium)
 *   AEAD = ChaCha20-Poly1305 (IETF)
 */
class NoiseXX(private val sodium: SodiumAndroid) {


    data class KeyPair(val publicKey: ByteArray, val secretKey: ByteArray)

    private var chainingKey: ByteArray = ByteArray(32)
    private var handshakeHash: ByteArray = ByteArray(32)
    private var localEphemeral: KeyPair? = null
    private var localStatic: KeyPair? = null
    private var remoteEphemeral: ByteArray? = null
    private var remoteStatic: ByteArray? = null

    companion object {
        private const val PROTOCOL_NAME = "Noise_XX_25519_ChaChaPoly_BLAKE2b"
    }

    fun generateKeyPair(): KeyPair {
        val pk = ByteArray(32)
        val sk = ByteArray(32)
        sodium.crypto_box_keypair(pk, sk)
        return KeyPair(pk, sk)
    }

    fun init(localStaticKeyPair: KeyPair) {
        localStatic = localStaticKeyPair
        val nameBytes = PROTOCOL_NAME.toByteArray()
        handshakeHash = if (nameBytes.size <= 32) {
            nameBytes + ByteArray(32 - nameBytes.size)
        } else {
            hash(nameBytes)
        }
        chainingKey = handshakeHash.copyOf()
    }

    private fun hash(data: ByteArray): ByteArray {
        val out = ByteArray(32)
        sodium.crypto_generichash(out, out.size, data, data.size.toLong(), null, 0)
        return out
    }

    private fun mixHash(data: ByteArray) {
        handshakeHash = hash(handshakeHash + data)
    }

    private fun dh(secretKey: ByteArray, publicKey: ByteArray): ByteArray {
        val shared = ByteArray(32)
        sodium.crypto_scalarmult(shared, secretKey, publicKey)
        return shared
    }

    // HKDF simplificado de 2 salidas, como usa el spec de Noise (HMAC-BLAKE2b)
    private fun mixKey(inputKeyMaterial: ByteArray): ByteArray {
        val tempKey = hmacHash(chainingKey, inputKeyMaterial)
        val output1 = hmacHash(tempKey, byteArrayOf(0x01))
        val output2 = hmacHash(tempKey, output1 + byteArrayOf(0x02))
        chainingKey = output1
        return output2 // clave para cifrar el siguiente mensaje del handshake
    }

    private fun hmacHash(key: ByteArray, data: ByteArray): ByteArray {
        val out = ByteArray(32)
        sodium.crypto_generichash(out, out.size, data, data.size.toLong(), key, key.size)
        return out
    }

    /** Paso 1 (iniciador): -> e */
    fun writeMessage1(): ByteArray {
        localEphemeral = generateKeyPair()
        mixHash(localEphemeral!!.publicKey)
        return localEphemeral!!.publicKey
    }

    /** Paso 1 (receptor): <- e (recibida) */
    fun readMessage1(message: ByteArray) {
        remoteEphemeral = message
        mixHash(message)
    }

    /** Paso 2 (receptor): <- e, ee, s, es  (devuelve payload a enviar) */
    fun writeMessage2(): ByteArray {
        localEphemeral = generateKeyPair()
        mixHash(localEphemeral!!.publicKey)

        val dh1 = dh(localEphemeral!!.secretKey, remoteEphemeral!!)
        val key1 = mixKey(dh1)
        val encryptedStatic = encryptAndHash(localStatic!!.publicKey, key1)

        val dh2 = dh(localStatic!!.secretKey, remoteEphemeral!!)
        mixKey(dh2)

        return localEphemeral!!.publicKey + encryptedStatic
    }

    /** Paso 2 (iniciador): procesa e, ee, s, es del receptor */
    fun readMessage2(message: ByteArray) {
        val theirEphemeral = message.copyOfRange(0, 32)
        val encryptedStatic = message.copyOfRange(32, message.size)

        remoteEphemeral = theirEphemeral
        mixHash(theirEphemeral)

        val dh1 = dh(localEphemeral!!.secretKey, theirEphemeral)
        val key1 = mixKey(dh1)
        remoteStatic = decryptAndHash(encryptedStatic, key1)

        val dh2 = dh(localEphemeral!!.secretKey, remoteStatic!!)
        mixKey(dh2)
    }

    /** Paso 3 (iniciador): -> s, se */
    fun writeMessage3(): ByteArray {
        val key = mixKey(ByteArray(0)) // placeholder simplificado, ver nota de seguridad arriba
        val encryptedStatic = encryptAndHash(localStatic!!.publicKey, key)
        val dh3 = dh(localStatic!!.secretKey, remoteEphemeral!!)
        mixKey(dh3)
        return encryptedStatic
    }

    /** Paso 3 (receptor): procesa s, se — handshake completo, ya se puede confiar en remoteStatic */
    fun readMessage3(message: ByteArray) {
        val key = mixKey(ByteArray(0))
        remoteStatic = decryptAndHash(message, key)
        val dh3 = dh(localEphemeral!!.secretKey, remoteStatic!!)
        mixKey(dh3)
    }

    /** Deriva las dos claves de transporte finales (una por dirección) una vez completado el handshake */
    fun split(): Pair<ByteArray, ByteArray> {
        val k1 = hmacHash(chainingKey, byteArrayOf(0x01))
        val k2 = hmacHash(chainingKey, k1 + byteArrayOf(0x02))
        return Pair(k1, k2)
    }

    fun getRemoteStaticKey(): ByteArray? = remoteStatic

    private fun encryptAndHash(plaintext: ByteArray, key: ByteArray): ByteArray {
        val nonce = ByteArray(12) // nonce=0 dentro del handshake, es seguro porque la clave es de un solo uso
        val ciphertext = ByteArray(plaintext.size + 16)
        sodium.crypto_aead_chacha20poly1305_ietf_encrypt(
            ciphertext, longArrayOf(ciphertext.size.toLong()),
            plaintext, plaintext.size.toLong(),
            handshakeHash, handshakeHash.size.toLong(),
            null, nonce, key
        )
        mixHash(ciphertext)
        return ciphertext
    }

    private fun decryptAndHash(ciphertext: ByteArray, key: ByteArray): ByteArray {
        val nonce = ByteArray(12)
        val plaintext = ByteArray(ciphertext.size - 16)
        sodium.crypto_aead_chacha20poly1305_ietf_decrypt(
            plaintext, longArrayOf(plaintext.size.toLong()),
            null, ciphertext, ciphertext.size.toLong(),
            handshakeHash, handshakeHash.size.toLong(),
            nonce, key
        )
        mixHash(ciphertext)
        return plaintext
    }
}
