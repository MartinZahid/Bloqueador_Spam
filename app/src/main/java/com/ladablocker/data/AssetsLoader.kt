package com.ladablocker.data

import android.content.Context

object AssetsLoader {

    fun loadLadas(context: Context): List<LadaInfo> = try {
        context.assets.open("ladas_mx.txt").bufferedReader().useLines { lines ->
            lines.mapNotNull { line ->
                val trimmed = line.trim()
                if (trimmed.isEmpty() || trimmed.startsWith("#")) return@mapNotNull null
                val parts = trimmed.split("|", limit = 2)
                if (parts.size != 2) return@mapNotNull null
                val code = parts[0].trim()
                if (code.length < 2) return@mapNotNull null
                LadaInfo(code, parts[1].trim())
            }.toList()
        }
    } catch (e: Exception) {
        emptyList()
    }

    fun loadCategory(context: Context, category: Categoria): List<ValidNumber> = try {
        context.assets.open(category.assetFile).bufferedReader().useLines { lines ->
            lines.mapNotNull { line ->
                val trimmed = line.trim()
                if (trimmed.isEmpty() || trimmed.startsWith("#")) return@mapNotNull null
                ensureValid(trimmed)
            }.toList()
        }
    } catch (e: Exception) {
        emptyList()
    }

    /** Devuelve número normalizado (exacto) o prefijo (mask) ya normalizados, o null si no es válido. */
    fun ensureValid(raw: String): ValidNumber? {
        val isMask = raw.endsWith("*")
        val clean = raw.removeSuffix("*").trim()
        if (clean.isEmpty()) return null
        val digits = clean.filter { it.isDigit() }
        if (digits.length < 7) return null
        val national = if (isMask) {
            if (digits.length > 10) return null
            digits
        } else {
            val n = com.ladablocker.util.Normalizer.normalizeToNational(clean)
            if (n.length > 10) return null
            n
        }
        return ValidNumber(national, mask = isMask || national.length < 10)
    }
}