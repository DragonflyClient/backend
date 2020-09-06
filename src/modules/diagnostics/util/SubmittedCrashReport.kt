package modules.diagnostics.util

data class SubmittedCrashReport(
    val cause: String,
    val comment: String,
    val user: String,
    val dragonflyUser: String?,
    val full: String
)