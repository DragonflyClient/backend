package modules.community.profile

import core.MongoDB
import modules.authentication.util.models.Account
import org.litote.kmongo.eq

/**
 * The profile manager is the interface between the backend and the community profiles.
 */
object ProfileManager {

    /**
     * The collection in which all profiles are stored.
     */
    val collection = MongoDB.communityDB.getCollection<Profile>("profiles")

    /**
     * Finds a profile by the Dragonfly [uuid].
     */
    suspend fun getProfile(uuid: String) = collection.findOne(Profile::dragonflyUUID eq uuid)

    /**
     * Extension function to quickly retrieve the community profile for an account.
     */
    suspend fun Account.getProfile() = getProfile(uuid) ?: Profile(uuid)

    /**
     * Extension function for updating or inserting the profile.
     */
    suspend fun Profile.update() = _id?.let { collection.updateOneById(it, this) }
        ?: collection.insertOne(this)
}