package modules.version.routes

import com.google.gson.JsonObject
import core.ModuleRoute
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import modules.version.util.VersionManager.earlyAccess
import modules.version.util.VersionManager.stable
import modules.version.util.update.UpdateChannel
import modules.version.util.update.UpdateChannel.STABLE

/**
 * Enables a route that the client or installer can send request to to get information about the
 * latest Dragonfly version.
 */
object VersionRoute : ModuleRoute {

    override fun Routing.provideRoute() {
        get("/version") {
            if (call.parameters.contains("channel")) {
                val channel = UpdateChannel.getByIdentifier(call.parameters["channel"]!!)
                val jsonObject = if (channel == STABLE) stable else earlyAccess

                call.respond(jsonObject.toMap())
            } else {
                call.respond(mapOf("error" to "Missing information"))
            }
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