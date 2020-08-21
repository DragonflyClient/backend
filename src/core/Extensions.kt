package core

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.util.date.*
import io.ktor.util.pipeline.*
import modules.authentication.util.Account
import modules.authentication.util.JwtConfig
import kotlin.random.Random

typealias Call = PipelineContext<Unit, ApplicationCall>

suspend fun Call.success() = json("success" to true)

suspend fun <K, V> Call.json(vararg pairs: Pair<K, V>) = call.respond(mapOf(*pairs))

suspend fun Call.json(block: JsonBuilder.() -> Unit) {
    val builder = JsonBuilder()
    builder.block()
    call.respond(builder.map)
}

suspend fun Call.respondAccount(account: Account?) {
    if (account == null) {
        json {
            "success" * false
            "error" * "Unauthenticated"
        }
    } else {
        json {
            "success" * true
            +account
        }
    }
}

suspend fun Call.respondToken(account: Account) {
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

    respondAccount(account)
}

class JsonBuilder {
    val map = mutableMapOf<String, Any?>()

    operator fun String.times(value: Any?): Int {
        map[this] = value
        return Random.nextInt()
    }

    operator fun Account.unaryPlus() {
        "identifier" * identifier
        "uuid" * uuid
        "username" * username
        "creationDate" * creationDate
        "permissionLevel" * permissionLevel
        "linkedMinecraftAccounts" * linkedMinecraftAccounts
    }
}