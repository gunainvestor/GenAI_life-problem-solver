package com.lifeproblemsolver.app.data.repository

import android.util.Log
import com.lifeproblemsolver.app.data.dao.ProblemDao
import com.lifeproblemsolver.app.data.model.Problem
import com.lifeproblemsolver.app.data.model.Priority
import com.lifeproblemsolver.app.data.remote.AiService
import com.lifeproblemsolver.app.data.remote.SolutionRequest
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProblemRepository @Inject constructor(
    private val problemDao: ProblemDao,
    private val aiService: AiService
) {
    private val TAG = "ProblemRepository"
    fun getAllProblems(): Flow<List<Problem>> = problemDao.getAllProblems()
    
    fun getProblemsByCategory(category: String): Flow<List<Problem>> = 
        problemDao.getProblemsByCategory(category)
    
    fun getProblemsByPriority(priority: String): Flow<List<Problem>> = 
        problemDao.getProblemsByPriority(priority)
    
    fun getProblemsByStatus(isResolved: Boolean): Flow<List<Problem>> = 
        problemDao.getProblemsByStatus(isResolved)
    
    fun getAllCategories(): Flow<List<String>> = problemDao.getAllCategories()
    
    suspend fun getProblemById(id: Long): Problem? = problemDao.getProblemById(id)
    
    suspend fun insertProblem(problem: Problem): Long = problemDao.insertProblem(problem)
    
    suspend fun updateProblem(problem: Problem) = problemDao.updateProblem(problem)
    
    suspend fun deleteProblem(problem: Problem) = problemDao.deleteProblem(problem)
    
    suspend fun generateAiSolution(problem: Problem): String {
        Log.d(TAG, "generateAiSolution called for problem: ${problem.title}")
        
        val request = SolutionRequest(
            problem = problem.title,
            context = problem.description,
            category = problem.category
        )
        Log.d(TAG, "Created SolutionRequest: problem='${request.problem}', category='${request.category}'")
        
        return try {
            Log.d(TAG, "Calling aiService.generateSolution...")
            val response = aiService.generateSolution(request)
            Log.d(TAG, "Received AI response, solution length: ${response.solution.length}")
            Log.d(TAG, "Solution preview: ${response.solution.take(100)}...")
            response.solution
        } catch (e: Exception) {
            Log.e(TAG, "Error in generateAiSolution", e)
            Log.e(TAG, "Error message: ${e.message}")
            Log.e(TAG, "Error type: ${e.javaClass.simpleName}")
            "Unable to generate AI solution at this time. Please try again later."
        }
    }
    
    suspend fun createProblem(
        title: String,
        description: String,
        category: String = "General",
        priority: Priority = Priority.MEDIUM
    ): Long {
        val problem = Problem(
            title = title,
            description = description,
            category = category,
            priority = priority,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        return insertProblem(problem)
    }
    
    suspend fun updateProblemWithAiSolution(problemId: Long, aiSolution: String) {
        val problem = getProblemById(problemId) ?: return
        val updatedProblem = problem.copy(
            aiSolution = aiSolution,
            updatedAt = LocalDateTime.now()
        )
        updateProblem(updatedProblem)
    }
    
    suspend fun markProblemAsResolved(problemId: Long) {
        val problem = getProblemById(problemId) ?: return
        val updatedProblem = problem.copy(
            isResolved = true,
            updatedAt = LocalDateTime.now()
        )
        updateProblem(updatedProblem)
    }
    
    suspend fun createSampleProblems() {
        val sampleProblems = listOf(
            Problem(
                title = "Work Schedule Conflict",
                description = "Need to balance multiple project deadlines this week",
                category = "Professional",
                priority = Priority.HIGH,
                createdAt = LocalDateTime.now().minusDays(2),
                updatedAt = LocalDateTime.now().minusDays(2)
            ),
            Problem(
                title = "Health Checkup",
                description = "Annual physical examination due this month",
                category = "Health",
                priority = Priority.MEDIUM,
                createdAt = LocalDateTime.now().minusDays(1),
                updatedAt = LocalDateTime.now().minusDays(1)
            ),
            Problem(
                title = "Budget Planning",
                description = "Need to create a monthly budget and stick to it",
                category = "Financial",
                priority = Priority.MEDIUM,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            ),
            Problem(
                title = "Relationship Communication",
                description = "Improve communication with partner about future plans",
                category = "Relationships",
                priority = Priority.LOW,
                createdAt = LocalDateTime.now().minusDays(3),
                updatedAt = LocalDateTime.now().minusDays(3)
            )
        )
        
        sampleProblems.forEach { problem ->
            insertProblem(problem)
        }
        Log.d(TAG, "Created ${sampleProblems.size} sample problems")
    }
} 