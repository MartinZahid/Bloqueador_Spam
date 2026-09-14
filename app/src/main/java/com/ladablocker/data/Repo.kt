package com.ladablocker.data

import android.content.Context
import com.ladablocker.call.BlockerCache
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

object Repo {

    private lateinit var context: Context
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _state = MutableStateFlow(AppState())
    val state: StateFlow<AppState> = _state.asStateFlow()

    fun init(ctx: Context) {
        context = ctx.applicationContext
        Prefs.init(context, scope)
        val ladas = AssetsLoader.loadLadas(context)
        _state.update { it.copy(ladas = ladas) }
        scope.launch {
            seedIfNeeded(context)
            refresh()
        }
    }

    private suspend fun seedIfNeeded(ctx: Context) {
        var p = Prefs.read()
        if (p.seeded) return
        val list = ArrayList<CommunityNumber>()
        Categoria.entries.forEach { cat ->
            AssetsLoader.loadCategory(ctx, cat).forEach { n ->
                list.add(CommunityNumber(n, cat, active = false, source = cat.assetFile, mask = n.length < 10))
            }
        }
        val ladas = p.activeLadas.toMutableSet()
        if (ladas.isEmpty()) ladas.add("637")
        // Aplicar por lotes para no guardar comunidades gigantes de golpe.
        p = p.copy(activeLadas = ladas, community = mergeCommunity(list, p.community), seeded = true)
        Prefs.save(p)
    }

    fun reload() {
        scope.launch { refresh() }
    }

    private suspend fun refresh() {
        val p = Prefs.read()
        val contacts = if (p.contactsBlanca) ContactsLoader.load(context) else emptyList()
        val permission = ContactsLoader.hasPermission(context)
        _state.update {
            it.copy(
                activeLadas = p.activeLadas,
                exactos = p.exactos,
                blanca = p.blanca,
                contactsBlanca = p.contactsBlanca,
                unknownBlock = p.unknownBlock,
                masterEnabled = p.masterEnabled,
                community = p.community,
                history = p.history,
                updateUrls = p.updateUrls,
                lastUpdate = p.lastUpdate,
                contacts = contacts,
                contactsPermission = permission
            )
        }
        BlockerCache.refresh(_state.value)
    }

    private suspend fun mutate(f: (PrefsSnapshot) -> PrefsSnapshot) {
        val p = Prefs.read()
        Prefs.save(f(p))
        refresh()
    }

    fun toggleLada(code: String) {
        scope.launch {
            mutate { p ->
                val set = p.activeLadas.toMutableSet()
                if (!set.add(code)) set.remove(code)
                p.copy(activeLadas = set)
            }
        }
    }

    fun addExact(number: String) {
        val n = com.ladablocker.util.Normalizer.normalizeToNational(number)
        if (n.length != 10) return
        scope.launch {
            mutate { p ->
                if (p.exactos.contains(n)) p else p.copy(exactos = p.exactos + n)
            }
        }
    }

    fun removeExact(number: String) {
        scope.launch {
            mutate { p -> p.copy(exactos = p.exactos - number) }
        }
    }

    fun addBlanca(number: String) {
        val n = com.ladablocker.util.Normalizer.normalizeToNational(number)
        if (n.length < 7) return
        scope.launch {
            mutate { p ->
                if (p.blanca.contains(n)) p else p.copy(blanca = p.blanca + n)
            }
        }
    }

    fun removeBlanca(number: String) {
        scope.launch {
            mutate { p -> p.copy(blanca = p.blanca - number) }
        }
    }

    fun setContactsBlanca(enable: Boolean) {
        scope.launch {
            mutate { p -> p.copy(contactsBlanca = enable) }
        }
    }

    fun setUnknownBlock(enable: Boolean) {
        scope.launch {
            mutate { p -> p.copy(unknownBlock = enable) }
        }
    }

    fun setMaster(enable: Boolean) {
        scope.launch {
            mutate { p -> p.copy(masterEnabled = enable) }
        }
    }

    /** Activa/desactiva toda una categoría comunitaria (activa = bloquear). */
    fun setCommunityCategory(category: Categoria, active: Boolean) {
        scope.launch {
            mutate { p ->
                val list = p.community.map { if (it.category == category) it.copy(active = active) else it }
                p.copy(community = list)
            }
        }
    }

    fun acceptCommunity(number: String, category: Categoria) {
        scope.launch {
            mutate { p ->
                val list = p.community.map {
                    if (it.category == category && it.number == number) it.copy(active = true) else it
                }
                p.copy(community = list)
            }
        }
    }

    fun removeCommunity(number: String, category: Categoria) {
        scope.launch {
            mutate { p ->
                p.copy(community = p.community.filterNot { it.category == category && it.number == number })
            }
        }
    }

    /** Agrega números importados/descargados como pendientes de revisión (active = false). */
    fun importNumbers(numbers: List<String>, category: Categoria, source: String, active: Boolean) {
        if (numbers.isEmpty()) return
        scope.launch {
            mutate { p ->
                val items = numbers.map {
                    CommunityNumber(it, category, active = active, source = source, mask = it.length < 10)
                }
                p.copy(community = mergeCommunity(items, p.community))
            }
        }
    }

    private fun mergeCommunity(new: List<CommunityNumber>, existing: List<CommunityNumber>): List<CommunityNumber> {
        val map = LinkedHashMap<String, CommunityNumber>()
        existing.forEach { map[it.category.name + "|" + it.number] = it }
        new.forEach { map.putIfAbsent(it.category.name + "|" + it.number, it) }
        return map.values.toList().sortedWith(compareBy({ it.category.ordinal }, { it.number }))
    }

    fun addBlockedCall(number: String, rule: String) {
        scope.launch {
            val now = System.currentTimeMillis()
            mutate { p ->
                val h = (listOf(BlockedCall(number, rule, now)) + p.history).take(300)
                p.copy(history = h)
            }
        }
    }

    fun clearHistory() {
        scope.launch {
            mutate { p -> p.copy(history = emptyList()) }
        }
    }

    fun addUrl(url: String) {
        val clean = url.trim()
        if (clean.isEmpty()) return
        scope.launch {
            mutate { p ->
                if (p.updateUrls.contains(clean)) p else p.copy(updateUrls = p.updateUrls + clean)
            }
        }
    }

    fun removeUrl(url: String) {
        scope.launch {
            mutate { p -> p.copy(updateUrls = p.updateUrls - url) }
        }
    }

    fun markLastUpdate() {
        scope.launch {
            mutate { p -> p.copy(lastUpdate = System.currentTimeMillis()) }
        }
    }

    fun updateNow() {
        CommunityUpdater.runNow(context)
    }
}