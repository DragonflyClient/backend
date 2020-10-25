package core

import AccountAttributeKey
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.util.date.*
import io.ktor.util.pipeline.*
import modules.authentication.util.JwtConfig
import modules.authentication.util.models.Account
import kotlin.random.Random

typealias CallContext = PipelineContext<Unit, ApplicationCall>

suspend fun CallContext.success(vararg properties: Pair<String, Any?>) = json("success" to true, *properties)

suspend fun <K, V> CallContext.json(vararg pairs: Pair<K, V>) = call.respond(mapOf(*pairs))

suspend fun CallContext.json(code: HttpStatusCode = HttpStatusCode.OK, block: JsonBuilder.() -> Unit) {
    val builder = JsonBuilder()
    builder.block()
    call.respond(code, builder.map)
}

fun checkedError(message: Any?, code: HttpStatusCode = HttpStatusCode.InternalServerError): Nothing {
    throw CheckedErrorException(message.toString(), code)
}

fun Any?.shouldBe(expected: Any?) = if (this == expected) null else CheckedErrorBuilder()

fun CallContext.requireAccount() = call.account ?: checkedError("Unauthenticated", HttpStatusCode.Unauthorized)

val ApplicationCall.account: Account?
    get() = attributes.getOrNull(AccountAttributeKey)

suspend fun CallContext.respondAccount(account: Account?) {
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

suspend fun CallContext.respondToken(account: Account) {
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
            "SameSite" to "Lax"
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
        "uuid" * uuid
        "username" * username
        "email" * email
        "creationDate" * creationDate
        "permissionLevel" * permissionLevel
        "linkedMinecraftAccounts" * linkedMinecraftAccounts
    }
}

class CheckedErrorBuilder {
    fun orError(message: Any?) {
        checkedError(message, HttpStatusCode.InternalServerError)
    }
}