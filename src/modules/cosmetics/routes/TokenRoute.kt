package modules.cosmetics.routes

import com.google.gson.JsonObject
import core.*
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.request.*
import io.ktor.routing.*
import modules.authentication.util.models.Account
import modules.community.notifications.NotificationsManager.sendNotification
import modules.community.profile.ProfileManager.getProfile
import modules.cosmetics.util.CosmeticsController
import modules.cosmetics.util.Filter
import modules.cosmetics.util.models.CosmeticItem
import modules.cosmetics.util.models.TokenDocument
import org.bson.Document
import org.litote.kmongo.eq
import java.util.*

object TokenRoute : ModuleRoute("token") {

    /**
     * The collection that contains all cosmetic tokens.
     */
    private val tokens = MongoDB.cosmeticsDB.getCollection<TokenDocument>("tokens")

    override fun Route.setup() {
        get("{payload}") {
            val payload = call.parameters["payload"]!!
            val token = getToken(payload)

            json {
                "success" * (token != null)
                "token" * token
            }
        }

        authenticate("jwt", optional = true) {
            put {
                val account = requireAccount()
                val cosmeticId = call.receive<JsonObject>().get("cosmeticId").asInt

                if (account.permissionLevel < 8)
                    checkedError("You don't have permissions to create a cosmetic token!")

                val token = TokenDocument(
                    payload = TokenDocument.generatePayload(),
                    cosmeticId = cosmeticId,
                    createdDate = System.currentTimeMillis(),
                    createdAccount = account.uuid
                )

                insertToken(token)
                json {
                    "success" * true
                    "payload" * token.payload
                }
            }

            post("{payload}") {
                val payload = call.parameters["payload"]!!
                val account = requireAccount()

                redeemToken(payload, account)
                success()
            }
        }
    }

    /**
     * Returns a token by its payload string.
     */
    private suspend fun getToken(payload: String): TokenDocument? = tokens.findOne(TokenDocument::payload eq payload)

    /**
     * Inserts a new token into the database.
     */
    private suspend fun insertToken(token: TokenDocument) = tokens.insertOne(token)

    /**
     * Redeems the cosmetic token with the [payload] on the [account].
     */
    private suspend fun redeemToken(payload: String, account: Account) {
        val token = getToken(payload) ?: checkedError("Token '$payload' was not found")
        if (token.isRedeemed) checkedError("This token has already been redeemed")

        token.isRedeemed = true
        token.redeemedAccount = account.uuid
        token.redeemedDate = System.currentTimeMillis()
        tokens.updateOneById(token._id!!, token)

        val cosmeticName = CosmeticsController.getAvailableById(token.cosmeticId)!!.getString("name")
        account.getProfile().sendNotification("Cosmetics", "**$cosmeticName** has been added to your cosmetics collection.", "new")

        CosmeticsController.insert(
            Filter.new().dragonfly(account.uuid),
            CosmeticItem(
                cosmeticId = token.cosmeticId,
                cosmeticQualifier = UUID.randomUUID().toString(),
                enabled = true,
                minecraft = null,
                config = Document()
            )
        )
    }
}