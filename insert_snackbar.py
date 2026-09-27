import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

# 1. Add imports if missing
imports = """
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
"""
content = re.sub(r'package com.example', 'package com.example\n' + imports, content)

# 2. Add SnackbarHostState to val declarations inside setContent
state_declarations = """        val batteryLevel = robotStatus.batteryLevel
        val snackbarHostState = remember { SnackbarHostState() }
        
        LaunchedEffect(robotStatus.signalStrength, robotStatus.heartbeatBpm) {
            if (robotStatus.signalStrength < 20 || robotStatus.heartbeatBpm < 30) {
                snackbarHostState.showSnackbar(
                    message = "CRITICAL: Robot Heartbeat Lost! Signal: ${robotStatus.signalStrength}%, BPM: ${robotStatus.heartbeatBpm}",
                    actionLabel = "DISMISS",
                    duration = SnackbarDuration.Long
                )
            }
        }
"""
content = re.sub(r'val batteryLevel = robotStatus.batteryLevel', state_declarations, content)

# 3. Add snackbarHost to Scaffold
scaffold_code = """              containerColor = BackgroundDark,
              snackbarHost = { SnackbarHost(snackbarHostState) },
              floatingActionButton = {"""
content = re.sub(r'containerColor = BackgroundDark,\s+floatingActionButton = \{', scaffold_code, content)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)

