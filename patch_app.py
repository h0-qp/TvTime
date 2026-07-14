import re

with open('app/src/main/java/com/example/TrackVerseApplication.kt', 'r') as f:
    content = f.read()

target = """        val app = FirebaseApp.initializeApp(this)
        if (app == null) {
            val options = com.google.firebase.FirebaseOptions.Builder()
                .setApplicationId("1:1234567890:android:321abc")
                .setApiKey("fake-api-key")
                .setProjectId("fake-project-id")
                .build()
            FirebaseApp.initializeApp(this, options)
        }"""

content = content.replace(target, "        FirebaseApp.initializeApp(this)")

with open('app/src/main/java/com/example/TrackVerseApplication.kt', 'w') as f:
    f.write(content)
