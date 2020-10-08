package modules.authentication.util

import io.ktor.auth.*
import org.bson.types.ObjectId

/**
 * A Dragonfly account that is stored in the database and applies to all Dragonfly products.
 *
 * @param identifier unique identifier for the account (the username in lowercase)
 * @param uuid a unique identifier for the account that remains used after the account has been deleted
 * @param username the username of the account
 * @param password a hash representation of password of the account
 * @param creationDate the date on which the account was created (milliseconds since January 1, 1970 UTC)
 * @param permissionLevel the level of permissions that the account has (see [PermissionLevel])
 */
data class Account(
    val _id: ObjectId? = null,
    val identifier: String,
    val uuid: String,
    val email: String? = null,
    val username: String,
    val password: String,
    val creationDate: Long,
    val permissionLevel: Int,
    var linkedMinecraftAccounts: List<String>? = null,
    var enable2FA: Boolean = false,
    var secret2FA: String? = null
) : Principal
