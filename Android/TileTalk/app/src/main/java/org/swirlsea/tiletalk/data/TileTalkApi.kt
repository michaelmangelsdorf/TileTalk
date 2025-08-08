
package org.swirlsea.tiletalk.data

import retrofit2.http.*

interface TileTalkApi {

    @POST("user/register")
    suspend fun register(@Body body: Map<String, String?>): ApiResponse<User>

    @POST("user/login")
    suspend fun login(@Body body: Map<String, String>): ApiResponse<LoginResponse>

    @GET("user/logout")
    suspend fun logout(): ApiResponse<Unit>

    @DELETE("user/delete")
    suspend fun deleteUser(): ApiResponse<Unit>

    @GET("user/get")
    suspend fun getProfile(@Query("userId") userId: Int): ApiResponse<User> // Returns User object

    @GET("user/get")
    suspend fun getProfileByUsername(@Query("username") username: String): ApiResponse<User>

    @PUT("user/put")
    suspend fun updateProfile(@Query("userId") userId: Int, @Body body: Map<String, String>): ApiResponse<Unit>


    @GET("contacts/list")
    suspend fun getContacts(): ApiResponse<ContactList>

    @POST("contact/request")
    suspend fun requestContact(@Body body: Map<String, Int>): ApiResponse<Unit>

    @POST("contact/accept")
    suspend fun acceptContact(@Body body: Map<String, Int>): ApiResponse<Unit>

    @POST("contact/remove")
    suspend fun removeContact(@Query("removableUserId") userId: Int): ApiResponse<Unit>


    @POST("tile/create")
    suspend fun createTile(@Body tile: Tile): ApiResponse<Int> // Returns the new tile's ID

    @GET("tile/read")
    suspend fun readTile(@Query("owner_id") ownerId: Int, @Query("x_coord") x: Int, @Query("y_coord") y: Int): ApiResponse<Tile>

    @POST("tile/update")
    suspend fun updateTile(@Body body: Map<String, @JvmSuppressWildcards Any?>): ApiResponse<Tile>

    @DELETE("tile/delete")
    suspend fun deleteTile(@Query("tileId") tileId: Int): ApiResponse<Unit>


    @POST("message/create")
    suspend fun createMessage(@Body body: CreateMessageRequest): ApiResponse<Unit>

    @GET("messages/read")
    suspend fun readMessages(@Query("owner_id") ownerId: Int, @Query("x_coord") x: Int, @Query("y_coord") y: Int): ApiResponse<List<Message>>

    @DELETE("message/delete")
    suspend fun deleteMessage(@Query("owner_id") ownerId: Int, @Query("x_coord") x: Int, @Query("y_coord") y: Int): ApiResponse<Unit>
}