package org.swirlsea.tiletalk

import kotlinx.coroutines.delay
import org.swirlsea.tiletalk.data.ApiResponse
import org.swirlsea.tiletalk.data.ApiService
import org.swirlsea.tiletalk.data.CreateMessageRequest
import org.swirlsea.tiletalk.data.MessageSet
import org.swirlsea.tiletalk.data.Tile
import org.swirlsea.tiletalk.data.TileTalkApi
import org.swirlsea.tiletalk.data.User
import org.swirlsea.tiletalk.data.WebSocketClient

/**
 * Repository to handle all data operations for the TileTalk app.
 * It abstracts the remote data source (REST API and WebSockets) from the ViewModels.
 */
class TileTalkRepository {

    // Initialize the Retrofit API service and the WebSocket client
    private val api = ApiService.retrofit.create(TileTalkApi::class.java)
    private val webSocketClient = WebSocketClient(ApiService.getOkHttpClient())

    /**
     * Lightweight call to check if the current session cookie is valid.
     * It attempts to fetch the profile of the currently logged-in user.
     * NOTE: A user ID of 0 is a placeholder since the backend determines the user from the session cookie.
     */
    suspend fun validateCurrentSession(): ApiResponse<User> {
        return api.getProfile(0) // The backend will use the session cookie, not this ID.
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

    // Contacts

    suspend fun getContacts() = api.getContacts()

    suspend fun requestContact(targetUserId: Int) =
        api.requestContact(mapOf("targetUserId" to targetUserId))

    suspend fun acceptContact(acceptedUserId: Int) =
        api.acceptContact(mapOf("acceptedUserId" to acceptedUserId))

    suspend fun removeContact(removableUserId: Int) = api.removeContact(removableUserId)

    // Tiles

    suspend fun createTile(tile: Tile) = api.createTile(tile)

    suspend fun readTile(ownerId: Int, x: Int, y: Int) = api.readTile(ownerId, x, y)

    suspend fun updateTile(updates: Map<String, @JvmSuppressWildcards Any?>) = api.updateTile(updates)

    suspend fun deleteTile(tileId: Int) = api.deleteTile(tileId)

    // Messages

    suspend fun createMessage(
        ownerId: Int,
        x: Int,
        y: Int,
        messageSet: List<MessageSet>
    ): ApiResponse<Unit> {
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


    // Websockets

    /**
     * A Flow that emits incoming messages from the WebSocket.
     * ViewModel can collect this flow to receive real-time updates.
     */
    val updates = webSocketClient.messages

    /**
     * Connects the WebSocket client to the server.
     * @param url The WebSocket URL
     */
    fun startListeningForUpdates(url: String) {
        webSocketClient.connect(url)
    }

    /**
     * Disconnects the WebSocket client.
     */
    fun stopListeningForUpdates() {
        webSocketClient.disconnect()
    }
}