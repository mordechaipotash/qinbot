#!/usr/bin/env python3
"""
Qin Online Feedback Listener
Listens to ntfy.sh for feedback when not on same WiFi
"""

import subprocess
import urllib.request
import json
import datetime

CHANNEL = "qin-feedback-mordechai"
LOG_FILE = "/Users/mordechai/qin/feedback.log"

EMOJI_MAP = {
    'thumbs_up': '👍',
    'thumbs_down': '👎',
    'star': '⭐',
    'note': '💬',
    'play': '▶️'
}

def notify_mac(action, emoji):
    """Show Mac notification"""
    try:
        subprocess.run([
            'osascript', '-e',
            f'display notification "{action}" with title "Qin Online {emoji}"'
        ])
    except:
        pass

def log_feedback(action):
    """Log to file"""
    timestamp = datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    with open(LOG_FILE, "a") as f:
        f.write(f"{timestamp}: {action.upper()} (online)\n")

def listen():
    print(f"""
╔══════════════════════════════════════════════════╗
║    🌐 Qin Online Feedback Listener 🌐            ║
╠══════════════════════════════════════════════════╣
║  Listening to: ntfy.sh/{CHANNEL}                 ║
║  Press Ctrl+C to stop                            ║
╚══════════════════════════════════════════════════╝
""")

    url = f"https://ntfy.sh/{CHANNEL}/json"

    while True:
        try:
            req = urllib.request.Request(url)
            with urllib.request.urlopen(req, timeout=90) as response:
                for line in response:
                    try:
                        data = json.loads(line.decode('utf-8'))
                        if data.get('event') == 'message':
                            action = data.get('message', 'unknown')
                            emoji = EMOJI_MAP.get(action, '📱')

                            print(f"{emoji} {action.upper()} received!")
                            notify_mac(action, emoji)
                            log_feedback(action)
                    except json.JSONDecodeError:
                        continue
        except Exception as e:
            print(f"Connection lost, reconnecting... ({e})")
            import time
            time.sleep(2)

if __name__ == '__main__':
    listen()
