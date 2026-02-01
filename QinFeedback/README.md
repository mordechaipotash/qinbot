# Qin Feedback Remote

Turn your Qin phone into a wireless 5-button remote for your Mac — works on WiFi or anywhere online.

```
┌─────────────┐                         ┌─────────────┐
│    Qin      │   WiFi (2s timeout)     │    Mac      │
│  Press 1-5  │ ───────────────────────>│   Server    │
└─────────────┘                         └─────────────┘
       │
       │  If WiFi fails, fallback to:
       ▼
┌─────────────┐        Internet         ┌─────────────┐
│   ntfy.sh   │ ───────────────────────>│  Listener   │
│   (cloud)   │                         │   (Mac)     │
└─────────────┘                         └─────────────┘
```

## Quick Start

```bash
# 1. Start receiver on Mac (choose one or both)
python3 feedback_server.py     # WiFi mode (same network)
python3 ntfy_listener.py       # Online mode (anywhere)

# 2. Install app on Qin
adb install qin-feedback.apk

# 3. Open "Qin Feedback" and press keys!
```

## Key Mappings

| Key | Action | Use Case |
|:---:|--------|----------|
| **1** | 👍 thumbs_up | Good / Like / Yes |
| **2** | 👎 thumbs_down | Bad / Dislike / No |
| **3** | ⭐ star | Favorite / Important |
| **4** | 💬 note | Comment / Flag |
| **5** | ▶ play | Play / Continue / Next |
| **0** | Exit | Close app |

## Requirements

| Component | Requirement |
|-----------|-------------|
| Phone | Qin 1s (Android 4.4+) |
| Mac | Python 3 |
| Network | Same WiFi **or** Internet |

## How It Works

1. **WiFi Mode** (fast, ~50ms): App sends HTTP GET to Mac on local network
2. **Online Mode** (fallback): If WiFi fails after 2 seconds, sends to ntfy.sh cloud service

The app automatically tries WiFi first, then falls back to online — no configuration needed.

## Configuration

### Change Mac IP (for WiFi mode)

```bash
# Find your Mac's IP
ipconfig getifaddr en0
```

Edit `app/src/main/java/com/qin/feedback/MainActivity.java`:
```java
private static final String LOCAL_SERVER = "http://YOUR_IP:8080/feedback?action=";
```

### Change ntfy.sh Channel (for online mode)

```java
private static final String NTFY_CHANNEL = "your-unique-channel-name";
```

Then rebuild the APK.

## Mac Receivers

### WiFi Mode: `feedback_server.py`
- Runs on port 8080
- Shows Mac notifications
- Logs to `feedback.log`

### Online Mode: `ntfy_listener.py`
- Subscribes to ntfy.sh channel
- Shows Mac notifications
- Logs to `feedback.log`
- Works from anywhere with internet

## Building from Source

```bash
# Environment
export ANDROID_HOME=~/Library/Android/sdk
export JAVA_HOME=/opt/homebrew/opt/openjdk@17
export PATH=$JAVA_HOME/bin:$PATH
ANDROID_JAR="$ANDROID_HOME/platforms/android-36/android.jar"
BUILD_TOOLS="$ANDROID_HOME/build-tools/36.0.0"

# Build
mkdir -p app/build/classes
javac -source 1.8 -target 1.8 -classpath "$ANDROID_JAR" \
  -d app/build/classes app/src/main/java/com/qin/feedback/MainActivity.java

"$BUILD_TOOLS/d8" --min-api 19 --output app/build/ \
  app/build/classes/com/qin/feedback/*.class

"$BUILD_TOOLS/aapt2" link -o app/build/base.apk \
  --manifest app/src/main/AndroidManifest.xml \
  -I "$ANDROID_JAR" --min-sdk-version 19 --target-sdk-version 28

cd app/build && zip -j base.apk classes.dex
"$BUILD_TOOLS/zipalign" -f 4 base.apk aligned.apk
"$BUILD_TOOLS/apksigner" sign --ks ../../debug.keystore \
  --ks-pass pass:android --key-pass pass:android \
  --out qin-feedback.apk aligned.apk
```

## API

### Local Server
```
GET http://<IP>:8080/feedback?action=<ACTION>
Response: {"status": "ok", "action": "<ACTION>"}
```

### ntfy.sh
```
POST http://ntfy.sh/<CHANNEL>
Headers: Title: "Qin <emoji>", Tags: <action>
Body: <action>
```

## Customization

**Add new actions** in `MainActivity.java`:
```java
case KeyEvent.KEYCODE_6:
    action = "custom_action";
    emoji = "🎉";
    break;
```

**Handle actions** in `feedback_server.py` or `ntfy_listener.py`:
```python
if action == 'custom_action':
    subprocess.run(['open', '-a', 'Spotify'])
```

## Troubleshooting

| Issue | Solution |
|-------|----------|
| "Error!" on send | Check WiFi/internet, verify server running |
| Slow response | WiFi failing, falling back to online (2s delay) |
| Can't install | Enable "Unknown sources", use `adb install -r` |
| No notification | Check `feedback.log`, verify listener running |

## Project Structure

```
QinFeedback/
├── qin-feedback.apk          # Ready to install
├── feedback_server.py        # WiFi receiver
├── ntfy_listener.py          # Online receiver
├── debug.keystore            # Signing key
├── README.md
├── build.gradle
├── settings.gradle
└── app/
    ├── build.gradle
    └── src/main/
        ├── AndroidManifest.xml
        └── java/com/qin/feedback/
            └── MainActivity.java
```

## License

MIT
