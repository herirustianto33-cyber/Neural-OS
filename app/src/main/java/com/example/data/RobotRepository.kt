package com.example.data

import kotlinx.coroutines.flow.Flow

class RobotRepository(private val robotDao: RobotDao) {
    val robotStatus: Flow<RobotStatus?> = robotDao.getRobotStatus()

    suspend fun saveStatus(status: RobotStatus) {
        robotDao.saveStatus(status)
    }

    val voiceLogs: Flow<List<VoiceCommandLog>> = robotDao.getAllVoiceLogs()

    suspend fun insertVoiceLog(log: VoiceCommandLog) {
        robotDao.insertVoiceLog(log)
    }
}
