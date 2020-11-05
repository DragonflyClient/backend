package modules.partner.partners

import core.*
import modules.authentication.util.AuthenticationManager
import modules.authentication.util.models.Account
import org.bson.types.ObjectId
import org.litote.kmongo.eq

/**
 * Handles the communication between the backend and the database.
 */
object PartnersManager {

    /**
     * The collection in which all partners are stored.
     */
    val collection = MongoDB.partnerDB.getCollection<Partner>("partners")

    /**
     * Creates a new partner for the account with the given [uuid].
     */
    suspend fun createNew(uuid: String) {
        val account = AuthenticationManager.getByUUID(uuid) ?: checkedError("Invalid Dragonfly UUID")
        account.getPartner().shouldBe(null)?.orError("This account already is a partner!")
        val partner = Partner(account.uuid)

        collection.insertOne(partner)
    }

    /**
     * Returns a list of all partners.
     */
    suspend fun getAll(): Collection<Partner> = collection.find().toList()

    /**
     * Returns the partner document for the given [uuid] or null if the account with the uuid is no partner.
     */
    suspend fun getByUUID(uuid: String) = collection.findOne(Partner::dragonflyUUID eq uuid)

    /**
     * Returns the partner document for the given [name] ignoring character case if [specified][ignoreCase] or
     * null if there is no partner with the given [name].
     */
    suspend fun getByName(name: String, ignoreCase: Boolean = true) = AuthenticationManager.getByUsername(name, ignoreCase)?.getPartner()

    /**
     * Returns the partner document for the given [partnerId] or null if none was found.
     */
    suspend fun getByPartnerId(partnerId: String) = collection.findOne(Partner::partnerId eq ObjectId(partnerId))

    /**
     * Convenient function for calling [getByUUID].
     */
    suspend fun Account.getPartner(): Partner? = getByUUID(uuid)
}