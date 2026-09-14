package com.ladablocker.call

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ladablocker.data.CommunityUpdater

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Reagenda la actualización semanal y asegura que sobrevive al reinicio.
            CommunityUpdater.schedule(context)
        }
    }
}