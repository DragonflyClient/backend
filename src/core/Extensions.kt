package core

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.util.date.*
import io.ktor.util.pipeline.*
import modules.authentication.util.Account
import modules.authentication.util.JwtConfig
import kotlin.random.Random

typealias RouteContext = PipelineContext<Unit, ApplicationCall>

suspend fun RouteContext.success() = json("success" to true)

suspend fun <K, V> RouteContext.json(vararg pairs: Pair<K, V>) = call.respond(mapOf(*pairs))

suspend fun RouteContext.json(block: JsonBuilder.() -> Unit) {
    val builder = JsonBuilder()
    builder.block()
    call.respond(builder.map)
}

suspend fun RouteContext.respondAccount(account: Account?) {
    if (account == null) {
        json {
            "success" * false
            "error" * "Unauthenticated"
        }
    } else {
        json {
            "success" * true
            "identifier" * account.identifier
            "username" * account.username
            "creationDate" * account.creationDate
            "permissionLevel" * account.permissionLevel
        }
    }
}

suspend fun RouteContext.respondToken(account: Account) {
    val token = JwtConfig.makeToken(account)

    call.response.cookies.append(Cookie(
        name = "dragonfly-token",
        value = token,
        httpOnly = true,
        secure = true,
        expires = GMTDate(System.currentTimeMillis() + (1000 * 60 * 60 * 24 * 30L)), // 30 days
        domain = "playdragonfly.net",
        path = "/",
        extensions = mapOf(
            "SameSite" to "Strict"
        )
    ))

    call.respond(mapOf(
        "success" to true,
        "identifier" to account.identifier,
        "username" to account.username,
        "creationDate" to account.creationDate,
        "permissionLevel" to account.permissionLevel
    ))
}

class JsonBuilder {
    val map = mutableMapOf<String, Any?>()

    operator fun String.times(value: Any?): Int {
        map[this] = value
        return Random.nextInt()
    }
}