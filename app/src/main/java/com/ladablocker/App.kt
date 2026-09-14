package com.ladablocker

import android.app.Application
import com.ladablocker.data.CommunityUpdater
import com.ladablocker.data.Repo

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Repo.init(this)
        CommunityUpdater.schedule(this)
    }
}