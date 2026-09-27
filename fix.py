with open('app/src/main/java/com/example/ui/ConnectivityScreen.kt', 'r') as f:
    content = f.read()

content = content.replace("package com.example.uiimport", "package com.example.ui\nimport")
content = content.replace("collectAsStateWithLifecycleimport", "collectAsStateWithLifecycle\nimport")
content = content.replace("viewModelimport", "viewModel\nimport")
content = content.replace("RobotViewModelimport", "RobotViewModel\nimport")
content = content.replace("BatteryFullimport", "BatteryFull\nimport")
content = content.replace("LinearEasingimport", "LinearEasing\nimport")
content = content.replace("RepeatModeimport", "RepeatMode\nimport")

with open('app/src/main/java/com/example/ui/ConnectivityScreen.kt', 'w') as f:
    f.write(content)
