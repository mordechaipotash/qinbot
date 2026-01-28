#!/usr/bin/env python3
"""
Qin → Clawdbot Bridge Server (with Whisper STT + Menu System)
Receives audio from Qin, transcribes with Whisper, forwards to Clawdbot.

Endpoints:
  GET  /menu   - Get menu config for Qin app
  POST /audio  - Receive audio, transcribe, forward to Clawdbot
  POST /chat   - Receive text, forward to Clawdbot
  GET  /health - Health check
"""

from http.server import HTTPServer, BaseHTTPRequestHandler
import urllib.request
import json
import os
import re
import socket
import subprocess
import tempfile
import shutil


def strip_markdown(text):
    """Strip markdown formatting for plain-text display on Qin."""
    # Code blocks first (before other processing)
    text = re.sub(r'```[\s\S]*?```', '', text)
    text = re.sub(r'`([^`]+)`', r'\1', text)
    
    # Headers → CAPS (handle ## at start of line)
    text = re.sub(r'^#{1,6}\s*(.+?)$', lambda m: m.group(1).strip().upper(), text, flags=re.MULTILINE)
    
    # Bold/italic (non-greedy, handle multiline)
    text = re.sub(r'\*\*([^*]+)\*\*', r'\1', text)
    text = re.sub(r'\*([^*]+)\*', r'\1', text)
    text = re.sub(r'__([^_]+)__', r'\1', text)
    text = re.sub(r'_([^_]+)_', r'\1', text)
    
    # Links [text](url) → text
    text = re.sub(r'\[([^\]]+)\]\([^)]+\)', r'\1', text)
    
    # Tables → convert to simple list format
    # Remove table header separator lines (|---|---|)
    text = re.sub(r'^\s*\|[-:\s|]+\|\s*$', '', text, flags=re.MULTILINE)
    # Convert table rows: | a | b | c | → a - b - c
    text = re.sub(r'^\s*\|(.+)\|\s*$', lambda m: ' - '.join(c.strip() for c in m.group(1).split('|') if c.strip()), text, flags=re.MULTILINE)
    # Clean up any remaining pipes
    text = re.sub(r'\|', ' ', text)
    
    # Horizontal rules
    text = re.sub(r'^[-*_]{3,}\s*$', '', text, flags=re.MULTILINE)
    
    # Bullet points → dashes
    text = re.sub(r'^\s*[-*+]\s+', '- ', text, flags=re.MULTILINE)
    
    # Numbered lists: keep as-is but clean brackets
    text = re.sub(r'^\[(\d+)\]', r'\1.', text, flags=re.MULTILINE)
    
    # Multiple blank lines → single
    text = re.sub(r'\n{3,}', '\n\n', text)
    
    # Multiple spaces → single
    text = re.sub(r'  +', ' ', text)
    
    return text.strip()

# Clawdbot Gateway API (OpenAI-compatible endpoint)
CLAWDBOT_API = os.getenv("CLAWDBOT_API", "http://127.0.0.1:18789/v1/chat/completions")
CLAWDBOT_TOKEN = os.getenv("CLAWDBOT_TOKEN", "9bcbf88fde50f8c79d50e31f2f57e5f5a051d461fef8a6ad")

# Whisper model (tiny for speed, base for accuracy)
WHISPER_MODEL = os.getenv("WHISPER_MODEL", "tiny")

PORT = 8081

# ═══════════════════════════════════════════════════════════════════════════════
# MENU CONFIGURATION - Edit this to add/change quick actions!
# ═══════════════════════════════════════════════════════════════════════════════
MENU_CONFIG = {
    "title": "🤖 QinBot",
    "items": {
        "1": {
            "label": "🎯 Focus",
            "type": "instant",
            "command": "What have I been focused on the last few days? Give me a brief summary of my current projects, priorities, and momentum. Check memory files and recent context."
        },
        "2": {
            "label": "📧 Emails", 
            "type": "instant",
            "command": "Check my emails. Summarize any unread messages briefly - who from, subject, urgency. Use gog skill."
        },
        "3": {
            "label": "📅 Calendar",
            "type": "instant", 
            "command": "What's on my calendar today and tomorrow? Any upcoming events I should know about?"
        },
        "4": {
            "label": "🌤️ Weather",
            "type": "instant",
            "command": "What's the weather like today and tomorrow in my location?"
        },
        "5": {
            "label": "🎤 Chat",
            "type": "voice",
            "prompt": "What do you want to say?"
        },
        "6": {
            "label": "⏰ Remind",
            "type": "voice",
            "prompt": "What should I remind you about?"
        },
        "7": {
            "label": "📝 Note",
            "type": "voice",
            "prompt": "What do you want to note?"
        },
        "8": {
            "label": "🔍 Search",
            "type": "voice",
            "prompt": "What do you want to search?"
        },
        "9": {
            "label": "📰 News",
            "type": "instant",
            "command": "Give me a quick 30-second AI and tech news briefing. What's happening today?"
        }
    }
}
# ═══════════════════════════════════════════════════════════════════════════════


class QinHandler(BaseHTTPRequestHandler):
    def log_message(self, format, *args):
        print(f"[Qin] {args[0]}")

    def do_GET(self):
        if self.path == "/health":
            self.send_json({"status": "ok", "service": "qin-clawdbot-bridge"})
        elif self.path == "/menu":
            self.send_json(MENU_CONFIG)
        else:
            self.send_error(404)

    def do_POST(self):
        if self.path == "/audio":
            self.handle_audio()
        elif self.path == "/chat":
            self.handle_chat()
        elif self.path == "/action":
            self.handle_action()
        else:
            self.send_error(404)

    def handle_action(self):
        """Handle a menu action (instant or with voice input)."""
        try:
            content_length = int(self.headers.get("Content-Length", 0))
            body = self.rfile.read(content_length).decode("utf-8")
            data = json.loads(body)
            
            action_key = data.get("action")
            voice_input = data.get("voice_input", "")
            
            if action_key not in MENU_CONFIG["items"]:
                self.send_json({"error": f"Unknown action: {action_key}"}, status=400)
                return
            
            item = MENU_CONFIG["items"][action_key]
            
            if item["type"] == "instant":
                # Direct command
                command = item["command"]
            else:
                # Voice input with context
                prompt = item.get("prompt")
                if item["label"] == "🎤 Chat":
                    command = voice_input
                elif item["label"] == "⏰ Remind":
                    command = f"Set a reminder: {voice_input}"
                elif item["label"] == "📝 Note":
                    command = f"Add this to today's memory/notes: {voice_input}"
                elif item["label"] == "🔍 Search":
                    command = f"Search the web for: {voice_input}"
                else:
                    command = voice_input
            
            print(f"🎯 Action {action_key}: {command[:50]}...")
            response = self.forward_to_clawdbot(command)
            response = strip_markdown(response)  # Clean for Qin display
            print(f"🤖 Response: {response[:100]}...")
            
            self.send_json({"response": response})
            
        except Exception as e:
            print(f"❌ Action error: {e}")
            self.send_json({"error": str(e)}, status=500)

    def handle_audio(self):
        """Receive audio, transcribe with Whisper, optionally send to Clawdbot."""
        try:
            content_length = int(self.headers.get("Content-Length", 0))
            if content_length == 0:
                self.send_json({"error": "No audio data"}, status=400)
                return

            transcribe_only = self.headers.get("X-Transcribe-Only", "").lower() == "true"
            audio_data = self.rfile.read(content_length)
            print(f"📱 Received {len(audio_data)} bytes of audio")

            with tempfile.NamedTemporaryFile(suffix=".3gp", delete=False) as f:
                f.write(audio_data)
                audio_path = f.name

            txt_path = None
            wav_path = None
            
            try:
                wav_path = audio_path.replace(".3gp", ".wav")
                subprocess.run(
                    ["/opt/homebrew/bin/ffmpeg", "-y", "-i", audio_path, "-ar", "16000", "-ac", "1", wav_path],
                    capture_output=True, timeout=30
                )
                
                if not os.path.exists(wav_path) or os.path.getsize(wav_path) == 0:
                    wav_path = audio_path

                print("🎤 Transcribing...")
                result = subprocess.run(
                    ["/Users/mordechai/.local/bin/whisper", wav_path, "--model", WHISPER_MODEL, "--output_format", "txt", "--output_dir", "/tmp"],
                    capture_output=True, text=True, timeout=60
                )

                txt_path = "/tmp/" + os.path.basename(wav_path).rsplit(".", 1)[0] + ".txt"
                
                transcript = ""
                if os.path.exists(txt_path):
                    with open(txt_path, "r") as f:
                        transcript = f.read().strip()

                if not transcript and result.stdout:
                    transcript = result.stdout.strip()
                if not transcript:
                    transcript = "(Could not transcribe)"

                print(f"📝 Transcript: {transcript}")

                if transcribe_only:
                    self.send_json({"transcript": transcript})
                    return

                response_text = self.forward_to_clawdbot(transcript)
                response_text = strip_markdown(response_text)  # Clean for Qin display
                self.send_json({"transcript": transcript, "response": response_text})

            finally:
                for path in [audio_path, wav_path, txt_path]:
                    try:
                        if path and os.path.exists(path):
                            os.unlink(path)
                    except:
                        pass

        except Exception as e:
            print(f"❌ Audio error: {e}")
            import traceback
            traceback.print_exc()
            self.send_json({"error": str(e)}, status=500)

    def handle_chat(self):
        """Receive text, forward to Clawdbot."""
        try:
            content_length = int(self.headers.get("Content-Length", 0))
            body = self.rfile.read(content_length).decode("utf-8")

            try:
                data = json.loads(body)
                user_text = data.get("text", body)
            except json.JSONDecodeError:
                user_text = body.strip()

            if not user_text:
                self.send_json({"error": "No text provided"}, status=400)
                return

            print(f"📱 Chat: {user_text}")
            response_text = self.forward_to_clawdbot(user_text)
            response_text = strip_markdown(response_text)  # Clean for Qin display
            self.send_json({"response": response_text})

        except Exception as e:
            print(f"❌ Chat error: {e}")
            self.send_json({"error": str(e)}, status=500)

    def forward_to_clawdbot(self, text):
        """Send text to Clawdbot and get response via OpenAI-compatible API."""
        try:
            # Inject instruction for dynamic menus - PREEMPTIVE is the goal
            enhanced_text = text + """

[IMPORTANT - QIN INTERFACE RULES:
User can ONLY respond by: pressing a number (1-9, 0) OR voice recording.
Your job: Be so preemptive that numbers handle 90% of interactions.

End EVERY response with 2-5 numbered options:
[1] Most likely next action
[2] Second most likely  
[3] Alternative
[0] ← Back/Menu

Options should be COMPLETE ACTIONS, not questions. 
Instead of "[1] Yes [2] No" → "[1] Send it [2] Edit first [3] Cancel"
Keep options SHORT (≤25 chars). Be specific, not generic.]"""
            
            # OpenAI-compatible format
            payload = json.dumps({
                "model": "clawdbot:main",
                "messages": [{"role": "user", "content": enhanced_text}],
                "user": "qin"  # For session persistence
            }).encode("utf-8")

            headers = {
                "Content-Type": "application/json",
                "x-clawdbot-agent-id": "main"
            }
            if CLAWDBOT_TOKEN:
                headers["Authorization"] = f"Bearer {CLAWDBOT_TOKEN}"

            req = urllib.request.Request(CLAWDBOT_API, data=payload, headers=headers, method="POST")

            with urllib.request.urlopen(req, timeout=120) as resp:
                result = json.loads(resp.read().decode("utf-8"))
                # OpenAI format: response is in choices[0].message.content
                if "choices" in result and len(result["choices"]) > 0:
                    return result["choices"][0]["message"]["content"]
                return result.get("response", result.get("message", str(result)))

        except urllib.error.URLError:
            return self.fallback_cli(text)
        except Exception as e:
            return f"Error: {e}"

    def fallback_cli(self, text):
        """Fallback: use clawdbot CLI."""
        try:
            result = subprocess.run(
                ["/opt/homebrew/bin/clawdbot", "agent", "--message", text, "--session-id", "qin"],
                capture_output=True, text=True, timeout=120
            )
            return result.stdout.strip() or result.stderr.strip() or "No response"
        except Exception as e:
            return f"CLI error: {e}"

    def send_json(self, data, status=200):
        self.send_response(status)
        self.send_header("Content-Type", "application/json")
        self.send_header("Access-Control-Allow-Origin", "*")
        self.end_headers()
        self.wfile.write(json.dumps(data).encode("utf-8"))


def get_local_ip():
    try:
        s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        s.connect(("8.8.8.8", 80))
        ip = s.getsockname()[0]
        s.close()
        return ip
    except:
        return "127.0.0.1"


def main():
    if not shutil.which("whisper"):
        print("⚠️  whisper not found. Run: pipx install openai-whisper")
    if not shutil.which("ffmpeg"):
        print("⚠️  ffmpeg not found. Run: brew install ffmpeg")

    ip = get_local_ip()
    server = HTTPServer(("0.0.0.0", PORT), QinHandler)

    menu_preview = "\n".join([f"  {k}: {v['label']}" for k, v in MENU_CONFIG["items"].items()])

    print(f"""
╔═══════════════════════════════════════════════════════════════╗
║         🤖 Qin → Clawdbot Bridge (Menu Edition) 🤖            ║
╠═══════════════════════════════════════════════════════════════╣
║  Endpoints:
║    GET  http://{ip}:{PORT}/menu     - Menu config
║    POST http://{ip}:{PORT}/action   - Execute action
║    POST http://{ip}:{PORT}/audio    - Voice → Whisper → Chat
║    POST http://{ip}:{PORT}/chat     - Text → Chat
╠═══════════════════════════════════════════════════════════════╣
║  Menu:
{menu_preview}
╠═══════════════════════════════════════════════════════════════╣
║  Whisper model: {WHISPER_MODEL}
║  Press Ctrl+C to stop
╚═══════════════════════════════════════════════════════════════╝
""")

    server.serve_forever()


if __name__ == "__main__":
    main()
