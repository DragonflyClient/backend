package modules.keys

import core.Module
import modules.keys.routes.*

object KeysModule : Module(
    "Keys",
    AttachRoute,
    GenerateRoute,
    RequestRoute,
    ValidateRoute
)