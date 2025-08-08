package org.swirlsea.tiletalk.contacts.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.swirlsea.tiletalk.data.ContactList
import org.swirlsea.tiletalk.contacts.ContactsUseCase

sealed interface ContactsUiState {
    object Idle : ContactsUiState
    object Loading : ContactsUiState
    data class Success(val contacts: ContactList) : ContactsUiState
    data class Error(val message: String) : ContactsUiState
}

class ContactsViewModel(private val contactsUseCase: ContactsUseCase) : ViewModel() {

    private val _uiState = MutableStateFlow<ContactsUiState>(ContactsUiState.Idle)
    val uiState = _uiState.asStateFlow()

    fun refreshContacts() {
        viewModelScope.launch {
            _uiState.value = ContactsUiState.Loading
            try {
                val response = contactsUseCase.fetchContacts()
                if (response.success && response.data != null) {
                    _uiState.value = ContactsUiState.Success(response.data)
                } else {
                    _uiState.value = ContactsUiState.Error(response.message)
                }
            } catch (e: Exception) {
                _uiState.value = ContactsUiState.Error("Failed to refresh contacts: ${e.message}")
            }
        }
    }

    fun requestContact(username: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            var success = false
            try {
                if (username.isNotBlank()) {
                    val response = contactsUseCase.requestContact(username)
                    if (response.success) {
                        success = true
                    } else {
                        _uiState.value = ContactsUiState.Error(response.message)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = ContactsUiState.Error("Failed to request contact: ${e.message}")
            }
            refreshContacts()
            onComplete(success)
        }
    }

    fun acceptContact(requesterId: Int, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            var success = false
            try {
                val response = contactsUseCase.acceptContact(requesterId)
                if (response.success) {
                    success = true
                } else {
                    _uiState.value = ContactsUiState.Error("Failed to accept contact: " + response.message)
                }
            } catch (e: Exception) {
                _uiState.value = ContactsUiState.Error("Failed to accept contact: ${e.message}")
            }
            refreshContacts()
            onComplete(success)
        }
    }

    fun removeContact(removableUserId: Int, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            var success = false
            try {
                val response = contactsUseCase.removeContact(removableUserId)
                if (response.success) {
                    success = true
                } else {
                    _uiState.value = ContactsUiState.Error("Failed to remove contact: " + response.message)
                }
            } catch (e: Exception) {
                _uiState.value = ContactsUiState.Error("Failed to remove contact: ${e.message}")
            }
            refreshContacts()
            onComplete(success)
        }
    }
}