import re

# 1. Update TmdbApi.kt language to en-US and add air_date to Episode
with open('app/src/main/java/com/example/data/remote/TmdbApi.kt', 'r') as f:
    content = f.read()

content = content.replace('"ar-AE"', '"en-US"')

if 'val air_date: String?' not in content:
    content = content.replace('val still_path: String?', 'val air_date: String? = null,\n    val still_path: String?')

with open('app/src/main/java/com/example/data/remote/TmdbApi.kt', 'w') as f:
    f.write(content)


# 2. Update DetailsScreen.kt rating out of 10
with open('app/src/main/java/com/example/ui/screens/details/DetailsScreen.kt', 'r') as f:
    content = f.read()

content = content.replace('"$rating/5"', '"$rating/10"')

with open('app/src/main/java/com/example/ui/screens/details/DetailsScreen.kt', 'w') as f:
    f.write(content)

