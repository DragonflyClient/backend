package core

import io.ktor.http.*
import io.ktor.routing.*

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
    val method: HttpMethod? = null,
    val authentication: String? = null,
    val isAuthenticationOptional: Boolean = false
) {
    /**
     * Handles all calls to this specific route
     */
    open suspend fun CallContext.handleCall() {}

    /**
     * Sets up a more complex route by defining the different HTTP methods inside of this function.
     */
    open fun Route.setup() {}

    /**
     * The version of this route that is prepended to the path
     */
    open fun version(): Int = 1

    /**
     * Can return the path for a legacy route to keep support for older versions of Dragonfly.A
     */
    open fun getLegacyRoute(): String? = null
}