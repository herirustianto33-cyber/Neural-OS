package com.example.ui



import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.RobotViewModel
import androidx.compose.material.icons.filled.BatteryFull


import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceVariantDark
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ConnectionLog(val time: String, val event: String)


@Composable
fun ConnectivityScreen(modifier: Modifier = Modifier) {
    val robotViewModel: RobotViewModel = viewModel()
    val robotStatus by robotViewModel.uiState.collectAsStateWithLifecycle()
    val batteryLevel = robotStatus.batteryLevel
    
    
    val latency = robotStatus.latency
    val signalStrength = robotStatus.signalStrength
    val heartbeatBpm = robotStatus.heartbeatBpm

    val logs = remember { mutableStateListOf<ConnectionLog>() }

    LaunchedEffect(Unit) {
        val timeFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
        logs.add(ConnectionLog(timeFormat.format(Date()), "Initializing hardware uplink..."))
        delay(500)
        logs.add(ConnectionLog(timeFormat.format(Date()), "WebSocket secured: wss://nexus-core.local"))
        logs.add(ConnectionLog(timeFormat.format(Date()), "Handshake accepted. Streaming telemetry."))
    }
    
    LaunchedEffect(latency) {
        val timeFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
        val now = timeFormat.format(Date())
        if ((1..10).random() > 6) {
            logs.add(0, ConnectionLog(now, "ACK received [ping: ${latency}ms]"))
        }
        if (logs.size > 20) logs.removeLast()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Hardware Link".uppercase(),
                    fontSize = 10.sp,
                    letterSpacing = 2.sp,
                    color = AccentPurple,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Connectivity Dashboard",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(SurfaceVariantDark)
                    .border(1.dp, Color(0xFF49454F), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.WifiTethering,
                    contentDescription = "Connectivity",
                    tint = AccentPurple,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Radar Visualizer
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.40f),
            shape = RoundedCornerShape(32.dp),
            color = SurfaceDark,
            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceVariantDark)
        ) {
            RadarVisualizer(signalStrength = signalStrength, latency = latency)
        }
        
        BatteryStatusIndicator(batteryLevel = batteryLevel)

        // Telemetry Metrics
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TelemetryCard(
                title = "LATENCY",
                value = "${latency}ms",
                subtitle = "WebSocket RTT",
                modifier = Modifier.weight(1f)
            )
            TelemetryCard(
                title = "SIGNAL",
                value = "${signalStrength}%",
                subtitle = "-${100 - signalStrength + 30} dBm",
                modifier = Modifier.weight(1f)
            )
            TelemetryCard(
                title = "HEARTBEAT",
                value = "OK",
                subtitle = "SYS.SYNC",
                modifier = Modifier.weight(1f)
            )
        }

        // Connection Logs
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.35f)
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF0F0F13),
            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceVariantDark)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.BluetoothConnected,
                        contentDescription = "Link",
                        tint = AccentPurple,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = "REAL-TIME UPLINK LOGS",
                        color = AccentPurple,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                LazyColumn {
                    items(logs) { log ->
                        Row(modifier = Modifier.padding(vertical = 4.dp)) {
                            Text(
                                text = "[${log.time}] ",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = log.event,
                                color = TextPrimary,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RadarVisualizer(signalStrength: Int, latency: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "radar")
    
    // Adjust radar speed based on latency (faster if latency is low)
    val duration = (latency * 20).coerceIn(1000, 3000)
    
    val radiusAnim1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(duration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radius1"
    )

    val radiusAnim2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(duration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radius2"
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val maxRadius = size.minDimension / 2.2f
            val center = androidx.compose.ui.geometry.Offset(size.width / 2, size.height / 2)

            // Draw base rings
            drawCircle(
                color = SurfaceVariantDark,
                radius = maxRadius,
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )
            drawCircle(
                color = SurfaceVariantDark,
                radius = maxRadius * 0.6f,
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )
            drawCircle(
                color = SurfaceVariantDark,
                radius = maxRadius * 0.2f,
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )

            // Draw expanding animated rings
            drawCircle(
                color = AccentPurple.copy(alpha = 1f - radiusAnim1),
                radius = maxRadius * radiusAnim1,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )
            
            // Staggered second ring
            val r2 = (radiusAnim1 + 0.5f) % 1f
            drawCircle(
                color = AccentPurple.copy(alpha = 1f - r2),
                radius = maxRadius * r2,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )

            // Center glowing core (brightness depends on signal strength)
            drawCircle(
                color = AccentPurple.copy(alpha = signalStrength / 100f),
                radius = 8.dp.toPx(),
                center = center
            )
        }
        
        // Overlay Status
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .background(SurfaceDark.copy(alpha = 0.8f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (signalStrength > 50) "LINK SECURED" else "WEAK SIGNAL",
                    color = if (signalStrength > 50) AccentPurple else Color(0xFFF2B8B5),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun TelemetryCard(title: String, value: String, subtitle: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = SurfaceDark,
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceVariantDark)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 10.sp,
                color = TextSecondary,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = AccentPurple,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
fun BatteryStatusIndicator(batteryLevel: Int, modifier: Modifier = Modifier) {
    val barColor = when {
        batteryLevel > 50 -> Color(0xFF4CAF50)
        batteryLevel > 20 -> Color(0xFFFFEB3B)
        else -> Color(0xFFF2B8B5) // red/pink
    }
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = SurfaceDark,
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceVariantDark)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.BatteryFull,
                        contentDescription = "Battery",
                        tint = barColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.size(6.dp))
                    Text(
                        text = "POWER STATUS",
                        fontSize = 10.sp,
                        color = TextSecondary,
                        letterSpacing = 1.sp
                    )
                }
                Text(
                    text = "$batteryLevel%",
                    fontSize = 14.sp,
                    color = barColor,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(BackgroundDark)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(batteryLevel / 100f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(barColor)
                )
            }
        }
    }
}
