package core

import io.ktor.auth.*
import io.ktor.routing.*

open class Module(val name: String, vararg val routes: ModuleRoute)

fun Routing.enable(module: Module) = module.routes.forEach {
    with(it) {
        route("v${version()}") {
            makeRoute(module, it)
        }
        if (it.legacyRoute() != null) {
            route(it.legacyRoute()!!) {
                handle {
                    handleCall()
                }
            }
        }
    }
}

private fun Route.makePlainRoute(module: Module, route: ModuleRoute) {
    route(module.name.toLowerCase()) {
        route(route.route, route.method) {
            handle {
                with(route) { handleCall() }
            }
        }
    }
}

private fun Route.makeRoute(module: Module, route: ModuleRoute) {
    if (route.authentication != null) {
        authenticate(route.authentication, optional = route.optional) {
            makePlainRoute(module, route)
        }
    } else {
        makePlainRoute(module, route)
    }
}