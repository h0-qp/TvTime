file_path = "app/src/main/java/com/example/data/firebase/FirestoreRepository.kt"

with open(file_path, "r") as f:
    lines = f.readlines()

with open(file_path, "w") as f:
    for line in lines[:-5]:
        f.write(line)
    f.write("}\n")
