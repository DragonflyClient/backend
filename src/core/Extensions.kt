package core

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.util.date.*
import io.ktor.util.pipeline.*
import modules.authentication.util.AuthenticationManager
import modules.authentication.util.JwtConfig
import modules.authentication.util.models.Account
import kotlin.random.Random

typealias CallContext = PipelineContext<Unit, ApplicationCall>

suspend fun CallContext.success() = json("success" to true)

suspend fun <K, V> CallContext.json(vararg pairs: Pair<K, V>) = call.respond(mapOf(*pairs))

suspend fun CallContext.json(code: HttpStatusCode = HttpStatusCode.OK, block: JsonBuilder.() -> Unit) {
    val builder = JsonBuilder()
    builder.block()
    call.respond(code, builder.map)
}

fun checkedError(message: Any?, code: HttpStatusCode = HttpStatusCode.InternalServerError): Nothing {
    throw CheckedErrorException(message.toString(), code)
}

suspend fun CallContext.getAccount() = call.getAccount() ?: checkedError("Unauthenticated", HttpStatusCode.Unauthorized)

suspend fun ApplicationCall.getAccount(): Account? {
    var account = authentication.principal<Account>()

    if (account == null) {
        val cookie = request.cookies["dragonfly-token"]
        val token = cookie?.let { JwtConfig.verifier.verify(cookie) }
        account = token?.getClaim("uuid")?.asString()?.let { uuid -> AuthenticationManager.getByUUID(uuid) }
    }

    return account
}

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