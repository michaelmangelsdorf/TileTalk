package org.swirlsea.tiletalk

import android.app.Application
import org.swirlsea.tiletalk.auth.LoginRegisterUseCase
import org.swirlsea.tiletalk.contacts.ContactsUseCase


class TileTalkApp : Application() {
    lateinit var container: AppContainer
    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}


class AppContainer(application: Application) {

    val repository by lazy { TileTalkRepository() }

    // Make sessionManager public so it can be accessed by the factory
    val sessionManager by lazy { SessionManager(application) }

    // Use cases that can be shared
    val loginRegisterUseCase by lazy { LoginRegisterUseCase(repository, sessionManager) }
    val contactsUseCase by lazy { ContactsUseCase(repository) }
}