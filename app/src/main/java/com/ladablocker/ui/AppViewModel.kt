package com.ladablocker.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.ladablocker.data.Categoria
import com.ladablocker.data.Repo
import com.ladablocker.data.ValidNumber

class AppViewModel(app: Application) : AndroidViewModel(app) {

    val state = Repo.state

    fun toggleLada(code: String) = Repo.toggleLada(code)

    fun addExact(number: String) = Repo.addExact(number)

    fun removeExact(number: String) = Repo.removeExact(number)

    fun addBlanca(number: String) = Repo.addBlanca(number)

    fun removeBlanca(number: String) = Repo.removeBlanca(number)

    fun setContactsBlanca(enable: Boolean) = Repo.setContactsBlanca(enable)

    fun setUnknownBlock(enable: Boolean) = Repo.setUnknownBlock(enable)

    fun setMaster(enable: Boolean) = Repo.setMaster(enable)

    fun setCommunityCategory(category: Categoria, active: Boolean) =
        Repo.setCommunityCategory(category, active)

    fun acceptCommunity(number: String, category: Categoria) =
        Repo.acceptCommunity(number, category)

    fun removeCommunity(number: String, category: Categoria) =
        Repo.removeCommunity(number, category)

    fun importNumbers(numbers: List<ValidNumber>, category: Categoria, source: String, active: Boolean) =
        Repo.importNumbers(numbers, category, source, active)

    fun clearHistory() = Repo.clearHistory()

    fun addUrl(url: String) = Repo.addUrl(url)

    fun removeUrl(url: String) = Repo.removeUrl(url)

    fun updateNow() = Repo.updateNow()

    fun reload() = Repo.reload()
}