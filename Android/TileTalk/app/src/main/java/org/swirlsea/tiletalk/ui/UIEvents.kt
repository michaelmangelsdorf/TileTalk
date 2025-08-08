
package org.swirlsea.tiletalk.ui

import org.swirlsea.tiletalk.data.User
import java.security.KeyPair
import android.net.Uri

sealed interface MainScreenEvent {


    data class Register(val userName: String, val password: String) : MainScreenEvent
    data class Login(val userName: String, val password: String) : MainScreenEvent
    object Logout : MainScreenEvent
    data class GenerateNewKeyPair(val user: User) : MainScreenEvent
    data class ConfirmGenerateNewKeyPair(val user: User) : MainScreenEvent
    data class ConfirmRekey(val user: User) : MainScreenEvent

    data class ExportKeyFile(val user: User, val uri: Uri) : MainScreenEvent
    data class ImportKeyFile(val user: User, val uri: Uri) : MainScreenEvent
    data class KeyPairImported(val keyPair: KeyPair?) : MainScreenEvent

    data class TileTapped(val ownerId: Int, val x: Int, val y: Int) : MainScreenEvent
    data class TileLongPressed(val ownerId: Int, val x: Int, val y: Int) : MainScreenEvent
    data class DeleteTile(val tileId: Int) : MainScreenEvent


    data class SaveTileChanges(
        val ownerId: Int,
        val tileId: Int?,
        val x: Int,
        val y: Int,
        val symbol: String,
        val animationType: Int,
        val flip: Boolean,
        val callout: String,
        val title: String
    ) : MainScreenEvent

    data class AddComment(
        val ownerId: Int,
        val x: Int,
        val y: Int,
        val message: String
    ) : MainScreenEvent

    data class DeleteMessage(val ownerId: Int, val x: Int, val y: Int) : MainScreenEvent
    data class DeleteAllMessagesOnTile(val tileId: Int) : MainScreenEvent
    object DismissDialog : MainScreenEvent
    object ClearSnackbar : MainScreenEvent
    data class SnackbarMessage(val message: String): MainScreenEvent

    data class StartEditingMessage(val messageId: Int, val currentText: String) : MainScreenEvent
    data class SaveEditedMessage(val newMessage: String) : MainScreenEvent
    object CancelEditingMessage : MainScreenEvent

    data class SelectContact(val contactId: Int) : MainScreenEvent
    data class RequestContact(val targetUsername: String) : MainScreenEvent
    data class AcceptContact(val requesterId: Int) : MainScreenEvent
    data class RemoveContact(val contactId: Int) : MainScreenEvent
    object RefreshContacts : MainScreenEvent

    object ShowAuthDialog : MainScreenEvent
    object ShowContactsDialog : MainScreenEvent
}