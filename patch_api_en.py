with open('app/src/main/java/com/example/data/remote/TmdbApi.kt', 'r') as f:
    content = f.read()

content = content.replace('"ar-AE"', '"en-US"')

with open('app/src/main/java/com/example/data/remote/TmdbApi.kt', 'w') as f:
    f.write(content)
