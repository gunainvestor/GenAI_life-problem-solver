package com.lifeproblemsolver.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeproblemsolver.app.data.model.Priority
import com.lifeproblemsolver.app.data.repository.ProblemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class TrendViewModel @Inject constructor(
    private val repository: ProblemRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TrendUiState())
    val uiState: StateFlow<TrendUiState> = _uiState.asStateFlow()

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    init {
        loadTrends(TimePeriod.WEEK)
    }

    fun loadTrends(timePeriod: TimePeriod) {
        _uiState.update { it.copy(isLoading = true, selectedPeriod = timePeriod) }
        
        viewModelScope.launch {
            try {
                val (startDate, endDate) = getDateRange(timePeriod)
                val startDateStr = startDate.format(dateFormatter)
                val endDateStr = endDate.format(dateFormatter)
                
                // Get all problems in date range
                val problems = repository.getProblemsByDateRange(startDateStr, endDateStr)
                
                // Calculate trend data
                val dailyTrend = calculateDailyTrend(problems, startDate, endDate, timePeriod)
                val categoryTrend = calculateCategoryTrend(problems)
                val priorityTrend = calculatePriorityTrend(problems)
                val statusTrend = calculateStatusTrend(problems)
                
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        dailyTrend = dailyTrend,
                        categoryTrend = categoryTrend,
                        priorityTrend = priorityTrend,
                        statusTrend = statusTrend,
                        totalProblems = problems.size,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load trends"
                    )
                }
            }
        }
    }

    private fun getDateRange(timePeriod: TimePeriod): Pair<LocalDateTime, LocalDateTime> {
        val endDate = LocalDateTime.now()
        val startDate = when (timePeriod) {
            TimePeriod.WEEK -> endDate.minusDays(7)
            TimePeriod.MONTH -> endDate.minusDays(30)
            TimePeriod.SIX_MONTHS -> endDate.minusDays(180)
        }
        return Pair(startDate, endDate)
    }

    private fun calculateDailyTrend(
        problems: List<com.lifeproblemsolver.app.data.model.Problem>,
        startDate: LocalDateTime,
        endDate: LocalDateTime,
        timePeriod: TimePeriod
    ): List<DailyTrendData> {
        val trendMap = mutableMapOf<String, Int>()
        
        // Initialize all days/weeks/months in the range with 0
        val current = startDate.toLocalDate()
        val end = endDate.toLocalDate()
        var date = current
        while (!date.isAfter(end)) {
            val key = when (timePeriod) {
                TimePeriod.WEEK, TimePeriod.MONTH -> date.format(DateTimeFormatter.ofPattern("MM/dd"))
                TimePeriod.SIX_MONTHS -> date.format(DateTimeFormatter.ofPattern("MMM"))
            }
            trendMap[key] = 0
            date = when (timePeriod) {
                TimePeriod.WEEK, TimePeriod.MONTH -> date.plusDays(1)
                TimePeriod.SIX_MONTHS -> date.plusMonths(1)
            }
        }
        
        // Count problems by date
        problems.forEach { problem ->
            val problemDate = problem.createdAt.toLocalDate()
            val key = when (timePeriod) {
                TimePeriod.WEEK, TimePeriod.MONTH -> problemDate.format(DateTimeFormatter.ofPattern("MM/dd"))
                TimePeriod.SIX_MONTHS -> problemDate.format(DateTimeFormatter.ofPattern("MMM"))
            }
            trendMap[key] = (trendMap[key] ?: 0) + 1
        }
        
        return trendMap.entries.sortedBy { it.key }
            .map { DailyTrendData(it.key, it.value) }
    }

    private fun calculateCategoryTrend(
        problems: List<com.lifeproblemsolver.app.data.model.Problem>
    ): List<CategoryTrendData> {
        val categoryMap = mutableMapOf<String, Int>()
        
        problems.forEach { problem ->
            categoryMap[problem.category] = (categoryMap[problem.category] ?: 0) + 1
        }
        
        return categoryMap.entries
            .sortedByDescending { it.value }
            .map { CategoryTrendData(it.key, it.value) }
    }

    private fun calculatePriorityTrend(
        problems: List<com.lifeproblemsolver.app.data.model.Problem>
    ): List<PriorityTrendData> {
        val priorityMap = mutableMapOf<Priority, Int>()
        
        Priority.values().forEach { priority ->
            priorityMap[priority] = 0
        }
        
        problems.forEach { problem ->
            priorityMap[problem.priority] = (priorityMap[problem.priority] ?: 0) + 1
        }
        
        return priorityMap.entries
            .sortedByDescending { it.value }
            .map { PriorityTrendData(it.key, it.value) }
    }

    private fun calculateStatusTrend(
        problems: List<com.lifeproblemsolver.app.data.model.Problem>
    ): StatusTrendData {
        val resolved = problems.count { it.isResolved }
        val unresolved = problems.count { !it.isResolved }
        
        return StatusTrendData(resolved, unresolved)
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

enum class TimePeriod(val displayName: String) {
    WEEK("Last Week"),
    MONTH("Last Month"),
    SIX_MONTHS("Last 6 Months")
}

data class TrendUiState(
    val selectedPeriod: TimePeriod = TimePeriod.WEEK,
    val dailyTrend: List<DailyTrendData> = emptyList(),
    val categoryTrend: List<CategoryTrendData> = emptyList(),
    val priorityTrend: List<PriorityTrendData> = emptyList(),
    val statusTrend: StatusTrendData = StatusTrendData(0, 0),
    val totalProblems: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null
)

data class DailyTrendData(
    val date: String,
    val count: Int
)

data class CategoryTrendData(
    val category: String,
    val count: Int
)

data class PriorityTrendData(
    val priority: Priority,
    val count: Int
)

data class StatusTrendData(
    val resolved: Int,
    val unresolved: Int
)

