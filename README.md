# QinBot: AI Assistant for Non-Smartphone Users

> **The most capable "kosher phone" on the planet** — Full AI assistant on a device with no browser, no apps, no images.

![License](https://img.shields.io/badge/license-MIT-blue.svg)
![Platform](https://img.shields.io/badge/platform-Android%20(Qin%20F21)-green.svg)
![AI](https://img.shields.io/badge/AI-Claude%20%2F%20Clawdbot-purple.svg)

---

> [!TIP]
> ### 🤖 Not technical? No problem!
> 
> Copy this page's URL and paste it into ChatGPT, Claude, or your favorite AI assistant. Ask it to explain QinBot at your level — whether you're curious about the concept, wondering if it's right for you, or want help setting it up.
>
> **Ready-to-use prompt (just copy & paste):**
> ```
> I'm reading about QinBot (https://github.com/mordechaipotash/qinbot). I'm not very technical.
> Please explain:
> 1. What this project does in simple terms
> 2. Who it's for and why they'd want it
> 3. What I'd need to set it up (in plain English)
> 4. Whether this might work for my situation: [describe your needs]
> 
> Keep explanations simple. Ask me clarifying questions if needed.
> ```

---

## 🎯 The Problem

Smartphones are designed to be addictive. For people who want to disconnect — whether for religious reasons, mental health, productivity, or digital minimalism — the choice has always been:

- **Full smartphone** → All the capability, all the addiction
- **Dumb phone** → No addiction, no capability

**QinBot solves this.** You get 90% of the utility with 10% of the risk.

## 💡 The Solution

QinBot turns a $50 non-touchscreen Android phone (Qin F21 Pro) into a powerful AI assistant:

- ✅ Check emails
- ✅ Get weather forecasts
- ✅ Manage calendar
- ✅ Take voice notes
- ✅ Search the web (text results only)
- ✅ Get news briefings
- ✅ Draft and send emails
- ✅ Set reminders
- ✅ Query your personal knowledge base (Brain MCP)
- ✅ Free-form AI chat

**Without:**
- ❌ Browser
- ❌ Images from internet
- ❌ Video
- ❌ Social media
- ❌ Infinite scroll
- ❌ App Store

## 📱 Screenshots

<table>
<tr>
<td><img src="demo-screenshots/01-qinbot-main-menu.png" width="200"/><br/><b>Main Menu</b></td>
<td><img src="demo-screenshots/03-weather-response.png" width="200"/><br/><b>Weather</b></td>
<td><img src="demo-screenshots/04-email-inbox-summary.png" width="200"/><br/><b>Email Summary</b></td>
</tr>
<tr>
<td><img src="demo-screenshots/06-voice-transcription-confirm.png" width="200"/><br/><b>Voice Input</b></td>
<td><img src="demo-screenshots/07-voice-ai-response.png" width="200"/><br/><b>AI Response</b></td>
<td><img src="demo-screenshots/09-news-briefing.png" width="200"/><br/><b>News Briefing</b></td>
</tr>
<tr>
<td><img src="demo-screenshots/14-brain-mcp-query.png" width="200"/><br/><b>Brain MCP Query</b></td>
<td><img src="demo-screenshots/11-email-draft-compose.png" width="200"/><br/><b>Email Draft</b></td>
<td><img src="demo-screenshots/08-dynamic-options-menu.png" width="200"/><br/><b>Dynamic Options</b></td>
</tr>
</table>

---

## 🎤 Why Voice? The Qin Input Problem

### The Challenge

The Qin F21 Pro has a **T9 physical keypad** — the same input method from 2003 Nokia phones. Typing "Hello, how are you?" requires pressing:

```
4-4 3-3 5-5-5 5-5-5 6-6-6 [space] 4-4 6-6-6 9-9-9 [space] 2 7-7-7 3-3 [space] 9-9-9 6-6-6 8-8 [?]
```

That's **~50 key presses** for a simple sentence. For anything beyond "ok" or "yes", T9 is impractical.

### The Solution: Voice-First Design

QinBot is designed **voice-first**. Instead of typing, you:

1. **Press a number** to select a menu item
2. **Speak** your message naturally
3. **Confirm** with a single key press

The AI handles the rest — understanding context, executing actions, and responding with numbered options you can select with one press.

### The Voice Recording Flow

```mermaid
stateDiagram-v2
    [*] --> Menu: Launch App
    Menu --> VoicePrompt: Press 5 Chat
    VoicePrompt --> Recording: Press 1 Start
    Recording --> Confirm: Press 1 Stop
    Confirm --> Sending: Press 2 Send
    Confirm --> Recording: Press 3 Redo
    Confirm --> Menu: Press 0 Cancel
    Sending --> Response: AI Responds
    Response --> Menu: Press 0
    Response --> VoicePrompt: Press 5
    Response --> Sending: Press 1 to 9 Followup
```

**Step-by-step:**

| Step | Screen Shows | User Action | What Happens |
|------|-------------|-------------|--------------|
| 1 | `"What do you want to say?"` | Press **1** | Recording starts 🔴 |
| 2 | `"Recording... Press 1 to stop"` | Speak naturally | Audio captured |
| 3 | `"Recording... Press 1 to stop"` | Press **1** | Recording stops, sent to Whisper |
| 4 | `"You: [transcript]"` | Review transcript | Confirm it's correct |
| 5 | `"2=Send 3=Redo 0=Cancel"` | Press **2** | Sent to AI |
| 6 | AI response with options | Press **1-9** | Follow-up action |

### Why This Flow?

- **Press 1 to start, Press 1 to stop**: Same key = muscle memory. No hunting for different buttons.
- **Confirmation step**: Whisper isn't perfect. Let the user verify before sending.
- **Redo option**: Bad recording? Just press 3 and try again. No penalty.
- **Numbered follow-ups**: AI responses include `[1] [2] [3]` options. Keep the conversation going without typing.

---

## 🧠 Brain MCP: Your Intellectual DNA

### What is Brain MCP?

Brain MCP is a **queryable archive of your entire intellectual history** — every conversation, every search, every project, every decision. It's not just storage; it's a system that understands *how you think*.

**The Real Numbers:**

| Source | Count |
|--------|-------|
| **Conversations** | 377,326 messages (132,815 from user) |
| **Embeddings** | 82,000+ semantic vectors (768-dimensional, LanceDB) |
| **GitHub** | 132 repositories, 1,427 commits |
| **YouTube** | 31,832 videos tracked (16,386 watched) |
| **Google Searches** | 52,791 queries |
| **Google Visits** | 58,650 page visits |
| **Markdown Docs** | 5,524 documents |

### The 8 Universal Principles (SEED)

Brain MCP has extracted **8 foundational mental models** from analyzing hundreds of thousands of messages:

| Principle | Description | Mentions |
|-----------|-------------|----------|
| **INVERSION** | Reverse the problem to find the solution | 1,415 |
| **TRANSLATION LAYER** | Interface between infinite AI output and finite human comprehension | 610 |
| **COMPRESSION** | Reduce complexity while preserving decision quality | 594 |
| **AGENCY PRESERVATION** | 100% human decision control, 100% machine execution | 395 |
| **TEMPORAL ECONOMICS** | Human time as the ultimate scarce resource | 71 |
| **BOTTLENECK AMPLIFICATION** | Find constraints and amplify them as leverage | 57 |
| **COGNITIVE ARCHITECTURE** | Design systems that amplify YOUR specific mind | 18 |
| **SEEDS & SOVEREIGNTY** | Autonomous, bounded systems with clear interfaces | 17 |

### 25 Consolidated Tools

Brain MCP exposes **25 tools** (17 generic + 8 prosthetic) for querying your intellectual DNA. The original 92 tools were consolidated into a focused, high-signal set:

**🧠 Cognitive Prosthetic Tools (8)** — designed for ADHD/monotropic minds:
- `tunnel_state` — "load game" for any domain (where was I?)
- `context_recovery` — full re-entry brief when returning to a topic
- `switching_cost` — quantified cost of changing focus
- `open_threads` — all unfinished business across all domains
- `dormant_contexts` — abandoned tunnels alarm
- `cognitive_patterns` — when do I think best?
- `tunnel_history` / `trust_dashboard` — engagement tracking & system health

**🔍 Generic Tools (17)** — search, synthesis, GitHub, analytics:
- **Search:** `semantic_search`, `search_conversations`, `unified_search`, `search_summaries`, `search_docs`, `unfinished_threads`
- **Synthesis:** `what_do_i_think`, `alignment_check`, `thinking_trajectory`, `what_was_i_thinking`
- **Conversation:** `get_conversation`, `conversations_by_date`, `brain_stats`
- **GitHub:** `github_search` (consolidated: timeline/conversations/code/validate)
- **Analytics:** `query_analytics` (consolidated: timeline/stacks/problems/spend)
- **Meta:** `list_principles`, `get_principle`

> 📦 See [brain-mcp](https://github.com/mordechaipotash/brain-mcp) for full documentation and architecture.

### Example Queries from Qin

From your dumb phone, you can ask:

| Query | What Brain MCP Does |
|-------|---------------------|
| *"What do I think about AI agents?"* | Synthesizes your views from 1,415 mentions of agency/agents |
| *"When did I first work on MCP?"* | Finds earliest mention across all conversations |
| *"Am I being consistent with my principles?"* | Runs alignment_check against SEED principles |
| *"What was I obsessed with in October?"* | Pulls monthly themes and focus areas |
| *"Find similar situations to this decision"* | Semantic search for precedents |

### Architecture

```mermaid
graph LR
    subgraph QinPhone["Qin Phone"]
        Q["Voice: What was I thinking in April?"]
    end
    
    subgraph MacServer["Mac Server"]
        B[Bridge Server]
        G[Clawdbot Gateway]
        MCP["Brain MCP Server"]
        DB[("SQLite: 377K messages")]
        LANCE[("LanceDB: 82K embeddings")]
    end
    
    Q -->|HTTPS| B
    B -->|API| G
    G -->|MCP Tool Call| MCP
    MCP -->|SQL| DB
    MCP -->|Vector Search| LANCE
    MCP -->|Results| G
    G -->|Natural Language| B
    B -->|Plain Text| Q
```

### Privacy & Sovereignty

- **100% Local**: Brain MCP runs on YOUR Mac. Nothing uploaded to cloud.
- **You Own Your Data**: SQLite + LanceDB files you can backup, export, delete.
- **AI Queries Locally**: Claude sees query results, not raw data.
- **Offline Capable**: Brain queries work even without internet (only AI response needs connection).

This is the **"Intellectual DNA"** concept — your thoughts, patterns, and decisions are searchable and queryable, turning years of scattered conversations into a coherent, accessible knowledge base.

---

## 🏗️ Architecture

```mermaid
graph TB
    subgraph QinPhone["Qin F21 Phone"]
        QIN[QinBot Android App]
        MIC[Microphone]
        KB[Physical Keyboard]
    end
    
    subgraph CloudflareEdge["Cloudflare Edge"]
        CF["Cloudflare Tunnel: qin.yourdomain.com"]
    end
    
    subgraph MacServer["Mac Home Server"]
        BRIDGE["Qin Bridge Server: Python :8081"]
        WHISPER["OpenAI Whisper: Speech to Text"]
        CLAWDBOT["Clawdbot Gateway: :18789"]
        TUNNEL[cloudflared daemon]
        BRAIN["Brain MCP Server: Personal Knowledge"]
    end
    
    subgraph ExternalAPIs["External APIs"]
        CLAUDE["Claude API: Anthropic"]
        GMAIL[Gmail API]
        GCAL[Google Calendar]
        WEATHER[Weather API]
    end
    
    KB -->|Key Press| QIN
    MIC -->|3GP Audio| QIN
    QIN <-->|HTTPS| CF
    CF <-->|Tunnel| TUNNEL
    TUNNEL <--> BRIDGE
    BRIDGE -->|WAV Audio| WHISPER
    WHISPER -->|Transcript| BRIDGE
    BRIDGE <-->|OpenAI API| CLAWDBOT
    CLAWDBOT <-->|Messages API| CLAUDE
    CLAWDBOT <-->|MCP Protocol| BRAIN
    CLAWDBOT --> GMAIL
    CLAWDBOT --> GCAL
    CLAWDBOT --> WEATHER
```

## 🔄 Complete Request Flow

```mermaid
sequenceDiagram
    participant U as User
    participant Q as Qin App
    participant C as Cloudflare
    participant B as Bridge Server
    participant W as Whisper
    participant G as Clawdbot Gateway
    participant AI as Claude AI
    participant T as Tools
    
    Note over U,Q: Instant Action Flow
    U->>Q: Press 4 Weather
    Q->>C: POST /action
    C->>B: Tunnel forward
    B->>G: POST /v1/chat/completions
    G->>AI: Whats the weather
    AI->>G: Tool call get_weather
    G->>T: Execute tool
    T->>G: Weather data
    G->>AI: Tool result
    AI->>G: Formatted response
    G->>B: JSON response
    B->>B: strip_markdown
    B->>C: Clean text response
    C->>Q: Response
    Q->>Q: Parse numbered options
    Q->>U: Display menu
    
    Note over U,Q: Voice Flow
    U->>Q: Press 5 Chat
    Q->>U: What do you want to say
    U->>Q: Press 1 Start recording
    Q->>Q: MediaRecorder.start
    U->>Q: Speak Check my emails
    U->>Q: Press 1 Stop recording
    Q->>Q: MediaRecorder.stop
    Q->>C: POST /audio 3GP binary
    C->>B: Audio data
    B->>B: FFmpeg 3GP to WAV
    B->>W: whisper audio.wav
    W->>B: Check my emails
    B->>C: transcript
    C->>Q: Transcript
    Q->>U: You Check my emails
    Q->>U: 2 Send 3 Redo 0 Cancel
    U->>Q: Press 2 Send
    Q->>C: POST /chat
    C->>B: Forward
    B->>G: POST /v1/chat/completions
    G->>AI: User query
    AI->>G: Response with options
    G->>B: Response
    B->>B: strip_markdown
    B->>C: Clean response
    C->>Q: Response
    Q->>U: Email summary with options
```

---

## 📁 Project Structure

```
~/qin/
├── qin_clawdbot_server.py    # Bridge server (Python)
├── start_tunnel.sh           # Cloudflare tunnel launcher
├── build_and_install.sh      # APK build script
├── README.md                 # This file
├── LICENSE                   # MIT License
├── .gitignore
├── demo-screenshots/         # Demo images
│   ├── 01-qinbot-main-menu.png
│   ├── 02-focus-recap-response.png
│   ├── 03-weather-response.png
│   ├── ...
│   └── 14-brain-mcp-query.png
└── QinFeedback/              # Android app source
    └── app/
        ├── build.gradle
        └── src/
            └── main/
                ├── AndroidManifest.xml
                └── java/
                    └── com/qin/feedback/
                        └── MainActivity.java
```

## 🔧 Components

### 1. Android App (QinFeedback)

**Location:** `QinFeedback/app/src/main/java/com/qin/feedback/MainActivity.java`

The Android app is designed for the Qin F21's unique constraints:
- **No touchscreen** — all interaction via physical numpad (0-9, *, #)
- **Small screen** (240x320) — maximized text area, hidden headers during responses
- **Limited resources** — minimal dependencies, no heavy frameworks

**State Machine:**
```java
STATE_MENU          // Main menu - showing 9 options
STATE_VOICE_PROMPT  // "Press 1 to speak"
STATE_RECORDING     // Recording audio (red indicator)
STATE_CONFIRM       // "2=Send 3=Redo 0=Cancel"
STATE_SENDING       // Waiting for response (yellow spinner)
STATE_RESPONSE      // Viewing AI response with [1][2][3] options
```

**Key Features:**
- **Server-driven menu**: Menu items fetched from `/menu` endpoint — add features without rebuilding APK
- **Dynamic response parsing**: Regex extracts `[1] Option` or `1. Option` patterns from AI responses
- **Audio recording**: Uses Android MediaRecorder, outputs 3GP format
- **Minimal UI**: Title hides during responses to maximize text space

### 2. Bridge Server (Python)

**Location:** `qin_clawdbot_server.py`

Lightweight HTTP server bridging Qin to Clawdbot:

**Endpoints:**
| Endpoint | Method | Description |
|----------|--------|-------------|
| `/health` | GET | Health check (`{"status": "ok"}`) |
| `/menu` | GET | Return menu configuration JSON |
| `/action` | POST | Execute instant action by key |
| `/audio` | POST | Receive 3GP, transcribe, return text |
| `/chat` | POST | Forward text to AI, return response |

**Audio Pipeline:**
```
3GP (from phone) → FFmpeg → WAV (16kHz mono) → Whisper → Text
```

**Response Processing:**
```python
def strip_markdown(text):
    # Headers → CAPS
    # **bold** → bold
    # | tables | → - lists
    # ``` code ``` → removed
    # Multiple newlines → single
```

**AI Instruction Injection:**
Every request to the AI includes:
```
[IMPORTANT - QIN INTERFACE RULES:
User can ONLY respond by: pressing a number (1-9, 0) OR voice recording.
End EVERY response with 2-5 numbered options:
[1] Most likely next action
[2] Second most likely  
[0] ← Back/Menu
Keep options SHORT (≤25 chars).]
```

### 3. Cloudflare Tunnel

**Purpose:** Secure, permanent URL for the phone to reach your home server.

**Why not port forwarding?**
- ISPs block ports / use CGNAT
- Dynamic IPs change
- No HTTPS certificate hassle
- Built-in DDoS protection

**Setup:**
```bash
# One-time: Create named tunnel
cloudflared tunnel create qin

# Add DNS record in Cloudflare dashboard
# CNAME: qin → <tunnel-id>.cfargotunnel.com

# Run with token (in LaunchAgent)
cloudflared tunnel run --token <token>
```

### 4. Clawdbot Gateway

**Port:** 18789

Self-hosted AI gateway providing:
- **OpenAI-compatible API** (`/v1/chat/completions`)
- **Tool execution**: Email (gog), Calendar, Weather, Web Search, Brain MCP
- **Session management**: Persistent conversation context
- **Model routing**: Switch between Claude models

**API Call:**
```python
POST http://127.0.0.1:18789/v1/chat/completions
Headers:
  Authorization: Bearer <token>
  Content-Type: application/json
  x-clawdbot-agent-id: main

Body:
{
  "model": "clawdbot:main",
  "messages": [{"role": "user", "content": "..."}],
  "user": "qin"
}
```

---

## 🚀 Installation

### Prerequisites

- Mac (Apple Silicon or Intel)
- Qin F21 Pro phone
- Android SDK (`adb`)
- Python 3.9+
- Node.js 18+ (for Clawdbot)
- Homebrew

### Step 1: Install Dependencies

```bash
# Whisper (speech-to-text)
pipx install openai-whisper

# FFmpeg (audio conversion)  
brew install ffmpeg

# Cloudflared (tunnel)
brew install cloudflared

# Java (for Android builds)
brew install openjdk@17
```

### Step 2: Clone Repository

```bash
git clone https://github.com/mordechaipotash/qinbot.git ~/qin
cd ~/qin
```

### Step 3: Configure Server

Edit `qin_clawdbot_server.py`:
```python
# Set your Clawdbot gateway token
CLAWDBOT_TOKEN = "your-token-here"

# Paths to binaries (if different)
WHISPER_PATH = "/Users/you/.local/bin/whisper"
FFMPEG_PATH = "/opt/homebrew/bin/ffmpeg"
```

### Step 4: Set Up Cloudflare Tunnel

```bash
# Login (opens browser)
cloudflared tunnel login

# Create tunnel
cloudflared tunnel create qin

# Note the tunnel ID and token
```

In Cloudflare dashboard:
1. Go to **Zero Trust → Networks → Tunnels**
2. Click your tunnel → **Public Hostname**
3. Add: `qin.yourdomain.com` → `http://localhost:8081`

### Step 5: Create LaunchAgents

See [LaunchAgent templates](#launchagent-templates) below.

```bash
# Load agents
launchctl load ~/Library/LaunchAgents/com.qin.clawdbot-bridge.plist
launchctl load ~/Library/LaunchAgents/com.qin.cloudflared-tunnel.plist
```

### Step 6: Build Android App

```bash
# Connect Qin via USB
adb devices

# Set server URL in MainActivity.java
# SERVER_BASE = "https://qin.yourdomain.com"

# Build and install
export JAVA_HOME=/opt/homebrew/opt/openjdk@17
./build_and_install.sh
```

---

## 📋 LaunchAgent Templates

### Bridge Server

`~/Library/LaunchAgents/com.qin.clawdbot-bridge.plist`:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" 
  "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    <key>Label</key>
    <string>com.qin.clawdbot-bridge</string>
    <key>ProgramArguments</key>
    <array>
        <string>/usr/bin/python3</string>
        <string>/Users/YOUR_USER/qin/qin_clawdbot_server.py</string>
    </array>
    <key>WorkingDirectory</key>
    <string>/Users/YOUR_USER/qin</string>
    <key>EnvironmentVariables</key>
    <dict>
        <key>PATH</key>
        <string>/opt/homebrew/bin:/usr/local/bin:/usr/bin:/bin:/Users/YOUR_USER/.local/bin</string>
    </dict>
    <key>RunAtLoad</key><true/>
    <key>KeepAlive</key><true/>
    <key>StandardOutPath</key>
    <string>/tmp/qin-server.log</string>
    <key>StandardErrorPath</key>
    <string>/tmp/qin-server.err</string>
</dict>
</plist>
```

### Cloudflare Tunnel

`~/Library/LaunchAgents/com.qin.cloudflared-tunnel.plist`:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" 
  "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    <key>Label</key>
    <string>com.qin.cloudflared-tunnel</string>
    <key>ProgramArguments</key>
    <array>
        <string>/opt/homebrew/bin/cloudflared</string>
        <string>tunnel</string>
        <string>run</string>
        <string>--token</string>
        <string>YOUR_TUNNEL_TOKEN_HERE</string>
    </array>
    <key>RunAtLoad</key><true/>
    <key>KeepAlive</key><true/>
    <key>StandardOutPath</key>
    <string>/tmp/qin-tunnel.log</string>
    <key>StandardErrorPath</key>
    <string>/tmp/qin-tunnel.err</string>
</dict>
</plist>
```

---

## 🐛 Troubleshooting

### "Server 500" on voice
```bash
# Check Whisper path
which whisper
# Should be in LaunchAgent PATH

# Check logs
tail -f /tmp/qin-server.err
```

### Dynamic options not working
The AI must format options as `[1] Text` or `1. Text`. Check:
```bash
# Test API response
curl -X POST http://localhost:8081/action \
  -H "Content-Type: application/json" \
  -d '{"action": "4"}'
```

### Tunnel not connecting
```bash
# Check tunnel status  
curl https://qin.yourdomain.com/health

# Check logs
tail -f /tmp/qin-tunnel.err
```

---

## 📊 Performance

| Operation | Time |
|-----------|------|
| Menu load | ~200ms |
| Instant action (weather) | 2-5s |
| Voice recording upload | 1-2s |
| Whisper transcription | 3-8s |
| Full voice round-trip | 8-15s |

---

## 🙏 Acknowledgments

- [Clawdbot](https://github.com/clawdbot/clawdbot) — AI gateway with tools
- [OpenAI Whisper](https://github.com/openai/whisper) — Speech recognition
- [Cloudflare Tunnel](https://developers.cloudflare.com/cloudflare-one/connections/connect-apps/) — Secure tunneling
- [Qin F21 Pro](https://www.duoqin.com/) — The hardware that makes this possible

---

## 🌐 Ecosystem

QinBot is part of a larger system of interconnected tools:

```mermaid
graph LR
    BM[brain-mcp] -->|memory / knowledge| QB[🔹 qinbot]
    LV[local-voice-ai] -->|voice I/O| QB
    MN[mordenews] -->|news briefings| QB
    AML[agent-memory-loop] -.->|manages| BM
    AML -.->|manages| QB
    AML -.->|manages| LV
    AML -.->|manages| MN
    XS[x-search] -.->|research| QB
    BC[brain-canvas] -.->|visual display| QB
```

| Repository | Description |
|-----------|-------------|
| **🔹 qinbot** | ← You are here — AI assistant for non-smartphone users |
| [brain-mcp](https://github.com/mordechaipotash/brain-mcp) | Intellectual DNA — 377K messages, 82K vectors, 25 MCP tools |
| [local-voice-ai](https://github.com/mordechaipotash/local-voice-ai) | Local Whisper STT + Kokoro TTS on Apple Silicon |
| [agent-memory-loop](https://github.com/mordechaipotash/agent-memory-loop) | Agent maintenance layer — memory, cron, context |
| [mordenews](https://github.com/mordechaipotash/mordenews) | Podcast pipeline — generates news briefings |
| [x-search](https://github.com/mordechaipotash/x-search) | Live X/Twitter search via Grok |
| [brain-canvas](https://github.com/mordechaipotash/brain-canvas) | Visual display for Brain MCP data |

---

## 📄 License

MIT License — See [LICENSE](LICENSE) for details.

---

<div align="center">

**Built with ❤️ for the digital minimalism community.**

*"The constraint creates freedom."*

</div>
