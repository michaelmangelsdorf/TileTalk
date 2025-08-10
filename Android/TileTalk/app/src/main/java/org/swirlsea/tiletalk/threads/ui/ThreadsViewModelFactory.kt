package org.swirlsea.tiletalk.threads.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.swirlsea.tiletalk.AuthViewModel
import org.swirlsea.tiletalk.TileTalkApp

class ThreadsViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ThreadsViewModel::class.java)) {
            val app = application as TileTalkApp
            val repository = app.container.repository
            val sessionManager = app.container.sessionManager
            val loginRegisterUseCase = app.container.loginRegisterUseCase
            val authViewModel = AuthViewModel(application, loginRegisterUseCase, repository, sessionManager)

            @Suppress("UNCHECKED_CAST")
            return ThreadsViewModel(application, repository, authViewModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}