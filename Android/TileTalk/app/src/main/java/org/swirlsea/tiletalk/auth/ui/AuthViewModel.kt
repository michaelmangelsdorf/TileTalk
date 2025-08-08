package org.swirlsea.tiletalk

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.swirlsea.tiletalk.auth.LoginRegisterUseCase
import org.swirlsea.tiletalk.data.User

sealed interface AuthUiState {
    object Idle : AuthUiState
    object Loading : AuthUiState
    data class LoginSuccess(val user: User) : AuthUiState
    data class Error(val message: String) : AuthUiState
}

class AuthViewModel(
    private val loginRegisterUseCase: LoginRegisterUseCase,
    private val repository: TileTalkRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Loading)
    val uiState = _uiState.asSharedFlow()

    init {
        initializeApp()
    }

    private fun initializeApp() {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val credentials = sessionManager.getCredentials()
            if (credentials != null) {
                relogin(credentials.username, credentials.password)
            } else {
                _uiState.value = AuthUiState.Idle
            }
        }
    }

    private fun relogin(userName: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val loginResponse = loginRegisterUseCase.reloginUser(userName, password)
                if (loginResponse.success && loginResponse.data != null) {
                    val userResponse = repository.getProfile(loginResponse.data.userId)
                    if (userResponse.success && userResponse.data != null) {
                        _uiState.value = AuthUiState.LoginSuccess(userResponse.data)
                    } else {
                        _uiState.value = AuthUiState.Error("Login successful, but failed to fetch user profile.")
                    }
                } else {
                    _uiState.value = AuthUiState.Error(loginResponse.message)
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error("Login failed: ${e.message}")
            }
        }
    }

    fun login(userName: String, password: String, onComplete: ((isSuccess: Boolean) -> Unit)? = null) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            var loginSuccess = false
            try {
                val loginResponse = loginRegisterUseCase.loginUser(userName, password)
                if (loginResponse.success && loginResponse.data != null) {
                    val userResponse = repository.getProfile(loginResponse.data.userId)
                    if (userResponse.success && userResponse.data != null) {
                        _uiState.value = AuthUiState.LoginSuccess(userResponse.data)
                        loginSuccess = true
                    } else {
                        _uiState.value = AuthUiState.Error("Login successful, but failed to fetch user profile.")
                    }
                } else {
                    _uiState.value = AuthUiState.Error(loginResponse.message)
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error("Login failed: ${e.message}")
            }
            onComplete?.invoke(loginSuccess)
            if (!loginSuccess) {
                _uiState.value = AuthUiState.Idle
            }
        }
    }

    fun register(userName: String, password: String, onComplete: ((isSuccess: Boolean) -> Unit)? = null) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val keyPair = CryptoUtils.generateRsaKeyPair()
                val publicKeyString = CryptoUtils.publicKeyToString(keyPair.public)

                val registerResponse = loginRegisterUseCase.registerUser(userName, password, publicKeyString)
                if (registerResponse.success) {
                    login(userName, password) { isSuccess ->
                        if (!isSuccess) {
                            _uiState.value = AuthUiState.Error("Registration successful, but auto-login failed.")
                        }
                        onComplete?.invoke(isSuccess)
                    }
                } else {
                    _uiState.value = AuthUiState.Error(registerResponse.message)
                    onComplete?.invoke(false)
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error("Registration failed: ${e.message}")
                onComplete?.invoke(false)
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            loginRegisterUseCase.logoutUser()
            _uiState.value = AuthUiState.Idle
        }
    }
}