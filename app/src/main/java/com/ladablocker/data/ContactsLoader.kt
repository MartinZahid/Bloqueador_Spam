package com.ladablocker.data

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.provider.ContactsContract
import androidx.core.content.ContextCompat
import com.ladablocker.util.Normalizer

object ContactsLoader {

    fun hasPermission(context: Context): Boolean =
        ContextCompat.checkSelfPermission(
            context, Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED

    fun load(context: Context): List<String> {
        if (!hasPermission(context)) return emptyList()
        val set = HashSet<String>()
        try {
            context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
                null, null, null
            )?.use { cursor ->
                val idx = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)
                while (cursor.moveToNext()) {
                    val n = Normalizer.normalizeToNational(cursor.getString(idx) ?: "")
                    if (n.length >= 7) set.add(n)
                }
            }
        } catch (e: Exception) {
            // ignorar
        }
        return set.toList()
    }
}