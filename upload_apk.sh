#!/bin/bash
while true; do
  if [ -f app/build/outputs/apk/release/app-release.apk ]; then
    echo "Uploading to tmpfiles..."
    curl -F "file=@app/build/outputs/apk/release/app-release.apk" https://tmpfiles.org/api/v1/upload
    break
  fi
  sleep 5
done
