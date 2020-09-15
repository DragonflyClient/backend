package modules.authentication.util

data class RegistrationData(
    val email: String,
    val username: String,
    val password: String,
    val code: String
)