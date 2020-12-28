package modules.client.announcements

import core.MongoDB
import org.bson.types.ObjectId

class AnnouncementsService {
    private val collection = MongoDB.launcherDB.getCollection<Announcement>("announcements")

    suspend fun getAnnouncements(take: Int?, skip: Int): List<Announcement> =
        collection.find().toList().sortedByDescending { it.identifier!!.timestamp }
            .drop(skip)
            .let { if (take != null) it.take(take) else it }

    suspend fun publishAnnouncement(announcement: Announcement) {
        announcement.identifier = ObjectId.get()
        collection.insertOne(announcement)
    }
}