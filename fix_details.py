import sys

with open("app/src/main/java/com/example/ui/screens/details/DetailsScreen.kt", "r") as f:
    lines = f.readlines()

new_lines = []
for line in lines:
    if line.startswith("import androidx.compose.material.icons.filled.Add"):
        new_lines.append("import androidx.compose.material.icons.filled.KeyboardArrowDown\n")
        new_lines.append("import androidx.compose.material.icons.filled.MoreHoriz\n")
        new_lines.append("import androidx.compose.material.icons.filled.DateRange\n")
        new_lines.append("import androidx.compose.material.icons.outlined.Visibility\n")
        new_lines.append(line)
    elif "Spacer(modifier = Modifier.height(8.dp))                            Spacer(modifier = Modifier.height(24.dp))" in line:
        new_lines.append(line.replace("Spacer(modifier = Modifier.height(8.dp))                            Spacer(modifier = Modifier.height(24.dp))", "Spacer(modifier = Modifier.height(8.dp))\n                            Spacer(modifier = Modifier.height(24.dp))"))
    elif "androidx.compose.material.icons.Icons.Default.MoreHoriz" in line:
        new_lines.append(line.replace("androidx.compose.material.icons.Icons.Default.MoreHoriz", "Icons.Default.MoreHoriz"))
    elif "androidx.compose.material.icons.Icons.Default.KeyboardArrowDown" in line:
        new_lines.append(line.replace("androidx.compose.material.icons.Icons.Default.KeyboardArrowDown", "Icons.Default.KeyboardArrowDown"))
    elif "androidx.compose.material.icons.Icons.Default.DateRange" in line:
        new_lines.append(line.replace("androidx.compose.material.icons.Icons.Default.DateRange", "Icons.Default.DateRange"))
    elif "androidx.compose.material.icons.Icons.Outlined.Visibility" in line:
        new_lines.append(line.replace("androidx.compose.material.icons.Icons.Outlined.Visibility", "Icons.Outlined.Visibility"))
    elif "androidx.compose.material.icons.Icons.Default.Check" in line:
        new_lines.append(line.replace("androidx.compose.material.icons.Icons.Default.Check", "Icons.Default.Check"))
    else:
        new_lines.append(line)

with open("app/src/main/java/com/example/ui/screens/details/DetailsScreen.kt", "w") as f:
    f.writelines(new_lines)
