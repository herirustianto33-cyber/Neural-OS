import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

imports = """
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.History
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.foundation.layout.height
import com.example.data.VoiceCommandLog
"""

content = re.sub(r'import com.example.ui.RobotViewModel', imports + '\nimport com.example.ui.RobotViewModel', content)

# 1. Update Speech Recognizer callback to log commands
voice_recognizer_target = """                if (!spokenText.isNullOrEmpty()) {
                    Toast.makeText(context, "Command: \\"$spokenText\\"", Toast.LENGTH_SHORT).show()
                    
                    scope.launch {
                        val voiceAction = parseVoiceCommand(spokenText)
                        if (voiceAction != null) {
                            when (voiceAction.action) {
                                "SHOW_LATENCY", "SHOW_CONNECTIVITY" -> {
                                    currentScreen = Screen.CONNECTIVITY
                                    robotViewModel.logVoiceCommand(spokenText, voiceAction.action, "SUCCESS")
                                }
                                "SHOW_INSIGHTS" -> {
                                    currentScreen = Screen.INSIGHTS
                                    robotViewModel.logVoiceCommand(spokenText, voiceAction.action, "SUCCESS")
                                }
                                "SHOW_DASHBOARD" -> {
                                    currentScreen = Screen.DASHBOARD
                                    robotViewModel.logVoiceCommand(spokenText, voiceAction.action, "SUCCESS")
                                }
                                "SHOW_HARDWARE" -> {
                                    currentScreen = Screen.HARDWARE
                                    robotViewModel.logVoiceCommand(spokenText, voiceAction.action, "SUCCESS")
                                }
                                "OPEN_CHAT" -> {
                                    currentScreen = Screen.CHAT
                                    robotViewModel.logVoiceCommand(spokenText, voiceAction.action, "SUCCESS")
                                }
                                "CALIBRATE_SENSORS" -> {
                                    Toast.makeText(context, "Calibrating sensors via Uplink...", Toast.LENGTH_LONG).show()
                                    robotViewModel.logVoiceCommand(spokenText, voiceAction.action, "SUCCESS")
                                }
                                else -> {
                                    Toast.makeText(context, "Unrecognized action: ${voiceAction.action}", Toast.LENGTH_SHORT).show()
                                    robotViewModel.logVoiceCommand(spokenText, voiceAction.action, "UNRECOGNIZED")
                                }
                            }
                        } else {
                            Toast.makeText(context, "Failed to parse command", Toast.LENGTH_SHORT).show()
                            robotViewModel.logVoiceCommand(spokenText, "UNKNOWN", "FAILED")
                        }
                    }
                }"""

old_voice_target = """                if (!spokenText.isNullOrEmpty()) {
                    Toast.makeText(context, "Command: \\"$spokenText\\"", Toast.LENGTH_SHORT).show()
                    
                    scope.launch {
                        val voiceAction = parseVoiceCommand(spokenText)
                        if (voiceAction != null) {
                            when (voiceAction.action) {
                                "SHOW_LATENCY", "SHOW_CONNECTIVITY" -> currentScreen = Screen.CONNECTIVITY
                                "SHOW_INSIGHTS" -> currentScreen = Screen.INSIGHTS
                                "SHOW_DASHBOARD" -> currentScreen = Screen.DASHBOARD
                                "SHOW_HARDWARE" -> currentScreen = Screen.HARDWARE
                                "OPEN_CHAT" -> currentScreen = Screen.CHAT
                                "CALIBRATE_SENSORS" -> Toast.makeText(context, "Calibrating sensors via Uplink...", Toast.LENGTH_LONG).show()
                                else -> Toast.makeText(context, "Unrecognized action: ${voiceAction.action}", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "Failed to parse command", Toast.LENGTH_SHORT).show()
                        }
                    }
                }"""

content = content.replace(old_voice_target, voice_recognizer_target)


# 2. Add `var showVoiceLogs by remember { mutableStateOf(false) }` and `val voiceLogs by robotViewModel.voiceLogs.collectAsStateWithLifecycle()`
state_target = """        val robotViewModel: RobotViewModel = viewModel()
        val robotStatus by robotViewModel.uiState.collectAsStateWithLifecycle()
        val voiceLogs by robotViewModel.voiceLogs.collectAsStateWithLifecycle()
        var showVoiceLogs by remember { mutableStateOf(false) }
"""
content = re.sub(r'        val robotViewModel: RobotViewModel = viewModel\(\)\n        val robotStatus by robotViewModel\.uiState\.collectAsStateWithLifecycle\(\)', state_target, content)


# 3. Update floatingActionButton to include History
fab_old = """              floatingActionButton = {
                  FloatingActionButton(
                      onClick = startVoiceRecognition,
                      containerColor = AccentPurple,
                      contentColor = Color(0xFF1D192B)
                  ) {
                      Icon(imageVector = Icons.Default.Mic, contentDescription = "Voice Command")
                  }
              },"""
fab_new = """              floatingActionButton = {
                  Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                      FloatingActionButton(
                          onClick = { showVoiceLogs = true },
                          containerColor = SurfaceCard,
                          contentColor = TextPrimary,
                          modifier = Modifier.size(48.dp)
                      ) {
                          Icon(imageVector = Icons.Default.History, contentDescription = "Voice Logs")
                      }
                      FloatingActionButton(
                          onClick = startVoiceRecognition,
                          containerColor = AccentPurple,
                          contentColor = Color(0xFF1D192B)
                      ) {
                          Icon(imageVector = Icons.Default.Mic, contentDescription = "Voice Command")
                      }
                  }
              },"""
content = content.replace(fab_old, fab_new)


# 4. Add the VoiceLogsSheet at the end of setContent
sheet_target = """              if (currentScreen == Screen.DASHBOARD) {"""
sheet_code = """
              if (showVoiceLogs) {
                  VoiceLogsSheet(logs = voiceLogs, onDismiss = { showVoiceLogs = false })
              }
              if (currentScreen == Screen.DASHBOARD) {"""
content = content.replace(sheet_target, sheet_code)


# 5. Add VoiceLogsSheet Composable function
sheet_composable = """
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceLogsSheet(logs: List<VoiceCommandLog>, onDismiss: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = BackgroundDark,
        contentColor = TextPrimary
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text("Command History", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(modifier = Modifier.height(16.dp))
            if (logs.isEmpty()) {
                Text("No voice commands recorded yet.", color = TextMuted)
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(logs) { log ->
                        Surface(color = SurfaceCard, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("\\"${log.commandText}\\"", fontStyle = FontStyle.Italic, color = TextPrimary)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Action: ${log.actionParsed}", fontSize = 12.sp, color = AccentPurple)
                                    Text(log.status, fontSize = 12.sp, color = if (log.status == "SUCCESS") Color.Green else Color.Red)
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
"""
content = content + sheet_composable

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
