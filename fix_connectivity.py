import re

with open('app/src/main/java/com/example/ui/ConnectivityScreen.kt', 'r') as f:
    content = f.read()

# Replace the broken LaunchedEffect
bad_effect = r'LaunchedEffect\(Unit\) \{.*?\n\s*ms\]"\)\)\s*\}\s*if \(logs.size > 20\) logs.removeLast\(\)\s*\}\s*\}'
good_effect = """LaunchedEffect(Unit) {
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
    }"""
content = re.sub(bad_effect, good_effect, content, flags=re.DOTALL)

with open('app/src/main/java/com/example/ui/ConnectivityScreen.kt', 'w') as f:
    f.write(content)
