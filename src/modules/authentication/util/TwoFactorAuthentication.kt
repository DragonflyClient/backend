package modules.authentication.util

import com.j256.twofactorauth.TimeBasedOneTimePasswordUtil
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * The core class of two factor authentication that manages the enabling/disabling
 * and verification of TOTP.
 */
object TwoFactorAuthentication {

    /**
     * Enables two factor authentication for the given [account].
     */
    fun enable2FA(account: Account) {
        account.prohibit2FA()

        val secret = TimeBasedOneTimePasswordUtil.generateBase32Secret(30)
        account.enable2FA = true
        account.secret2FA = secret
    }

    /**
     * Disables two factor authentication for the given [account].
     */
    fun disable2FA(account: Account) {
        account.require2FA()

        account.enable2FA = false
        account.secret2FA = null
    }

    /**
     * Returns whether the given [code] matches the current TOTP of the given [account].
     */
    fun verifyCode(account: Account, code: String): Boolean {
        account.require2FA()

        val current = TimeBasedOneTimePasswordUtil.generateCurrentNumberString(account.secret2FA!!)
        return code.replace(" ", "") == current
    }

    /**
     * Generates the QR code that adds the 2FA secret of the [account] to the authenticator app.
     * This function returns an url that contains an image of the QR code generated with the
     * Google APIs.
     */
    fun generateQRCode(account: Account): String {
        account.require2FA()

        val name = account.email ?: account.identifier
        val urlEncodedName = URLEncoder.encode(name, StandardCharsets.UTF_8.toString())

        return "https://chart.googleapis.com/chart" +
                "?chs=200x200&cht=qr&chl=200x200&chld=M|0&cht=qr" +
                "&chl=otpauth://totp/$urlEncodedName?secret=${account.secret2FA!!}"
    }

    /**
     * Throws an exception if two factor authentication is **not** set up.
     */
    private fun Account.require2FA() {
        if (!this.enable2FA) error("Two factor authentication is not enabled on this account")
        if (this.secret2FA == null) error("There is no two factor authentication secret set for this account")
    }

    /**
     * Throws an exception if two factor authentication **is** set up.
     */
    private fun Account.prohibit2FA() {
        if (this.enable2FA) error("Two factor authentication is already enabled on this account")
        if (this.secret2FA != null) error("There is already a two factor authentication secret set for this account")
    }
}