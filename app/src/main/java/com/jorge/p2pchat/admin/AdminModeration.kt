package com.jorge.p2pchat.admin

import com.goterl.lazysodium.LazySodiumAndroid
import com.goterl.lazysodium.SodiumAndroid
import com.goterl.lazysodium.utils.Key
import java.security.MessageDigest

class AdminModeration(sodium: SodiumAndroid) {

    private val lazySodium = LazySodiumAndroid(sodium)

    enum class Action { BAN_USER, UNBAN_USER, DELETE_ACCOUNT }

    data class SignedAction(
        val targetUserId: String,
        val action: Action,
        val timestampMillis: Long,
        val adminPublicKey: ByteArray,
        val signature: ByteArray
    )

    private val knownAdminKeys = mutableSetOf<ByteArray>()

    fun registerAdminPublicKey(publicKey: ByteArray) {
        knownAdminKeys.add(publicKey)
    }

    fun signAction(
        targetUserId: String,
        action: Action,
        adminSecretKey: ByteArray,
        adminPublicKey: ByteArray
    ): SignedAction {
        val timestamp = System.currentTimeMillis()
        val payloadHex = buildPayloadHex(targetUserId, action, timestamp)
        val secretKey = Key.fromBytes(adminSecretKey)

        val signatureHex = lazySodium.cryptoSignDetached(payloadHex, secretKey)
        val signature = Key.fromHexString(signatureHex).asBytes

        return SignedAction(targetUserId, action, timestamp, adminPublicKey, signature)
    }

    fun verifyAndIsFromKnownAdmin(signed: SignedAction): Boolean {
        val isKnownAdmin = knownAdminKeys.any { it.contentEquals(signed.adminPublicKey) }
        if (!isKnownAdmin) return false

        val payloadHex = buildPayloadHex(signed.targetUserId, signed.action, signed.timestampMillis)
        val signatureHex = Key.fromBytes(signed.signature).asHexString
        val publicKey = Key.fromBytes(signed.adminPublicKey)

        return lazySodium.cryptoSignVerifyDetached(signatureHex, payloadHex, publicKey)
    }

    private fun buildPayloadHex(userId: String, action: Action, timestamp: Long): String {
        val raw = "$userId|${action.name}|$timestamp".toByteArray()
        val hash = MessageDigest.getInstance("SHA-256").digest(raw)
        return hash.joinToString("") { "%02x".format(it) }
    }
}
