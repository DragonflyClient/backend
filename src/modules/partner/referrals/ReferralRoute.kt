package modules.partner.referrals

import com.google.gson.JsonObject
import core.*
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.request.*
import io.ktor.routing.*
import modules.partner.partners.PartnersManager
import modules.partner.partners.PartnersManager.getPartner
import modules.partner.referrals.ReferralManager.addCredit
import modules.partner.referrals.ReferralManager.getReferral

class ReferralRoute : ModuleRoute("referral") {

    override fun Route.setup() {
        authenticate("master") {
            post("{partnerId}") {
                val body = call.receive<JsonObject>()
                val field = ReferralField.valueOf(body.get("field").asString.toUpperCase())
                val credit = body.get("credit_in_cents").asInt

                val partnerId = call.parameters["partnerId"]!!
                val partner = PartnersManager.getByPartnerId(partnerId) ?: checkedError("Invalid partner id!", errorCode = "invalid_partner_id")

                partner.addCredit(field, credit)
                success()
            }

            get("{partnerId}") {
                val partnerId = call.parameters["partnerId"]!!
                val partner = PartnersManager.getByPartnerId(partnerId) ?: checkedError("Invalid partner id!", errorCode = "invalid_partner_id")
                val referral = partner.getReferral() ?: checkedError("Partner hasn't earned any credit yet!", errorCode = "no_credit")

                success("referral" to referral)
            }
        }

        authenticate("jwt", optional = true) {
            get {
                val account = requireAccount()
                val partner = account.getPartner() ?: checkedError("You are not a Dragonfly partner!", errorCode = "no_partner")
                val referral = partner.getReferral() ?: checkedError("You haven't earned any referral credit so far!", errorCode = "no_credit")

                success("referral" to referral)
            }
        }
    }
}