import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

imports = """
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
"""
content = re.sub(r'import androidx.compose.animation.SizeTransform', imports.strip() + '\nimport androidx.compose.animation.SizeTransform', content)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
