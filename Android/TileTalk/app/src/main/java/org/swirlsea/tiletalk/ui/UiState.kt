
package org.swirlsea.tiletalk.ui

import org.swirlsea.tiletalk.data.ContactList
import org.swirlsea.tiletalk.data.User
import java.security.KeyPair

data class DecryptedMessage(
    val authorId: Int,
    val authorUsername: String,
    val content: String,
    val createdAt: String,
    val canDelete: Boolean
)

data class TileUiState(
    val tileId: Int? = null,
    val ownerId: Int? = null,
    val symbol: String? = null,
    val starterId: Int? = null,
    val hasMessages: Boolean = false,
    val animationType: Int = 0,

    val flip: Boolean = false,
    val callout: String? = null,
    val title: String? = null,
    val hasNewMessages: Boolean = false
)

data class GridUiState(
    val owner: User,
    val tiles: List<List<TileUiState>> = List(4) { List(4) { TileUiState() } }
)

sealed interface DialogState {
    object Hidden : DialogState

    data class ShowingMessages(
        val tileOwnerId: Int,
        val x: Int,
        val y: Int,
        val messages: List<DecryptedMessage>,
        val canDeleteAll: Boolean,
        val canComment: Boolean,
        val editingMessageId: Int? = null,
        val editingMessageText: String = ""
    ) : DialogState

    data class EditingTile(
        val tileOwnerId: Int,
        val x: Int,
        val y: Int,
        val tileId: Int?,
        val currentSymbol: String?,
        val animationType: Int,

        val flip: Boolean,
        val callout: String?,
        val title: String?,
        val canEditSymbol: Boolean,
        val canDelete: Boolean
    ) : DialogState

    data class Auth(val isRegistering: Boolean = false) : DialogState

    data class Contacts(
        val contactList: ContactList,
        val resolvedContacts: List<User> = emptyList(),
        val resolvedIncoming: List<User> = emptyList(),
        val resolvedPending: List<User> = emptyList()
    ) : DialogState

    data class ConfirmKeyGeneration(val user: User) : DialogState
    data class ConfirmRekey(val user: User) : DialogState
}

data class MainUiState(
    val isLoading: Boolean = false,
    val loggedInUser: User? = null,
    val selectedContact: User? = null,
    val userGrid: GridUiState? = null,
    val contactGrid: GridUiState? = null,
    val contacts: ContactList? = null,
    val currentDialog: DialogState = DialogState.Hidden,
    val snackbarMessage: String? = null,
    val encryptionKeyPair: KeyPair? = null
)