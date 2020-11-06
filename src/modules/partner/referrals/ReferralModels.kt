package modules.partner.referrals

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Holds information about the referral bonus of a partner.
 */
data class Referral(

    /**
     * The id of the partner that this document belongs to.
     */
    @JsonProperty("partner_id")
    val partnerId: String,

    /**
     * The total amount of credit in cents that this partner has earned.
     */
    @JsonProperty("credit_in_cents")
    var creditInCents: Int = 0,

    /**
     * Insights into the different fields of referral to show how the partner earned
     * his [creditInCents].
     */
    val insights: ReferralInsights = ReferralInsights()
)

/**
 * Contains insights about how much credit the partner earned in which referral fields.
 */
data class ReferralInsights(

    /**
     * Details about how many accounts have been created with the referral link of the partner.
     */
    @JsonProperty("create_accounts")
    var createAccounts: ReferralInsightsDetails = ReferralInsightsDetails()
)

/**
 * Contains the details for a specific referral field.
 */
data class ReferralInsightsDetails(

    /**
     * How often credit has been added in this field.
     */
    @JsonProperty("times_received")
    var timesReceived: Int = 0,

    /**
     * Total amount of credit earned in this field.
     */
    @JsonProperty("credit_in_cents")
    var creditInCents: Int = 0
)

/**
 * Enum of all available referral fields and its [path] in the referral document.
 */
enum class ReferralField(val path: String) {

    CREATE_ACCOUNTS("create_accounts")
}