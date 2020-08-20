package core

import io.ktor.http.*

abstract class ModuleRoute(
    val route: String,
    val method: HttpMethod,
    val authentication: String? = null,
    val optional: Boolean = false
) {
    abstract suspend fun Call.handleCall()

    open fun legacyRoute(): String? = null

    open fun version(): Int = 1
}