package org.swirlsea.tiletalk.threads.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.swirlsea.tiletalk.AuthUiState
import org.swirlsea.tiletalk.AuthViewModel
import org.swirlsea.tiletalk.CryptoUtils
import org.swirlsea.tiletalk.data.MessageSet
import org.swirlsea.tiletalk.data.TileTalkRepository
import org.swirlsea.tiletalk.data.User
import org.swirlsea.tiletalk.threads.ThreadsUiState

class ThreadsViewModel(
    private val application: Application,
    private val repository: TileTalkRepository,
    private val authViewModel: AuthViewModel
) : ViewModel() {

    private val _uiState = MutableStateFlow<ThreadsUiState>(ThreadsUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private var currentUser: User? = null

    init {
        viewModelScope.launch {
            authViewModel.uiState.collect { authState ->
                when (authState) {
                    is AuthUiState.LoginSuccess -> {
                        currentUser = authState.user
                        loadThreads()
                    }
                    is AuthUiState.Error -> {
                        _uiState.value = ThreadsUiState.Error(authState.message)
                    }
                    is AuthUiState.Idle -> {
                        // You might want to clear the threads here or show a "logged out" message
                        _uiState.value = ThreadsUiState.Error("Please log in to view threads.")
                    }
                    else -> {
                        // Loading state is handled by the initial _uiState value
                    }
                }
            }
        }
    }

    fun loadThreads() {
        viewModelScope.launch {
            _uiState.value = ThreadsUiState.Loading
            try {
                val threads = currentUser?.let { user ->
                    repository.getAllThreads(user, application)
                } ?: emptyList()
                _uiState.value = ThreadsUiState.Success(threads, currentUser)
            } catch (e: Exception) {
                _uiState.value = ThreadsUiState.Error("Failed to load threads: ${e.message}")
            }
        }
    }

    // Function to reset the scroll index after scrolling is done
    fun onScrollCompleted() {
        (_uiState.value as? ThreadsUiState.Success)?.let {
            _uiState.value = it.copy(scrollToThreadIndex = null)
        }
    }

    // Pass the thread's index to trigger a scroll
    fun startEditingMessage(threadIndex: Int, messageId: Int, currentText: String) {
        (_uiState.value as? ThreadsUiState.Success)?.let {
            _uiState.value = it.copy(
                editingMessageId = messageId,
                editingMessageText = currentText,
                scrollToThreadIndex = threadIndex // Set the index to scroll to
            )
        }
    }

    // Pass the thread's index to trigger a scroll
    fun startAddingComment(threadIndex: Int, tileId: Int) {
        (_uiState.value as? ThreadsUiState.Success)?.let {
            _uiState.value = it.copy(
                addingCommentToTileId = tileId,
                scrollToThreadIndex = threadIndex // Set the index to scroll to
            )
        }
    }

    // ... (other functions like deleteThread, addComment, etc. remain the same)
    fun deleteThread(tileId: Int) {
        viewModelScope.launch {
            val response = repository.deleteTile(tileId)
            if (response.success) {
                loadThreads()
            }
        }
    }

    fun deleteMessage(ownerId: Int, x: Int, y: Int) {
        viewModelScope.launch {
            val response = repository.deleteMessage(ownerId, x, y)
            if (response.success) {
                loadThreads()
            }
        }
    }

    fun cancelEditingMessage() {
        (_uiState.value as? ThreadsUiState.Success)?.let {
            _uiState.value = it.copy(editingMessageId = null, editingMessageText = "")
        }
    }

    fun addComment(ownerId: Int, x: Int, y: Int, message: String) {
        viewModelScope.launch {
            if (message.isNotBlank() && currentUser != null) {
                val contactsResponse = repository.getContacts()
                if(contactsResponse.success && contactsResponse.data != null) {
                    val recipientIds = (contactsResponse.data.contacts + ownerId + currentUser!!.id).distinct()
                    val messageSet = recipientIds.mapNotNull { recipientId ->
                        val profileResponse = repository.getProfile(recipientId)
                        if (profileResponse.success && profileResponse.data?.publicKey != null) {
                            val publicKey = CryptoUtils.stringToPublicKey(profileResponse.data.publicKey)
                            val payload = CryptoUtils.encryptPayload(message, publicKey)
                            MessageSet(recipient_id = recipientId, payload = payload)
                        } else {
                            null
                        }
                    }
                    repository.createMessage(ownerId, x, y, messageSet)
                    cancelAddingComment()
                    loadThreads()
                }
            }
        }
    }

    fun cancelAddingComment() {
        (_uiState.value as? ThreadsUiState.Success)?.let {
            _uiState.value = it.copy(addingCommentToTileId = null)
        }
    }

    fun saveEditedMessage(ownerId: Int, x: Int, y: Int, newMessage: String) {
        viewModelScope.launch {
            val deleteResponse = repository.deleteMessage(ownerId, x, y)
            if (deleteResponse.success) {
                addComment(ownerId, x, y, newMessage)
            }
        }
    }
}