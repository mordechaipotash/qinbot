#!/bin/bash
# Start cloudflared quick tunnel and log the URL

LOG_FILE="/tmp/qin-tunnel.log"
URL_FILE="/tmp/qin-tunnel-url.txt"

echo "Starting cloudflared tunnel at $(date)" >> "$LOG_FILE"

# Run cloudflared and capture the URL
/opt/homebrew/bin/cloudflared tunnel --url http://localhost:8081 2>&1 | while read line; do
    echo "$line" >> "$LOG_FILE"
    # Extract and save the trycloudflare URL
    if echo "$line" | grep -q "trycloudflare.com"; then
        url=$(echo "$line" | grep -oE 'https://[a-z0-9-]+\.trycloudflare\.com')
        if [ -n "$url" ]; then
            echo "$url" > "$URL_FILE"
            echo "Tunnel URL: $url" >> "$LOG_FILE"
        fi
    fi
done
