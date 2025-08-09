package org.swirlsea.tiletalk.threads.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.swirlsea.tiletalk.TileTalkApp

class ThreadsViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ThreadsViewModel::class.java)) {
            val app = application as TileTalkApp
            val repository = app.container.repository
            @Suppress("UNCHECKED_CAST")
            // The fix is here: pass both the application and repository
            return ThreadsViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}