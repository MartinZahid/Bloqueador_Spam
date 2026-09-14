package com.ladablocker.call

import com.ladablocker.data.AppState
import com.ladablocker.util.Normalizer

/**
 * Copia en memoria de todas las reglas, para que el CallScreeningService conteste en microsegundos
 * sin leer de disco por cada llamada. Vive en el proceso de la app y se refresca al cambiar algo.
 */
object BlockerCache {

    @Volatile private var whitelist: Set<String> = emptySet()
    @Volatile private var exactos: Set<String> = emptySet()
    @Volatile private var community: Set<String> = emptySet()
    @Volatile private var masks: List<Pair<String, String>> = emptyList()
    @Volatile private var ladas: Set<String> = emptySet()
    @Volatile private var unknownBlock: Boolean = false
    @Volatile private var masterEnabled: Boolean = true

    @Synchronized
    fun refresh(st: AppState) {
        val w = HashSet<String>()
        w.addAll(st.blanca)
        w.addAll(st.contacts)
        whitelist = w

        exactos = st.exactos.toSet()

        val c = HashSet<String>()
        val m = ArrayList<Pair<String, String>>()
        st.community.filter { it.active }.forEach { cn ->
            if (cn.mask) m.add(cn.number to cn.category.label) else c.add(cn.number)
        }
        community = c
        masks = m

        ladas = st.activeLadas.map { Normalizer.digitsOnly(it) }.filter { it.isNotEmpty() }.toSet()
        unknownBlock = st.unknownBlock
        masterEnabled = st.masterEnabled
    }

    @Synchronized
    fun shouldBlock(national: String): Pair<Boolean, String> {
        if (!masterEnabled) return false to ""
        if (national.isNotEmpty() && national in whitelist) return false to ""
        if (national.isEmpty()) {
            return if (unknownBlock) true to "Número oculto" else false to ""
        }
        if (national in exactos) return true to "Número exacto"
        if (national in community) return true to "Base comunitaria"
        for ((prefix, label) in masks) {
            if (national.startsWith(prefix)) return true to label
        }
        for (l in ladas) {
            if (national.startsWith(l)) return true to "Lada $l"
        }
        return false to ""
    }
}