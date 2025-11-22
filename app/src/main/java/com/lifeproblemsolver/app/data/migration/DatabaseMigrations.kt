package com.lifeproblemsolver.app.data.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Database migrations to preserve data during version upgrades.
 * 
 * IMPORTANT: This file ensures that user data is preserved when the database schema changes.
 * Without proper migrations, Room will fail to upgrade the database, potentially causing data loss.
 * 
 * When adding a new migration:
 * 1. Increment the database version in AppDatabase.kt (e.g., from version = 1 to version = 2)
 * 2. Create a new Migration object here (e.g., MIGRATION_1_2)
 * 3. Add it to the migrations list in getMigrations() function below
 * 
 * Common migration patterns:
 * 
 * 1. Adding a new column:
 *    database.execSQL("ALTER TABLE problems ADD COLUMN new_field TEXT DEFAULT ''")
 * 
 * 2. Creating a new table:
 *    database.execSQL("""
 *        CREATE TABLE new_table (
 *            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
 *            name TEXT NOT NULL,
 *            created_at INTEGER NOT NULL
 *        )
 *    """.trimIndent())
 * 
 * 3. Renaming a table:
 *    database.execSQL("ALTER TABLE old_table RENAME TO new_table")
 * 
 * 4. Dropping a column (SQLite doesn't support DROP COLUMN directly):
 *    - Create new table without the column
 *    - Copy data from old table
 *    - Drop old table
 *    - Rename new table
 * 
 * 5. Changing column type:
 *    - Similar to dropping a column, requires table recreation
 * 
 * Example migration:
 * 
 * val MIGRATION_1_2 = object : Migration(1, 2) {
 *     override fun migrate(database: SupportSQLiteDatabase) {
 *         // Add new column with default value
 *         database.execSQL("ALTER TABLE problems ADD COLUMN tags TEXT DEFAULT ''")
 *         
 *         // Create new table
 *         database.execSQL("""
 *             CREATE TABLE IF NOT EXISTS problem_tags (
 *                 id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
 *                 problem_id INTEGER NOT NULL,
 *                 tag TEXT NOT NULL,
 *                 FOREIGN KEY(problem_id) REFERENCES problems(id) ON DELETE CASCADE
 *             )
 *         """.trimIndent())
 *     }
 * }
 */

// ============================================================================
// MIGRATION DEFINITIONS
// ============================================================================
// Add your migration objects here as the database version increases.
// Each migration should be named MIGRATION_X_Y where X is the old version and Y is the new version.

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Add solutionRating column to problems table
        database.execSQL("ALTER TABLE problems ADD COLUMN solutionRating REAL")
    }
}

// ============================================================================
// MIGRATION REGISTRY
// ============================================================================
/**
 * Returns all database migrations in order.
 * 
 * This function ensures migrations are applied in the correct sequence.
 * Add new migrations to the list as you create them.
 * 
 * @return Array of Migration objects to be applied during database upgrades
 */
fun getMigrations(): Array<Migration> {
    val migrations = mutableListOf<Migration>()
    
    // Add migrations in chronological order (oldest to newest)
    migrations.add(MIGRATION_1_2)
    
    return migrations.toTypedArray()
}

