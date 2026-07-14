import requests
import json

file_path = "app/build/outputs/apk/release/app-release.apk"

with open(file_path, "rb") as f:
    response = requests.post('https://file.io', files={'file': f})
    data = response.json()
    print("Download URL:", data.get("link", "Error uploading to file.io"))
