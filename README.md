# Minecraft Paper Manhunt Plugin

A premium, highly interactive, and feature-rich Minecraft Manhunt game mode plugin built specifically for PaperMC (1.20.4+ / Java 21).

## 🌟 Key Features
- **Dynamic Multi-Runner Tracking**: Supports 1 or multiple runners. Every hunter automatically receives one custom tracking compass for each assigned runner.
- **Advanced Lodestone Compass**:
  - Works across all dimensions (Overworld, Nether, End).
  - When in the same dimension, points directly to the runner with live distance display in meters in the lore.
  - When in a different dimension, the compass needle locks North (pointing Top on the hotbar) and updates lore with target dimension.
- **Adjustable Head-Start Grace Period**: Configurable grace period countdown before match start. Runners sprint freely while hunters are held perfectly frozen at spawn with on-screen countdown titles.
- **Free Movement with Absolute Security**: Hunters can freely drag and sort tracking compasses anywhere inside their 36 main inventory slots or offhand (slot 40). Compasses are absolutely protected against dropping (`Q`), storing in containers/chests/hoppers, mounting onto entity frames/stands, or dropping on death.
- **Smart Overflow & Full-Scanning**: Scans all 36+1 slots instantly. If a hunter's inventory is 100% full when a new compass is issued, it is forced into slot 8, and the existing item is safely dropped at the hunter's feet with a chat notification.
- **Combat Log Safety & Rejoin Alerts**: If a runner logs out, compasses lock to their last known coordinate and show `"§cRunner is OFFLINE (§7Last Known Location)"`. When they reconnect, compasses instantly resume tracking and broadcast an alert to hunters!
- **Isolated Multi-World Profiles**: Completely isolated player states (inventories, armor, ender chests, health, XP level, location, potion effects) between the Base Server World and Manhunt Worlds. Players automatically rejoin exactly where they left off, and `/mh generate` cleanly wipes all Manhunt profiles for a fresh match.
- **Tournament Edge Case Protections**:
  - **Respawn Limbo Prevention**: When resetting worlds, dead players on the respawn screen are forcefully respawned to prevent server thread crashes.
  - **Beds & Respawn Anchors Respected**: Player beds in overworld and anchors in nether are perfectly honored instead of overriding to world spawn.
  - **Mid-Air Ender Pearl Cancellation**: Pearls in flight are cancelled during pause or cross-realm teleportation to prevent glitching.
  - **Dragon Death Window**: Gives players a 15-second celebratory window in the End after dragon defeat before concluding the match.
- **Interactive GUI & CLI Controls**: Manage the entire match, add/remove runners, toggle chat logs, configure time limits, and customize head-start durations via an intuitive chest GUI (`/manhunt gui`) or CLI with instant disk synchronization (`config.yml`).

---

## 🚀 How to Build & Install

### Requirements
- **Java 21**
- **Apache Maven 3.8+**
- **PaperMC 1.20.4+ Server**

### Build Instructions
1. Clone or navigate to the project directory:
   ```bash
   cd manhunt
   ```
2. Build the plugin jar using Maven:
   ```bash
   mvn clean package
   ```
3. Locate the compiled artifact in the `target/` directory: `manhunt-paper-1.0.0.jar`.
4. Copy the jar into your Paper server's `plugins/` directory and restart the server.

---

## 🛠️ Commands & Permissions

**Main Command**: `/manhunt` (Alias: `/mh`)  
**Permission Required**: `manhunt.admin` (Default: op)

| Subcommand | Description |
| :--- | :--- |
| `/mh gui` | Opens the interactive chest GUI control panel (with multi-layer menus and back buttons). |
| `/mh generate` | Generates dedicated manhunt worlds (Overworld, Nether, End) and resets all manhunt profiles. |
| `/mh tp <manhunt|base> [player|all]` | Teleports specific or all players to the manhunt world or base world with full profile switching. |
| `/mh start` | Starts the manhunt match, triggering any configured head-start grace period. |
| `/mh pause` | Freezes an active match (no movement, damage, or block breaking). Safe-guarded against inactive matches. |
| `/mh resume` or `/mh unpause` | Unpauses and resumes a paused match exactly from where it left off. |
| `/mh end` | Ends an active match, clears tracking compasses, and returns players to base world. |
| `/mh runner add <player>` | Assigns a player as a runner and instantly issues compasses. |
| `/mh runner remove <player>` | Removes a runner and cleans up tracking compasses across the server. |
| `/mh runner list` | Lists all currently active runners. |
| `/mh timer limit <minutes>` or `indefinite` | Instantly sets a countdown limit in minutes or indefinite run. Supports in-game custom chat prompt via GUI. |
| `/mh headstart <seconds>` or `grace` | Sets a head-start grace period in seconds. Prohibited from changing once match is active. |
| `/mh logs <on|off|toggle>` | Toggles plugin chat broadcast alerts (also toggleable via Slot 13 in Main GUI). |
| `/mh reload` | Reloads configuration from `config.yml`. |

---

## ⚙️ Configuration (`config.yml`)

```yaml
# Slots where tracking compasses will be initially placed for hunters if available.
# 0-8 are hotbar slots (left to right). 40 is off-hand. 9-35 are main inventory.
compass-slots:
  - 8
  - 7
  - 6
  - 5
  - 4
  - 40

# Time limit in seconds (0 means indefinite).
time-limit: 0

# Head-start grace period in seconds (0 means no headstart).
head-start: 0

# Base server world name where players are returned when manhunt ends.
base-world: "world"

# Manhunt generated world prefix.
manhunt-world: "manhunt_world"

# Whether plugin broadcast alerts (e.g. runner rejoin) are enabled.
chat-logs: true

# List of assigned runner UUIDs (persisted across restarts).
runners: []
```
