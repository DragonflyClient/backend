package modules.store.routes

import com.google.gson.JsonObject
import core.*
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import log
import modules.authentication.util.AuthenticationManager
import modules.authentication.util.JwtConfig
import modules.cosmetics.util.*
import modules.store.util.Payment
import modules.store.util.ShopItem
import org.bson.Document
import org.litote.kmongo.eq
import java.util.*

object ExecutePaymentRoute : ModuleRoute("execute_payment", HttpMethod.Post) {

    private val shopItems = MongoDB.dragonflyDB.getCollection<ShopItem>("shop-items")

    private val payments = MongoDB.dragonflyDB.getCollection<Payment>("payments")

    private val processing = mutableListOf<String>()

    override suspend fun CallContext.handleCall() {
        // parse request
        val payload = call.receive<JsonObject>()
        val paymentId = payload["paymentId"].asString
        processing.add(paymentId)
        log("Payment $paymentId is now being processed...")

        try {
            // load payment data
            val payment = payments.findOne("{ paymentId: '$paymentId' }")
                ?: checkedError("Payment $paymentId not found")

            // validate payment state
            if (payment.paymentState != "succeeded" && payment.paymentState != "approved")
                checkedError("Payment didn't succeed")
            else if (payment.executed == true)
                checkedError("Payment has already been executed!")

            // load shop item
            val payedItemId = payment.itemId
            val shopItem = shopItems.findOne("{ id: '$payedItemId' }")
                ?: checkedError("Shop item $payedItemId not found")

            // load Dragonfly account
            val dragonflyToken = JwtConfig.verifier.verify(payment.dragonflyToken)
            val account = dragonflyToken.getClaim("uuid").asString()?.let { uuid -> AuthenticationManager.getByUUID(uuid) }
                ?: checkedError("Account not found")

            // insert cosmetic
            val cosmeticId = shopItem.cosmeticId
                ?: checkedError("Shop item doesn't contain cosmetic id!")

            CosmeticsController.insert(
                Filter.new().dragonfly(account.uuid),
                CosmeticItem(cosmeticId, UUID.randomUUID().toString(), enabled = true, config = Document())
            )

            log("Inserted cosmetic #$cosmeticId for user ${account.username}.")

            payment.executed = true
            payments.updateOne(Payment::paymentId eq paymentId, payment)
            success()
        } catch (e: Throwable) {
            throw e
        } finally {
            processing.remove(paymentId)
            log("Payment $paymentId has been successfully processed.")
        }
    }
}