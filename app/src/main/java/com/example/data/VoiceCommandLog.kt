package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "voice_logs")
data class VoiceCommandLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val commandText: String,
    val actionParsed: String,
    val status: String,
    val timestamp: Long = System.currentTimeMillis()
)
