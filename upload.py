import urllib.request
import json
import subprocess

try:
    req = urllib.request.Request('https://api.gofile.io/servers')
    with urllib.request.urlopen(req) as response:
        data = json.loads(response.read().decode())
        server = data['data']['servers'][0]['name']

    print(f"Uploading to {server}...")
    subprocess.run(["curl", "-s", "-F", "file=@app/build/outputs/apk/debug/app-debug.apk", f"https://{server}.gofile.io/contents/uploadfile"])
except Exception as e:
    print(e)
