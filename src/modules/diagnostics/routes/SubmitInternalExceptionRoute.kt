package modules.diagnostics.routes

import com.google.gson.Gson
import core.*
import io.github.schreddo.nerdy.clickup.api.models.CUList
import io.github.schreddo.nerdy.clickup.api.models.CUTask
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import khttp.post
import modules.diagnostics.DiagnosticsModule
import modules.diagnostics.DiagnosticsModule.clickUp
import modules.diagnostics.util.InternalException
import java.util.*

private const val SPACE_ID = 4656056L

@Suppress("UNCHECKED_CAST")
class SubmitInternalExceptionRoute : ModuleRoute("submit_internal_exception", HttpMethod.Post, "master") {

    override suspend fun CallContext.handleCall() {
        try {
            val internalException = call.receive<InternalException>()
            val list = internalException.createList()
            val tasks = clickUp.getTasks(list.id.toLong(), false).obj() as List<CUTask>

            val existing = tasks.firstOrNull { it.name == internalException.name }?.id
                ?: internalException.createTask(list)

            internalException.postComment(existing)
            success()
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    private fun InternalException.createList(): CUList {
        val lists = clickUp.getFolderlessLists(SPACE_ID, false).obj() as List<CUList>
        val existing = lists.firstOrNull { it.name.equals(category, true) }

        return existing ?: clickUp.createFolderlessList(SPACE_ID, CUList().apply { name = category }).obj() as CUList
    }

    private fun InternalException.createTask(list: CUList): String {
        val task = post(
            url = "https://api.clickup.com/api/v2/list/${list.id}/task",
            headers = mapOf(
                "Authorization" to DiagnosticsModule.ACCESS_TOKEN
            ),
            json = mapOf(
                "name" to name,
                "priority" to 4,
                "description" to exception
            ).also { println(it) }
        ).jsonObject
        return task.getString("id")
    }

    private fun InternalException.postComment(taskId: String) {
        var comment = "This exception occurred at ${Date().toLocaleString()}."

        if (data != null) {
            comment += "\n"
            comment += try {
                Gson().toJson(data)
            } catch (e: Throwable) {
                data.toString()
            }
        }

        post(
            url = "https://api.clickup.com/api/v2/task/$taskId/comment",
            headers = mapOf(
                "Authorization" to DiagnosticsModule.ACCESS_TOKEN
            ),
            json = mapOf(
                "comment_text" to comment,
                "notify_all" to true
            )
        )
    }
}