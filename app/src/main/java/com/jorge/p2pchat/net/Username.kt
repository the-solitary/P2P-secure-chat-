package com.jorge.p2pchat.net

/**
 * Solo letras, números y guion bajo. Sin espacios ni símbolos que
 * puedan causar problemas al usarse como identificador de red o en
 * los payloads firmados de moderación.
 */
object Username {
    private val VALID_PATTERN = Regex("^[a-zA-Z0-9_]{3,20}$")

    fun isValid(name: String): Boolean = VALID_PATTERN.matches(name)

    fun validationMessage(name: String): String? {
        if (name.length < 3) return "Debe tener al menos 3 caracteres"
        if (name.length > 20) return "Máximo 20 caracteres"
        if (!VALID_PATTERN.matches(name)) return "Solo letras, números y guion bajo (sin espacios ni símbolos)"
        return null
    }
}
