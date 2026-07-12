import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

content = content.replace(
    'repository = appContainer.mediaRepository,\n                    onNavigateToDetails',
    'repository = appContainer.mediaRepository,\n                    firestoreRepository = appContainer.firestoreRepository,\n                    onNavigateToDetails'
)

content = content.replace(
    'authRepository = appContainer.authRepository,\n                    onSignOut',
    'authRepository = appContainer.authRepository,\n                    firestoreRepository = appContainer.firestoreRepository,\n                    onSignOut'
)

content = content.replace(
    'repository = appContainer.mediaRepository,\n                    mediaId',
    'repository = appContainer.mediaRepository,\n                    firestoreRepository = appContainer.firestoreRepository,\n                    mediaId'
)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)

