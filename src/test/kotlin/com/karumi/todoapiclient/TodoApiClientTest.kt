package com.karumi.todoapiclient

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import todoapiclient.TodoApiClient
import todoapiclient.dto.TaskDto
import todoapiclient.exception.ItemNotFoundError
import todoapiclient.exception.UnknownApiError

class TodoApiClientTest : MockWebServerTest() {

    private lateinit var apiClient: TodoApiClient

    @Before
    override fun setUp() {
        super.setUp()
        val mockWebServerEndpoint = baseEndpoint
        apiClient = TodoApiClient(mockWebServerEndpoint)
    }

    @Test
    fun sendsAcceptAndContentTypeHeaders() {
        enqueueMockResponse(200, "getTasksResponse.json")

        apiClient.allTasks

        assertRequestContainsHeader("Accept", "application/json")
    }

    @Test
    fun sendsGetAllTaskRequestToTheCorrectEndpoint() {
        enqueueMockResponse(200, "getTasksResponse.json")

        apiClient.allTasks

        assertGetRequestSentTo("/todos")
    }

    @Test
    fun parsesTasksProperlyGettingAllTheTasks() {
        enqueueMockResponse(200, "getTasksResponse.json")

        val tasks = apiClient.allTasks.right!!

        assertEquals(200, tasks.size.toLong())
        assertTaskContainsExpectedValues(tasks[0])
    }

    @Test
    fun parsesEmptyTasksProperlyGettingEmptyTasks() {
        enqueueMockResponse(200, "getEmptyTasksResponse.json")

        val response = apiClient.allTasks

        assertEquals(emptyList<TaskDto>(), response.right)
    }

    @Test
    fun throwsUnknownExceptionWhenTheStatusCodeReturnedIsUnknown() {
        enqueueMockResponse(418)

        val error = apiClient.allTasks.left

        assertEquals(UnknownApiError(418), error)
    }

    @Test
    fun sendGetRequestWhenGettingTodoById() {
        enqueueMockResponse(200, FILENAME_GET_TASK_BY_ID_RESPONSE)

        apiClient.getTaskById(ANY_TASK_ID)

        assertGetRequestSentTo("/todos/$ANY_TASK_ID")
    }

    @Test
    fun parsesTaskProperlyGettingTaskById() {
        enqueueMockResponse(200, FILENAME_GET_TASK_BY_ID_RESPONSE)

        val task = apiClient.getTaskById(ANY_TASK_ID).right!!

        assertTaskContainsExpectedValues(task)
    }

    @Test
    fun throwsUnknownExceptionWhenTheStatusCodeReturnedIsServerError() {
        enqueueMockResponse(500)

        val error = apiClient.getTaskById(ANY_TASK_ID).left!!

        assertEquals(UnknownApiError(500), error)
    }

    @Test
    fun throwsItemNotFoundWhenTheStatusCodeReturnedIsNotFound() {
        enqueueMockResponse(404)

        val error = apiClient.getTaskById(ANY_TASK_ID).left!!

        assertEquals(ItemNotFoundError, error)
    }

    private fun assertTaskContainsExpectedValues(task: TaskDto?) {
        assertTrue(task != null)
        assertEquals(task?.id, "1")
        assertEquals(task?.userId, "1")
        assertEquals(task?.title, "delectus aut autem")
        assertFalse(task!!.isFinished)
    }

    companion object {
        const val ANY_TASK_ID = "any"
        const val FILENAME_GET_TASK_BY_ID_RESPONSE = "getTaskByIdResponse.json"
    }
}
