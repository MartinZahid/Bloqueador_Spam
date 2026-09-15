package com.ladablocker.util

object Normalizer {

    fun digitsOnly(raw: String): String = raw.filter { it.isDigit() }

    /** Normaliza a número nacional de 10 dígitos (México). */
    fun normalizeToNational(raw: String): String {
        var s = digitsOnly(raw)
        if (s.isEmpty()) return s
        when {
            s.length >= 13 && s.startsWith("521") -> s = s.drop(3)
            s.length == 13 && (s.startsWith("044") || s.startsWith("045")) -> s = s.drop(3)
            s.length == 12 && s.startsWith("52") -> s = s.drop(2)
            s.length == 12 && s.startsWith("01") -> s = s.drop(2)
            s.length == 11 && s.startsWith("1") -> s = s.drop(1)
        }
        return s
    }
}