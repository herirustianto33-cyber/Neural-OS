package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RobotDao {
    @Query("SELECT * FROM robot_status WHERE id = 1")
    fun getRobotStatus(): Flow<RobotStatus?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveStatus(status: RobotStatus)

    @Query("SELECT * FROM voice_logs ORDER BY timestamp DESC")
    fun getAllVoiceLogs(): Flow<List<VoiceCommandLog>>

    @Insert
    suspend fun insertVoiceLog(log: VoiceCommandLog)
}
