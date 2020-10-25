package modules.community.notifications

import com.fasterxml.jackson.annotation.JsonProperty
import org.bson.types.ObjectId

/**
 * The model class for the documents in the notifications collection.
 */
data class Notification(

    /**
     * The id of the Dragonfly profile which this notification belongs to.
     */
    @JsonProperty("profile_id")
    val profileId: String,

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
    val _id: ObjectId = ObjectId.get()
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
 * An action that is executed when a notification. It consists of a [type] that specifies which
 * action is taken and the [target] whose role depends on the type of the action.
 */
data class NotificationAction(val type: String, val target: String)
