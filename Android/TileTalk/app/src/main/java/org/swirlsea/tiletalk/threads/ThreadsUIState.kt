package org.swirlsea.tiletalk.threads

import org.swirlsea.tiletalk.data.Thread
import org.swirlsea.tiletalk.data.User

sealed interface ThreadsUiState {
    object Loading : ThreadsUiState
    data class Success(
        val threads: List<Thread>,
        val currentUser: User?,
        val editingMessageId: Int? = null,
        val editingMessageText: String = "",
        val addingCommentToTileId: Int? = null,
        val scrollToThreadIndex: Int? = null
    ) : ThreadsUiState
    data class Error(val message: String) : ThreadsUiState
}