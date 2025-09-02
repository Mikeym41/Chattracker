<h1 align="center">📢 ChatTracker</h1>
<p align="center">
  <i>A lightweight chat moderation plugin for Minecraft</i><br>
  <b>Developed by MikeyM41</b><br>
  <img src="https://img.shields.io/badge/Minecraft-1.21-blue?style=flat-square" />
  <img src="https://img.shields.io/github/license/yourusername/chattracker?style=flat-square" />
  <img src="https://img.shields.io/github/stars/yourusername/chattracker?style=flat-square" />
</p>

---

<h2> Description & Features</h2>
What it does is watch everything players type in chat and keeps a log of it, with timestamps and who said what. That way, the server always has a record.

But here’s the cool part: it has a feature called Big Brother, not creepy, just funny name, it checks messages for words or phrases that the server doesn’t want people saying. If it spots one, it automatically flags the message and alerts any moderators online. So mods don’t have to be staring at chat all the time; they get a heads-up when someone might be breaking the rules.

Everything it flags gets logged separately too, so you can go back and see exactly what was said and when. And the words it flags? You can change them yourself in a simple config.yml file. Right now it’s pretty basic, but it works, and I’m thinking of improving how the logs are organized maybe by player, by day, or some other way that makes it easier to track things. 

Basically, it’s a simple tool for keeping chat clean and helping moderators stay on top of things without doing all the work themselves.
-  **Banned Words/Phrases** – Easily configurable in `config.yml` just configure what words or phrases you want flagged. 
-  **Big Brother System** – Real-time notifications to moderators- This is the ingame notfication to server staff
-  **Logging** – Dated, timestamped, and player-tagged logs (Working on improving further)


---

<h2> Screenshots</h2>
<p align="center">
  <img src="docs/Screenshot 2025-09-01 165144.png" width="600" />
  <br>
  <i>Example of moderator chat notification in-game</i>
</p>

---

<h2> Development Roadmap</h2>

-  Better log organization (per player, per day, or hybrid system)    
-  API hooks for developers
-  Staff Chat
-  Private Messages
-  Custom chat formatting
-  Private Messages logging
-  Warn/Kick/Mute System
-  Discord Integration
-  Chat Formatting & Prefixes
-  Spy Mode
-  Command Monitoring
-  Player Notes System (server staff tool)
  

---
<h2> Contributing</h2>

I welcome contributions from other developers!  
If you have ideas, improvements, or fixes, feel free to **fork the repository** and submit a **pull request (PR)**.  

Guidelines:  
- Ensure your code is well-documented  
- Follow existing code style and conventions  
- Test your changes before submitting  

Every contribution helps me improve ChatTracker and I thank you for it!
---

<h2>🚀 Installation</h2>

1. Download the latest release from [Releases](../../releases).  
2. Drop the `.jar` into your server’s `plugins` folder.  
3. Restart the server.  
4. Edit the `config.yml` to your liking.  

---

<h3>Example <code>config.yml</code></h3>

<pre><code class="yaml"># ChatTracker Configuration File
# List of words or phrases to flag in chat
banned-words:
  - badword1
  - badword2
  - spamword
  - cheat
  - exampleword

# Prefix shown to moderators when a message is flagged
alert-prefix: "[ChatTracker]"

# Whether flagged messages should also be logged to a separate file
log-flagged-messages: true
</code></pre>

<p>
  Customize this file to define which words or phrases should be flagged, how alerts appear to moderators, 
  and whether flagged messages should be saved to a separate log.
</p>

---

<h3>Permissions</h3>

<pre><code class="yaml"># Example permissions setup using PermissionsEx or your preferred plugin

moderator:
  permissions:
    - chattracker.moderate   # Allows the player to receive flagged chat notifications
    - essentials.kick        # Optional: kick players (if using Essentials)
    - essentials.ban         # Optional: ban players
  prefix: '&c[Mod] &f'

users:
  Example Username:
    group:
      - moderator
</code></pre>

<p>
  Grant <code>chattracker.moderate</code> to any player who should receive real-time alerts about flagged chat messages. 
  You can also integrate with existing permission groups like moderators or admins.
</p>

<hr/>

<h3>Commands</h3>

<pre><code class="yaml"># Commands included in ChatTracker

/chattracker
  description: Test command to ensure the plugin is working
  usage: /chattracker
</code></pre>

<p>
  The <code>/chattracker</code> command currently only tests that the plugin is loaded and functioning. 
  Future versions may include commands to view flagged messages, manage banned words, or configure alerts in-game.
</p>


<h2>License</h2>
<p>This project is licensed under the <a href="LICENSE">MIT License</a>.</p>

