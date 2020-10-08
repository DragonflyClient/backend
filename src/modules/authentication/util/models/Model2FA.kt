package modules.authentication.util.models

data class Model2FA(
    var enabled: Boolean = false,
    var requested: Boolean = false,
    var secret: String? = null,
    var backupCodes: List<String>? = null
)