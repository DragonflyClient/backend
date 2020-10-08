package modules.authentication.util

import DragonflyBackend
import at.favre.lib.crypto.bcrypt.BCrypt
import core.checkedError
import log
import modules.authentication.util.models.*
import org.litote.kmongo.coroutine.updateOne
import org.litote.kmongo.eq
import java.util.*

/**
 * The base authentication class connects to the database and manages the accounts that
 * are stored in it.
 */
object AuthenticationManager {

    /** The Dragonfly database */
    private val database = DragonflyBackend.mongo.getDatabase("dragonfly")

    /** The collection in which the [accounts][Account] are stored */
    val accountsCollection = database.getCollection<Account>("accounts")

    /** The collection that manages the uuids */
    private val uuidsCollection = database.getCollection<UUIDs>("uuids")

    private val emailVerification = database.getCollection<EmailVerificationDocument>("email-verification")

    /**
     * Registers a new account with the [username] and [password].
     *
     * This function checks if the [username] is already in use and encrypts the password using
     * a hash function with a randomized salt and inserts the created [Account] into
     * the [database].
     */
    suspend fun register(email: String, username: String, password: String): Account {
        validateInput(username, password)
        if (getByUsername(username) != null) checkedError("An account with the given username ('$username') does already exist!")
        if (getByEmail(email) != null) checkedError("An account with the given email ('$email') does already exist!")

        val document = emailVerification.findOne(EmailVerificationDocument::email eq email)
            ?.takeIf { it.status == "confirmed" }
            ?: checkedError("No email verification document found!")
        val encryptedPassword = BCrypt.withDefaults().hashToString(12, password.toCharArray())
        val account = Account(
            identifier = username.toLowerCase(),
            uuid = generateUUID(),
            email = email,
            username = username,
            password = encryptedPassword,
            creationDate = System.currentTimeMillis(),
            permissionLevel = PermissionLevel.USER.value
        )

        log("Created account $account")
        accountsCollection.insertOne(account)

        document.status = "account_created"
        emailVerification.updateOne(document)
        return account
    }

    /**
     * Verifies the [usernameOrEmail] and [password] credentials.
     *
     * Returns a [Account] from the [database] to which the credentials apply if found or
     * null if the username or password isn't correct.
     */
    suspend fun verify(usernameOrEmail: String, password: String): Account? {
        val account = if (usernameOrEmail.contains("@")) {
            getByEmail(usernameOrEmail)
        } else {
            getByUsername(usernameOrEmail)?.takeIf { it.username.equals(usernameOrEmail, ignoreCase = false) }
        } ?: return null

        val verified = BCrypt.verifyer().verify(password.toCharArray(), account.password).verified
        return account.takeIf { verified }
    }

    /**
     * Verifies that the given [email] has been confirmed and that the [code] matches the
     * confirmation code.
     */
    suspend fun verifyEmail(email: String?, code: String): Boolean {
        if (email == null) return false
        if (getByEmail(email) != null) checkedError("An account with the given email ('$email') does already exist!")

        val document = emailVerification.findOne(EmailVerificationDocument::email eq email) ?: return false
        if (document.code != code) return false
        if (document.status != "confirmed") return false
        return true
    }

    /**
     * Returns a [Account] from the [database] by its [username][Account.username].
     */
    suspend fun getByUsername(username: String) = accountsCollection.findOne(Account::identifier eq username.toLowerCase())

    /**
     * Returns a [Account] from the [database] by its [email][Account.email].
     */
    suspend fun getByEmail(email: String) = accountsCollection.findOne(Account::email eq email)

    /**
     * Returns a [Account] from the [database] by its [uuid][Account.uuid].
     */
    suspend fun getByUUID(uuid: String) = accountsCollection.findOne(Account::uuid eq uuid)

    /**
     * Validates the length of the [username] and [password].
     */
    private fun validateInput(username: String, password: String) {
        if (!username.matches(Regex("[a-zA-Z0-9]*"))) checkedError("Username must only contain numbers and letters")
        if (username.equals("master", ignoreCase = true)) checkedError("Username is not valid!")
        if (username.length !in 4..16) checkedError("The username must have between 4 and 16 characters")
        if (password.length !in 10..30) checkedError("The password must have between 10 and 30 characters")
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
