import requests
import json
import sys

def upload_file(filepath):
    print("Getting server...")
    try:
        r = requests.get("https://api.gofile.io/servers")
        data = r.json()
        server = data['data']['servers'][0]['name']
        print(f"Using server: {server}")
        
        url = f"https://{server}.gofile.io/contents/upload"
        print(f"Uploading to {url}...")
        
        with open(filepath, 'rb') as f:
            files = {'file': f}
            response = requests.post(url, files=files)
            print("Response:")
            print(response.text)
            
            resp_data = response.json()
            if resp_data['status'] == 'ok':
                print(f"\nDownload link: {resp_data['data']['downloadPage']}")
            else:
                print(f"Upload failed: {resp_data}")
    except Exception as e:
        print(f"Error: {e}")

if __name__ == '__main__':
    upload_file("/app/applet/app/build/outputs/apk/release/app-release.apk")
