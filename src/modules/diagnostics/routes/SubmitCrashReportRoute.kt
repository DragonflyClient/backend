package modules.diagnostics.routes

import core.*
import io.github.schreddo.nerdy.clickup.api.models.CUTask
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import khttp.*
import modules.diagnostics.DiagnosticsModule.ACCESS_TOKEN
import modules.diagnostics.DiagnosticsModule.CRASH_REPORTS_LIST_ID
import modules.diagnostics.DiagnosticsModule.clickUp
import modules.diagnostics.util.CrashReport
import org.apache.http.HttpEntity
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.ContentType
import org.apache.http.entity.mime.HttpMultipartMode
import org.apache.http.entity.mime.MultipartEntityBuilder
import org.apache.http.entity.mime.content.ByteArrayBody
import org.apache.http.entity.mime.content.StringBody
import org.apache.http.impl.client.HttpClientBuilder
import java.nio.charset.Charset
import java.util.*
import kotlin.math.absoluteValue

class SubmitCrashReportRoute : ModuleRoute("submit_crash_report", HttpMethod.Post) {

    override suspend fun CallContext.handleCall() {
        val report = call.receive<CrashReport>()

        // validate report secretly
        if (!report.full.contains("Is Modded: Definitely; Client brand changed to 'Dragonfly'")) return success()

        val id = report.cause.hashCode().absoluteValue
        val tasks = clickUp.getTasks(CRASH_REPORTS_LIST_ID, false).obj() as List<*>
        val existing = tasks.map { it as CUTask }.firstOrNull { it.name == "Crash #$id" }
        val taskId: String

        if (existing != null) {
            taskId = existing.id
            increaseTimesOccurred(taskId)
        } else {
            taskId = report.createTask(id)
        }

        report.postComment(taskId)
        report.upload(taskId)

        success()
    }

    private fun CrashReport.createTask(id: Int): String {
        val task = post(
            url = "https://api.clickup.com/api/v2/list/$CRASH_REPORTS_LIST_ID/task",
            headers = mapOf(
                "Authorization" to ACCESS_TOKEN
            ),
            json = mapOf(
                "name" to "Crash #$id",
                "priority" to 4,
                "description" to cause,
                "custom_fields" to listOf(
                    mapOf(
                        "id" to "618b998d-5169-4370-ab7c-ff7127a410d3",
                        "value" to 1
                    )
                )
            )
        ).jsonObject
        return task.getString("id")
    }

    private fun CrashReport.postComment(taskId: String) {
        post(
            url = "https://api.clickup.com/api/v2/task/$taskId/comment",
            headers = mapOf(
                "Authorization" to ACCESS_TOKEN
            ),
            json = mapOf(
                "comment_text" to """
                    This issue occurred at ${Date().toLocaleString()}.
                    
                    Minecraft Account: $user
                    Dragonfly Account: $dragonflyUser
                """.trimIndent(),
                "notify_all" to true
            )
        )
    }

    private fun CrashReport.upload(taskId: String) {
        val fileName = "Crash Report ${Date().toLocaleString()}.txt"
        val client: HttpClient = HttpClientBuilder.create().build()

        val post = HttpPost("https://api.clickup.com/api/v2/task/$taskId/attachment")
        val fileBody = ByteArrayBody(full.toByteArray(Charset.defaultCharset()), ContentType.DEFAULT_BINARY, fileName)
        val stringBody = StringBody(fileName, ContentType.MULTIPART_FORM_DATA)

        val builder: MultipartEntityBuilder = MultipartEntityBuilder.create()
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
        builder.addPart("attachment", fileBody)
        builder.addPart("filename", stringBody)
        val entity: HttpEntity = builder.build()

        post.entity = entity
        post.setHeader("Authorization", ACCESS_TOKEN)

        client.execute(post)
    }

    private fun increaseTimesOccurred(taskId: String) {
        val previousState = get(
            url = "https://api.clickup.com/api/v2/task/$taskId",
            headers = mapOf(
                "Authorization" to ACCESS_TOKEN
            )
        ).jsonObject

        val timesOccurred = previousState.getJSONArray("custom_fields").getJSONObject(0).getInt("value")
        val priority = previousState.getJSONObject("priority").getInt("id")

        put(
            url = "https://api.clickup.com/api/v2/task/$taskId",
            headers = mapOf("Authorization" to ACCESS_TOKEN),
            json = mapOf("priority" to (priority - 1).coerceAtLeast(1))
        )

        post(
            url = "https://api.clickup.com/api/v2/task/$taskId/field/618b998d-5169-4370-ab7c-ff7127a410d3",
            headers = mapOf(
                "Authorization" to ACCESS_TOKEN
            ),
            json = mapOf(
                "value" to timesOccurred + 1
            )
        )
    }
}