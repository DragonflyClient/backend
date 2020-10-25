package modules.community.notifications

import core.MongoDB
import org.bson.types.ObjectId

/**
 * Manages the notifications of the Dragonfly community.
 */
object NotificationsManager {

    /**
     * The collection in which all profiles are stored.
     */
    val collection = MongoDB.communityDB.getCollection<Notification>("notifications")

    /**
     * Finds a notification by its [id].
     */
    suspend fun getNotification(id: String) = id.runCatching { collection.findOneById(ObjectId(this)) }.getOrNull()

    /**
     * Extension function for updating or inserting the profile.
     */
    suspend fun Notification.update() = _id?.let { collection.updateOneById(it, this) }
        ?: collection.insertOne(this)
}