#!/bin/bash
cat << 'INNER_EOF' > app/src/main/java/com/example/ui/InsightsScreen.kt
package com.example.ui

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceVariantDark
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.FloatEntry
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun InsightsScreen(modifier: Modifier = Modifier) {
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
                    text = "System Telemetry".uppercase(),
                    fontSize = 10.sp,
                    letterSpacing = 2.sp,
                    color = AccentPurple,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Latency Trends",
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
                    imageVector = Icons.Default.Timeline,
                    contentDescription = "Analytics",
                    tint = AccentPurple,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Main Latency Trend Chart
        LatencyTrendChartCard(modifier = Modifier.weight(1f))
        
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun LatencyTrendChartCard(modifier: Modifier = Modifier) {
    val modelProducer = remember { ChartEntryModelProducer() }
    
    val totalHours = 168 // 7 days * 24 hours
    
    LaunchedEffect(Unit) {
        val generated = mutableListOf<FloatEntry>()
        for (i in 0 until totalHours) {
            val isSpike = Random.nextFloat() < 0.05f
            val latency = if (isSpike) Random.nextInt(200, 501) else Random.nextInt(40, 61)
            generated.add(FloatEntry(i.toFloat(), latency.toFloat()))
        }
        modelProducer.setEntries(generated)

        // Real-time update simulation
        while (true) {
            delay(2000)
            val isSpike = Random.nextFloat() < 0.05f
            val newLatency = if (isSpike) Random.nextInt(200, 501) else Random.nextInt(40, 61)
            generated.removeAt(0)
            
            // Shift x coordinates so the chart scrolls
            val updated = generated.mapIndexed { index, entry -> 
                FloatEntry(index.toFloat(), entry.y)
            }.toMutableList()
            
            updated.add(FloatEntry((totalHours - 1).toFloat(), newLatency.toFloat()))
            
            generated.clear()
            generated.addAll(updated)
            
            modelProducer.setEntries(generated)
        }
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        color = SurfaceDark,
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceVariantDark)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoGraph,
                        contentDescription = "Chart",
                        tint = TextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = "Real-time Latency (Vico Engine)",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))

            // Chart area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(BackgroundDark, RoundedCornerShape(16.dp))
                    .border(1.dp, SurfaceVariantDark, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Chart(
                    chart = lineChart(),
                    chartModelProducer = modelProducer,
                    startAxis = rememberStartAxis(),
                    bottomAxis = rememberBottomAxis(),
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Zoom / Pan to explore timeline", color = TextSecondary, fontSize = 12.sp)
            }
        }
    }
}
INNER_EOF
