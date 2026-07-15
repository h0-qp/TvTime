import requests

url = "https://store1.gofile.io/contents/uploadfile"
file_path = "app/build/outputs/apk/release/app-release.apk"

try:
    with open(file_path, "rb") as f:
        response = requests.post(url, files={"file": f}, timeout=60)
        print(response.text)
except Exception as e:
    print(e)
