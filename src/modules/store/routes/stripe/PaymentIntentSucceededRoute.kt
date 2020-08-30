package modules.store.routes.stripe

import DragonflyBackend
import com.google.gson.Gson
import com.stripe.model.*
import core.*
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import log
import modules.authentication.util.*
import modules.authentication.util.Account
import org.bson.Document
import java.io.File
import java.text.DecimalFormat

object PaymentIntentSucceededRoute : ModuleRoute("stripe/payment_intent_succeeded", HttpMethod.Post) {

    private val database = DragonflyBackend.mongo.getDatabase("dragonfly")

    /** The collection in which the [accounts][Account] are stored */
    private val collection = database.getCollection<Document>("shop-items")

    override suspend fun Call.handleCall() {
        val payload = call.receiveText()
        val event = Gson().fromJson(payload, Event::class.java)

        val dataObjectDeserializer = event.dataObjectDeserializer
        val stripeObject: StripeObject

        if (dataObjectDeserializer.getObject().isPresent) {
            stripeObject = dataObjectDeserializer.getObject().get()
        } else {
            error("Deserialization failed")
        }

        val paymentIntent = stripeObject as? PaymentIntent ?: error("Invalid event type")
        val metadata = paymentIntent.metadata

        val dragonflyToken = JwtConfig.verifier.verify(metadata["dragonfly_token"])
        val account = dragonflyToken.getClaim("uuid").asString()?.let { uuid -> AuthenticationManager.getByUUID(uuid) }!!

        val itemId = metadata["item_id"]
        val item = collection.findOne("{ sku: '$itemId' }")!!
        val itemName = item.getString("name")!!
        val price = DecimalFormat("0.00").format(item.getInteger("price") / 100.0) + item.getString("currency").toUpperCase()

        log("${account.username} bought $itemName for $price")

        File("payment_intent.json").writeText(paymentIntent.toString())
        success()
    }
}