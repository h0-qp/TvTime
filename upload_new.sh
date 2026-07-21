#!/bin/bash
while ! pgrep -f "gradle" > /dev/null; do sleep 1; done
while pgrep -f "gradle" > /dev/null; do sleep 2; done
curl -s -F "file=@app/build/outputs/apk/release/app-release.apk" https://store4.gofile.io/contents/uploadfile > final_new_upload.txt
