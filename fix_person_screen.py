import sys
content = open("app/src/main/java/com/example/ui/screens/person/PersonScreen.kt").read()

content = content.replace("modifier = Modifier.fillMaxSize(),\n                    contentPadding = PaddingValues(bottom = 32.dp)", "modifier = Modifier.fillMaxSize(),\n                    contentPadding = PaddingValues(top = 0.dp, bottom = 32.dp)")

content = content.replace("Scaffold(\n        topBar = {", "Scaffold(\n        contentWindowInsets = WindowInsets(0.dp),\n        topBar = {")
open("app/src/main/java/com/example/ui/screens/person/PersonScreen.kt", "w").write(content)
