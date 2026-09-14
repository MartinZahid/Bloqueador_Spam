package com.ladablocker.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject

data class PrefsSnapshot(
    val activeLadas: Set<String> = emptySet(),
    val exactos: List<String> = emptyList(),
    val blanca: List<String> = emptyList(),
    val contactsBlanca: Boolean = true,
    val unknownBlock: Boolean = false,
    val masterEnabled: Boolean = true,
    val community: List<CommunityNumber> = emptyList(),
    val history: List<BlockedCall> = emptyList(),
    val updateUrls: List<String> = emptyList(),
    val lastUpdate: Long = 0,
    val seeded: Boolean = false
)

object Prefs {

    private const val FILE_NAME = "ladablocker_prefs"
    private val KEY_LADAS = stringPreferencesKey("ladas")
    private val KEY_EXACTOS = stringPreferencesKey("exactos")
    private val KEY_BLANCA = stringPreferencesKey("blanca")
    private val KEY_CONTACTS_BLANCA = booleanPreferencesKey("contacts_blanca")
    private val KEY_UNKNOWN_BLOCK = booleanPreferencesKey("unknown_block")
    private val KEY_MASTER = booleanPreferencesKey("master_enabled")
    private val KEY_COMMUNITY = stringPreferencesKey("community_json")
    private val KEY_HISTORY = stringPreferencesKey("history_json")
    private val KEY_URLS = stringPreferencesKey("update_urls")
    private val KEY_LAST_UPDATE = longPreferencesKey("last_update")
    private val KEY_SEEDED = booleanPreferencesKey("seeded")

    private var dataStore: androidx.datastore.core.DataStore<Preferences>? = null

    fun init(context: Context, scope: CoroutineScope) {
        if (dataStore != null) return
        dataStore = PreferenceDataStoreFactory.create(
            scope = scope,
            produceFile = { context.preferencesDataStoreFile(FILE_NAME) }
        )
    }

    private fun ds(): androidx.datastore.core.DataStore<Preferences> =
        checkNotNull(dataStore) { "Prefs.init(context) debe llamarse primero" }

    private fun encodeSet(list: List<String>): String = list.joinToString("|")

    private fun decodeSet(raw: String?): List<String> =
        raw?.split("|")?.filter { it.isNotBlank() } ?: emptyList()

    private fun communityToJson(list: List<CommunityNumber>): String {
        val arr = JSONArray()
        list.forEach {
            val o = JSONObject()
                .put("n", it.number)
                .put("c", it.category.name)
                .put("a", it.active)
                .put("m", it.mask)
                .put("s", it.source)
            arr.put(o)
        }
        return arr.toString()
    }

    private fun communityFromJson(raw: String): List<CommunityNumber> = try {
        val arr = JSONArray(raw)
        (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            CommunityNumber(
                number = o.getString("n"),
                category = runCatching { Categoria.valueOf(o.getString("c")) }
                    .getOrDefault(Categoria.TELEMARKETING),
                active = o.optBoolean("a"),
                source = o.optString("s"),
                mask = o.optBoolean("m")
            )
        }
    } catch (e: Exception) {
        emptyList()
    }

    private fun historyToJson(list: List<BlockedCall>): String {
        val arr = JSONArray()
        list.forEach {
            arr.put(JSONObject().put("n", it.number).put("r", it.rule).put("t", it.time))
        }
        return arr.toString()
    }

    private fun historyFromJson(raw: String): List<BlockedCall> = try {
        val arr = JSONArray(raw)
        (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            BlockedCall(number = o.getString("n"), rule = o.getString("r"), time = o.optLong("t"))
        }
    } catch (e: Exception) {
        emptyList()
    }

    suspend fun read(): PrefsSnapshot {
        val p = ds().data.first()
        return PrefsSnapshot(
            activeLadas = decodeSet(p[KEY_LADAS]).toSet(),
            exactos = decodeSet(p[KEY_EXACTOS]),
            blanca = decodeSet(p[KEY_BLANCA]),
            contactsBlanca = p[KEY_CONTACTS_BLANCA] ?: true,
            unknownBlock = p[KEY_UNKNOWN_BLOCK] ?: false,
            masterEnabled = p[KEY_MASTER] ?: true,
            community = communityFromJson(p[KEY_COMMUNITY] ?: "[]"),
            history = historyFromJson(p[KEY_HISTORY] ?: "[]"),
            updateUrls = decodeSet(p[KEY_URLS]),
            lastUpdate = p[KEY_LAST_UPDATE] ?: 0L,
            seeded = p[KEY_SEEDED] ?: false
        )
    }

    suspend fun save(s: PrefsSnapshot) {
        ds().edit { p ->
            p[KEY_LADAS] = encodeSet(s.activeLadas.toList())
            p[KEY_EXACTOS] = encodeSet(s.exactos)
            p[KEY_BLANCA] = encodeSet(s.blanca)
            p[KEY_CONTACTS_BLANCA] = s.contactsBlanca
            p[KEY_UNKNOWN_BLOCK] = s.unknownBlock
            p[KEY_MASTER] = s.masterEnabled
            p[KEY_COMMUNITY] = communityToJson(s.community)
            p[KEY_HISTORY] = historyToJson(s.history)
            p[KEY_URLS] = encodeSet(s.updateUrls)
            p[KEY_LAST_UPDATE] = s.lastUpdate
            p[KEY_SEEDED] = s.seeded
        }
    }
}