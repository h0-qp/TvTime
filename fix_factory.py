file_path = "app/src/main/java/com/example/MainActivity.kt"
with open(file_path, "r") as f:
    content = f.read()

import1 = """import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModel
"""
if "import androidx.lifecycle.ViewModelProvider" not in content:
    content = content.replace("import android.os.Bundle", "import android.os.Bundle\n" + import1)

old_tv = """val viewModel = androidx.lifecycle.viewmodel.compose.viewModel<AllTvShowsViewModel> {
                    AllTvShowsViewModel(appContainer.firestoreRepository, appContainer.mediaRepository)
                }"""

new_tv = """val viewModel: AllTvShowsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                    factory = object : ViewModelProvider.Factory {
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return AllTvShowsViewModel(appContainer.firestoreRepository, appContainer.mediaRepository) as T
                        }
                    }
                )"""

old_movie = """val viewModel = androidx.lifecycle.viewmodel.compose.viewModel<AllMoviesViewModel> {
                    AllMoviesViewModel(appContainer.firestoreRepository)
                }"""

new_movie = """val viewModel: AllMoviesViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                    factory = object : ViewModelProvider.Factory {
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return AllMoviesViewModel(appContainer.firestoreRepository) as T
                        }
                    }
                )"""

content = content.replace(old_tv, new_tv)
content = content.replace(old_movie, new_movie)

with open(file_path, "w") as f:
    f.write(content)
