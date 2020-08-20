package modules.auth.util

import DragonflyBackend
import at.favre.lib.crypto.bcrypt.BCrypt
import log
import org.litote.kmongo.eq
import java.util.*

/**
 * The base authentication class connects to the database and manages the accounts that
 * are stored in it.
 */
object Authentication {

    /** The Dragonfly database */
    private val database = DragonflyBackend.mongo.getDatabase("dragonfly")

    /** The collection in which the [accounts][Account] are stored */
    private val accountsCollection = database.getCollection<Account>("accounts")

    /** The collection that manages the uuids */
    private val uuidsCollection = database.getCollection<UUIDs>("uuids")

    /**
     * Registers a new account with the [username] and [password].
     *
     * This function checks if the [username] is already in use and encrypts the password using
     * a hash function with a randomized salt and inserts the created [Account] into
     * the [database].
     */
    suspend fun register(username: String, password: String): Account {
        validateInput(username, password)
        if (getByUsername(username) != null)
            throw IllegalArgumentException("An account with the given username ('$username') does already exist!")

        val encryptedPassword = BCrypt.withDefaults().hashToString(12, password.toCharArray())
        val account = Account(
            identifier = username.toLowerCase(),
            uuid = generateUUID(),
            username = username,
            password = encryptedPassword,
            creationDate = System.currentTimeMillis(),
            permissionLevel = PermissionLevel.USER.value
        )

        log("Created account $account")

        accountsCollection.insertOne(account)
        return account
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
     * Returns a [Account] from the [database] by its [username][Account.username].
     */
    suspend fun getByUsername(username: String) =
        accountsCollection.findOne(Account::identifier eq username.toLowerCase())

    /**
     * Returns a [Account] from the [database] by its [uuid][Account.uuid].
     */
    suspend fun getByUUID(uuid: String) =
        accountsCollection.findOne(Account::uuid eq uuid)

    /**
     * Validates the length of the [username] and [password].
     */
    private fun validateInput(username: String, password: String) {
        require(username.matches(Regex("[a-zA-Z0-9]*"))) { "Username must only contain numbers and letters" }
        require(!username.equals("master", ignoreCase = true)) { "Username is not valid!" }
        require(username.length in 4..16) { "The username must have between 4 and 16 characters" }
        require(password.length in 10..30) { "The password must have between 10 and 30 characters" }
    }

    /**
     * Generates a random unused UUID and adds it to the used-uuids array.
     */
    private suspend fun generateUUID(): String {
        var uuid = UUID.randomUUID().toString()
        val uuids = uuidsCollection.findOne()

        while (uuids?.used?.contains(uuid) == true) {
            log("UUID $uuid is already in use!")
            uuid = UUID.randomUUID().toString()
        }

        return uuid.also {
            val new = UUIDs(
                uuids?.used?.toMutableList()?.apply { add(uuid) } ?: listOf(uuid)
            )
            uuidsCollection.deleteOne()
            uuidsCollection.insertOne(new)
        }
    }
}
