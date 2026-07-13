#!/bin/bash
RESPONSE=$(curl -s -F "file=@app/build/outputs/apk/release/app-release.apk" https://tmpfiles.org/api/v1/upload)
echo "Response: $RESPONSE"
URL=$(echo $RESPONSE | grep -o '"url":"[^"]*"' | cut -d'"' -f4)
if [ ! -z "$URL" ]; then
    # Add dl/ for direct download
    DIRECT_URL=$(echo $URL | sed 's/tmpfiles.org\//tmpfiles.org\/dl\//')
    echo "Direct Download: $DIRECT_URL"
fi
