package com.ladablocker.data

import com.ladablocker.util.Normalizer

/** Parsea listas en txt/csv/vcf (un número por línea, comas, puntos y comas, o líneas TEL de vCard). */
object ListsImporter {

    fun parse(raw: String): List<ValidNumber> {
        val result = ArrayList<ValidNumber>()
        raw.split('\n', '\r', ',', ';').forEach { token ->
            val trimmed = token.trim()
            if (trimmed.isEmpty()) return@forEach
            val lower = trimmed.lowercase()
            if (lower.startsWith("#") || lower.startsWith("begin:") || lower.startsWith("end:") ||
                lower.startsWith("version:") || lower.startsWith("fn:") || lower.startsWith("n:") ||
                lower.startsWith("contact") || lower.startsWith("telefono") || lower.startsWith("numero")
            ) return@forEach

            var item: String
            if (lower.startsWith("tel:") || lower.startsWith("tel:+") || lower.startsWith("tel;")) {
                item = trimmed.substringAfter(':')
            } else {
                item = trimmed
            }
            AssetsLoader.ensureValid(item)?.let { result.add(it) }
        }
        return result.distinct()
    }
}