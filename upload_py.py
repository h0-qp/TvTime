import urllib.request
import urllib.parse
import json
import subprocess
import re
import sys

try:
    with urllib.request.urlopen('https://api.gofile.io/servers') as response:
        data = json.loads(response.read())
        server = data['data']['servers'][0]['name']
except Exception as e:
    print('Error getting server:', e)
    sys.exit(1)

print(f"Uploading to {server}...")
cmd = f"curl -s -F 'file=@app/build/outputs/apk/release/app-release.apk' https://{server}.gofile.io/contents/upload"
process = subprocess.Popen(cmd, shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
stdout, stderr = process.communicate()

print("Output:")
print(stdout.decode())
print(stderr.decode())
