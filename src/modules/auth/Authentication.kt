package modules.auth

import at.favre.lib.crypto.bcrypt.BCrypt
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.eq
import org.litote.kmongo.reactivestreams.KMongo
import secrets.CONNECTION_STRING

/**
 * The base authentication class connects to the database and manages the accounts that
 * are stored in it.
 */
object Authentication {

    /** A coroutine-based KMongo client to connect to the database */
    private val client = KMongo.createClient(CONNECTION_STRING).coroutine

    /** The Dragonfly database */
    private val database = client.getDatabase("dragonfly")

    /** The collection in which the [accounts][Account] are stored */
    private val collection = database.getCollection<Account>("accounts")

    /**
     * Registers a new account with the [username] and [password].
     *
     * This function checks if the [username] is already in use and encrypts the password using
     * a hash function with a randomized salt and inserts the created [Account] into
     * the [database].
     */
    suspend fun register(username: String, password: String) {
        validateInput(username, password)
        if (getByUsername(username) != null)
            throw IllegalArgumentException("An account with the given username ('$username') does already exist!")

        val encryptedPassword = BCrypt.withDefaults().hashToString(12, password.toCharArray())
        val account = Account(
            identifier = username.toLowerCase(),
            username = username,
            password = encryptedPassword,
            creationDate = System.currentTimeMillis(),
            permissionLevel = PermissionLevel.USER.value
        )

        collection.insertOne(account)
    }

    /**
     * Verifies the [username] and [password] credentials.
     *
     * Returns a [Account] from the [database] to which the credentials apply if found or
     * null if the username or password isn't correct.
     */
    suspend fun verify(username: String, password: String): Account? {
        val account = getByUsername(username)?.takeIf { it.username.equals(username, ignoreCase = false) } ?: return null
        val verified = BCrypt.verifyer().verify(password.toCharArray(), account.password).verified

        return account.takeIf { verified }
    }

    /**
     * Validates the length of the [username] and [password].
     */
    fun validateInput(username: String, password: String) {
        require(username.matches(Regex("[a-zA-Z0-9]*"))) { "Username must only contain numbers and letters" }
        require(!username.equals("master", ignoreCase = true)) { "Username is not valid!" }
        require(username.length in 4..16) { "The username must have between 4 and 16 characters" }
        require(password.length in 10..30) { "The password must have between 10 and 30 characters" }
    }

    /**
     * Returns a [Account] from the [database] by its username.
     */
    suspend fun getByUsername(username: String) =
        collection.findOne(Account::identifier eq username.toLowerCase())
}