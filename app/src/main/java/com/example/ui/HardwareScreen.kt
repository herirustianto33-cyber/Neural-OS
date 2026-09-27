package com.example.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceVariantDark
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

data class HardwareComponent(
    val id: String,
    val name: String,
    val temp: Float,
    val minTemp: Float = 30f,
    val maxTemp: Float = 85f
) {
    fun getStatus(): String {
        val fraction = ((temp - minTemp) / (maxTemp - minTemp)).coerceIn(0f, 1f)
        return when {
            fraction > 0.8f -> "CRITICAL"
            fraction > 0.6f -> "WARNING"
            else -> "NOMINAL"
        }
    }
}

fun getThermalColor(temp: Float, min: Float, max: Float): Color {
    val fraction = ((temp - min) / (max - min)).coerceIn(0f, 1f)
    return when {
        fraction < 0.25f -> lerp(Color(0xFF0055FF), Color(0xFF00FFFF), fraction / 0.25f)
        fraction < 0.50f -> lerp(Color(0xFF00FFFF), Color(0xFFFFFF00), (fraction - 0.25f) / 0.25f)
        else -> lerp(Color(0xFFFFFF00), Color(0xFFFF0000), (fraction - 0.5f) / 0.5f)
    }
}

@Composable
fun HardwareScreen(modifier: Modifier = Modifier) {
    val robotViewModel: RobotViewModel = viewModel()
    val robotStatus by robotViewModel.uiState.collectAsStateWithLifecycle()

    val cpu = HardwareComponent("CPU", "Main Core", robotStatus.cpuTemp)
    val motorL = HardwareComponent("MOT_L", "Left Track", robotStatus.motorLTemp, maxTemp = 75f)
    val motorR = HardwareComponent("MOT_R", "Right Track", robotStatus.motorRTemp, maxTemp = 80f)
    val lidar = HardwareComponent("LDR", "Lidar Array", robotStatus.lidarTemp, maxTemp = 50f)
    val battery = HardwareComponent("BAT", "Power Cell", robotStatus.batTemp, maxTemp = 60f)

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
                    text = "Diagnostics".uppercase(),
                    fontSize = 10.sp,
                    letterSpacing = 2.sp,
                    color = AccentPurple,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Hardware Topology",
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
                    imageVector = Icons.Default.Build,
                    contentDescription = "Build",
                    tint = AccentPurple,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // 3D Isometric Map
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.55f),
            shape = RoundedCornerShape(32.dp),
            color = SurfaceDark,
            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceVariantDark)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    val scale = size.minDimension / 5.5f
                    val center = Offset(size.width / 2, size.height / 2 + scale * 0.5f)

                    // Draw Base Chassis
                    drawIsoBlock(-1.5f, -2.0f, 1.5f, 2.0f, 0f, 0.2f, center, scale, Color(0xFF222233), strokeOnly = false)

                    // Draw Left Track
                    drawIsoBlock(-2.2f, -1.8f, -1.6f, 1.8f, 0f, 0.5f, center, scale, getThermalColor(motorL.temp, motorL.minTemp, motorL.maxTemp))
                    
                    // Draw Right Track
                    drawIsoBlock(1.6f, -1.8f, 2.2f, 1.8f, 0f, 0.5f, center, scale, getThermalColor(motorR.temp, motorR.minTemp, motorR.maxTemp))

                    // Draw Lidar (Front/Top)
                    drawIsoBlock(-0.5f, -1.5f, 0.5f, -0.5f, 0.2f, 0.8f, center, scale, getThermalColor(lidar.temp, lidar.minTemp, lidar.maxTemp))

                    // Draw CPU (Center)
                    drawIsoBlock(-0.6f, -0.2f, 0.6f, 0.6f, 0.2f, 0.4f, center, scale, getThermalColor(cpu.temp, cpu.minTemp, cpu.maxTemp))

                    // Draw Battery (Rear)
                    drawIsoBlock(-1.0f, 0.8f, 1.0f, 1.8f, 0.2f, 0.6f, center, scale, getThermalColor(battery.temp, battery.minTemp, battery.maxTemp))
                }
                
                Text(
                    text = "3D THERMAL MAPPING",
                    color = AccentPurple.copy(alpha = 0.5f),
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)
                )
            }
        }

        // Component List
        Column(
            modifier = Modifier.weight(0.45f).padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val components = listOf(cpu, motorL, motorR, lidar, battery)
            components.forEach { comp ->
                ComponentRow(comp)
            }
        }
    }
}

fun projectIso(x: Float, y: Float, z: Float, center: Offset, scale: Float): Offset {
    val isoX = (x - y) * 0.866f // cos(30)
    val isoY = (x + y) * 0.5f - z // sin(30)
    return Offset(center.x + isoX * scale, center.y + isoY * scale)
}

fun DrawScope.drawIsoBlock(
    x1: Float, y1: Float, x2: Float, y2: Float, z1: Float, z2: Float,
    center: Offset, scale: Float,
    color: Color,
    strokeOnly: Boolean = false
) {
    val p1 = projectIso(x1, y1, z2, center, scale) // Top-Left
    val p2 = projectIso(x2, y1, z2, center, scale) // Top-Right
    val p3 = projectIso(x2, y2, z2, center, scale) // Bottom-Right
    val p4 = projectIso(x1, y2, z2, center, scale) // Bottom-Left
    
    val p5 = projectIso(x1, y1, z1, center, scale) // Base Top-Left
    val p6 = projectIso(x2, y1, z1, center, scale) // Base Top-Right
    val p7 = projectIso(x2, y2, z1, center, scale) // Base Bottom-Right
    val p8 = projectIso(x1, y2, z1, center, scale) // Base Bottom-Left
    
    val leftColor = color.copy(alpha = color.alpha * 0.5f)
    val rightColor = color.copy(alpha = color.alpha * 0.25f)
    val topColor = color.copy(alpha = color.alpha * 0.8f)

    if (!strokeOnly) {
        // Left Face
        val leftFace = Path().apply {
            moveTo(p1.x, p1.y)
            lineTo(p4.x, p4.y)
            lineTo(p8.x, p8.y)
            lineTo(p5.x, p5.y)
            close()
        }
        drawPath(leftFace, color = leftColor)
        
        // Right Face
        val rightFace = Path().apply {
            moveTo(p4.x, p4.y)
            lineTo(p3.x, p3.y)
            lineTo(p7.x, p7.y)
            lineTo(p8.x, p8.y)
            close()
        }
        drawPath(rightFace, color = rightColor)
        
        // Top Face
        val topFace = Path().apply {
            moveTo(p1.x, p1.y)
            lineTo(p2.x, p2.y)
            lineTo(p3.x, p3.y)
            lineTo(p4.x, p4.y)
            close()
        }
        drawPath(topFace, color = topColor)
    }

    // Edges
    val edgeColor = if (strokeOnly) color else color.copy(alpha = 1f)
    val edgeStyle = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
    
    val wireframe = Path().apply {
        // Top rect
        moveTo(p1.x, p1.y)
        lineTo(p2.x, p2.y)
        lineTo(p3.x, p3.y)
        lineTo(p4.x, p4.y)
        close()
        // Visible verticals
        moveTo(p1.x, p1.y); lineTo(p5.x, p5.y)
        moveTo(p3.x, p3.y); lineTo(p7.x, p7.y)
        moveTo(p4.x, p4.y); lineTo(p8.x, p8.y)
        // Visible bottom edges
        moveTo(p5.x, p5.y); lineTo(p8.x, p8.y)
        lineTo(p7.x, p7.y)
    }
    drawPath(wireframe, color = edgeColor, style = edgeStyle)
}

@Composable
fun ComponentRow(comp: HardwareComponent) {
    val statusColor = when (comp.getStatus()) {
        "CRITICAL" -> Color(0xFFFF4444)
        "WARNING" -> Color(0xFFFFFF00)
        else -> AccentPurple
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0F0F13), RoundedCornerShape(12.dp))
            .border(1.dp, SurfaceVariantDark, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = comp.id,
                fontSize = 10.sp,
                color = TextSecondary,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = comp.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }
        
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = "${comp.temp.toInt()}°C",
                fontSize = 16.sp,
                fontFamily = FontFamily.Monospace,
                color = getThermalColor(comp.temp, comp.minTemp, comp.maxTemp)
            )
            
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(statusColor.copy(alpha = 0.2f))
                    .border(1.dp, statusColor.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = comp.getStatus(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = statusColor,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
