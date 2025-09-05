

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

**New in v1.3**: Staff can now issue **clickable punishments (Warn, Kick, Ban)** directly from reports, with all punishments logged for transparency.  

ChatTracker helps staff stay on top of rule-breaking behavior efficiently and reliably, with enhanced message formatting to make chat clear and readable.

<hr>

## Features
- Tracks all player chat messages in real time.
- Color-coded chat for ranks, messages stand out according to group.
- Hoverable and clickable reports to quickly flag inappropriate messages.
- **Clickable punishments** (Warn, Kick, Ban) with full logging.
- Logs all messages, reports, and punishments with timestamps for staff review.
- Flags inappropriate messages automatically using the Big Brother system.
- **Staff Chat** – private channel for moderators, fully implemented with toggle functionality.
- Temporary rank overrides with commands for testing or promotions.
- Lightweight and optimized for performance.
- Easy setup with minimal configuration required.

<hr>

## Roadmap
- Private Messages system.
- Advanced log filtering and organization: per player, per day, or hybrid system.
- Mute system integration.
- Discord/Webhook integration for real-time notifications.
- Player Notes System for staff.
- Command monitoring and auditing for moderation transparency.
- Enhanced hoverable messages with quick actions for reporting or moderation.

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
1. Download the latest release from [GitHub Releases](https://github.com/Mikeym41/Chattracker/releases).
2. Place the `ChatTracker.jar` file into your server's `/plugins` folder.
3. Restart your server.
4. Configuration files will be generated automatically on first run.

<hr>

## Permissions
- `chattracker.view` – Allows viewing tracked chat logs.
- `chattracker.moderate` – Allows receiving flagged message alerts, reports, and issuing punishments.
- `chattracker.staffchat` – Access to the completed Staff Chat channel.
- `chattracker.setrank` – Allows temporary rank changes.
- `chattracker.admin` – Full access to ChatTracker commands.

<hr>

## Commands
- `/chattracker reload` – Reloads the configuration.
- `/chattracker logs` – Displays recent chat logs in-game.
- `/staffchat <message>` – Sends a message to all online staff (toggle functionality included).
- `/setrank <player> <group>` – Temporarily sets a player's rank.
- `/resetrank <player>` – Resets a player to their config-defined rank.
- `/report <player> <message>` – Report a message in-game (clickable through hoverable chat).
- `/punish <player> <warn|kick|ban>` – Issue punishments (clickable buttons also available).

<hr>

## Changelog

### v1.3 (Current Release)
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

<hr>

## License
This project is licensed under the MIT License.


