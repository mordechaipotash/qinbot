#!/usr/bin/env python3
"""
Qin Wireless Feedback Server
Run on Mac, access from Qin via WiFi
Supports feedback buttons and voice messages
"""

from http.server import HTTPServer, SimpleHTTPRequestHandler
import urllib.parse
import subprocess
import datetime
import json
import os
import threading

LOG_FILE = "/Users/mordechai/qin/feedback.log"
AUDIO_DIR = "/Users/mordechai/qin/audio"

# Ensure audio directory exists
os.makedirs(AUDIO_DIR, exist_ok=True)

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

        # Serve static files
        return SimpleHTTPRequestHandler.do_GET(self)

    def do_POST(self):
        parsed = urllib.parse.urlparse(self.path)

        # Handle audio upload
        if parsed.path == '/audio':
            content_length = int(self.headers.get('Content-Length', 0))

            if content_length == 0:
                self.send_error(400, "No audio data")
                return

            # Read audio data
            audio_data = self.rfile.read(content_length)

            # Save with timestamp
            timestamp = datetime.datetime.now().strftime("%Y%m%d_%H%M%S")
            audio_file = os.path.join(AUDIO_DIR, f"voice_{timestamp}.3gp")

            with open(audio_file, 'wb') as f:
                f.write(audio_data)

            # Log it
            log_timestamp = datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S")
            with open(LOG_FILE, "a") as f:
                f.write(f"{log_timestamp}: VOICE ({len(audio_data)} bytes)\n")

            print(f"🎤 Voice message received! ({len(audio_data)} bytes)")

            # Show notification
            try:
                subprocess.run([
                    'osascript', '-e',
                    f'display notification "Voice message received" with title "Qin Voice 🎤"'
                ])
            except:
                pass

            # Play the audio in background
            def play_audio():
                try:
                    # Convert 3gp to wav and play (ffmpeg needed)
                    wav_file = audio_file.replace('.3gp', '.wav')
                    subprocess.run(['ffmpeg', '-y', '-i', audio_file, wav_file],
                                   capture_output=True, timeout=10)
                    subprocess.run(['afplay', wav_file], timeout=60)
                except Exception as e:
                    # Try playing directly with afplay (may not work for 3gp)
                    try:
                        subprocess.run(['afplay', audio_file], timeout=60)
                    except:
                        print(f"Could not play audio: {e}")

            threading.Thread(target=play_audio, daemon=True).start()

            # Send response
            self.send_response(200)
            self.send_header('Content-type', 'application/json')
            self.send_header('Access-Control-Allow-Origin', '*')
            self.end_headers()
            self.wfile.write(json.dumps({'status': 'ok', 'file': audio_file}).encode())
            return

        self.send_error(404, "Not found")

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
╔══════════════════════════════════════════════════╗
║     🎮 Qin Feedback Server (Voice Enabled) 🎮    ║
╠══════════════════════════════════════════════════╣
║  Server: http://{ip}:{port}
║  Audio saved to: {AUDIO_DIR}
╠══════════════════════════════════════════════════╣
║  Keys 1-5: Feedback buttons                      ║
║  Key 6: Voice recording                          ║
║  Press Ctrl+C to stop                            ║
╚══════════════════════════════════════════════════╝
""")

    server.serve_forever()

if __name__ == '__main__':
    run_server()
