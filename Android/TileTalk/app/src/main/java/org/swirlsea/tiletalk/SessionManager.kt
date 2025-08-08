package org.swirlsea.tiletalk

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Manages user credentials securely using EncryptedSharedPreferences.
 * This class handles saving, retrieving, and clearing the user's username and password.
 */
class SessionManager(context: Context) {

    private val prefs: SharedPreferences

    companion object {
        private const val FILENAME = "secure_user_prefs"
        private const val USERNAME_KEY = "username_key"
        private const val PASSWORD_KEY = "password_key"
    }

    init {
        // 1. Create a master key for encryption. This key is stored in the Android Keystore.
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        // 2. Initialize EncryptedSharedPreferences
        prefs = EncryptedSharedPreferences.create(
            context,
            FILENAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    /**
     * Saves the user's username and password securely.
     */
    fun saveCredentials(username: String, password: String) {
        prefs.edit()
            .putString(USERNAME_KEY, username)
            .putString(PASSWORD_KEY, password)
            .apply()
    }

    /**
     * Retrieves the stored user credentials.
     * @return UserCredentials object if found, otherwise null.
     */
    fun getCredentials(): UserCredentials? {
        val username = prefs.getString(USERNAME_KEY, null)
        val password = prefs.getString(PASSWORD_KEY, null)

        return if (username != null && password != null) {
            UserCredentials(username, password)
        } else {
            null
        }
    }

    /**
     * Clears all stored credentials.
     */
    fun clear() {
        prefs.edit().clear().apply()
    }
}