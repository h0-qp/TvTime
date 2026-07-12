sed -i '/firestoreRepository = appContainer.firestoreRepository,/d' app/src/main/java/com/example/MainActivity.kt
sed -i 's/repository = appContainer.mediaRepository,/repository = appContainer.mediaRepository,\n                    firestoreRepository = appContainer.firestoreRepository,/g' app/src/main/java/com/example/MainActivity.kt
sed -i 's/authRepository = appContainer.authRepository,/authRepository = appContainer.authRepository,\n                    firestoreRepository = appContainer.firestoreRepository,/g' app/src/main/java/com/example/MainActivity.kt
