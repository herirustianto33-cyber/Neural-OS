import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

# Add imports
imports = """
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.togetherWith
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
"""
content = re.sub(r'import androidx.compose.animation.AnimatedVisibility', imports.strip() + '\nimport androidx.compose.animation.AnimatedVisibility', content)

# Add dashboardMode state
state_target = """        var currentScreen by remember { mutableStateOf(Screen.DASHBOARD) }"""
state_replacement = """        var currentScreen by remember { mutableStateOf(Screen.DASHBOARD) }
        var dashboardMode by remember { mutableStateOf("TELEMETRY") }"""
content = content.replace(state_target, state_replacement)

# Replace AndroidView with Toggle + AnimatedContent
target_androidview = """                    AndroidView(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(bottom = 16.dp)
                            .clip(RoundedCornerShape(24.dp)),
                        factory = { ctx ->
                            WebView(ctx).apply {
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                                // Disable hardware acceleration to prevent MESA rendernode graphics errors in emulator
                                setLayerType(android.view.View.LAYER_TYPE_SOFTWARE, null)
                                settings.javaScriptEnabled = true
                                settings.domStorageEnabled = true
                                webViewClient = WebViewClient()
                                loadUrl("file:///android_asset/dashboard.html")
                            }
                        }
                    )"""

replacement_animated = """                    DashboardModeToggle(
                        currentMode = dashboardMode,
                        onModeSelected = { dashboardMode = it }
                    )

                    AnimatedContent(
                        targetState = dashboardMode,
                        transitionSpec = {
                            if (targetState == "PREDICTIVE") {
                                (slideInHorizontally(animationSpec = tween(400)) { width -> width } + fadeIn(animationSpec = tween(400))) togetherWith
                                (slideOutHorizontally(animationSpec = tween(400)) { width -> -width } + fadeOut(animationSpec = tween(400)))
                            } else {
                                (slideInHorizontally(animationSpec = tween(400)) { width -> -width } + fadeIn(animationSpec = tween(400))) togetherWith
                                (slideOutHorizontally(animationSpec = tween(400)) { width -> width } + fadeOut(animationSpec = tween(400)))
                            }.using(SizeTransform(clip = false))
                        },
                        label = "DashboardModeTransition",
                        modifier = Modifier.weight(1f)
                    ) { mode ->
                        if (mode == "TELEMETRY") {
                            Column(modifier = Modifier.fillMaxSize()) {
                                MainCard(modifier = Modifier.weight(1f))
                                GridCards(
                                    modifier = Modifier.padding(bottom = 16.dp),
                                    onChatClicked = { currentScreen = Screen.CHAT }
                                )
                            }
                        } else {
                            AndroidView(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(bottom = 16.dp)
                                    .clip(RoundedCornerShape(24.dp)),
                                factory = { ctx ->
                                    WebView(ctx).apply {
                                        layoutParams = ViewGroup.LayoutParams(
                                            ViewGroup.LayoutParams.MATCH_PARENT,
                                            ViewGroup.LayoutParams.MATCH_PARENT
                                        )
                                        setLayerType(android.view.View.LAYER_TYPE_SOFTWARE, null)
                                        settings.javaScriptEnabled = true
                                        settings.domStorageEnabled = true
                                        webViewClient = WebViewClient()
                                        loadUrl("file:///android_asset/dashboard.html")
                                    }
                                }
                            )
                        }
                    }"""
content = content.replace(target_androidview, replacement_animated)

# Append DashboardModeToggle composable
toggle_composable = """
@Composable
fun DashboardModeToggle(currentMode: String, onModeSelected: (String) -> Unit) {
    Surface(
        color = SurfaceDark,
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceVariantDark),
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            val modes = listOf("TELEMETRY", "PREDICTIVE")
            val labels = listOf("Real-time Telemetry", "Predictive Trend")
            
            modes.forEachIndexed { index, mode ->
                val isSelected = currentMode == mode
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) AccentPurple else Color.Transparent)
                        .clickable { onModeSelected(mode) }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = labels[index],
                        color = if (isSelected) Color(0xFF1D192B) else TextSecondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
"""

content = content + toggle_composable

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
