package modules.partner.partners

import com.google.gson.JsonObject
import core.*
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.request.*
import io.ktor.routing.*
import modules.partner.partners.PartnersManager.getPartner

class PartnersRoute : ModuleRoute("partners") {

    override fun Route.setup() {
        authenticate("jwt", optional = true) {
            get("me") {
                val account = requireAccount()
                val partner = account.getPartner() ?: checkedError("You are not a Dragonfly partner!")
                success("partner" to partner)
            }
        }

        authenticate("master") {
            post {
                val uuid = call.receive<JsonObject>().get("uuid").asString
                PartnersManager.createNew(uuid)
                success()
            }

            get {
                success("partners" to PartnersManager.getAll())
            }

            get("uuid/{dragonflyUUID}") {
                val dragonflyUUID = call.parameters["dragonflyUUID"]!!
                val partner = PartnersManager.getByUUID(dragonflyUUID) ?: checkedError("This account is not a partner")

                success("partner" to partner)
            }

            get("name/{partnerName}") {
                val partnerName = call.parameters["partnerName"]!!
                val partner = PartnersManager.getByName(partnerName, ignoreCase = true)
                    ?: checkedError("No partner found for name '$partnerName'", errorCode = "invalid_partner_name")

                success("partner" to partner)
            }

            get("{partnerId}") {
                val partnerId = call.parameters["partnerId"]!!
                val partner = PartnersManager.getByPartnerId(partnerId) ?: checkedError("Invalid partner id")

                success("partner" to partner)
            }
        }
    }
}