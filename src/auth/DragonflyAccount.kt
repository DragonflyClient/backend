package auth

import io.ktor.auth.*

/**
 * A Dragonfly account that is stored in the database and applies to all Dragonfly products.
 *
 * @param identifier unique identifier for the account (the username in lowercase)
 * @param username the username of the account
 * @param password a hash representation of password of the account
 * @param creationDate the date on which the account was created (milliseconds since January 1, 1970 UTC)
 * @param permissionLevel the level of permissions that the account has (see [PermissionLevel])
 */
data class DragonflyAccount(
    val identifier: String,
    val username: String,
    val password: String,
    val creationDate: Long,
    val permissionLevel: Int
) : Principal