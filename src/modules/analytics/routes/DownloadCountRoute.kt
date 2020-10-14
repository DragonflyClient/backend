package modules.analytics.routes

import core.*
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import log
import modules.analytics.util.DownloadCounter

class DownloadCountRoute : ModuleRoute("download", HttpMethod.Post) {

    private val lastCalls = mutableMapOf<String, Long>()
    private val totalAmounts = mutableMapOf<String, Long>()

    override suspend fun CallContext.handleCall() {
        val ip = call.request.header("x-forwarded-for") ?: return success()
        val lastCall = lastCalls[ip]

        val amount = (totalAmounts[ip] ?: 0L) + 1L
        totalAmounts[ip] = amount

        if (lastCall != null && (System.currentTimeMillis() - lastCall) < 5_000) {
            log("Ignored call by $ip since its last call was less than 5 seconds ago.")
            return success()
        } else if (amount >= 20) {
            log("Ignored call by $ip since it has already called $amount times.")
            return success()
        }

        lastCalls[ip] = System.currentTimeMillis()

        DownloadCounter.countDownload()
        success()
    }
}
