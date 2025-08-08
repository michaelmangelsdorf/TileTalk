
package org.swirlsea.tiletalk.data

import com.google.gson.annotations.SerializedName
import java.util.Date

data class User(
    val id: Int,
    val username: String,
    @SerializedName("public_key") val publicKey: String? = null
)

data class LoginResponse(
    @SerializedName("userId") val userId: Int,
    @SerializedName("userName") val userName: String
)

data class ContactList(
    @SerializedName("userId") val userId: Int,
    @SerializedName("contacts") val contacts: List<Int>,
    @SerializedName("pending") val pending: List<Int>,
    @SerializedName("incoming") val incoming: List<Int>
)

data class Tile(
    val id: Int,
    @SerializedName("owner_id") val owner_id: Int,
    @SerializedName("x_coord") val x_coord: Int,
    @SerializedName("y_coord") val y_coord: Int,
    @SerializedName("starter_id") val starter_id: Int,
    val symbol: String?,
    @SerializedName("animation_type") val animation_type: Int,

    val flip: Boolean?,
    val tile_bg: Int?,
    val callout: String?,
    val title: String?
)

data class Message(
    @SerializedName("responder_id") val responder_id: Int,
    @SerializedName("payload") val payload: CryptogramPayload,
    @SerializedName("created_at") val createdAt: String, // Added this field
    @SerializedName("seen") val seen: Boolean? // Added this field
)

data class CryptogramPayload(
    @SerializedName("ivBase64") val ivBase64: String,
    @SerializedName("encryptedAesKeyBase64") val encryptedAesKeyBase64: String,
    @SerializedName("encryptedDataBytesBase64") val encryptedDataBytesBase64: String
)

data class MessageSet(
    @SerializedName("recipient_id") val recipient_id: Int,
    @SerializedName("payload") val payload: CryptogramPayload
)

data class CreateMessageRequest(
    @SerializedName("owner_id") val ownerId: Int,
    @SerializedName("x_coord") val xCoord: Int,
    @SerializedName("y_coord") val yCoord: Int,
    @SerializedName("message_set") val messageSet: List<MessageSet>
)


data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T? = null
)