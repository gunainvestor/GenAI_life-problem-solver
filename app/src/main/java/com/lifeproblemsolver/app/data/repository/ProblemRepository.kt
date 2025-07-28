package com.lifeproblemsolver.app.data.repository

import com.lifeproblemsolver.app.data.dao.ProblemDao
import com.lifeproblemsolver.app.data.model.Problem
import com.lifeproblemsolver.app.data.model.Priority
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProblemRepository @Inject constructor(
    private val problemDao: ProblemDao
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
    
    suspend fun createProblem(
        title: String,
        description: String,
        category: String,
        priority: Priority,
        notes: String = ""
    ): Long {
        val problem = Problem(
            title = title,
            description = description,
            category = category,
            priority = priority,
            notes = notes
        )
        return insertProblem(problem)
    }
    
    suspend fun updateProblemWithAiSolution(id: Long, solution: String) {
        updateProblemSolution(id, solution)
    }
    
    suspend fun generateAiSolution(problem: Problem): String {
        // This would typically call an AI service
        // For now, return a placeholder
        return "AI solution placeholder for: ${problem.title}"
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