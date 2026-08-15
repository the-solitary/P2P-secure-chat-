package com.jorge.p2pchat.admin

import com.goterl.lazysodium.SodiumAndroid
import java.security.MessageDigest

/**
 * Sistema de moderación SIN acceso a contenido de mensajes.
 *
 * Cómo funciona (pensado para una red P2P sin servidor central):
 *  - Cada admin ("tamarindo", "Movistar") tiene un par de claves Ed25519.
 *  - Para banear a alguien, el admin firma un mensaje: {userId, acción, timestamp}.
 *  - Ese mensaje firmado se propaga por la red igual que cualquier mensaje
 *    (gossip entre peers) — cualquier dispositivo puede verificar la firma
 *    contra la clave pública del admin sin necesitar conectarse a nadie.
 *  - Si la firma es válida, el dispositivo local deja de aceptar conexiones
 *    de ese userId. No hay ningún punto central que "vea" quién está baneado
 *    en tiempo real ni el contenido de sus chats.
 *
 * Los admins NUNCA reciben el contenido de mensajes — solo pueden emitir
 * estas acciones firmadas, y opcionalmente recibir el conteo (no el
 * contenido) de chats/grupos si el usuario lo activó en Ajustes.
 */
class AdminModeration(private val sodium: SodiumAndroid) {

    enum class Action { BAN_USER, UNBAN_USER, DELETE_ACCOUNT }

    data class SignedAction(
        val targetUserId: String,
        val action: Action,
        val timestampMillis: Long,
        val adminPublicKey: ByteArray,
        val signature: ByteArray
    )

    // Claves públicas conocidas de los administradores (se distribuyen
    // junto con la app, NO se descubren dinámicamente por seguridad)
    private val knownAdminKeys = mutableSetOf<ByteArray>()

    fun registerAdminPublicKey(publicKey: ByteArray) {
        knownAdminKeys.add(publicKey)
    }

    /** Firma una acción — solo la ejecuta un dispositivo que tenga la clave secreta del admin */
    fun signAction(
        targetUserId: String,
        action: Action,
        adminSecretKey: ByteArray,
        adminPublicKey: ByteArray
    ): SignedAction {
        val payload = buildPayload(targetUserId, action, System.currentTimeMillis())
        val signature = ByteArray(64)
        sodium.crypto_sign_detached(signature, longArrayOf(64L), payload, payload.size.toLong(), adminSecretKey)
        return SignedAction(targetUserId, action, System.currentTimeMillis(), adminPublicKey, signature)
    }

    /** Cada dispositivo verifica esto localmente antes de aplicar el baneo — sin confiar ciegamente en la red */
    fun verifyAndIsFromKnownAdmin(signed: SignedAction): Boolean {
        val isKnownAdmin = knownAdminKeys.any { it.contentEquals(signed.adminPublicKey) }
        if (!isKnownAdmin) return false

        val payload = buildPayload(signed.targetUserId, signed.action, signed.timestampMillis)
        val result = sodium.crypto_sign_verify_detached(
            signed.signature, payload, payload.size, signed.adminPublicKey
        )
        return result == 0
    }

    private fun buildPayload(userId: String, action: Action, timestamp: Long): ByteArray {
        val raw = "$userId|${action.name}|$timestamp".toByteArray()
        return MessageDigest.getInstance("SHA-256").digest(raw)
    }
}
