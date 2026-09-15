package com.ladablocker.data

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class CommunityUpdaterWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val p = Prefs.read()
        if (p.updateUrls.isEmpty()) return Result.success()
        var imported = false
        for (url in p.updateUrls) {
            runCatching {
                val text = withContext(Dispatchers.IO) { fetchLimited(url, MAX_DOWNLOAD_BYTES) }
                    ?: return@runCatching
                val numbers = ListsImporter.parse(text)
                if (numbers.isNotEmpty()) {
                    Repo.importNumbers(numbers, Categoria.TELEMARKETING, url, active = false)
                    imported = true
                }
            }
        }
        if (imported) Repo.markLastUpdate()
        return Result.success()
    }

    private fun fetchLimited(url: String, maxBytes: Int): String? {
        val conn = java.net.URL(url).openConnection()
        return try {
            val ins = conn.getInputStream()
            try {
                val out = java.io.ByteArrayOutputStream()
                val buf = ByteArray(8192)
                var total = 0
                while (true) {
                    val r = ins.read(buf)
                    if (r < 0) break
                    total += r
                    if (total > maxBytes) return null
                    out.write(buf, 0, r)
                }
                out.toString(Charsets.UTF_8.name())
            } finally {
                ins.close()
            }
        } finally {
            (conn as? java.net.HttpURLConnection)?.disconnect()
        }
    }
}

private const val MAX_DOWNLOAD_BYTES = 5 * 1024 * 1024

object CommunityUpdater {

    private const val UNIQUE_PERIODIC = "actualizar-listas"
    private const val UNIQUE_NOW = "actualizar-listas-ahora"

    fun schedule(context: Context) {
        val request = PeriodicWorkRequestBuilder<CommunityUpdaterWorker>(7, TimeUnit.DAYS)
            .setConstraints(
                Constraints.Builder().setRequiredNetworkType(NetworkType.UNMETERED).build()
            )
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            UNIQUE_PERIODIC, ExistingPeriodicWorkPolicy.KEEP, request
        )
    }

    fun runNow(context: Context) {
        val request = OneTimeWorkRequestBuilder<CommunityUpdaterWorker>()
            .setConstraints(
                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
            )
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            UNIQUE_NOW, ExistingWorkPolicy.REPLACE, request
        )
    }
}