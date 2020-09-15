package modules.diagnostics

import core.Module
import io.github.schreddo.nerdy.clickup.api.ClickUp
import modules.diagnostics.routes.SubmitCrashReportRoute
import modules.diagnostics.routes.SubmitInternalExceptionRoute

object DiagnosticsModule : Module(
    "Diagnostics",
    SubmitCrashReportRoute(),
    SubmitInternalExceptionRoute()
) {
    const val CRASH_REPORTS_LIST_ID = 27813950L
    const val ACCESS_TOKEN = "2670016_e5e97fcf4e46186ec1a3e7979f0544d28482e7e9"
    val clickUp = ClickUp(ACCESS_TOKEN)
}