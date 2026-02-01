#!/usr/bin/env python3
"""
Qin Wireless Feedback Server
Run on Mac, access from Qin via WiFi
"""

from http.server import HTTPServer, SimpleHTTPRequestHandler
import urllib.parse
import subprocess
import datetime
import json
import os

LOG_FILE = "/Users/mordechai/qin/feedback.log"

class FeedbackHandler(SimpleHTTPRequestHandler):
    def do_GET(self):
        parsed = urllib.parse.urlparse(self.path)

        # Handle feedback endpoint
        if parsed.path == '/feedback':
            query = urllib.parse.parse_qs(parsed.query)
            action = query.get('action', ['unknown'])[0]

            # Log the feedback
            timestamp = datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S")
            log_entry = f"{timestamp}: {action.upper()}"

            with open(LOG_FILE, "a") as f:
                f.write(log_entry + "\n")

            # Show Mac notification
            emoji_map = {
                'thumbs_up': '👍',
                'thumbs_down': '👎',
                'star': '⭐',
                'note': '💬',
                'play': '▶️'
            }
            emoji = emoji_map.get(action, '📱')

            try:
                subprocess.run([
                    'osascript', '-e',
                    f'display notification "{action}" with title "Qin Feedback {emoji}"'
                ])
            except:
                pass

            print(f"{emoji} {action.upper()} received!")

            # Send response
            self.send_response(200)
            self.send_header('Content-type', 'application/json')
            self.send_header('Access-Control-Allow-Origin', '*')
            self.end_headers()
            self.wfile.write(json.dumps({'status': 'ok', 'action': action}).encode())
            return

        # Serve static files (like the HTML panel)
        return SimpleHTTPRequestHandler.do_GET(self)

def run_server(port=8080):
    os.chdir("/Users/mordechai/qin")
    server = HTTPServer(('0.0.0.0', port), FeedbackHandler)

    # Get local IP
    import socket
    s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    try:
        s.connect(('8.8.8.8', 80))
        ip = s.getsockname()[0]
    except:
        ip = '127.0.0.1'
    finally:
        s.close()

    print(f"""
╔══════════════════════════════════════════════╗
║     🎮 Qin Wireless Feedback Server 🎮       ║
╠══════════════════════════════════════════════╣
║  Open on your Qin:                           ║
║  http://{ip}:{port}/feedback_panel.html      ║
╠══════════════════════════════════════════════╣
║  Or scan QR code in browser                  ║
║  Press Ctrl+C to stop                        ║
╚══════════════════════════════════════════════╝
""")

    server.serve_forever()

if __name__ == '__main__':
    run_server()
