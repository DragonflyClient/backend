package modules.diagnostics.routes

import core.*
import io.github.schreddo.nerdy.clickup.api.ClickUp
import io.github.schreddo.nerdy.clickup.api.models.CUTask
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import khttp.*
import modules.diagnostics.util.SubmittedCrashReport
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

object SubmitCrashReportRoute : ModuleRoute("submit_crash_report", HttpMethod.Post) {

    private const val CRASH_REPORTS_LIST_ID = 27813950L
    private const val ACCESS_TOKEN = "2670016_e5e97fcf4e46186ec1a3e7979f0544d28482e7e9"

    private val clickUp = ClickUp(ACCESS_TOKEN)

    override suspend fun Call.handleCall() {
        try {
            val report = call.receive<SubmittedCrashReport>()

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
        } catch (e: Exception) {
            e.printStackTrace()
            error(e.message!!)
        }
    }

    private fun SubmittedCrashReport.createTask(id: Int): String {
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

    private fun SubmittedCrashReport.postComment(taskId: String) {
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

    private fun SubmittedCrashReport.upload(taskId: String) {
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
            headers = mapOf(
                "Authorization" to ACCESS_TOKEN
            ),
            json = mapOf(
                "priority" to (priority - 1).coerceAtLeast(1)
            )
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