package com.lifeproblemsolver.app.data.callback

import android.content.Context
import android.util.Log
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.lifeproblemsolver.app.services.ExcelExportService
import com.lifeproblemsolver.app.services.CsvExportService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

class DatabaseCallback @Inject constructor() : RoomDatabase.Callback() {
    
    companion object {
        private const val TAG = "DatabaseCallback"
        private var isInitialized = false
        private var lastExcelExportTime = 0L
        private var lastCsvExportTime = 0L
        private const val EXPORT_DEBOUNCE_MS = 30000L // 30 seconds debounce
        private const val MIN_EXPORT_INTERVAL_MS = 60000L // 1 minute minimum between exports
    }
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val excelExportMutex = Mutex()
    private val csvExportMutex = Mutex()
    private var isExcelExporting = false
    private var isCsvExporting = false
    
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        Log.d(TAG, "Database created")
        isInitialized = true
    }
    
    override fun onOpen(db: SupportSQLiteDatabase) {
        super.onOpen(db)
        Log.d(TAG, "Database opened")
        if (!isInitialized) {
            isInitialized = true
        }
    }
    
    /**
     * Trigger automatic Excel and CSV export when data changes
     * Implements debouncing to prevent multiple simultaneous exports
     */
    fun triggerAutoExport(context: Context) {
        // Excel export
        scope.launch {
            try {
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastExcelExportTime < MIN_EXPORT_INTERVAL_MS) {
                    Log.d(TAG, "Skipping Excel export - too soon since last export")
                    return@launch
                }
                excelExportMutex.withLock {
                    if (isExcelExporting) {
                        Log.d(TAG, "Excel export already in progress, skipping")
                        return@launch
                    }
                    isExcelExporting = true
                }
                delay(EXPORT_DEBOUNCE_MS)
                Log.d(TAG, "Triggering automatic Excel export")
                val exportService = ExcelExportService(context)
                val result = exportService.exportAllData()
                if (result.isSuccess) {
                    Log.d(TAG, "Auto Excel export successful: ${result.getOrNull()}")
                    lastExcelExportTime = System.currentTimeMillis()
                } else {
                    Log.e(TAG, "Auto Excel export failed", result.exceptionOrNull())
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error during auto Excel export", e)
            } finally {
                excelExportMutex.withLock {
                    isExcelExporting = false
                }
            }
        }
        // CSV export
        scope.launch {
            try {
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastCsvExportTime < MIN_EXPORT_INTERVAL_MS) {
                    Log.d(TAG, "Skipping CSV export - too soon since last export")
                    return@launch
                }
                csvExportMutex.withLock {
                    if (isCsvExporting) {
                        Log.d(TAG, "CSV export already in progress, skipping")
                        return@launch
                    }
                    isCsvExporting = true
                }
                delay(EXPORT_DEBOUNCE_MS)
                Log.d(TAG, "Triggering automatic CSV export")
                val exportService = CsvExportService(context)
                val result = exportService.exportAllData()
                if (result.isSuccess) {
                    Log.d(TAG, "Auto CSV export successful: ${result.getOrNull()}")
                    lastCsvExportTime = System.currentTimeMillis()
                } else {
                    Log.e(TAG, "Auto CSV export failed", result.exceptionOrNull())
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error during auto CSV export", e)
            } finally {
                csvExportMutex.withLock {
                    isCsvExporting = false
                }
            }
        }
    }
} 