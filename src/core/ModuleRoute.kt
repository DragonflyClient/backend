package core

import io.ktor.http.*

/**
 * Represents a route that belongs to a [Module].
 *
 * @param path The path of the route, this should be only one word
 * @param method The HTTP method that this route uses
 * @param authentication An optional authentication provider
 * @param isAuthenticationOptional Whether the previously set authentication provider can be used optionally
 */
abstract class ModuleRoute(
    val path: String,
    val method: HttpMethod,
    val authentication: String? = null,
    val isAuthenticationOptional: Boolean = false
) {
    /**
     * Handles all calls to this specific route
     */
    abstract suspend fun CallContext.handleCall()

    /**
     * The version of this route that is prepended to the path
     */
    open fun version(): Int = 1
}