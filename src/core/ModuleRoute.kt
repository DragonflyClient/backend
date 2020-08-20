package core

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.util.pipeline.*

abstract class ModuleRoute(
    val route: String,
    val method: HttpMethod,
    val authentication: String? = null,
    val optional: Boolean = false
) {
    abstract suspend fun PipelineContext<Unit, ApplicationCall>.handleCall()

    open fun legacyRoute(): String? = null

    open fun version(): Int = 1
}