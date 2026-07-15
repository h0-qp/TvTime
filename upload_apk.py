import requests
import json

url = "https://api.gofile.io/contents/upload"
file_path = "app/build/outputs/apk/release/app-release.apk"

try:
    with open(file_path, "rb") as f:
        files = {"file": f}
        # In Gofile API, standard anonymous uploads don't need token, but let's see
        response = requests.post(url, files=files)
        print("Status Code:", response.status_code)
        try:
            print("Response JSON:", response.json())
        except Exception:
            print("Response Text:", response.text)
except Exception as e:
    print("Error occurred:", str(e))
