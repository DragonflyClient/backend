package modules.authentication.util.data

data class LoginData(
    val name: String,
    val password: String,
    val code2FA: String?
)