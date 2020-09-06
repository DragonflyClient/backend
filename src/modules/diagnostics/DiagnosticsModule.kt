package modules.diagnostics

import core.Module
import modules.diagnostics.routes.SubmitCrashReportRoute

object DiagnosticsModule : Module(
    "Diagnostics",
    SubmitCrashReportRoute
)