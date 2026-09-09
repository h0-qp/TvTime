import re

# Fix FirestoreRepository.kt
with open('app/src/main/java/com/example/data/firebase/FirestoreRepository.kt', 'r') as f:
    content = f.read()

if "import com.example.data.firebase.Comment" not in content:
    content = content.replace("package com.example.data.firebase\n", "package com.example.data.firebase\n\nimport com.example.data.firebase.Comment\n")
with open('app/src/main/java/com/example/data/firebase/FirestoreRepository.kt', 'w') as f:
    f.write(content)


# Fix DetailsViewModel.kt
with open('app/src/main/java/com/example/ui/screens/details/DetailsViewModel.kt', 'r') as f:
    content = f.read()

# Clean up bad package declarations at the top
# Let's just find the first "import" or real code, and set standard imports.
content = re.sub(r'package com\.example\.ui\.screens\.details.*?import ', 'package com.example.ui.screens.details\n\nimport com.example.data.firebase.Comment\nimport ', content, flags=re.DOTALL, count=1)
# wait, there's `import com.example.data.firebase.Commentpackage com.example.ui.screens.detailsimport androidx.lifecycle.ViewModel`
content = re.sub(r'^.*?(?=import androidx\.lifecycle\.ViewModel)', 'package com.example.ui.screens.details\n\nimport com.example.data.firebase.Comment\n', content, flags=re.DOTALL)

with open('app/src/main/java/com/example/ui/screens/details/DetailsViewModel.kt', 'w') as f:
    f.write(content)

# Fix DetailsScreen.kt
with open('app/src/main/java/com/example/ui/screens/details/DetailsScreen.kt', 'r') as f:
    content = f.read()

content = re.sub(r'^.*?(?=import kotlinx\.coroutines\.launch)', 'package com.example.ui.screens.details\n\nimport androidx.compose.ui.platform.LocalContext\n', content, flags=re.DOTALL)

# Fix 'local val context' conflicting declaration in DetailsScreen.kt
# We might have injected 'val context = LocalContext.current' twice
content = content.replace('val context = LocalContext.current', '', 1) # remove the first one if there are duplicates

with open('app/src/main/java/com/example/ui/screens/details/DetailsScreen.kt', 'w') as f:
    f.write(content)

