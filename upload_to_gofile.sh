#!/bin/bash
SERVER=$(curl -s https://api.gofile.io/servers | grep -o '"name":"[^"]*"' | head -1 | cut -d'"' -f4)
if [ -z "$SERVER" ]; then
  echo "Failed to get Gofile server."
  exit 1
fi
echo "Uploading to $SERVER.gofile.io..."
RESPONSE=$(curl -s -F "file=@app/build/outputs/apk/release/app-release.apk" "https://$SERVER.gofile.io/contents/upload")
DOWNLOAD_LINK=$(echo $RESPONSE | grep -o '"downloadPage":"[^"]*"' | cut -d'"' -f4)
echo "Download link: $DOWNLOAD_LINK"
