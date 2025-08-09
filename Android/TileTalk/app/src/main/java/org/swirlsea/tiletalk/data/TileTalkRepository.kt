package org.swirlsea.tiletalk.data

import android.content.Context
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import org.swirlsea.tiletalk.CryptoUtils
import org.swirlsea.tiletalk.grid.DecryptedMessage

class TileTalkRepository {

    private val api = ApiService.retrofit.create(TileTalkApi::class.java)
    private val webSocketClient = WebSocketClient(ApiService.getOkHttpClient())

    suspend fun validateCurrentSession(): ApiResponse<User> {
        return api.getProfile(0)
    }

    suspend fun register(userName: String, password: String, publicKey: String?): ApiResponse<User> {
        delay(2000)
        val body = mutableMapOf("userName" to userName, "password" to password)
        publicKey?.let { body["publicKey"] = it }
        return api.register(body)
    }

    suspend fun login(userName: String, password: String) =
        api.login(mapOf("userName" to userName, "password" to password))

    suspend fun logout() = api.logout()

    suspend fun deleteUser() = api.deleteUser()

    suspend fun getProfile(userId: Int) = api.getProfile(userId)

    suspend fun getProfileByUsername(username: String) = api.getProfileByUsername(username)

    suspend fun updateProfile(userId: Int, publicKey: String) =
        api.updateProfile(userId, mapOf("publicKey" to publicKey))

    suspend fun getContacts() = api.getContacts()

    suspend fun requestContact(targetUserId: Int) =
        api.requestContact(mapOf("targetUserId" to targetUserId))

    suspend fun acceptContact(acceptedUserId: Int) =
        api.acceptContact(mapOf("acceptedUserId" to acceptedUserId))

    suspend fun removeContact(removableUserId: Int) = api.removeContact(removableUserId)

    suspend fun createTile(tile: Tile) = api.createTile(tile)

    suspend fun readTile(ownerId: Int, x: Int, y: Int) = api.readTile(ownerId, x, y)

    suspend fun updateTile(updates: Map<String, @JvmSuppressWildcards Any?>) = api.updateTile(updates)

    suspend fun deleteTile(tileId: Int) = api.deleteTile(tileId)

    suspend fun createMessage(
        ownerId: Int,
        x: Int,
        y: Int,
        messageSet: List<MessageSet>
    ): ApiResponse<Int> {
        val request = CreateMessageRequest(
            ownerId = ownerId,
            xCoord = x,
            yCoord = y,
            messageSet = messageSet
        )
        return api.createMessage(request)
    }

    suspend fun readMessages(ownerId: Int, x: Int, y: Int) = api.readMessages(ownerId, x, y)

    suspend fun deleteMessage(ownerId: Int, x: Int, y: Int) = api.deleteMessage(ownerId, x, y)

    suspend fun getAllThreads(currentUser: User, context: Context): List<Thread> = coroutineScope {
        val contactsResponse = getContacts()
        if (!contactsResponse.success || contactsResponse.data == null) {
            return@coroutineScope emptyList()
        }

        val contactList = contactsResponse.data
        // The definitive fix is here: Use the passed-in 'currentUser.id'
        // to guarantee the user's own grid is included in the list.
        val allUserIds = (contactList.contacts + currentUser.id).distinct()

        val threadsDeferred = allUserIds.map { userId ->
            async {
                (0..3).flatMap { y ->
                    (0..3).mapNotNull { x ->
                        val tileResponse = readTile(userId, x, y)
                        if (tileResponse.success && tileResponse.data != null) {
                            val tile = tileResponse.data
                            val messagesResponse = readMessages(userId, x, y)
                            if (messagesResponse.success && messagesResponse.data?.isNotEmpty() == true) {
                                val ownerResponse = getProfile(tile.owner_id)
                                val starterResponse = getProfile(tile.starter_id)

                                if (ownerResponse.success && ownerResponse.data != null && starterResponse.success && starterResponse.data != null) {
                                    val authorIds = messagesResponse.data.map { it.responder_id }.distinct()
                                    val authorProfiles = authorIds.associateWith { id -> getProfile(id).data }

                                    val decryptedMessages = messagesResponse.data.mapNotNull { msg ->
                                        val decryptedContent = CryptoUtils.decryptPayload(msg.payload, context, currentUser.username)
                                        if (decryptedContent != null) {
                                            DecryptedMessage(
                                                authorId = msg.responder_id,
                                                authorUsername = authorProfiles[msg.responder_id]?.username ?: "Unknown",
                                                content = decryptedContent,
                                                createdAt = msg.createdAt,
                                                canDelete = msg.responder_id == currentUser.id
                                            )
                                        } else null
                                    }

                                    if (decryptedMessages.isNotEmpty()) {
                                        val sortedMessages = decryptedMessages.sortedBy { it.createdAt }
                                        val lastMessage = sortedMessages.last()
                                        Thread(
                                            tile = tile,
                                            owner = ownerResponse.data,
                                            starter = starterResponse.data,
                                            messages = sortedMessages,
                                            lastActivity = lastMessage.createdAt,
                                            lastMessageSnippet = lastMessage.content
                                        )
                                    } else null
                                } else null
                            } else null
                        } else null
                    }
                }
            }
        }

        val allThreads = threadsDeferred.map { it.await() }.flatten()
        return@coroutineScope allThreads.sortedByDescending { it.lastActivity }
    }

    val updates = webSocketClient.messages

    fun startListeningForUpdates(url: String) {
        webSocketClient.connect(url)
    }

    fun stopListeningForUpdates() {
        webSocketClient.disconnect()
    }
}