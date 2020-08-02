import input.InputListener
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import modules.auth.JwtConfig
import modules.auth.routes.*
import modules.keys.routes.*
import modules.version.routes.*
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo
import org.slf4j.event.Level
import secrets.CONNECTION_STRING
import secrets.KEYS_MASTER_PASSWORD
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * The main class of the Dragonfly backend server.
 */
object DragonflyBackend {

    /**
     * A coroutine-based KMongo client to connect to the database
     */
    val mongo = KMongo.createClient(CONNECTION_STRING).coroutine
}


/**
 * The main module of the application.
 *
 * By the ktor convention, the entry point of a ktor server is an extension function for
 * the [Application] type called module.
 */
@Suppress("unused") // will be called by ktor
fun Application.main() {
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
            it.printStackTrace()
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
                modules.auth.Authentication.verify(it.name, it.password)
                    ?.let { account -> UserIdPrincipal(account.username) }
            }
        }
        jwt(name = "jwt") {
            verifier(JwtConfig.verifier)
            realm = "inceptioncloud.net"
            validate {
                it.payload.getClaim("identifier").asString()
                    ?.let { identifier -> modules.auth.Authentication.getByUsername(identifier) }
            }
        }
    }

    routing {
        get {
            call.respond(mapOf(
                "available" to true
            ))
        }

        routeKeysGenerate()
        routeKeysRequest()
        routeKeysAttach()
        routeKeysValidate()

        routeVersion()
        routeVersionInstaller()
        routeVersionUpdates()
        routeVersionUpdatesHistory()
        routeVersionPublish()

        routeAuth()
        routeAuthLogin()
        routeAuthRegister()
        routeAuthCookie()
        routeAuthCookieLogin()
        routeAuthCookieRegister()
    }
}

/**
 * Logs the given [message] with some additional information to the console.
 */
fun log(message: String) = println(
    "[${SimpleDateFormat("HH:mm:ss.SSS").format(Date())}] " +
            "[${Thread.currentThread().name}: " + message
)
