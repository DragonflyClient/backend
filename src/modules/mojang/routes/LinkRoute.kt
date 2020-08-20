package modules.mojang.routes

import core.Call
import core.ModuleRoute
import io.ktor.http.*

object LinkRoute : ModuleRoute("link", HttpMethod.Post, "jwt") {

    override suspend fun Call.handleCall() {

    }
}