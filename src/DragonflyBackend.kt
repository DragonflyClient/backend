import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.firestore.Firestore
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.cloud.FirestoreClient
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
import java.io.FileInputStream
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * The main class of the Dragonfly backend server.
 */
object DragonflyBackend {

    /**
     * The Firestore instance that is used as a database for the backend.
     */
    lateinit var firestore: Firestore

    /**
     * A coroutine-based KMongo client to connect to the database
     */
    val mongo = KMongo.createClient(CONNECTION_STRING).coroutine

    /**
     * Initializes the Cloud Firestore instance by authorizing using the admin key and
     * initializing the [FirebaseApp].
     */
    fun initializeFirestore() {
        val serviceAccount = FileInputStream("firebase-admin-key.json")
        val options = FirebaseOptions.Builder()
            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
            .build()

        FirebaseApp.initializeApp(options)
        firestore = FirestoreClient.getFirestore()
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
    DragonflyBackend.initializeFirestore()
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
