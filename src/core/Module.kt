package core

import io.ktor.routing.*
import modules.authentication.AuthModule.provideRouting

abstract class Module {

    abstract fun Routing.provideRouting()
}

fun Routing.enable(module: Module) = with(this) { provideRouting() }