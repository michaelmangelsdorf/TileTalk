package org.swirlsea.tiletalk

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.swirlsea.tiletalk.data.CryptogramPayload
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.security.*
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.io.encoding.ExperimentalEncodingApi


/**
 * A utility object for handling end-to-end encryption operations.
 * It uses a hybrid encryption scheme:
 * - RSA for asymmetric key exchange (encrypting a symmetric key).
 * - AES for symmetric data encryption (encrypting the message payload).
 *
 * Keys are stored securely using EncryptedSharedPreferences.
 */
@OptIn(ExperimentalEncodingApi::class)
object CryptoUtils {

    private const val RSA_ALGORITHM = "RSA/ECB/PKCS1Padding"
    private const val AES_ALGORITHM = "AES/GCM/NoPadding"
    private const val KEY_ALIAS_PREFIX = "tile_talk_key_alias_"
    private const val AES_KEY_SIZE = 256
    private const val GCM_IV_LENGTH = 12 // bytes
    private const val GCM_TAG_LENGTH = 128 // bits
    private const val KEY_PREFS_FILENAME = "secure_rsa_key_prefs"

    private fun getAliasForUser(username: String) = "$KEY_ALIAS_PREFIX$username"

    private fun getEncryptedPrefs(context: Context): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            context,
            KEY_PREFS_FILENAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    /**
     * Checks if a key pair exists in EncryptedSharedPreferences for a specific user.
     */
    fun keyPairExists(context: Context, username: String): Boolean {
        val prefs = getEncryptedPrefs(context)
        return prefs.contains(getAliasForUser(username) + "_public")
    }

    /**
     * Saves a key pair to EncryptedSharedPreferences.
     */
    private fun saveKeyPair(context: Context, username: String, keyPair: KeyPair) {
        val prefs = getEncryptedPrefs(context)
        val alias = getAliasForUser(username)
        prefs.edit()
            .putString(alias + "_public", publicKeyToString(keyPair.public))
            .putString(alias + "_private", privateKeyToString(keyPair.private))
            .apply()
    }

    /**
     * Generates a new software-based RSA key pair.
     * This key is exportable because it is not tied to the AndroidKeyStore.
     */
    fun generateRsaKeyPair(): KeyPair {
        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(2048)
        return keyPairGenerator.generateKeyPair()
    }

    /**
     * Generates a new RSA key pair and saves it for the given user.
     */
    fun generateAndSaveRsaKeyPair(context: Context, username: String): KeyPair {
        val keyPair = generateRsaKeyPair()
        saveKeyPair(context, username, keyPair)
        return keyPair
    }


    /**
     * Retrieves the private key for a specific user from EncryptedSharedPreferences.
     */
    fun getPrivateKey(context: Context, username: String): PrivateKey? {
        return loadKeyPair(context, username)?.private
    }

    /**
     * Retrieves the key pair for a specific user from EncryptedSharedPreferences.
     */
    fun getKeyPair(context: Context, username: String): KeyPair? {
        return loadKeyPair(context, username)
    }

    /**
     * Loads a KeyPair from EncryptedSharedPreferences.
     */
    private fun loadKeyPair(context: Context, username: String): KeyPair? {
        val prefs = getEncryptedPrefs(context)
        val alias = getAliasForUser(username)
        val pubKeyStr = prefs.getString(alias + "_public", null)
        val privKeyStr = prefs.getString(alias + "_private", null)

        return if (pubKeyStr != null && privKeyStr != null) {
            KeyPair(stringToPublicKey(pubKeyStr), stringToPrivateKey(privKeyStr))
        } else {
            null
        }
    }


    /**
     * Converts a PublicKey object to a Base64 encoded string for storage or transmission.
     */
    fun publicKeyToString(publicKey: PublicKey): String {
        return Base64.encodeToString(publicKey.encoded, Base64.NO_WRAP)
    }

    /**
     * Converts a Base64 encoded string back into a PublicKey object.
     */
    fun stringToPublicKey(publicKeyString: String): PublicKey {
        val keyBytes = Base64.decode(publicKeyString, Base64.NO_WRAP)
        val keySpec = X509EncodedKeySpec(keyBytes)
        val keyFactory = KeyFactory.getInstance("RSA")
        return keyFactory.generatePublic(keySpec)
    }

    /**
     * Converts a PrivateKey object to a Base64 encoded string for storage or transmission.
     */
    fun privateKeyToString(privateKey: PrivateKey): String {
        return Base64.encodeToString(privateKey.encoded, Base64.NO_WRAP)
    }

    /**
     * Converts a Base64 encoded string back into a PrivateKey object.
     */
    fun stringToPrivateKey(privateKeyString: String): PrivateKey {
        val privateBytes = Base64.decode(privateKeyString, Base64.NO_WRAP)
        val keySpec = PKCS8EncodedKeySpec(privateBytes)
        val keyFactory = KeyFactory.getInstance("RSA")
        return keyFactory.generatePrivate(keySpec)
    }


    /**
     * Encrypts a plaintext message for a recipient using hybrid encryption.
     */
    fun encryptPayload(plaintext: String, recipientPublicKey: PublicKey): CryptogramPayload {
        val aesKey = KeyGenerator.getInstance("AES").run {
            init(AES_KEY_SIZE)
            generateKey()
        }

        val rsaCipher = Cipher.getInstance(RSA_ALGORITHM)
        rsaCipher.init(Cipher.ENCRYPT_MODE, recipientPublicKey)
        val encryptedAesKeyBytes = rsaCipher.doFinal(aesKey.encoded)

        val aesCipher = Cipher.getInstance(AES_ALGORITHM)
        aesCipher.init(Cipher.ENCRYPT_MODE, aesKey)
        val ivBytes = aesCipher.iv
        val encryptedDataBytes = aesCipher.doFinal(plaintext.toByteArray())

        return CryptogramPayload(
            ivBase64 = Base64.encodeToString(ivBytes, Base64.NO_WRAP),
            encryptedAesKeyBase64 = Base64.encodeToString(encryptedAesKeyBytes, Base64.NO_WRAP),
            encryptedDataBytesBase64 = Base64.encodeToString(encryptedDataBytes, Base64.NO_WRAP)
        )
    }

    /**
     * Decrypts a CryptogramPayload using the specified user's private key.
     */
    fun decryptPayload(payload: CryptogramPayload, context: Context, username: String): String? {
        val privateKey = getPrivateKey(context, username) ?: return null

        try {
            val encryptedAesKeyBytes = Base64.decode(payload.encryptedAesKeyBase64, Base64.NO_WRAP)
            val ivBytes = Base64.decode(payload.ivBase64, Base64.NO_WRAP)
            val encryptedDataBytes = Base64.decode(payload.encryptedDataBytesBase64, Base64.NO_WRAP)

            val rsaCipher = Cipher.getInstance(RSA_ALGORITHM)
            rsaCipher.init(Cipher.DECRYPT_MODE, privateKey)
            val decryptedAesKeyBytes = rsaCipher.doFinal(encryptedAesKeyBytes)
            val aesKey: SecretKey = SecretKeySpec(decryptedAesKeyBytes, "AES")

            val gcmParamSpec = GCMParameterSpec(GCM_TAG_LENGTH, ivBytes)
            val aesCipher = Cipher.getInstance(AES_ALGORITHM)
            aesCipher.init(Cipher.DECRYPT_MODE, aesKey, gcmParamSpec)
            val decryptedDataBytes = aesCipher.doFinal(encryptedDataBytes)

            return String(decryptedDataBytes)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    /**
     * Deletes the keypair for a specific user from EncryptedSharedPreferences.
     */
    fun deleteKeyPair(context: Context, username: String) {
        val prefs = getEncryptedPrefs(context)
        val alias = getAliasForUser(username)
        prefs.edit()
            .remove(alias + "_public")
            .remove(alias + "_private")
            .apply()
    }

    /**
     * Handles the export of a key pair to a user-selected file.
     */
    fun handleExportKeyfile(
        context: Context,
        keyPairToExport: KeyPair?,
        outputUri: Uri
    ) {
        if (keyPairToExport == null) {
            return
        }

        val publicKeyStr = publicKeyToString(keyPairToExport.public)
        val privateKeyStr = privateKeyToString(keyPairToExport.private)

        val keyMap = mapOf(
            "publicKey" to publicKeyStr,
            "privateKey" to privateKeyStr
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                context.contentResolver.openOutputStream(outputUri)?.use { outputStream ->
                    OutputStreamWriter(outputStream).use { writer ->
                        writer.write(Gson().toJson(keyMap))
                    }
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    /**
     * Handles the import of a key pair from a user-selected file.
     */
    fun handleImportKeyfile(
        context: Context,
        username: String,
        inputUri: Uri,
        onKeyPairImported: suspend (KeyPair?) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            var importedKeyPair: KeyPair? = null
            try {
                val content = context.contentResolver.openInputStream(inputUri)?.use { inputStream ->
                    BufferedReader(InputStreamReader(inputStream)).use { reader ->
                        reader.readText()
                    }
                }

                if (!content.isNullOrEmpty()) {
                    val type = object : TypeToken<Map<String, String>>() {}.type
                    val keyMap: Map<String, String>? = Gson().fromJson(content, type)

                    if (keyMap != null) {
                        val pubKeyStr = keyMap["publicKey"]
                        val privKeyStr = keyMap["privateKey"]

                        if (pubKeyStr != null && privKeyStr != null) {
                            val publicKey = stringToPublicKey(pubKeyStr)
                            val privateKey = stringToPrivateKey(privKeyStr)
                            importedKeyPair = KeyPair(publicKey, privateKey)
                            // Persist the newly imported key
                            saveKeyPair(context, username, importedKeyPair)
                        }
                    }
                }
            } catch (e: Exception) {
                importedKeyPair = null
            } finally {
                withContext(Dispatchers.Main) {
                    onKeyPairImported(importedKeyPair)
                }
            }
        }
    }
}