package com.lifeproblemsolver.app.data.dao

import androidx.room.*
import com.lifeproblemsolver.app.data.model.Problem
import kotlinx.coroutines.flow.Flow

@Dao
interface ProblemDao {
    
    @Query("SELECT * FROM problems ORDER BY createdAt DESC")
    fun getAllProblems(): Flow<List<Problem>>
    
    @Query("SELECT * FROM problems WHERE priority = :priority ORDER BY createdAt DESC")
    fun getProblemsByPriority(priority: String): Flow<List<Problem>>
    
    @Query("SELECT * FROM problems WHERE category = :category ORDER BY createdAt DESC")
    fun getProblemsByCategory(category: String): Flow<List<Problem>>
    
    @Query("SELECT * FROM problems WHERE id = :id")
    fun getProblemById(id: Long): Flow<Problem?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProblem(problem: Problem): Long
    
    @Update
    suspend fun updateProblem(problem: Problem)
    
    @Delete
    suspend fun deleteProblem(problem: Problem)
    
    @Query("DELETE FROM problems WHERE id = :id")
    suspend fun deleteProblemById(id: Long)
    
    @Query("UPDATE problems SET isResolved = :isResolved WHERE id = :id")
    suspend fun markProblemAsResolved(id: Long, isResolved: Boolean)
    
    @Query("UPDATE problems SET solution = :solution WHERE id = :id")
    suspend fun updateProblemSolution(id: Long, solution: String)
    
    @Query("SELECT * FROM problems WHERE isResolved = 0 ORDER BY createdAt DESC")
    fun getUnresolvedProblems(): Flow<List<Problem>>
    
    @Query("SELECT * FROM problems WHERE isResolved = 1 ORDER BY createdAt DESC")
    fun getResolvedProblems(): Flow<List<Problem>>
    
    @Query("SELECT * FROM problems WHERE title LIKE :query OR description LIKE :query ORDER BY createdAt DESC")
    fun searchProblems(query: String): Flow<List<Problem>>
    
    @Query("DELETE FROM problems")
    suspend fun deleteAllProblems()
    
    @Query("SELECT DISTINCT category FROM problems ORDER BY category")
    fun getAllCategories(): Flow<List<String>>
    
    @Query("SELECT * FROM problems WHERE isResolved = :isResolved ORDER BY createdAt DESC")
    fun getProblemsByStatus(isResolved: Boolean): Flow<List<Problem>>
    
    @Query("SELECT * FROM problems ORDER BY createdAt DESC LIMIT :limit OFFSET :offset")
    suspend fun getProblemsPaged(limit: Int, offset: Int): List<Problem>
} 