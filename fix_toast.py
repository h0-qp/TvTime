import re

with open('app/src/main/java/com/example/ui/screens/details/DetailsScreen.kt', 'r') as f:
    content = f.read()

old_add = """            onAddComment = { text, uri, isGif ->
                val bytes = uri?.let { u ->
                    context.contentResolver.openInputStream(u)?.readBytes()
                }
                viewModel.addComment(text, bytes, isGif)
            }"""

new_add = """            onAddComment = { text, uri, isGif ->
                val bytes = uri?.let { u ->
                    context.contentResolver.openInputStream(u)?.readBytes()
                }
                viewModel.addComment(text, bytes, isGif)
                android.widget.Toast.makeText(context, "جاري نشر التعليق...", android.widget.Toast.LENGTH_SHORT).show()
            }"""

content = content.replace(old_add, new_add)

with open('app/src/main/java/com/example/ui/screens/details/DetailsScreen.kt', 'w') as f:
    f.write(content)
