package com.lifeproblemsolver.app.data.repository

import android.util.Log
import com.lifeproblemsolver.app.data.dao.ProblemDao
import com.lifeproblemsolver.app.data.exception.RateLimitExceededException
import com.lifeproblemsolver.app.data.model.Problem
import com.lifeproblemsolver.app.data.model.Priority
import com.lifeproblemsolver.app.data.remote.AiService
import com.lifeproblemsolver.app.data.remote.SolutionRequest
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProblemRepository @Inject constructor(
    private val problemDao: ProblemDao,
    private val aiService: AiService
) {
    
    fun getAllProblems(): Flow<List<Problem>> {
        return problemDao.getAllProblems()
    }
    
    fun getProblemsByPriority(priority: String): Flow<List<Problem>> {
        return problemDao.getProblemsByPriority(priority)
    }
    
    fun getProblemsByCategory(category: String): Flow<List<Problem>> {
        return problemDao.getProblemsByCategory(category)
    }
    
    fun getProblemById(id: Long): Flow<Problem?> {
        return problemDao.getProblemById(id)
    }
    
    suspend fun insertProblem(problem: Problem): Long {
        return problemDao.insertProblem(problem)
    }
    
    suspend fun updateProblem(problem: Problem) {
        problemDao.updateProblem(problem)
    }
    
    suspend fun deleteProblem(problem: Problem) {
        problemDao.deleteProblem(problem)
    }
    
    suspend fun deleteProblemById(id: Long) {
        problemDao.deleteProblemById(id)
    }
    
    suspend fun markProblemAsResolved(id: Long, isResolved: Boolean) {
        problemDao.markProblemAsResolved(id, isResolved)
    }
    
    suspend fun updateProblemSolution(id: Long, solution: String) {
        problemDao.updateProblemSolution(id, solution)
    }
    
    fun getUnresolvedProblems(): Flow<List<Problem>> {
        return problemDao.getUnresolvedProblems()
    }
    
    fun getResolvedProblems(): Flow<List<Problem>> {
        return problemDao.getResolvedProblems()
    }
    
    fun searchProblems(query: String): Flow<List<Problem>> {
        return problemDao.searchProblems("%$query%")
    }
    
    suspend fun deleteAllProblems() {
        problemDao.deleteAllProblems()
    }
    
    fun getAllCategories(): Flow<List<String>> {
        return problemDao.getAllCategories()
    }
    
    fun getProblemsByStatus(isResolved: Boolean): Flow<List<Problem>> {
        return problemDao.getProblemsByStatus(isResolved)
    }
    
    suspend fun getProblemsByDateRange(startDate: String, endDate: String): List<Problem> {
        return problemDao.getProblemsByDateRange(startDate, endDate)
    }
    
    suspend fun createProblem(
        title: String,
        description: String,
        category: String,
        priority: Priority,
        notes: String = ""
    ): Long {
        android.util.Log.d("ProblemRepository", "createProblem called - title: '$title', category: '$category'")
        val problem = Problem(
            title = title,
            description = description,
            category = category,
            priority = priority,
            notes = notes
        )
        val problemId = insertProblem(problem)
        android.util.Log.d("ProblemRepository", "Problem inserted with ID: $problemId")
        return problemId
    }
    
    suspend fun updateProblemWithAiSolution(id: Long, solution: String) {
        updateProblemSolution(id, solution)
    }
    
    suspend fun updateSolutionRating(id: Long, rating: Float) {
        problemDao.updateSolutionRating(id, rating)
    }
    
    suspend fun getAverageSolutionRating(): Float? {
        return problemDao.getAverageSolutionRating()
    }
    
    suspend fun getRatedProblemsCount(): Int {
        return problemDao.getRatedProblemsCount()
    }
    
    suspend fun getProblemsWithSolutionCount(): Int {
        return problemDao.getProblemsWithSolutionCount()
    }
    
    suspend fun generateAiSolution(problem: Problem): String {
        Log.d("ProblemRepository", "Generating AI solution for problem: ${problem.title}")
        return try {
            val problemText = if (problem.description.isNotBlank()) {
                "${problem.title}: ${problem.description}"
            } else {
                problem.title
            }
            val request = SolutionRequest(
                problem = problemText,
                context = problem.category,
                category = problem.category
            )
            val response = aiService.generateSolution(request)
            Log.d("ProblemRepository", "AI solution generated successfully")
            response.solution
        } catch (e: RateLimitExceededException) {
            Log.e("ProblemRepository", "Rate limit exceeded", e)
            throw e
        } catch (e: Exception) {
            Log.e("ProblemRepository", "Error generating AI solution", e)
            throw e
        }
    }
    
    suspend fun createSampleProblems() {
        val sampleProblems = listOf(
            Problem(
                title = "Work-Life Balance",
                description = "Struggling to maintain a healthy balance between work and personal life",
                category = "Personal",
                priority = Priority.HIGH,
                notes = "Need to set better boundaries"
            ),
            Problem(
                title = "Career Growth",
                description = "Feeling stuck in current role and want to advance",
                category = "Career",
                priority = Priority.MEDIUM,
                notes = "Consider additional training or certifications"
            ),
            Problem(
                title = "Financial Planning",
                description = "Need to create a better budget and savings plan",
                category = "Finance",
                priority = Priority.URGENT,
                notes = "Review current expenses and income"
            )
        )
        
        sampleProblems.forEach { problem ->
            insertProblem(problem)
        }
    }
} 