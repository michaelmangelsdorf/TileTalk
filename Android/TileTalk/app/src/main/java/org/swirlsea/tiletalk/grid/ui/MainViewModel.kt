
package org.swirlsea.tiletalk

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.swirlsea.tiletalk.contacts.ui.ContactsUiState
import org.swirlsea.tiletalk.contacts.ui.ContactsViewModel
import org.swirlsea.tiletalk.data.MessageSet
import org.swirlsea.tiletalk.data.Tile
import org.swirlsea.tiletalk.data.TileTalkRepository
import org.swirlsea.tiletalk.data.User
import org.swirlsea.tiletalk.grid.DecryptedMessage
import org.swirlsea.tiletalk.grid.DialogState
import org.swirlsea.tiletalk.grid.GridUiState
import org.swirlsea.tiletalk.grid.MainScreenEvent
import org.swirlsea.tiletalk.grid.MainUiState
import org.swirlsea.tiletalk.grid.TileUiState
import retrofit2.HttpException

class MainViewModel(
    private val application: Application,
    private val repository: TileTalkRepository,
    private val authViewModel: AuthViewModel,
    private val contactsViewModel: ContactsViewModel
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState = _uiState.asStateFlow()

    private val gson = Gson()

    init {
        viewModelScope.launch {
            authViewModel.uiState.collect { authState ->
                when (authState) {
                    is AuthUiState.LoginSuccess -> {
                        postLoginSetup(authState.user)
                    }
                    is AuthUiState.Error -> _uiState.update { it.copy(snackbarMessage = authState.message, isLoading = false) }
                    is AuthUiState.Idle -> {
                        if (_uiState.value.loggedInUser != null) {
                            _uiState.update { MainUiState() }
                        }
                        _uiState.update { it.copy(isLoading = false) }
                    }
                    is AuthUiState.Loading -> _uiState.update { it.copy(isLoading = true) }
                }
            }
        }

        viewModelScope.launch {
            contactsViewModel.uiState.collect { contactsState ->
                if (contactsState is ContactsUiState.Success) {
                    _uiState.update { it.copy(contacts = contactsState.contacts) }
                }
            }
        }

        repository.startListeningForUpdates("wss://swirlsea.org/api/tiletalk/ws/")
        viewModelScope.launch {
            repository.updates.collect { message ->
                try {
                    val notification = gson.fromJson(message, Map::class.java)
                    when (notification["type"]) {
                        "tile_update", "new_message" -> {
                            val tileOwnerId = (notification["tileOwnerId"] as? Double)?.toInt()
                            if (tileOwnerId != null) {
                                if (tileOwnerId == _uiState.value.loggedInUser?.id) fetchUserGrid(tileOwnerId)
                                if (tileOwnerId == _uiState.value.selectedContact?.id) fetchContactGrid(tileOwnerId)
                                _uiState.update { it.copy(snackbarMessage = "Grid updated!") }
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("MainViewModel", "Error parsing WebSocket message", e)
                }
            }
        }
    }

    fun onEvent(event: MainScreenEvent) {
        when (event) {
            is MainScreenEvent.Login -> authViewModel.login(event.userName, event.password) { isSuccess ->
                if (isSuccess) {
                    _uiState.update { it.copy(currentDialog = DialogState.Hidden) }
                }
            }
            is MainScreenEvent.Register -> authViewModel.register(event.userName, event.password) { isSuccess ->
                if (isSuccess) {
                    _uiState.update { it.copy(currentDialog = DialogState.Hidden) }
                }
            }
            is MainScreenEvent.Logout -> authViewModel.logout()
            is MainScreenEvent.GenerateNewKeyPair -> {
                if (CryptoUtils.keyPairExists(application, event.user.username)) {
                    _uiState.update { it.copy(currentDialog = DialogState.ConfirmRekey(event.user)) }
                } else {
                    _uiState.update { it.copy(currentDialog = DialogState.ConfirmKeyGeneration(event.user)) }
                }
            }
            is MainScreenEvent.ConfirmGenerateNewKeyPair -> viewModelScope.launch {
                updatePublicKey(event.user)
                _uiState.update { it.copy(currentDialog = DialogState.Hidden) }
            }
            is MainScreenEvent.ConfirmRekey -> viewModelScope.launch {
                updatePublicKey(event.user)
                _uiState.update { it.copy(currentDialog = DialogState.Hidden) }
            }
            is MainScreenEvent.KeyPairImported -> {
                _uiState.update { it.copy(encryptionKeyPair = event.keyPair, snackbarMessage = "Keypair imported.") }
            }
            is MainScreenEvent.ExportKeyFile -> {
                CryptoUtils.handleExportKeyfile(application.applicationContext, _uiState.value.encryptionKeyPair, event.uri)
                _uiState.update { it.copy(snackbarMessage = "Keyfile export initiated.") }
            }
            is MainScreenEvent.ImportKeyFile -> {
                val user = _uiState.value.loggedInUser
                if (user != null) {
                    CryptoUtils.handleImportKeyfile(application.applicationContext, user.username, event.uri) { importedKeyPair ->
                        _uiState.update { it.copy(encryptionKeyPair = importedKeyPair) }
                        if (importedKeyPair != null) {
                            viewModelScope.launch {
                                val publicKeyString = CryptoUtils.publicKeyToString(importedKeyPair.public)
                                repository.updateProfile(user.id, publicKeyString)
                                _uiState.update { it.copy(snackbarMessage = "Keypair imported - Public key uploaded to server") }
                            }
                        } else {
                            _uiState.update { it.copy(snackbarMessage = "Failed to import keypair.") }
                        }
                    }
                }
            }
            is MainScreenEvent.SelectContact -> selectContact(event.contactId)

            is MainScreenEvent.TileTapped -> showMessagesForTile(event.ownerId, event.x, event.y)
            is MainScreenEvent.TileLongPressed -> showEditTileDialog(event.ownerId, event.x, event.y)

            is MainScreenEvent.DeleteTile -> deleteTile(event.tileId)
            is MainScreenEvent.SaveTileChanges -> saveTile(event)
            is MainScreenEvent.AddComment -> addComment(event.ownerId, event.x, event.y, event.message)
            is MainScreenEvent.DeleteMessage -> deleteMessage(event.ownerId, event.x, event.y)
            is MainScreenEvent.DismissDialog -> _uiState.update { it.copy(currentDialog = DialogState.Hidden) }
            is MainScreenEvent.ShowAuthDialog -> _uiState.update { it.copy(currentDialog = DialogState.Auth()) }
            is MainScreenEvent.ShowContactsDialog -> _uiState.value.contacts?.let { contacts ->
                viewModelScope.launch {
                    val resolvedContacts = contacts.contacts.mapNotNull { contactId ->
                        try {
                            val profileResponse = repository.getProfile(contactId)
                            if (profileResponse.success) profileResponse.data else null
                        } catch (e: Exception) {
                            null
                        }
                    }
                    val resolvedIncoming = contacts.incoming.mapNotNull { contactId ->
                        try {
                            val profileResponse = repository.getProfile(contactId)
                            if (profileResponse.success) profileResponse.data else null
                        } catch (e: Exception) {
                            null
                        }
                    }
                    val resolvedPending = contacts.pending.mapNotNull { contactId ->
                        try {
                            val profileResponse = repository.getProfile(contactId)
                            if (profileResponse.success) profileResponse.data else null
                        } catch (e: Exception) {
                            null
                        }
                    }
                    _uiState.update {
                        it.copy(
                            currentDialog = DialogState.Contacts(
                                contactList = contacts,
                                resolvedContacts = resolvedContacts,
                                resolvedIncoming = resolvedIncoming,
                                resolvedPending = resolvedPending
                            )
                        )
                    }
                }
            }
            is MainScreenEvent.ClearSnackbar -> _uiState.update { it.copy(snackbarMessage = null) }
            is MainScreenEvent.RequestContact -> contactsViewModel.requestContact(event.targetUsername) { isSuccess ->
                if (isSuccess) {
                    _uiState.update { it.copy(currentDialog = DialogState.Hidden, snackbarMessage = "Contact request sent!") }
                }
            }
            is MainScreenEvent.AcceptContact -> contactsViewModel.acceptContact(event.requesterId) { isSuccess ->
                if (isSuccess) {
                    _uiState.update { it.copy(currentDialog = DialogState.Hidden, snackbarMessage = "Contact accepted!") }
                }
            }
            is MainScreenEvent.RemoveContact -> contactsViewModel.removeContact(event.contactId) { isSuccess ->
                if (isSuccess) {
                    _uiState.update { it.copy(currentDialog = DialogState.Hidden, snackbarMessage = "Contact removed!") }
                }
            }
            is MainScreenEvent.RefreshContacts -> contactsViewModel.refreshContacts()

            is MainScreenEvent.StartEditingMessage -> {
                val currentDialog = _uiState.value.currentDialog
                if (currentDialog is DialogState.ShowingMessages) {
                    _uiState.update {
                        it.copy(
                            currentDialog = currentDialog.copy(
                                editingMessageId = event.messageId,
                                editingMessageText = event.currentText
                            )
                        )
                    }
                }
            }
            is MainScreenEvent.CancelEditingMessage -> {
                val currentDialog = _uiState.value.currentDialog
                if (currentDialog is DialogState.ShowingMessages) {
                    _uiState.update {
                        it.copy(
                            currentDialog = currentDialog.copy(
                                editingMessageId = null,
                                editingMessageText = ""
                            )
                        )
                    }
                }
            }
            is MainScreenEvent.SaveEditedMessage -> {
                val currentDialog = _uiState.value.currentDialog
                if (currentDialog is DialogState.ShowingMessages) {
                    saveEditedMessage(currentDialog.tileOwnerId, currentDialog.x, currentDialog.y, event.newMessage)
                }
            }
            is MainScreenEvent.RefreshGrids -> refreshUserAndContactGrids()
            else -> {}
        }
    }

    private suspend fun postLoginSetup(user: User) {
        _uiState.update { it.copy(isLoading = true) }
        try {
            _uiState.update { it.copy(loggedInUser = user) }
            if (!CryptoUtils.keyPairExists(application, user.username)) {
                _uiState.update {
                    it.copy(currentDialog = DialogState.ConfirmKeyGeneration(user))
                }
            }
            _uiState.update { it.copy(encryptionKeyPair = CryptoUtils.getKeyPair(application, user.username)) }
            fetchUserGrid(user.id)
            contactsViewModel.refreshContacts()
        } catch (e: Exception) {
            Log.e("MainViewModel", "Failed during post-login setup", e)
            _uiState.update {
                it.copy(snackbarMessage = "A network error occurred: ${e.message}")
            }
        } finally {
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private suspend fun updatePublicKey(user: User) {
        try {
            val keyPair = CryptoUtils.generateAndSaveRsaKeyPair(application, user.username)
            val publicKeyString = CryptoUtils.publicKeyToString(keyPair.public)
            repository.updateProfile(user.id, publicKeyString)
            _uiState.update { it.copy(encryptionKeyPair = keyPair, snackbarMessage = "Public key successfully created/updated.") }
        } catch (e: Exception) {
            _uiState.update { it.copy(snackbarMessage = "Failed to update public key: ${e.message}") }
        }
    }

    private suspend fun fetchUserGrid(userId: Int) {
        val user = _uiState.value.loggedInUser ?: return
        val tiles = fetchAllTilesForUser(userId)
        _uiState.update { it.copy(userGrid = GridUiState(owner = user, tiles = tiles)) }
    }

    private suspend fun fetchContactGrid(contactId: Int) {
        val contact = _uiState.value.selectedContact ?: return
        val tiles = fetchAllTilesForUser(contactId)
        _uiState.update { it.copy(contactGrid = GridUiState(owner = contact, tiles = tiles)) }
    }

    private suspend fun fetchAllTilesForUser(userId: Int): List<List<TileUiState>> {
        val grid = MutableList(4) { MutableList(4) { TileUiState() } }
        val deferredTiles = mutableListOf<kotlinx.coroutines.Deferred<Unit>>()

        for (y in 0..3) {
            for (x in 0..3) {
                deferredTiles.add(viewModelScope.async {
                    try {
                        val tileResponse = repository.readTile(userId, x, y)
                        if (tileResponse.success && tileResponse.data != null) {
                            val tile = tileResponse.data
                            val messagesResponse = repository.readMessages(userId, x, y)
                            val hasMessages = messagesResponse.success && !messagesResponse.data.isNullOrEmpty()

                            val hasNew = if (hasMessages) {
                                messagesResponse.data!!.any { it.seen == false }
                            } else {
                                false
                            }

                            grid[y][x] = TileUiState(
                                tileId = tile.id,
                                ownerId = tile.owner_id,
                                symbol = tile.symbol,
                                starterId = tile.starter_id,
                                hasMessages = hasMessages,
                                animationType = tile.animation_type,
                                flip = tile.flip ?: false,
                                callout = tile.callout,
                                title = tile.title,
                                hasNewMessages = hasNew
                            )
                        }
                    } catch (e: Exception) {
                        Log.d("MainViewModel", "No tile found at ($x, $y) for user $userId")
                    }
                    Unit
                })
            }
        }
        deferredTiles.awaitAll()
        return grid
    }

    private fun selectContact(contactId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val contactUserResponse = repository.getProfile(contactId)
                if (contactUserResponse.success && contactUserResponse.data != null) {
                    val contactUser = contactUserResponse.data
                    _uiState.update { it.copy(selectedContact = contactUser) }
                    fetchContactGrid(contactId)
                }
            } catch (e: HttpException) {
                if (e.code() != 401 && e.code() != 403) {
                    _uiState.update { it.copy(snackbarMessage = "Error fetching contact details: ${e.message()}") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(snackbarMessage = "An unexpected error occurred while selecting a contact.") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun refreshUserAndContactGrids() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                _uiState.value.loggedInUser?.id?.let { fetchUserGrid(it) }
                _uiState.value.selectedContact?.id?.let { fetchContactGrid(it) }
                _uiState.update { it.copy(snackbarMessage = "Grids refreshed.") }
            } catch (e: Exception) {
                _uiState.update { it.copy(snackbarMessage = "Failed to refresh grids: ${e.message}") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun showMessagesForTile(ownerId: Int, x: Int, y: Int) {
        viewModelScope.launch {
            val currentUser = _uiState.value.loggedInUser ?: return@launch
            try {
                val tileResponse = repository.readTile(ownerId, x, y)
                val isTilePopulated = tileResponse.success && tileResponse.data != null

                if (!isTilePopulated && ownerId != currentUser.id) {
                    _uiState.update {
                        it.copy(
                            currentDialog = DialogState.ShowingMessages(
                                tileOwnerId = ownerId, x = x, y = y, messages = emptyList(),
                                canDeleteAll = false, canComment = false
                            )
                        )
                    }
                    return@launch
                }

                val messagesResponse = repository.readMessages(ownerId, x, y)
                if (!messagesResponse.success || messagesResponse.data.isNullOrEmpty()) {
                    _uiState.update {
                        it.copy(
                            currentDialog = DialogState.ShowingMessages(
                                tileOwnerId = ownerId, x = x, y = y, messages = emptyList(),
                                canDeleteAll = ownerId == currentUser.id,
                                canComment = isTilePopulated
                            )
                        )
                    }
                    return@launch
                }

                val messages = messagesResponse.data
                val authorIds = messages.map { it.responder_id }.distinct().filter { it != -1 }
                val authorProfiles = authorIds.map { id ->
                    async {
                        try { repository.getProfile(id) } catch (e: Exception) { null }
                    }
                }.awaitAll().filterNotNull()

                val authorUsernameMap = authorProfiles
                    .filter { it.success && it.data != null }
                    .associate { it.data!!.id to it.data.username }

                val decryptedMessages = messages.mapNotNull { message ->
                    val decryptedContent = CryptoUtils.decryptPayload(message.payload, application, currentUser.username)
                    if (decryptedContent != null) {
                        DecryptedMessage(
                            authorId = message.responder_id,
                            authorUsername = authorUsernameMap[message.responder_id] ?: "Unknown User",
                            content = decryptedContent,
                            createdAt = message.createdAt,
                            canDelete = message.responder_id == currentUser.id
                        )
                    } else {
                        DecryptedMessage(
                            authorId = -1,
                            authorUsername = "System",
                            content = "(can't decrypt)",
                            createdAt = message.createdAt,
                            canDelete = false
                        )
                    }
                }

                val userHasAlreadyCommented = decryptedMessages.any { it.authorId == currentUser.id }
                val canComment = isTilePopulated && !userHasAlreadyCommented

                _uiState.update {
                    it.copy(
                        currentDialog = DialogState.ShowingMessages(
                            tileOwnerId = ownerId, x = x, y = y, messages = decryptedMessages,
                            canDeleteAll = ownerId == currentUser.id,
                            canComment = canComment
                        )
                    )
                }

                // After showing messages, refresh the grid to update the "new messages" icon
                if (ownerId == currentUser.id) {
                    fetchUserGrid(ownerId)
                } else {
                    fetchContactGrid(ownerId)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(snackbarMessage = "An error occurred: ${e.message}") }
            }
        }
    }

    private fun deleteMessage(ownerId: Int, x: Int, y: Int) {
        viewModelScope.launch {
            try {
                val response = repository.deleteMessage(ownerId, x, y)
                if (response.success) {
                    _uiState.update { it.copy(snackbarMessage = "Message deleted.") }
                    showMessagesForTile(ownerId, x, y)
                } else {
                    _uiState.update { it.copy(snackbarMessage = response.message) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(snackbarMessage = "Error deleting message: ${e.message}") }
            }
        }
    }

    private fun showEditTileDialog(ownerId: Int, x: Int, y: Int) {
        viewModelScope.launch {
            try {
                val tileResponse = repository.readTile(ownerId, x, y)
                val loggedInUserId = _uiState.value.loggedInUser?.id

                val tile = tileResponse.data
                val isTileEmpty = !tileResponse.success || tile == null
                val isStarter = loggedInUserId != null && tile != null && loggedInUserId == tile.starter_id

                val canEditSymbol = isTileEmpty || isStarter
                val canDelete = loggedInUserId != null && tile != null && (loggedInUserId == tile.owner_id || loggedInUserId == tile.starter_id)

                _uiState.update {
                    it.copy(
                        currentDialog = DialogState.EditingTile(
                            tileOwnerId = ownerId,
                            x = x,
                            y = y,
                            tileId = tile?.id,
                            currentSymbol = tile?.symbol,
                            animationType = tile?.animation_type ?: 0,
                            flip = tile?.flip ?: false,
                            callout = tile?.callout,
                            title = tile?.title,
                            canEditSymbol = canEditSymbol,
                            canDelete = canDelete
                        )
                    )
                }
            } catch (e: Exception) {
                Log.d("MainViewModel", "Could not read tile at ($x, $y). Assuming it's empty.", e)
                _uiState.update {
                    it.copy(
                        currentDialog = DialogState.EditingTile(
                            tileOwnerId = ownerId,
                            x = x,
                            y = y,
                            tileId = null,
                            currentSymbol = null,
                            animationType = 0,
                            flip = false,
                            callout = null,
                            title = null,
                            canEditSymbol = true,
                            canDelete = false
                        )
                    )
                }
            }
        }
    }

// tiletalk/grid/ui/MainViewModel.kt

    private fun saveTile(event: MainScreenEvent.SaveTileChanges) {
        viewModelScope.launch {
            val loggedInUser = _uiState.value.loggedInUser ?: return@launch
            try {
                if (event.tileId != null) {
                    val updates = mapOf(
                        "id" to event.tileId,
                        "symbol" to event.symbol,
                        "animation_type" to event.animationType,
                        "flip" to event.flip,
                        "callout" to event.callout, // Changed line
                        "title" to event.title   // Changed line
                    )
                    repository.updateTile(updates)
                } else {
                    val newTile = Tile(
                        id = 0,
                        owner_id = event.ownerId,
                        x_coord = event.x,
                        y_coord = event.y,
                        starter_id = loggedInUser.id,
                        symbol = event.symbol,
                        animation_type = event.animationType,
                        flip = event.flip,
                        tile_bg = 0,
                        callout = event.callout, // Changed line
                        title = event.title   // Changed line
                    )
                    repository.createTile(newTile)
                }

                if (event.ownerId == loggedInUser.id) {
                    fetchUserGrid(loggedInUser.id)
                } else {
                    fetchContactGrid(event.ownerId)
                }
                _uiState.update { it.copy(currentDialog = DialogState.Hidden, snackbarMessage = "Tile saved!") }

            } catch (e: Exception) {
                _uiState.update { it.copy(snackbarMessage = "Error saving tile: ${e.message}") }
            }
        }
    }

    private fun addComment(ownerId: Int, x: Int, y: Int, message: String) {
        viewModelScope.launch {
            if (message.isNotBlank()) {
                _uiState.update { it.copy(isLoading = true) }
                createMessageSetAndSend(ownerId, x, y, message)
                showMessagesForTile(ownerId, x, y)
                _uiState.update { it.copy(isLoading = false, snackbarMessage = "Comment added!") }
            }
        }
    }

    private fun saveEditedMessage(ownerId: Int, x: Int, y: Int, newMessage: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val deleteResponse = repository.deleteMessage(ownerId, x, y)
                if (deleteResponse.success) {
                    createMessageSetAndSend(ownerId, x, y, newMessage)
                    _uiState.update { it.copy(snackbarMessage = "Message updated!") }
                    showMessagesForTile(ownerId, x, y)
                } else {
                    _uiState.update { it.copy(snackbarMessage = "Failed to update message: Could not delete original.") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(snackbarMessage = "Error updating message: ${e.message}") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private suspend fun createMessageSetAndSend(ownerId: Int, x: Int, y: Int, message: String) {
        val loggedInUser = _uiState.value.loggedInUser ?: return

        try {
            val contactsResponse = repository.getContacts()
            if (!contactsResponse.success || contactsResponse.data == null) {
                _uiState.update { it.copy(snackbarMessage = "Could not fetch contacts to send message.") }
                return
            }
            val contacts = contactsResponse.data

            val recipientIds = (contacts.contacts + ownerId + loggedInUser.id).distinct()
            val messageSet = recipientIds.mapNotNull { recipientId ->
                try {
                    val profileResponse = repository.getProfile(recipientId)
                    if (profileResponse.success && profileResponse.data?.publicKey != null) {
                        val publicKey = CryptoUtils.stringToPublicKey(profileResponse.data.publicKey)
                        val payload = CryptoUtils.encryptPayload(message, publicKey)
                        MessageSet(recipient_id = recipientId, payload = payload)
                    } else {
                        Log.w("MainViewModel", "Could not create message for recipient ${recipientId}: No public key found.")
                        null
                    }
                } catch (e: Exception) {
                    null
                }
            }

            val ownerMessageExists = messageSet.any { it.recipient_id == ownerId }
            if (!ownerMessageExists && ownerId != loggedInUser.id) {
                _uiState.update { it.copy(snackbarMessage = "Could not send message: Your contact may not have an encryption key.") }
                return
            }

            if (messageSet.isNotEmpty()) {
                repository.createMessage(ownerId, x, y, messageSet)
            } else {
                _uiState.update { it.copy(snackbarMessage = "Could not encrypt message for any recipient.") }
            }

        } catch (e: Exception) {
            _uiState.update { it.copy(snackbarMessage = "Failed to prepare message: ${e.message}") }
        }
    }

    private fun deleteTile(tileId: Int) {
        viewModelScope.launch {
            try {
                val response = repository.deleteTile(tileId)
                if (response.success) {
                    _uiState.update { it.copy(snackbarMessage = "Tile deleted.", currentDialog = DialogState.Hidden) }
                    _uiState.value.loggedInUser?.id?.let { fetchUserGrid(it) }
                    _uiState.value.selectedContact?.id?.let { fetchContactGrid(it) }
                } else {
                    _uiState.update { it.copy(snackbarMessage = response.message) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(snackbarMessage = "Error deleting tile: ${e.message}") }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        repository.stopListeningForUpdates()
    }
}
