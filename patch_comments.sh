#!/bin/bash
awk '
/HorizontalDivider\(color = DarkGrey\)/ { hd_count++ }
hd_count == 3 && /HorizontalDivider\(color = DarkGrey\)/ {
    print "        Spacer(modifier = Modifier.height(32.dp))"
    print "    }"
    print "}"
    exit
}
{ print }
' app/src/main/java/com/example/ui/screens/details/DetailsScreen.kt > tmp.kt && mv tmp.kt app/src/main/java/com/example/ui/screens/details/DetailsScreen.kt
