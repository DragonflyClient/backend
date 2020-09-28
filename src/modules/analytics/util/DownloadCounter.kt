package modules.analytics.util

import DragonflyBackend
import com.mongodb.client.model.Filters
import org.bson.Document

object DownloadCounter {

    private val collection = DragonflyBackend.mongo.getDatabase("dragonfly").getCollection<Document>("analytics")

    suspend fun countDownload() {
        val document = collection.findOne(Filters.eq("title", "downloads"))

        if (document == null) {
            collection.insertOne(Document().append("title", "downloads").append("amount", 1))
        } else {
            val amount = (document["amount"] as? Int ?: 0) + 1
            document["amount"] = amount
            collection.updateOne("{ title: 'downloads' }", "{ \$set: { amount: $amount } }")
        }
    }
}
