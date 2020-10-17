package core

import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.routing.*
import log

/**
 * A module that is used to structure the backend into several groups.
 *
 * @param name The name of this module
 * @param routes All routes that belong to this module
 */
open class Module(val name: String, vararg val routes: ModuleRoute)

/**
 * Enables the given [module] for the whole application in the given routing context.
 */
fun Routing.enable(module: Module) {
    log("* ${module.name}")
    module.routes.forEach {
        with(it) {
            route("v${version()}") {
                createRoute(module, it)
            }
        }
    }
}

/**
 * Creates the route for the given [module route][module] of the [module] while respecting the authentication
 * specifications.
 */
private fun Route.createRoute(module: Module, route: ModuleRoute) {
    if (route.authentication != null) {
        authenticate(route.authentication, optional = route.isAuthenticationOptional) {
            buildRoute(module, route)
        }
    } else {
        buildRoute(module, route)
    }
}

/**
 * Builds the route for the given [module route][route] of the [module].
 */
private fun Route.buildRoute(module: Module, route: ModuleRoute) {
    route(module.name.toLowerCase()) {
        if (route.method != null) {
            route(route.path, route.method) {
                log(" - ${route.method.pretty()} /v${route.version()}/${module.name.toLowerCase()}/${route.path}")
                handle {
                    with(route) { handleCall() }
                }
            }
        } else {
            route(route.path) {
                log(" - ROUTE   /v${route.version()}/${module.name.toLowerCase()}/${route.path}")
                with(route) {
                    setup()
                }
            }
        }
    }
}

/**
 * Convenience function to expand a string to a specific length.
 */
private fun String.fill(amountOfChars: Int): String {
    if (length >= amountOfChars) return this
    return this + " ".repeat(amountOfChars - length)
}

/**
 * Prettifies the HTTP method for logging.
 */
private fun HttpMethod.pretty() = value.toUpperCase().fill(7)
