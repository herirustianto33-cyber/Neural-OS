import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

# Replace snackbarHost
custom_snackbar = """snackbarHost = { 
                  SnackbarHost(snackbarHostState) { data ->
                      Snackbar(
                          snackbarData = data,
                          containerColor = Color(0xFFB3261E), // M3 Error Red
                          contentColor = Color.White,
                          actionColor = Color(0xFFFFB4AB) // Light red/pink for action
                      )
                  }
              },"""
content = re.sub(r'snackbarHost = \{ SnackbarHost\(snackbarHostState\) \},', custom_snackbar, content)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
