package com.lifeproblemsolver.app.data.repository

import com.lifeproblemsolver.app.data.dao.ProblemDao
import com.lifeproblemsolver.app.data.model.Problem
import com.lifeproblemsolver.app.data.model.Priority
import com.lifeproblemsolver.app.data.remote.AiService
import com.lifeproblemsolver.app.data.remote.SolutionRequest
import com.lifeproblemsolver.app.data.remote.SolutionResponse
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import org.junit.Assert.*

class ProblemRepositoryTest {

    private lateinit var repository: ProblemRepository
    private lateinit var problemDao: ProblemDao
    private lateinit var aiService: AiService

    @Before
    fun setup() {
        problemDao = mock()
        aiService = mock()
        repository = ProblemRepository(problemDao, aiService)
    }

    @Test
    fun `getAllProblems returns flow from dao`() = runTest {
        // Given
        val problems = listOf(
            createTestProblem(1L, "Test Problem 1"),
            createTestProblem(2L, "Test Problem 2")
        )
        whenever(problemDao.getAllProblems()).thenReturn(flowOf(problems))

        // When
        val result = repository.getAllProblems()

        // Then
        assertEquals(problems, result.first())
        verify(problemDao).getAllProblems()
    }

    @Test
    fun `getProblemsByCategory returns filtered problems`() = runTest {
        // Given
        val category = "Work"
        val problems = listOf(createTestProblem(1L, "Work Problem", category))
        whenever(problemDao.getProblemsByCategory(category)).thenReturn(flowOf(problems))

        // When
        val result = repository.getProblemsByCategory(category)

        // Then
        assertEquals(problems, result.first())
        verify(problemDao).getProblemsByCategory(category)
    }

    @Test
    fun `getProblemsByPriority returns filtered problems`() = runTest {
        // Given
        val priority = Priority.HIGH.name
        val problems = listOf(createTestProblem(1L, "High Priority Problem", priority = Priority.HIGH))
        whenever(problemDao.getProblemsByPriority(priority)).thenReturn(flowOf(problems))

        // When
        val result = repository.getProblemsByPriority(priority)

        // Then
        assertEquals(problems, result.first())
        verify(problemDao).getProblemsByPriority(priority)
    }

    @Test
    fun `getProblemsByStatus returns filtered problems`() = runTest {
        // Given
        val isResolved = true
        val problems = listOf(createTestProblem(1L, "Resolved Problem", isResolved = true))
        whenever(problemDao.getProblemsByStatus(isResolved)).thenReturn(flowOf(problems))

        // When
        val result = repository.getProblemsByStatus(isResolved)

        // Then
        assertEquals(problems, result.first())
        verify(problemDao).getProblemsByStatus(isResolved)
    }

    @Test
    fun `getAllCategories returns categories from dao`() = runTest {
        // Given
        val categories = listOf("Work", "Personal", "Health")
        whenever(problemDao.getAllCategories()).thenReturn(flowOf(categories))

        // When
        val result = repository.getAllCategories()

        // Then
        assertEquals(categories, result.first())
        verify(problemDao).getAllCategories()
    }

    @Test
    fun `getProblemById returns problem from dao`() = runTest {
        // Given
        val problemId = 1L
        val problem = createTestProblem(problemId, "Test Problem")
        whenever(problemDao.getProblemById(problemId)).thenReturn(problem)

        // When
        val result = repository.getProblemById(problemId)

        // Then
        assertEquals(problem, result)
        verify(problemDao).getProblemById(problemId)
    }

    @Test
    fun `insertProblem calls dao and returns id`() = runTest {
        // Given
        val problem = createTestProblem(0L, "Test Problem")
        val expectedId = 1L
        whenever(problemDao.insertProblem(problem)).thenReturn(expectedId)

        // When
        val result = repository.insertProblem(problem)

        // Then
        assertEquals(expectedId, result)
        verify(problemDao).insertProblem(problem)
    }

    @Test
    fun `updateProblem calls dao`() = runTest {
        // Given
        val problem = createTestProblem(1L, "Updated Problem")

        // When
        repository.updateProblem(problem)

        // Then
        verify(problemDao).updateProblem(problem)
    }

    @Test
    fun `deleteProblem calls dao`() = runTest {
        // Given
        val problem = createTestProblem(1L, "Problem to Delete")

        // When
        repository.deleteProblem(problem)

        // Then
        verify(problemDao).deleteProblem(problem)
    }

    @Test
    fun `generateAiSolution calls ai service and returns solution`() = runTest {
        // Given
        val problem = createTestProblem(1L, "Test Problem")
        val expectedSolution = "Here's a solution to your problem..."
        val solutionResponse = SolutionResponse(expectedSolution)
        
        whenever(aiService.generateSolution(any())).thenReturn(solutionResponse)

        // When
        val result = repository.generateAiSolution(problem)

        // Then
        assertEquals(expectedSolution, result)
        verify(aiService).generateSolution(
            SolutionRequest(
                problem = problem.title,
                context = problem.description,
                category = problem.category
            )
        )
    }

    @Test
    fun `generateAiSolution returns error message when ai service fails`() = runTest {
        // Given
        val problem = createTestProblem(1L, "Test Problem")
        whenever(aiService.generateSolution(any())).thenThrow(RuntimeException("AI Service Error"))

        // When
        val result = repository.generateAiSolution(problem)

        // Then
        assertEquals("Unable to generate AI solution at this time. Please try again later.", result)
    }

    @Test
    fun `createProblem creates problem with correct data`() = runTest {
        // Given
        val title = "New Problem"
        val description = "Problem description"
        val notes = "Additional notes"
        val category = "Work"
        val priority = Priority.HIGH
        val expectedId = 1L
        
        whenever(problemDao.insertProblem(any())).thenReturn(expectedId)

        // When
        val result = repository.createProblem(title, description, notes, category, priority)

        // Then
        assertEquals(expectedId, result)
        verify(problemDao).insertProblem(
            argThat { problem ->
                problem.title == title &&
                problem.description == description &&
                problem.notes == notes &&
                problem.category == category &&
                problem.priority == priority &&
                !problem.isResolved &&
                problem.aiSuggestion.isEmpty()
            }
        )
    }

    @Test
    fun `updateProblemWithAiSolution updates problem with ai suggestion`() = runTest {
        // Given
        val problemId = 1L
        val aiSuggestion = "AI generated solution"
        val problem = createTestProblem(problemId, "Test Problem")
        whenever(problemDao.getProblemById(problemId)).thenReturn(problem)

        // When
        repository.updateProblemWithAiSolution(problemId, aiSuggestion)

        // Then
        verify(problemDao).getProblemById(problemId)
        verify(problemDao).updateProblem(
            argThat { updatedProblem ->
                updatedProblem.aiSuggestion == aiSuggestion
            }
        )
    }

    @Test
    fun `markProblemAsResolved updates problem status`() = runTest {
        // Given
        val problemId = 1L
        val problem = createTestProblem(problemId, "Test Problem", isResolved = false)
        whenever(problemDao.getProblemById(problemId)).thenReturn(problem)

        // When
        repository.markProblemAsResolved(problemId)

        // Then
        verify(problemDao).getProblemById(problemId)
        verify(problemDao).updateProblem(
            argThat { updatedProblem ->
                updatedProblem.isResolved
            }
        )
    }

    private fun createTestProblem(
        id: Long,
        title: String,
        category: String = "General",
        priority: Priority = Priority.MEDIUM,
        isResolved: Boolean = false
    ): Problem {
        return Problem(
            id = id,
            title = title,
            description = "Test description",
            notes = "Test notes",
            category = category,
            priority = priority,
            createdAt = Instant.fromEpochMilliseconds(System.currentTimeMillis()),
            updatedAt = Instant.fromEpochMilliseconds(System.currentTimeMillis()),
            aiSuggestion = "",
            isResolved = isResolved
        )
    }
} 