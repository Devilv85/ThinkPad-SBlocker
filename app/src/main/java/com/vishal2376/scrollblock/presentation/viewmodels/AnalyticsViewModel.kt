package com.vishal2376.scrollblock.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vishal2376.scrollblock.data.local.SessionDataDao
import com.vishal2376.scrollblock.domain.model.SessionData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val sessionDataDao: SessionDataDao
) : ViewModel() {
    
    private val _analyticsData = MutableStateFlow(AnalyticsData())
    val analyticsData: StateFlow<AnalyticsData> = _analyticsData.asStateFlow()
    
    private val _recentSessions = MutableStateFlow<List<SessionData>>(emptyList())
    val recentSessions: StateFlow<List<SessionData>> = _recentSessions.asStateFlow()
    
    init {
        loadAnalyticsData()
        loadRecentSessions()
    }
    
    private fun loadAnalyticsData() {
        viewModelScope.launch {
            val today = LocalDate.now()
            val todayStart = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val todayEnd = today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            
            sessionDataDao.getSessionsInRange(todayStart, todayEnd).collect { sessions ->
                val totalBlockedScrolls = sessions.sumOf { it.blockedScrolls }
                val totalSessions = sessions.size
                val averageSessionDuration = if (sessions.isNotEmpty()) {
                    sessions.map { it.endTime - it.startTime }.average().toInt()
                } else 0
                
                // Estimate time saved (blocked scrolls * average time per scroll)
                val timeSaved = totalBlockedScrolls * 3 // Assume 3 seconds per blocked scroll
                
                _analyticsData.value = AnalyticsData(
                    totalBlockedScrolls = totalBlockedScrolls,
                    timeSaved = timeSaved,
                    totalSessions = totalSessions,
                    averageSessionDuration = averageSessionDuration
                )
            }
        }
    }
    
    private fun loadRecentSessions() {
        viewModelScope.launch {
            // Get sessions from all supported apps
            val packages = listOf(
                "com.instagram.android",
                "com.google.android.youtube",
                "com.linkedin.android",
                "com.snapchat.android"
            )
            
            val allSessions = mutableListOf<SessionData>()
            packages.forEach { packageName ->
                sessionDataDao.getRecentSessions(packageName).collect { sessions ->
                    allSessions.addAll(sessions)
                }
            }
            
            _recentSessions.value = allSessions.sortedByDescending { it.startTime }.take(10)
        }
    }
}

data class AnalyticsData(
    val totalBlockedScrolls: Int = 0,
    val timeSaved: Int = 0,
    val totalSessions: Int = 0,
    val averageSessionDuration: Int = 0
)