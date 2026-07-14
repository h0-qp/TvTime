import re

with open('app/src/main/java/com/example/TrackVerseApplication.kt', 'r') as f:
    content = f.read()

target = "FirebaseApp.initializeApp(this)"
replacement = """        var app = FirebaseApp.initializeApp(this)
        if (app == null) {
            val options = com.google.firebase.FirebaseOptions.Builder()
                .setApplicationId("1:987873552497:android:5003fcf43a28487b2353d0")
                .setApiKey("AIzaSyC-yfevZTtLf8lOp9D1Orsai-G4QugPNRQ")
                .setProjectId("tvtime-9265c")
                .build()
            FirebaseApp.initializeApp(this, options)
        }"""

content = content.replace(target, replacement, 1)

with open('app/src/main/java/com/example/TrackVerseApplication.kt', 'w') as f:
    f.write(content)
