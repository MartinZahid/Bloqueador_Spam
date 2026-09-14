package com.ladablocker.call

import android.telecom.Call
import android.telecom.CallScreeningService
import com.ladablocker.data.Repo
import com.ladablocker.util.Normalizer

class SpamCallScreen : CallScreeningService() {

    override fun onScreenCall(callDetails: Call.Details) {
        val raw = callDetails.handle?.schemeSpecificPart
        val national = raw?.let { Normalizer.normalizeToNational(it) } ?: ""
        val (blocked, rule) = BlockerCache.shouldBlock(national)
        if (blocked) {
            Repo.addBlockedCall(raw ?: "(oculto)", rule)
            try {
                val response = CallScreeningService.CallResponse.Builder()
                    .setRejectCall(true)
                    .setSilenceCall(true)
                    .setSkipCallLog(true)
                    .setSkipNotification(true)
                    .build()
                respondToCall(callDetails, response)
            } catch (e: IllegalStateException) {
                // la llamada ya terminó
            }
        }
        // Si no se bloquea, no respondemos: Android deja entrar la llamada con normalidad.
    }
}