import com.fasterxml.jackson.databind.SerializationFeature
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.firestore.Firestore
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.cloud.FirestoreClient
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.jackson.*
import io.ktor.response.*
import io.ktor.routing.*
import keys.routes.keysAttach
import keys.routes.keysValidate
import keys.routes.master.keysMasterGenerate
import keys.routes.master.keysMaterRequest
import java.io.FileInputStream

/**
 * The main class of the Dragonfly backend server.
 */
object DragonflyBackend {

    /**
     * The Firestore instance that is used as a database for the backend.
     */
    lateinit var firestore: Firestore

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

    install(ContentNegotiation) {
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT)
        }
    }

    install(StatusPages) {
        exception<Throwable> {
            call.respond(HttpStatusCode.InternalServerError)
        }
    }

    install(Authentication) {
        basic {
            realm = "Master Authentication"
            validate { if (it.name == "master" && it.password == "JRa5zHzyft4ikNAKGhAbAdStp7HzX97qomzSPDdo") UserIdPrincipal("master") else null }
        }
    }

    routing {
        get {
            call.respond(
                mapOf(
                    "available" to true
                )
            )
        }

        keysMasterGenerate()
        keysMaterRequest()
        keysAttach()
        keysValidate()
    }
}