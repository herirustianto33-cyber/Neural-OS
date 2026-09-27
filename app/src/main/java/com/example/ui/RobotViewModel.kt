package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.RobotDatabase
import com.example.data.RobotRepository
import com.example.data.RobotStatus
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import com.example.data.VoiceCommandLog

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

class RobotViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: RobotRepository
    private val _uiState = MutableStateFlow(RobotStatus())
    val uiState: StateFlow<RobotStatus> = _uiState.asStateFlow()

    init {
        val database = RobotDatabase.getDatabase(application)
        repository = RobotRepository(database.robotDao())

        // Load cached status first
        viewModelScope.launch {
            repository.robotStatus.collect { cached ->
                if (cached != null) {
                    _uiState.value = cached
                }
            }
        }

        // Start simulation loop
        startSimulation()
    }


    val voiceLogs: StateFlow<List<VoiceCommandLog>> = repository.voiceLogs.stateIn(
        viewModelScope, SharingStarted.Lazily, emptyList()
    )

    fun logVoiceCommand(command: String, action: String, status: String) {
        viewModelScope.launch {
            repository.insertVoiceLog(VoiceCommandLog(commandText = command, actionParsed = action, status = status))
        }
    }

    private var lastReconnectTime = 0L

    fun reconnect() {
        lastReconnectTime = System.currentTimeMillis()
        val current = _uiState.value
        val newState = current.copy(
            signalStrength = 100,
            heartbeatBpm = 60,
            latency = 12
        )
        _uiState.value = newState
        viewModelScope.launch {
            repository.saveStatus(newState)
        }
    }

    private fun startSimulation() {
        viewModelScope.launch {
            var counter = 0
            while (true) {
                delay(1000)
                counter++
                val current = _uiState.value
                
                // Simulate battery drain
                val newBatLevel = if (current.batteryLevel - 1 < 0) 100 else current.batteryLevel - 1
                
                // Simulate temps
                val cpu = (current.cpuTemp + (Random.nextFloat() * 4 - 2)).coerceIn(30f, 85f)
                val motorL = (current.motorLTemp + (Random.nextFloat() * 3 - 1.5f)).coerceIn(30f, 75f)
                val motorR = (current.motorRTemp + (Random.nextFloat() * 4 - 1.5f)).coerceIn(30f, 80f)
                val lidar = (current.lidarTemp + (Random.nextFloat() * 2 - 1)).coerceIn(25f, 50f)
                val bat = (current.batTemp + (Random.nextFloat() * 2 - 1)).coerceIn(30f, 60f)
                
                // Simulate Connectivity / Heartbeat
                var latency = (current.latency + (-4..4).random()).coerceIn(12, 120)
                var signalStrength = (current.signalStrength + (-2..2).random()).coerceIn(40, 100)
                var heartbeatBpm = (current.heartbeatBpm + (-1..2).random()).coerceIn(55, 80)
                
                // Occasional critical drop every 20 seconds for demo purposes
                val timeSinceReconnect = System.currentTimeMillis() - lastReconnectTime
                if (counter % 20 in 1..3 && timeSinceReconnect > 10000) {
                    signalStrength = Random.nextInt(0, 15) // Critical threshold (e.g. < 20)
                    heartbeatBpm = Random.nextInt(0, 30) // Critical heartbeat
                }
                
                val newState = current.copy(
                    batteryLevel = newBatLevel,
                    cpuTemp = cpu,
                    motorLTemp = motorL,
                    motorRTemp = motorR,
                    lidarTemp = lidar,
                    batTemp = bat,
                    latency = latency,
                    signalStrength = signalStrength,
                    heartbeatBpm = heartbeatBpm
                )
                
                _uiState.value = newState
                
                // Save to Room cache
                repository.saveStatus(newState)
            }
        }
    }
}
