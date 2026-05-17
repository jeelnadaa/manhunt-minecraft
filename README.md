# Minecraft Paper Manhunt Plugin

A premium, highly interactive, and feature-rich Minecraft Manhunt game mode plugin built specifically for PaperMC (1.20.4+ / Java 21).

## 🌟 Key Features
- **Dynamic Multi-Runner Tracking**: Supports 1 or multiple runners. Every hunter automatically receives one custom tracking compass for each assigned runner.
- **Advanced Lodestone Compass**:
  - Works across all dimensions (Overworld, Nether, End).
  - When in the same dimension, points directly to the runner with live distance display in the lore.
  - When in a different dimension, the compass needle locks North (pointing Top on the hotbar) and updates lore with target dimension.
- **Slot Locking & Persistence**: Compasses are placed in designated configurable inventory/hotbar slots. They are protected against dropping, moving into chests, or being lost on hunter death.
- **Isolated World Generation**: `/manhunt generate` creates dedicated `manhunt_world`, `manhunt_world_nether`, and `manhunt_world_the_end` worlds with automatic portal linking.
- **Interactive Control GUI**: Manage the entire match, add/remove runners, generate worlds, and configure time limits via an intuitive chest GUI (`/manhunt gui`).
- **BossBar HUD Timer**: Live elapsed time and countdown limits displayed cleanly at the top of the screen.
- **Isolated Multi-World Profiles**: Completely isolated player profiles (inventories, ender chests, health, XP, location) between the Base Server World and Manhunt Worlds. Players automatically rejoin where they left off, and `/mh generate` cleanly wipes all Manhunt profiles for a fresh match.
- **Instant Config Synchronization**: All changes made via GUI or CLI instantly update active memory and persist to `config.yml`.

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
| `/mh generate` | Generates dedicated manhunt worlds (Overworld, Nether, End). |
| `/mh tp <manhunt|base> [player|all]` | Teleports specific or all players to the manhunt world spawn or base world spawn. |
| `/mh start` | Starts the manhunt match, resets stats, and starts the HUD timer. |
| `/mh pause` | Freezes an active match (no movement, damage, or block breaking). Safe-guarded against inactive matches. |
| `/mh resume` or `/mh unpause` | Unpauses and resumes a paused match exactly from where it left off. |
| `/mh end` | Ends an active match, clears tracking compasses, and returns players to base world. |
| `/mh runner add <player>` | Assigns a player as a runner and instantly issues compasses. |
| `/mh runner remove <player>` | Removes a runner and cleans up tracking compasses. |
| `/mh runner list` | Lists all currently active runners. |
| `/mh timer limit <sec>` or `/mh timer indefinite` | Instantly sets a countdown limit or indefinite run. Supports in-game custom chat prompt via GUI. |
| `/mh reload` | Reloads configuration from `config.yml`. |

---

## ⚙️ Configuration (`config.yml`)

```yaml
# Slots where tracking compasses will be placed for hunters.
# 0-8 are hotbar slots (left to right). 40 is off-hand. 9-35 are main inventory.
compass-slots:
  - 8
  - 7
  - 6
  - 5
  - 4
  - 40

# Time limit in seconds. 0 means indefinite.
time-limit: 0

# Base server world name where players are returned when manhunt ends.
base-world: "world"

# Manhunt generated world prefix.
manhunt-world: "manhunt_world"

# List of assigned runner UUIDs (persisted across restarts).
runners: []
```
