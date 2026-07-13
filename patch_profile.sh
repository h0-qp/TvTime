sed -i 's/val firstBackdrop.*/\/\/ no backdrop/' app/src/main/java/com/example/ui/screens/profile/ProfileScreen.kt
sed -i '/if (firstBackdrop != null) {/,/        } else {/d' app/src/main/java/com/example/ui/screens/profile/ProfileScreen.kt
sed -i '/                }/d' app/src/main/java/com/example/ui/screens/profile/ProfileScreen.kt
