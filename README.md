

<p align="center">
  <img src="https://github.com/Mikeym41/Chattracker/blob/master/docs/Screenshot%202025-09-01%20223401.png?raw=true" alt="ChatTracker Logo" width="200">
</p>

<p align="center">
  Track conversations, analyze chat logs, and keep your server community safe and fun.
</p>

<hr>

## Description
**ChatTracker** monitors all chat activity on your server, logging messages with timestamps and player names so you always have a record. Its **Big Brother** feature checks messages for banned words or phrases, flags them, and notifies online moderators automatically.  

**Staff Chat** lets moderators communicate privately without interfering with public chat.  
**Hoverable and clickable reports** allow players to report inappropriate messages in-game directly to staff.  

**New in v1.4**: **LuckPerms integration**. Rank prefixes, suffixes, and colors come straight from LuckPerms, and `/setrank` creates real temporary ranks that expire on their own.  

ChatTracker helps staff stay on top of rule-breaking behavior efficiently and reliably, with enhanced message formatting to make chat clear and readable.

<hr>

## Features
- Tracks all player chat messages in real time.
- **LuckPerms integration** – prefixes, suffixes, and name/message colors pulled from LuckPerms (optional; config.yml ranks work without it).
- Color-coded chat for ranks, including hex colors (`&#RRGGBB`).
- Hoverable and clickable reports to quickly flag inappropriate messages.
- **Clickable punishments** (Warn, Kick, Ban) with full logging.
- Logs all messages, reports, and punishments with timestamps for staff review.
- Flags inappropriate messages automatically using the Big Brother system.
- **Staff Chat** – private channel for moderators, fully implemented with toggle functionality.
- Temporary ranks (`/setrank vip 7d`) that expire automatically via LuckPerms.
- Lightweight and optimized for performance.
- Easy setup with minimal configuration required.

<hr>

## Roadmap
Player Monitoring
- /check <player> → Shows info (IP, alts, punishments, reports, last login).
- /history <player> → See all punishments and reports tied to them.
- /seen <player> → Last online time.

Staff Utilities

- CommandSpy → View commands players are running.
- SocialSpy → View private messages between players.
- /freeze <player> → Temporarily freeze a player for investigation.
- Silent Join/Leave → Staff can log in/out without alerts.
- Staff Mode → Special gamemode with inspector tools (teleport, vanish, freeze, randomTP).

Punishment Enhancements

- Timed Punishments → e.g., /ban <player> 7d <reason>.
- Mute System → Temporary/permanent mute with reasons.
- Warning Escalation → X warnings = auto mute/ban.
- Appeals Log → Optional system to mark punishments as appealed.

Logs & Analytics
- MySQL / SQLite support → Store punishments/reports in a database.
- Web Panel support → Optional frontend for staff to review punishments/reports.
- Export Logs → Generate staff activity reports weekly/monthly.

Player Protection
- Anti-Alt Detection → Alert staff if players with the same IP join.
- VPN/Proxy detection → Optional alerts if suspicious connections are used.
- Chat Filter → Block certain words with staff alerts (already partly in).

Quality of Life
- Clickable Teleports in reports (click player name to teleport).
- Customizable GUI Menus (e.g., for punishments, staff tools).
- Discord Integration → Send reports & punishments to a staff Discord channel.

<hr>

## Screenshots & Demo

### Video Showcase
[![ChatTracker Demo](https://img.youtube.com/vi/9yy0CFVh4Mc/0.jpg)](https://www.youtube.com/watch?v=9yy0CFVh4Mc)  
*Click to watch ChatTracker in action — see staff chat, hoverable reports, and flagged message alerts in-game.*

---

### Screenshots

<p align="center">
  <img src="https://github.com/Mikeym41/Chattracker/blob/master/docs/Screenshot%202025-09-01%20165144.png?raw=true" width="450" />
  <img src="https://github.com/Mikeym41/Chattracker/blob/master/docs/Screenshot%202025-09-02%20135305.png?raw=true" width="450" />
  <img src="https://github.com/Mikeym41/Chattracker/blob/master/docs/Screenshot%202025-09-03%20002723.png?raw=true" width="450" />
</p>

<p align="center"><i>Examples of Staff Chat, hoverable clickable reports, and flagged message notifications in-game.</i></p>

<hr>

## Installation
1. Download the latest version from [Modrinth](https://modrinth.com/plugin/chattracker) or [GitHub Releases](https://github.com/Mikeym41/Chattracker/releases).
2. Place the ChatTracker `.jar` file into your server's `/plugins` folder.
3. Restart your server.
4. Configuration files will be generated automatically on first run.

<hr>

## LuckPerms Integration
ChatTracker hooks into [LuckPerms](https://luckperms.net) automatically when it's installed. No extra setup is needed.

- **Prefixes & suffixes** come straight from LuckPerms, including hex colors like `&#FF8800`.
- **Name and message colors** can be set per group with LuckPerms meta:
  ```
  /lp group vip meta addprefix 100 "&6[VIP] "
  /lp group vip meta set name-color &e
  /lp group vip meta set message-color &e
  ```
- Anything LuckPerms doesn't set falls back to the `ranks` section of `config.yml`, matched by the player's primary group.
- **`/setrank` creates real LuckPerms temporary groups** that expire on their own, survive restarts, and sync across a network.

Without LuckPerms, ChatTracker uses the `ranks` section of `config.yml` exactly as before. Set `luckperms.enabled: false` to ignore LuckPerms even when it's installed.

<hr>

## Permissions
- `chattracker.use` – Use `/chattracker` and report messages (everyone by default).
- `chattracker.chatcolor` – Use `&` color codes in your own chat messages.
- `chattracker.view` – Allows viewing tracked chat logs.
- `chattracker.moderate` – Allows receiving flagged message alerts, reports, and issuing punishments.
- `chattracker.staffchat` – Access to the completed Staff Chat channel.
- `chattracker.setrank` – Allows temporary rank changes.
- `chattracker.admin` – Full access to ChatTracker commands, including reload.

<hr>

## Commands
- `/chattracker` – Shows the version and which rank source is active.
- `/chattracker reload` – Reloads the configuration and re-checks LuckPerms.
- `/staffchat <message>` – Sends a message to all online staff (toggle functionality included).
- `/setrank <player> <group> [duration]` – Temporarily puts a player in a group. With LuckPerms this is a real temporary group (e.g. `30m`, `2h`, `7d`, `1d12h`; default set in config).
- `/resetrank <player> [group]` – Removes a player's temporary group(s). Permanent groups are never touched.
- `/report <player> <message>` – Report a message in-game (clickable through hoverable chat).
- `/punish <player> <warn|kick|ban>` – Issue punishments (clickable buttons also available).

<hr>

## Changelog

### v1.4 (Current Release)
- **LuckPerms integration**: prefixes, suffixes, and colors come from LuckPerms when installed.
- `/setrank` creates real LuckPerms temporary groups with a duration; `/resetrank` removes them.
- Hex color support (`&#RRGGBB`) in prefixes, suffixes, and colors.
- Tab completion for groups and durations.
- `/chattracker reload` added; setrank/resetrank now work from console.
- Players can no longer use color codes in chat without `chattracker.chatcolor`.
- `/report` now works for regular players by default.

### v1.3
- **Clickable punishments (Warn, Kick, Ban)** added.
- Full punishment logging implemented.
- Expanded logging system for chat, reports, and punishments.
- UX improvements for staff workflow.

### v1.2
- Hoverable and clickable message reporting implemented.
- Color-coded chat messages per rank.
- Enhanced chat logging and report logging system.
- Temporary rank override commands for quick testing or promotions.
- Improved Staff Chat toggle functionality.

### v1.1
- Staff Chat feature fully implemented for moderators.
- Improved flagged message notifications.
- Enhanced log formatting and organization.

### v1.0-BigBrother
- Core chat tracking system.
- Permissions system added.
- Commands for reloading and viewing logs.
- Big Brother flagged word alerts to staff.

<hr>

##  Dependencies
**PaperMC** (latest recommended build). No additional plugins required.
**LuckPerms** (optional) for rank prefixes, colors, and temporary groups.

<hr>

## License
This project is licensed under the MIT License.


