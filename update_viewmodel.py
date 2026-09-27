import re

with open('app/src/main/java/com/example/ui/RobotViewModel.kt', 'r') as f:
    content = f.read()

imports = """
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import com.example.data.VoiceCommandLog
"""
content = re.sub(r'import com.example.data.RobotStatus', 'import com.example.data.RobotStatus' + imports, content)

logs_code = """
    val voiceLogs: StateFlow<List<VoiceCommandLog>> = repository.voiceLogs.stateIn(
        viewModelScope, SharingStarted.Lazily, emptyList()
    )

    fun logVoiceCommand(command: String, action: String, status: String) {
        viewModelScope.launch {
            repository.insertVoiceLog(VoiceCommandLog(commandText = command, actionParsed = action, status = status))
        }
    }
"""

content = re.sub(r'    private var lastReconnectTime = 0L', logs_code + '\n    private var lastReconnectTime = 0L', content)

with open('app/src/main/java/com/example/ui/RobotViewModel.kt', 'w') as f:
    f.write(content)

