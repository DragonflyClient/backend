package modules.version.routes

import com.google.gson.JsonObject
import core.ModuleRoute
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.util.pipeline.*
import modules.version.util.VersionManager.earlyAccess
import modules.version.util.VersionManager.stable
import modules.version.util.update.UpdateChannel
import modules.version.util.update.UpdateChannel.STABLE
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set

/**
 * Enables a route that the client or installer can send request to to get information about the
 * latest Dragonfly version.
 */
object VersionRoute : ModuleRoute("/", HttpMethod.Get) {

    override suspend fun PipelineContext<Unit, ApplicationCall>.handleCall() {
        if (call.parameters.contains("channel")) {
            val channel = UpdateChannel.getByIdentifier(call.parameters["channel"]!!)
            val jsonObject = if (channel == STABLE) stable else earlyAccess

            call.respond(jsonObject.toMap())
        } else {
            error("Missing information")
        }
    }
}

/**
 * Converts the given json object to a simple map of strings and objects.
 */
private fun JsonObject.toMap(): Map<String, Any> {
    val map = mutableMapOf<String, Any>()
    for ((key, value) in entrySet())
        map[key] = value as Any
    return map
}