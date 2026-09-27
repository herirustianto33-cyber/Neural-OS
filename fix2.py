import re
with open('app/src/main/java/com/example/ui/ConnectivityScreen.kt', 'r') as f:
    content = f.read()

# Replace any occurrence of 'import' that is immediately preceded by a word character with '\nimport'
content = re.sub(r'([a-zA-Z0-9_])import ', r'\1\nimport ', content)
# Ensure package is on its own line
content = re.sub(r'package com.example.ui', r'package com.example.ui\n\n', content)

with open('app/src/main/java/com/example/ui/ConnectivityScreen.kt', 'w') as f:
    f.write(content)
