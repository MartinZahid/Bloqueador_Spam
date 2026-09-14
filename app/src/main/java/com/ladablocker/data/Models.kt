package com.ladablocker.data

enum class Categoria(val label: String, val assetFile: String) {
    COBRANZA("Cobranza / deudas", "lista_cobranza.txt"),
    TELEMARKETING("Telemarketing / spam", "lista_telemarketing.txt"),
    FRAUDE("Fraude / extorsión", "lista_fraude.txt");

    companion object {
        fun fromFileName(name: String): Categoria? =
            entries.firstOrNull { it.assetFile == name }
    }
}

data class LadaInfo(val code: String, val name: String)

data class CommunityNumber(
    val number: String,
    val category: Categoria,
    val active: Boolean,
    val source: String,
    val mask: Boolean
)

data class BlockedCall(
    val number: String,
    val rule: String,
    val time: Long
)

data class AppState(
    val ladas: List<LadaInfo> = emptyList(),
    val activeLadas: Set<String> = emptySet(),
    val exactos: List<String> = emptyList(),
    val blanca: List<String> = emptyList(),
    val contacts: List<String> = emptyList(),
    val contactsBlanca: Boolean = true,
    val unknownBlock: Boolean = false,
    val masterEnabled: Boolean = true,
    val community: List<CommunityNumber> = emptyList(),
    val history: List<BlockedCall> = emptyList(),
    val updateUrls: List<String> = emptyList(),
    val lastUpdate: Long = 0,
    val contactsPermission: Boolean = false
) {
    val pendingReview: Int
        get() = community.count { !it.active }
}