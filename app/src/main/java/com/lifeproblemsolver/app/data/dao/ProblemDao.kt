package com.lifeproblemsolver.app.data.dao

import androidx.room.*
import com.lifeproblemsolver.app.data.model.Problem
import kotlinx.coroutines.flow.Flow

@Dao
interface ProblemDao {
    @Query("SELECT * FROM problems ORDER BY createdAt DESC")
    fun getAllProblems(): Flow<List<Problem>>

    @Query("SELECT * FROM problems WHERE id = :id")
    suspend fun getProblemById(id: Long): Problem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProblem(problem: Problem): Long

    @Update
    suspend fun updateProblem(problem: Problem)

    @Delete
    suspend fun deleteProblem(problem: Problem)

    @Query("SELECT * FROM problems WHERE category = :category ORDER BY createdAt DESC")
    fun getProblemsByCategory(category: String): Flow<List<Problem>>

    @Query("SELECT * FROM problems WHERE priority = :priority ORDER BY createdAt DESC")
    fun getProblemsByPriority(priority: String): Flow<List<Problem>>

    @Query("SELECT * FROM problems WHERE isResolved = :isResolved ORDER BY createdAt DESC")
    fun getProblemsByStatus(isResolved: Boolean): Flow<List<Problem>>

    @Query("SELECT DISTINCT category FROM problems ORDER BY category")
    fun getAllCategories(): Flow<List<String>>
    
    @Query("DELETE FROM problems")
    suspend fun deleteAllProblems()

    @Query("SELECT * FROM problems LIMIT :limit OFFSET :offset")
    suspend fun getProblemsPaged(limit: Int, offset: Int): List<Problem>
} 