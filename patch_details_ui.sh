#!/bin/bash
awk '
/Scaffold\(/ {
    in_scaffold = 1
}
in_scaffold == 1 && /bottomBar = \{/ {
    in_bottom_bar = 1
}
in_bottom_bar == 1 && /containerColor = TrueBlack/ {
    in_bottom_bar = 0
}
' app/src/main/java/com/example/ui/screens/details/DetailsScreen.kt
