import re

with open('app/src/main/java/com/example/ui/ConnectivityScreen.kt', 'r') as f:
    content = f.read()

# Replace variables with robotStatus properties
replace_vars = """
    val latency = robotStatus.latency
    val signalStrength = robotStatus.signalStrength
    val heartbeatBpm = robotStatus.heartbeatBpm
"""
content = re.sub(r'var latency by remember \{ mutableStateOf\(24\) \}\s*var signalStrength by remember \{ mutableStateOf\(92\) \}\s*var heartbeatBpm by remember \{ mutableStateOf\(60\) \}', replace_vars, content)

# Remove the while loop from LaunchedEffect but keep logs logic
# Since it's a multiline block, it's easier to just do a precise replace.
# Let's see the while loop:
content = re.sub(r'while \(true\) \{.*?\n\s*delay.*?latency = .*?signalStrength = .*?heartbeatBpm = .*?\}', '', content, flags=re.DOTALL)

with open('app/src/main/java/com/example/ui/ConnectivityScreen.kt', 'w') as f:
    f.write(content)

