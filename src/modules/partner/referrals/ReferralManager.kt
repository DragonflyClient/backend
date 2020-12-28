package modules.partner.referrals

import com.mongodb.client.model.UpdateOptions
import com.mongodb.client.model.Updates
import core.MongoDB
import modules.partner.partners.Partner
import org.litote.kmongo.eq
import org.litote.kmongo.path

/**
 * Manages the granting of referral credits to Dragonfly partners.
 */
object ReferralManager {

    /**
     * The collection in which the referral information is stored.
     */
    val collection = MongoDB.partnerDB.getCollection<Referral>("referrals")

    /**
     * Returns the referral document for the given [partnerId].
     */
    suspend fun getReferral(partnerId: String) = collection.findOne(Referral::partnerId eq partnerId)

    /**
     * Adds the given [credit] (in cents) to the partner specified by the [partnerId] in the [referralField].
     */
    suspend fun addCredit(partnerId: String, referralField: ReferralField, credit: Int) {
        val detailsPath = Referral::insights.path() + "." + referralField.path

        collection.updateOne(
            Referral::partnerId eq partnerId,
            Updates.combine(
                Updates.inc(Referral::creditInCents.path(), credit),
                Updates.inc("$detailsPath.${ReferralInsightsDetails::creditInCents.path()}", credit),
                Updates.inc("$detailsPath.${ReferralInsightsDetails::timesReceived.path()}", 1)
            ),
            UpdateOptions().upsert(true)
        )
        Unit
    }

    /**
     * Convenient function for calling [addCredit].
     */
    suspend fun Partner.addCredit(field: ReferralField, credit: Int) {
        addCredit(partnerId.toHexString(), field, credit)
    }

    /**
     * Convenient function for calling [getReferral].
     */
    suspend fun Partner.getReferral() = getReferral(partnerId.toHexString())
}