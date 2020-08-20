import core.enable
import input.InputListener
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import modules.authentication.AuthenticationModule
import modules.authentication.util.JwtConfig
import modules.keys.KeysModule
import modules.version.VersionModule
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo
import org.slf4j.event.Level
import secrets.CONNECTION_STRING
import secrets.KEYS_MASTER_PASSWORD
import java.text.DateFormat

/**
 * The main class of the Dragonfly backend server.
 */
object DragonflyBackend {

    /**
     * A coroutine-based KMongo client to connect to the database
     */
    val mongo = KMongo.createClient(CONNECTION_STRING).coroutine

    /**
     * The instance of the ktor application
     */
    lateinit var application: Application
}

/**
 * Logs the given [message] with some additional information to the console.
 */
fun log(message: String, level: Level = Level.INFO) = with(DragonflyBackend.application.log) {
    when (level) {
        Level.ERROR -> error(message)
        Level.WARN -> warn(message)
        Level.INFO -> info(message)
        Level.DEBUG -> debug(message)
        Level.TRACE -> trace(message)
    }
}

/**
 * The main module of the application.
 *
 * By the ktor convention, the entry point of a ktor server is an extension function for
 * the [Application] type called module.
 */
@Suppress("unused") // will be called by ktor
fun Application.main() {
    DragonflyBackend.application = this
    InputListener.startListening()

    install(CallLogging) {
        level = Level.INFO
    }

    install(CORS) {
        method(HttpMethod.Get)
        method(HttpMethod.Post)
        header(HttpHeaders.AccessControlAllowHeaders)
        header(HttpHeaders.AccessControlAllowOrigin)
        header(HttpHeaders.ContentType)

        host("inceptioncloud.net", schemes = listOf("https"))
        host("playdragonfly.net", schemes = listOf("https"))
        host("www.playdragonfly.net", schemes = listOf("https"))
        host("ideas.playdragonfly.net", schemes = listOf("https"))
        hosts.add("null")

        header("Authorization")
        allowCredentials = true
    }

    install(ContentNegotiation) {
        gson {
            setDateFormat(DateFormat.LONG)
            setPrettyPrinting()
        }
    }

    install(StatusPages) {
        exception<Throwable> {
            call.respond(mapOf(
                "success" to false,
                "error" to it.message
            ))
        }
    }

    install(Authentication) {
        basic(name = "master") {
            realm = "Master Authentication"
            validate {
                if (it.name == "master" && it.password == KEYS_MASTER_PASSWORD)
                    UserIdPrincipal("master")
                else null
            }
        }
        basic(name = "dragonfly-account") {
            realm = "Dragonfly Account Authentication"
            validate {
                modules.authentication.util.Authentication.verify(it.name, it.password)
                    ?.let { account -> UserIdPrincipal(account.username) }
            }
        }
        jwt(name = "jwt") {
            verifier(JwtConfig.verifier)
            realm = "inceptioncloud.net"
            validate {
                it.payload.getClaim("uuid").asString().let { uuid -> modules.authentication.util.Authentication.getByUUID(uuid) }
            }
        }
    }

    routing {
        get {
            call.respond(mapOf(
                "available" to true
            ))
        }

        enable(AuthenticationModule)
        enable(KeysModule)
        enable(VersionModule)
    }
}