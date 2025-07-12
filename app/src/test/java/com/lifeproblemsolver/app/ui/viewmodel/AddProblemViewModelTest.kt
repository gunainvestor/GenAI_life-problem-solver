package com.lifeproblemsolver.app.ui.viewmodel

import app.cash.turbine.test
import com.lifeproblemsolver.app.data.model.Priority
import com.lifeproblemsolver.app.data.repository.ProblemRepository
import com.lifeproblemsolver.app.data.analytics.AnalyticsService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class AddProblemViewModelTest {
    
    private lateinit var viewModel: AddProblemViewModel
    private lateinit var repository: ProblemRepository
    private lateinit var analyticsService: AnalyticsService
    private val testDispatcher = StandardTestDispatcher()
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mock()
        analyticsService = mock()
        viewModel = AddProblemViewModel(repository, analyticsService)
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `appendToDescription should append text to existing description`() = runTest {
        // Given
        viewModel.updateDescription("Initial description")
        
        // When
        viewModel.appendToDescription("additional text")
        
        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("Initial description additional text", state.description)
        }
    }
    
    @Test
    fun `appendToDescription should set text when description is empty`() = runTest {
        // Given
        viewModel.updateDescription("")
        
        // When
        viewModel.appendToDescription("new text")
        
        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("new text", state.description)
        }
    }
    
    @Test
    fun `updateDescription should update description correctly`() = runTest {
        // Given
        val newDescription = "Updated description"
        
        // When
        viewModel.updateDescription(newDescription)
        
        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(newDescription, state.description)
        }
    }
} 