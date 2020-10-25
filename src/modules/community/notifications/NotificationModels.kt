package modules.community.notifications

import com.fasterxml.jackson.annotation.JsonProperty
import org.bson.types.ObjectId

/**
 * The model class for the documents in the notifications collection.
 */
data class Notification(

    /**
     * The category which the notification belongs to. The category is displayed in the overview.
     */
    val category: String?,

    /**
     * The message of the notification. This message is displayed in the notifications overview.
     */
    val message: String,

    /**
     * A text representation of the icon that this notification shows. Only supported icons can be chosen.
     */
    val icon: String,

    /**
     * UNIX timestamp of when the notification was sent.
     */
    @JsonProperty("send_time")
    val sendTime: Long,

    /**
     * The action that is performed when the notification is clicked.
     */
    val action: NotificationAction,

    /**
     * Whether the notification was read by the user.
     */
    var read: Boolean = false,

    /**
     * UNIX timestamp of when the notification was read.
     */
    @JsonProperty("read_time")
    var readTime: Long? = null,

    /**
     * Unique object id to identify this notification.
     */
    val _id: ObjectId? = null
) {
    /**
     * Marks the notification as read by setting the [read] and [readTime] properties.
     */
    fun markAsRead() {
        read = true
        readTime = System.currentTimeMillis()
    }
}

/**
 * An action that is executed when a notification. It consists of a [type] that is specified
 * by the inheriting class and the [target] whose role depends on the type of the action.
 *
 * @param type
 */
sealed class NotificationAction(val type: String, val target: String)

/**
 * Opens the given [url][target] when the notification is clicked.
 */
class OpenUrlAction(url: String) : NotificationAction("open_url", url)
