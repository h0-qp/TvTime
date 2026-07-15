file_path = "app/src/main/java/com/example/ui/screens/profile/ProfileScreen.kt"
with open(file_path, "r") as f:
    content = f.read()

content = content.replace("""@Composable
fun ProfileScreen(
    authRepository: AuthRepository,
    firestoreRepository: FirestoreRepository,
    onSignOut: () -> Unit,
    onNavigateToDetails: (String, Int) -> Unit,
    modifier: Modifier = Modifier
) {""", """@Composable
fun ProfileScreen(
    authRepository: AuthRepository,
    firestoreRepository: FirestoreRepository,
    onSignOut: () -> Unit,
    onNavigateToDetails: (String, Int) -> Unit,
    onNavigateToAllTvShows: () -> Unit,
    onNavigateToAllMovies: () -> Unit,
    modifier: Modifier = Modifier
) {""")

content = content.replace("""fun SectionHeader(title: String, isFavorite: Boolean = false) {""", """fun SectionHeader(title: String, isFavorite: Boolean = false, onClick: (() -> Unit)? = null) {""")

content = content.replace("""        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 32.dp, bottom = 16.dp),""", """        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick?.invoke() }
            .padding(start = 16.dp, end = 16.dp, top = 32.dp, bottom = 16.dp),""")

content = content.replace("""            SectionHeader(title = "مسلسلات")""", """            SectionHeader(title = "مسلسلات", onClick = onNavigateToAllTvShows)""")
content = content.replace("""            SectionHeader(title = "أفلام")""", """            SectionHeader(title = "أفلام", onClick = onNavigateToAllMovies)""")

with open(file_path, "w") as f:
    f.write(content)

