package com.jorge.p2pchat.admin

/**
 * Un reporte SOLO lleva metadata — nunca el contenido del mensaje o
 * chat reportado, para no romper el cifrado del resto de participantes
 * de esa conversación (que no dieron su consentimiento para que su
 * mensaje se comparta con nadie más).
 */
data class UserReport(
    val reportedUserId: String,
    val reason: ReportReason,
    val reporterUserId: String,
    val timestampMillis: Long = System.currentTimeMillis()
)

enum class ReportReason {
    PIRACY,
    GORE,
    NSFW,
    OTHER
}
