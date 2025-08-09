package org.swirlsea.tiletalk

import android.app.Application
import org.swirlsea.tiletalk.auth.LoginRegisterUseCase
import org.swirlsea.tiletalk.auth.SessionManager
import org.swirlsea.tiletalk.contacts.ContactsUseCase
import org.swirlsea.tiletalk.data.TileTalkRepository


class TileTalkApp : Application() {
    lateinit var container: AppContainer
    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}


class AppContainer(application: Application) {

    // The fix is here: The constructor now takes no arguments.
    val repository by lazy { TileTalkRepository() }

    val sessionManager by lazy { SessionManager(application) }

    // Use cases that can be shared
    val loginRegisterUseCase by lazy { LoginRegisterUseCase(repository, sessionManager) }
    val contactsUseCase by lazy { ContactsUseCase(repository) }
}