package modules.cosmetics.routes

import com.google.gson.JsonObject
import core.*
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import modules.cosmetics.util.CosmeticsController
import modules.cosmetics.util.Filter
import org.bson.Document

object ConfigureRoute : ModuleRoute("configure", HttpMethod.Post, "jwt", optional = true) {

    override suspend fun Call.handleCall() {
        try {
            val account = twoWayAuthentication()
            val body = call.receive<JsonObject>()
            val cosmeticQualifier = body["cosmeticQualifier"].asString
            val config = body["config"].asJsonObject

            CosmeticsController.updateEach(Filter.new().dragonfly(account.uuid)) {
                if (it.cosmeticQualifier == cosmeticQualifier) {
                    val cosmeticId = it.cosmeticId
                    val schema = CosmeticsController.getPropertiesSchema(cosmeticId)

                    it.config = Document.parse(schema.clean(config).toString())
                }
            }

            success()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}