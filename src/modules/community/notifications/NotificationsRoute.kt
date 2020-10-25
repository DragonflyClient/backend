package modules.community.notifications

import com.google.gson.*
import core.*
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.routing.*
import modules.authentication.util.AuthenticationManager
import modules.community.notifications.NotificationsManager.update
import modules.community.profile.ProfileManager.getProfile
import modules.community.profile.ProfileManager.update

class NotificationsRoute : ModuleRoute("notifications") {

    override fun Route.setup() {
        authenticate("jwt", optional = true) {
            get {
                val account = requireAccount()
                val profile = account.getProfile()
                val notifications = profile.notifications.mapNotNull { NotificationsManager.getNotification(it) }

                json {
                    "success" * true
                    "notifications" * notifications
                }
            }

            patch("{id}") {
                val id = call.parameters["id"]!!
                val account = requireAccount()
                val profile = account.getProfile()
                val notification = NotificationsManager.getNotification(id) ?: checkedError("Invalid notification id", HttpStatusCode.NotFound)

                notification.profileId.shouldBe(profile._id.toHexString())?.orError("This notification does not belong to you")

                if (!notification.read) {
                    notification.markAsRead()
                    notification.update()
                    success("modified" to true)
                } else {
                    success("modified" to false)
                }
            }
        }

        authenticate("master") {
            post("{uuid}") {
                val uuid = call.parameters["uuid"]!!
                val profile = AuthenticationManager.getByUUID(uuid)?.getProfile() ?: checkedError("Invalid Dragonfly UUID")
                val body = call.receive<JsonObject>()

                val notification = Notification(
                    profile._id.toHexString(),
                    body.takeIf { it.has("category") }?.get("category")?.takeIf { it is JsonPrimitive && it.isString }?.asString,
                    body["message"].asString,
                    body["icon"].asString,
                    System.currentTimeMillis(),
                    if (body.has("action")) Gson().fromJson(body["action"].asJsonObject, NotificationAction::class.java) else null
                )

                notification.update()
                profile.notifications.add(notification._id.toHexString())
                profile.update()

                success()
            }
        }
    }
}