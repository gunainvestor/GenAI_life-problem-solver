package com.lifeproblemsolver.app.ui.viewmodel

import com.lifeproblemsolver.app.data.model.Problem
import com.lifeproblemsolver.app.data.model.Priority
import com.lifeproblemsolver.app.data.repository.ProblemRepository
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import org.junit.Assert.*

class ProblemListViewModelTest {

    private lateinit var viewModel: ProblemListViewModel
    private lateinit var repository: ProblemRepository
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        // Dispatchers.setMain(testDispatcher) // Removed as per new_code
        repository = mock()
        viewModel = ProblemListViewModel(repository)
    }

    @Test
    fun `init loads problems and categories`() = runTest {
        // Given
        val problems = listOf(createTestProblem(1L, "Test Problem"))
        val categories = listOf("Work", "Personal")
        
        whenever(repository.getAllProblems()).thenReturn(flowOf(problems))
        whenever(repository.getAllCategories()).thenReturn(flowOf(categories))

        // When
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(problems, viewModel.uiState.value.problems)
        assertEquals(categories, viewModel.uiState.value.categories)
        assertFalse(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `loadProblems updates state correctly`() = runTest {
        // Given
        val problems = listOf(createTestProblem(1L, "Test Problem"))
        whenever(repository.getAllProblems()).thenReturn(flowOf(problems))

        // When
        viewModel.loadProblems()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(problems, viewModel.uiState.value.problems)
        assertFalse(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `loadProblems handles error correctly`() = runTest {
        // Given
        val errorMessage = "Database error"
        whenever(repository.getAllProblems()).thenThrow(RuntimeException(errorMessage))

        // When
        viewModel.loadProblems()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertTrue(viewModel.uiState.value.problems.isEmpty())
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(errorMessage, viewModel.uiState.value.error)
    }

    @Test
    fun `loadProblemsByFilter updates state correctly`() = runTest {
        // Given
        val filterType = FilterType.RESOLVED
        val problems = listOf(createTestProblem(1L, "Resolved Problem", isResolved = true))
        whenever(repository.getProblemsByStatus(true)).thenReturn(flowOf(problems))

        // When
        viewModel.loadProblemsByFilter(filterType)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(filterType, viewModel.filterType.value)
        assertEquals(problems, viewModel.uiState.value.problems)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `loadProblemsByCategory updates state correctly`() = runTest {
        // Given
        val category = "Work"
        val problems = listOf(createTestProblem(1L, "Work Problem", category))
        whenever(repository.getProblemsByCategory(category)).thenReturn(flowOf(problems))

        // When
        viewModel.loadProblemsByCategory(category)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(problems, viewModel.uiState.value.problems)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `deleteProblem calls repository and handles error`() = runTest {
        // Given
        val problem = createTestProblem(1L, "Problem to Delete")
        val errorMessage = "Delete failed"
        doThrow(RuntimeException(errorMessage)).whenever(repository).deleteProblem(problem)

        // When
        viewModel.deleteProblem(problem)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        verify(repository).deleteProblem(problem)
        assertEquals(errorMessage, viewModel.uiState.value.error)
    }

    @Test
    fun `clearError removes error from state`() = runTest {
        // Given
        viewModel.uiState.value = viewModel.uiState.value.copy(error = "Test error")

        // When
        viewModel.clearError()

        // Then
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `filter type changes trigger correct repository calls`() = runTest {
        // Given
        val problems = listOf(createTestProblem(1L, "Test Problem"))
        
        whenever(repository.getAllProblems()).thenReturn(flowOf(problems))
        whenever(repository.getProblemsByStatus(true)).thenReturn(flowOf(problems))
        whenever(repository.getProblemsByStatus(false)).thenReturn(flowOf(problems))
        whenever(repository.getProblemsByPriority(Priority.HIGH.name)).thenReturn(flowOf(problems))
        whenever(repository.getProblemsByPriority(Priority.URGENT.name)).thenReturn(flowOf(problems))

        // When & Then
        viewModel.loadProblemsByFilter(FilterType.ALL)
        testDispatcher.scheduler.advanceUntilIdle()
        verify(repository).getAllProblems()

        viewModel.loadProblemsByFilter(FilterType.RESOLVED)
        testDispatcher.scheduler.advanceUntilIdle()
        verify(repository).getProblemsByStatus(true)

        viewModel.loadProblemsByFilter(FilterType.UNRESOLVED)
        testDispatcher.scheduler.advanceUntilIdle()
        verify(repository).getProblemsByStatus(false)

        viewModel.loadProblemsByFilter(FilterType.HIGH_PRIORITY)
        testDispatcher.scheduler.advanceUntilIdle()
        verify(repository).getProblemsByPriority(Priority.HIGH.name)

        viewModel.loadProblemsByFilter(FilterType.URGENT)
        testDispatcher.scheduler.advanceUntilIdle()
        verify(repository).getProblemsByPriority(Priority.URGENT.name)
    }

    @Test
    fun `initial state is correct`() {
        // Then
        assertTrue(viewModel.uiState.value.problems.isEmpty())
        assertTrue(viewModel.uiState.value.categories.isEmpty())
        assertFalse(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.error)
        assertEquals(FilterType.ALL, viewModel.filterType.value)
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