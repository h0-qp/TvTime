#!/bin/bash
SERVER=$(curl -s https://api.gofile.io/servers | grep -oP '"name":"\K[^"]+' | head -n 1)
echo "Uploading to $SERVER..."
curl -s -F "file=@$1" "https://$SERVER.gofile.io/contents/uploadfile" | grep -oP '"downloadPage":"\K[^"]+'
