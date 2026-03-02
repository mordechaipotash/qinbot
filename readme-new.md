# QinBot

**Full Claude AI on a phone with no browser, no apps, no images.**

<video src="https://github.com/mordechaipotash/qinbot/raw/main/assets/demo.mp4" width="100%" autoplay loop muted playsinline></video>

---

Smartphones are designed to be addictive. The choice has always been:

- **Full smartphone** → All the capability, all the addiction
- **Dumb phone** → No addiction, no capability

QinBot gives you 90% of the utility with 10% of the risk. Full AI assistant on a $39 device that can't browse the web, can't install apps, and can't show images.

---

## What It Does

Your Qin F21 phone sends texts to a Clawdbot-powered AI agent. The agent has access to:

- **Claude AI** — Full reasoning, code generation, analysis
- **Calendar** — Schedule, reminders, appointments
- **Email** — Read, compose, send
- **Search** — Web search via the agent
- **Notes** — Create, retrieve, organize
- **Brain MCP** — Your entire conversation history, searchable

All through text messages. No screen time. No doomscrolling.

---

## Setup

### What You Need

1. **Qin F21 phone** (~$39 on AliExpress)
2. **SIM card** with SMS
3. **Clawdbot** running on a server/Mac
4. **WhatsApp** or **Signal** bridge

### Steps

```bash
# 1. Install Clawdbot
npm install -g clawdbot

# 2. Configure WhatsApp bridge
clawdbot config whatsapp

# 3. Link the Qin phone's WhatsApp
# (scan QR code from the Qin's basic WhatsApp)

# 4. Done — text the bot, get Claude
```

---

## Why Qin F21

| Feature | Qin F21 | iPhone | Dumb phone |
|---------|---------|--------|------------|
| AI assistant | ✅ (via QinBot) | ✅ (native) | ❌ |
| Browser | ❌ | ✅ | ❌ |
| App store | ❌ | ✅ | ❌ |
| Social media | ❌ | ✅ | ❌ |
| Addictive | ❌ | ✅ | ❌ |
| Price | $39 | $999+ | $20 |

The Qin F21 runs a minimal Android that supports WhatsApp but not much else. That's the sweet spot.

---

## Who This Is For

- **Religious communities** wanting "kosher phones" with capability
- **Digital minimalists** who still need AI access
- **Parents** who want their kids to have a capable but limited device
- **Anyone** trying to disconnect without losing productivity

---

## Part of the ecosystem

[brain-mcp](https://github.com/mordechaipotash/brain-mcp) · [local-voice-ai](https://github.com/mordechaipotash/local-voice-ai) · [agent-memory-loop](https://github.com/mordechaipotash/agent-memory-loop) · [x-search](https://github.com/mordechaipotash/x-search)

## License

MIT
