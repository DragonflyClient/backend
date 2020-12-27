import com.google.gson.*
import core.*
import input.InputListener
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.*
import kotlinx.coroutines.runBlocking
import modules.analytics.AnalyticsModule
import modules.authentication.AuthenticationModule
import modules.authentication.util.AuthenticationManager
import modules.authentication.util.JwtConfig
import modules.authentication.util.models.Account
import modules.community.CommunityModule
import modules.cosmetics.CosmeticsModule
import modules.diagnostics.DiagnosticsModule
import modules.keys.KeysModule
import modules.launcher.LauncherModule
import modules.minecraft.MinecraftModule
import modules.partner.PartnerModule
import modules.store.StoreModule
import modules.version.VersionModule
import org.bson.types.ObjectId
import org.slf4j.event.Level
import secrets.KEYS_MASTER_PASSWORD
import java.lang.reflect.Type
import java.text.DateFormat

/**
 * The main class of the Dragonfly backend server.
 */
object DragonflyBackend {
    /**
     * The instance of the ktor application
     */
    lateinit var application: Application
}

private val silentRoutes = listOf("/v1/cosmetics/find")

val AccountAttributeKey = AttributeKey<Account>("account")

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
        format { call ->
            runBlocking {
                buildString {
                    append(call.response.status() ?: "Unhandled")
                    append(": ")
                    append(call.request.toLogString())
                    append(" from ")
                    append(call.request.header("x-forwarded-for") ?: "unknown")

                    call.account?.let {
                        append(" (")
                        append(it.username)
                        append("#")
                        append(it.uuid)
                        append(")")
                    }
                }
            }
        }
        filter { call -> call.request.path() !in silentRoutes }
    }

    install(CORS) {
        method(HttpMethod.Get)
        method(HttpMethod.Post)
        method(HttpMethod.Patch)
        method(HttpMethod.Put)
        method(HttpMethod.Delete)
        method(HttpMethod.Head)
        header(HttpHeaders.AccessControlAllowHeaders)
        header(HttpHeaders.AccessControlAllowOrigin)
        header(HttpHeaders.ContentType)

        host("inceptioncloud.net", schemes = listOf("https"))
        host("playdragonfly.net", schemes = listOf("https"))
        host("www.playdragonfly.net", schemes = listOf("https"))
        host("ideas.playdragonfly.net", schemes = listOf("https"))
        host("store.playdragonfly.net", schemes = listOf("https"))
        host("dashboard.playdragonfly.net", schemes = listOf("https"))

        header("Authorization")
        allowCredentials = true
    }

    install(ContentNegotiation) {
        gson {
            setDateFormat(DateFormat.LONG)
            setPrettyPrinting()
            registerTypeAdapter(ObjectId::class.java, object : JsonSerializer<ObjectId> {
                override fun serialize(obj: ObjectId?, type: Type?, context: JsonSerializationContext?): JsonElement {
                    return JsonPrimitive(obj?.toHexString())
                }
            })
        }
    }

    install(StatusPages) {
        exception<Throwable> {
            val silent = call.request.path() in silentRoutes

            if (it is CheckedErrorException) {
                call.respond(it.statusCode, mapOf(
                    "success" to false,
                    "error" to it.message,
                    "errorCode" to it.errorCode
                ))

                if (!silent)
                    log("Checked: \"${it.message.toString()}\"", Level.WARN)
            } else {
                call.respond(HttpStatusCode.InternalServerError, mapOf(
                    "success" to false,
                    "error" to "Unhandled exception",
                    "errorCode" to "unhandled_exception"
                ))

                if (!silent)
                    log("Exception: \"${it.message.toString()}\"", Level.ERROR)
            }

            if (!silent && Debugger.isDebuggingEnabled) {
                it.stackTraceToString().split("\n").dropLast(1)
                    .forEach { line -> log(line, Level.ERROR) }
            }
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
        jwt(name = "jwt") {
            verifier(JwtConfig.verifier)
            realm = "inceptioncloud.net"
            validate {
                val principal = it.payload.getClaim("uuid").asString().let { uuid -> AuthenticationManager.getByUUID(uuid) }
                if (principal != null && !attributes.contains(AccountAttributeKey)) {
                    attributes.put(AccountAttributeKey, principal)
                }
                principal
            }
        }
    }

    intercept(ApplicationCallPipeline.Call) {
        val cookie = call.request.cookies["dragonfly-token"]
        val token = cookie?.runCatching { JwtConfig.verifier.verify(this) }?.getOrNull()
        val account = token?.getClaim("uuid")?.asString()?.let { uuid -> AuthenticationManager.getByUUID(uuid) }

        if (account != null) {
            call.attributes.put(AccountAttributeKey, account)
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
        enable(MinecraftModule)
        enable(CosmeticsModule)
        enable(DiagnosticsModule)
        enable(StoreModule)
        enable(AnalyticsModule)
        enable(CommunityModule)
        enable(PartnerModule)
        enable(LauncherModule)
    }
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
