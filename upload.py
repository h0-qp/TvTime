import requests
import sys

def upload_to_gofile(file_path):
    server_response = requests.get('https://api.gofile.io/servers')
    server = server_response.json()['data']['servers'][0]['name']
    
    with open(file_path, 'rb') as f:
        upload_response = requests.post(
            f'https://{server}.gofile.io/contents/uploadfile',
            files={'file': f}
        )
    return upload_response.json()['data']['downloadPage']

print(upload_to_gofile('app/build/outputs/apk/debug/app-debug.apk'))
