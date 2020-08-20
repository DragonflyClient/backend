package core

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.util.pipeline.*
import kotlin.random.Random

typealias RouteContext = PipelineContext<Unit, ApplicationCall>

suspend fun RouteContext.success() = json("success" to true)

suspend fun <K, V> RouteContext.json(vararg pairs: Pair<K, V>) = call.respond(mapOf(*pairs))

suspend fun RouteContext.json(block: JsonBuilder.() -> Unit) {
    val builder = JsonBuilder()
    builder.block()
    call.respond(builder.map)
}

class JsonBuilder {
    val map = mutableMapOf<String, Any>()

    operator fun String.times(value: Any): Int {
        map[this] = value
        return Random.nextInt()
    }
}