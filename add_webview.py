import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

imports = """import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.ui.viewinterop.AndroidView
import android.view.ViewGroup
"""
content = re.sub(r'import android.os.Bundle', imports + 'import android.os.Bundle', content)

webview_ui = """                    AndroidView(
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
                                settings.javaScriptEnabled = true
                                settings.domStorageEnabled = true
                                webViewClient = WebViewClient()
                                loadUrl("file:///android_asset/dashboard.html")
                            }
                        }
                    )"""

# Target replacement
target = """                    MainCard(modifier = Modifier.weight(1f))
                    GridCards(
                        modifier = Modifier.padding(bottom = 16.dp),
                        onChatClicked = { currentScreen = Screen.CHAT }
                    )"""

content = content.replace(target, webview_ui)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
