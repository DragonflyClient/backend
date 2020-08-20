package core

import io.ktor.routing.*

interface ModuleRoute {
    fun Routing.provideRoute()
}