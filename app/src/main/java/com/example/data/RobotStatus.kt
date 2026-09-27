package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "robot_status")
data class RobotStatus(
    @PrimaryKey val id: Int = 1,
    val batteryLevel: Int = 100,
    val cpuTemp: Float = 45f,
    val motorLTemp: Float = 35f,
    val motorRTemp: Float = 36f,
    val lidarTemp: Float = 30f,
    val batTemp: Float = 40f,
    val heartbeatBpm: Int = 60,
    val signalStrength: Int = 92,
    val latency: Int = 24
)
