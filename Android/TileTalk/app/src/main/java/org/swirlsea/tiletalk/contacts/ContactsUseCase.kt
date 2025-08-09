package org.swirlsea.tiletalk.contacts

import android.util.Log
import org.swirlsea.tiletalk.data.ApiResponse
import org.swirlsea.tiletalk.data.ContactList
import org.swirlsea.tiletalk.data.TileTalkRepository
import org.swirlsea.tiletalk.data.User

class ContactsUseCase(private val repository: TileTalkRepository) {

    suspend fun fetchContacts(): ApiResponse<ContactList> {
        return try {
            repository.getContacts()
        } catch (e: Exception) {
            Log.e("ContactsUseCase", "Error fetching contacts", e)
            ApiResponse(success = false, message = "Error fetching contacts: ${e.message}")
        }
    }

    suspend fun requestContact(targetUsername: String): ApiResponse<User> {
        return try {
            val profileResponse = repository.getProfileByUsername(targetUsername)
            if (profileResponse.success && profileResponse.data != null) {
                repository.requestContact(profileResponse.data.id)
            }
            profileResponse
        } catch (e: Exception) {
            Log.e("ContactsUseCase", "Error requesting contact", e)
            ApiResponse(success = false, message = "Error requesting contact: " + e.message)
        }
    }

    suspend fun acceptContact(requesterId: Int): ApiResponse<Unit> {
        return repository.acceptContact(requesterId)
    }

    suspend fun removeContact(removableUserId: Int): ApiResponse<Unit> {
        return repository.removeContact(removableUserId)
    }
}