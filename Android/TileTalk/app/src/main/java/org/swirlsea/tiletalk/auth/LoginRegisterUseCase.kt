package org.swirlsea.tiletalk.auth

import android.util.Log
import org.swirlsea.tiletalk.data.ApiResponse
import org.swirlsea.tiletalk.data.LoginResponse
import org.swirlsea.tiletalk.SessionManager
import org.swirlsea.tiletalk.TileTalkRepository
import org.swirlsea.tiletalk.data.User

class LoginRegisterUseCase(
    private val repository: TileTalkRepository,
    private val sessionManager: SessionManager
) {
    suspend fun loginUser(userName: String, password: String): ApiResponse<LoginResponse> {
        val loginResponse = repository.login(userName, password)
        if (loginResponse.success && loginResponse.data != null) {
            sessionManager.saveCredentials(userName, password)
        }
        return loginResponse
    }

    suspend fun reloginUser(userName: String, password: String): ApiResponse<LoginResponse> {
        return loginUser(userName, password)
    }

    suspend fun registerUser(userName: String, password: String, publicKey: String?): ApiResponse<User> {
        return repository.register(userName, password, publicKey)
    }

    suspend fun logoutUser() {
        try {
            repository.logout()
        } catch (e: Exception) {
            Log.e("LoginRegisterUseCase", "API logout call failed, but clearing local session anyway.", e)
        } finally {
            sessionManager.clear()
        }
    }
}