package modules.client.announcements

import core.*
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import modules.client.ClientModule

class AnnouncementsRoute : ModuleRoute("announcements") {
    override fun Route.setup() {
        get {
            val skip = call.parameters["skip"]?.runCatching { toInt() }?.getOrNull() ?: 0
            val take = call.parameters["take"]?.runCatching { toInt() }?.getOrNull()

            call.respond(ClientModule.announcementsService.getAnnouncements(take, skip))
        }

        authenticate("jwt") {
            post {
                val account = requireAccount().takeIf { it.permissionLevel >= 10 }
                    ?: checkedError("Insufficient permissions", code = HttpStatusCode.Unauthorized, errorCode = "insufficient_permissions")

                val announcement = call.receive<Announcement>().apply {
                    publishedBy = account.uuid
                }

                ClientModule.announcementsService.publishAnnouncement(announcement)
                success()
            }
        }
    }
}