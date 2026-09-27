import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

# Add imports
imports_to_add = """
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HelpOutline
"""
content = re.sub(r'import androidx.compose.material.icons.Icons', imports_to_add.strip() + '\nimport androidx.compose.material.icons.Icons', content)

# Target for replacement
old_sheet = """fun VoiceLogsSheet(logs: List<VoiceCommandLog>, onDismiss: () -> Unit) {
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
}"""

new_sheet = """fun VoiceLogsSheet(logs: List<VoiceCommandLog>, onDismiss: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = BackgroundDark,
        contentColor = TextPrimary
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text("Command Log", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(modifier = Modifier.height(16.dp))
            if (logs.isEmpty()) {
                Text("No voice commands recorded yet.", color = TextMuted)
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(logs) { log ->
                        Surface(color = SurfaceCard, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                            Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                val statusIcon = when (log.status) {
                                    "SUCCESS" -> Icons.Default.CheckCircle
                                    "UNRECOGNIZED" -> Icons.Default.HelpOutline
                                    else -> Icons.Default.Error
                                }
                                val statusColor = when (log.status) {
                                    "SUCCESS" -> Color(0xFF4CAF50)
                                    "UNRECOGNIZED" -> Color(0xFFFFEB3B)
                                    else -> Color(0xFFF44336)
                                }
                                
                                Icon(
                                    imageVector = statusIcon,
                                    contentDescription = log.status,
                                    tint = statusColor,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("\\"${log.commandText}\\"", fontStyle = FontStyle.Italic, color = TextPrimary, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("Action: ${log.actionParsed}", fontSize = 12.sp, color = AccentPurple)
                                }
                                Text(log.status, fontSize = 10.sp, color = statusColor, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}"""

content = content.replace(old_sheet, new_sheet)

# Add width import just in case
if 'import androidx.compose.foundation.layout.width' not in content:
    content = re.sub(r'import androidx.compose.foundation.layout.height', 'import androidx.compose.foundation.layout.height\nimport androidx.compose.foundation.layout.width', content)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)

