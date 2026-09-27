#!/bin/bash
tail -n +5 app/src/main/java/com/example/ui/ConnectivityScreen.kt > temp.kt
sed -i 's/^package com.example.ui/package com.example.ui\n\nimport androidx.lifecycle.compose.collectAsStateWithLifecycle\nimport androidx.lifecycle.viewmodel.compose.viewModel\nimport com.example.ui.RobotViewModel\nimport androidx.compose.material.icons.filled.BatteryFull\n/' temp.kt
mv temp.kt app/src/main/java/com/example/ui/ConnectivityScreen.kt
