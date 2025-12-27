<div align="center">
  <img src="https://i.imgur.com/pY0rpkr.jpeg" alt="EndSectors Banner" width="100%" />

  <h1>EndSectors</h1>

  <img src="https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=java" alt="Java 21" />
  <img src="https://img.shields.io/badge/Architecture-Microservices-blueviolet?style=for-the-badge" alt="Microservices" />
  <img src="https://img.shields.io/badge/License-Non--Commercial-red?style=for-the-badge" alt="License" />

<br><br>

  <p>
    🎬 <b>See it in action:</b> <a href="https://www.youtube.com/watch?v=U_wk1nABo_M">YouTube Demo</a>
    &nbsp; | &nbsp;
    🗺️ <b>Interactive Map:</b> <a href="https://oski646.github.io/sectors-generator/">Sectors Generator</a>
  </p>
</div>

<hr>

**EndSectors** — experimental Minecraft sector framework for **Paper 1.24.1** with **NATS & Redis** 🗄️⚡

EndSectors allows you to run a single **Minecraft world** across multiple **Spigot servers**, each representing a **sector**.  
All sectors are connected via **Velocity**, giving players the feeling of one seamless world.

- Each sector has its own **boundaries** (default 10k per axis)
- Players can move seamlessly between sectors
- Player data (inventory, gamemode, enderchest, etc.) is synced in real-time via **Redis**
- Inter-server messaging and packets are handled by **NATS**
- **Common service** coordinates proxy and sectors, handles core logic, and ensures proper communication

> [!WARNING]
> This project is **experimental** and **not intended for production use**.  
> It is designed for testing and learning sector-based world mechanics.

---

## 🔹 Architecture

- **Common Service (The Brain)** – The core of the system. It processes logic and monitors the network. **If Common stops, all connected servers automatically shut down** to ensure data safety.
- **Velocity Proxy (The Connector)** – Merges all sectors into one seamless world and handles player switching between servers.
- **Spigot Sectors (The Game)** – Dedicated servers running specific parts of the world. They detect sector boundaries and sync with the rest of the system.
- **NATS** – The high-speed transport layer responsible for **all packet communication** between services.
- **Redis** – Strictly used for **data storage** and synchronizing player state (inventory, enderchest, stats).

> [!IMPORTANT]
> **🌍 Map Consistency is Critical**
> For the illusion of a single world to work, **all sector servers MUST use the exact same world map** (identical `world` folder).
> 
> **Example:** If you have a fully built spawn on `spawn_1`, you must upload **this exact world folder** to all surrounding sectors (`w1`, `n1`, `s1`, `e1`).
> The terrain and builds at the border must perfectly match across all instances. Do not try to split the map file manually.

---

## 📂 Project Modules & Installation

The project consists of 4 artifacts. You must install and configure them in the correct order.

### 📦 Module List

| Module | Filename | Role |
| :--- | :--- | :--- |
| **Common App** | `common-1.6-BETA-all.jar` | **The Brain.** Standalone app. Must run first. |
| **Proxy Plugin** | `proxy-1.6-BETA-all.jar` | **The Bridge.** Goes into Velocity `/plugins`. Generates sector map config. |
| **Paper Plugin** | `paper-1.6-BETA-all.jar` | **The Core.** Goes into Spigot `/plugins`. Handles logic. |
| **Tools Plugin** | `Tools-1.6-BETA-all.jar` | **API Example.** Goes into Spigot `/plugins`. Adds `/spawn`, `/rtp`, `/home`. |

### 📂 Configuration Paths (Critical)
Each module generates its own `config.json`. You **MUST** ensure that **NATS** and **Redis** credentials are **IDENTICAL** in all of them.

| Module | Config Location | Note |
| :--- | :--- | :--- |
| **Common App** | `./config.json` | Located in the same folder as the `.jar`. |
| **Proxy** | `plugins/EndSectorsProxy/config.json` | Defines the sector layout map. |
| **Paper** | `plugins/EndSectors/config.json` | Main configuration for sector logic. |
| **Tools** | `plugins/EndSectors/config.json` | Uses the main EndSectors configuration. |

> [!CAUTION]
> **❌ Mismatched Credentials = Network Failure**
> If the `redisPassword` or `natsUrl` differs even by one character between **Common**, **Proxy**, and **Paper**, the connection will be rejected.
> **Copy-paste** your connection details to ensure consistency across all servers.

### 🚦 Startup Sequence
The system must be launched in this strict order to establish connections correctly:

1.  🔴 **Start Common App** (`java -jar common-1.6-BETA-all.jar`)
2.  🟡 **Start Velocity Proxy** (Wait for it to connect to Common)
3.  🟢 **Start Spigot Sectors** (They will register themselves to the network)

---

## ✨ Key Features

* **🧠 Smart Border Handover** – The system calculates player position relative to sector boundaries. Crossing a line triggers an instant, seamless transfer to the neighbor server.
* **💾 Atomic Data Sync** – Inventory, HP, Food, Enderchest, and Potion Effects are synchronized via Redis. No item duplication or rollback glitches.
* **📢 Synchronized Chat** – Chat is global. A message sent on `sector_north` is instantly visible on `sector_south`.
* **⚖️ Queue & Load Balancing** – If a sector is full, players are queued globally. The system also remembers the player's last known sector.
* **🛡️ Fail-Safe Protocol** – If the **Common App** loses connection, all sectors execute an emergency shutdown to prevent data desynchronization.

---

## ⚙️ Configuration Example

The main sector layout is generated by the **Proxy Plugin** (`plugins/EndSectorsProxy/config.json`).

**Example `config.json` (Proxy Side):**
```json
{
  "redisHost": "127.0.0.1",
  "redisPort": 6379,
  "redisPassword": "",
  "natsUrl": "nats://127.0.0.1:4222",
  "natsConnectionName": "proxy",
  "sectors": {
    "SPAWN": {
      "spawn_1": {
        "pos1X": -200,
        "pos1Z": -200,
        "pos2X": 200,
        "pos2Z": 200,
        "type": "SPAWN",
        "world": "world"
      },
      "spawn_2": {
        "pos1X": -200,
        "pos1Z": -200,
        "pos2X": 200,
        "pos2Z": 200,
        "type": "SPAWN",
        "world": "world"
      }
    },
    "QUEUE": {
      "queue": {
        "pos1X": -200,
        "pos1Z": -200,
        "pos2X": 200,
        "pos2Z": 200,
        "type": "QUEUE",
        "world": "world"
      }
    },
    "SECTOR": {
      "s1": {
        "pos1X": -200,
        "pos1Z": 200,
        "pos2X": 5000,
        "pos2Z": 5000,
        "type": "SECTOR",
        "world": "world"
      },
      "w1": {
        "pos1X": -5000,
        "pos1Z": -200,
        "pos2X": -200,
        "pos2Z": 5000,
        "type": "SECTOR",
        "world": "world"
      },
      "e1": {
        "pos1X": 200,
        "pos1Z": -5000,
        "pos2X": 5000,
        "pos2Z": 200,
        "type": "SECTOR",
        "world": "world"
      },
      "n1": {
        "pos1X": -5000,
        "pos1Z": -5000,
        "pos2X": 200,
        "pos2Z": -200,
        "type": "SECTOR",
        "world": "world"
      }
    },
    "NETHER": {
      "nether01": {
        "pos1X": -200,
        "pos1Z": -200,
        "pos2X": 200,
        "pos2Z": 200,
        "type": "NETHER",
        "world": "world_nether"
      },
      "nether02": {
        "pos1X": -200,
        "pos1Z": -200,
        "pos2X": 200,
        "pos2Z": 200,
        "type": "NETHER",
        "world": "world_nether"
      }
    },
    "END": {
      "end01": {
        "pos1X": -200,
        "pos1Z": -200,
        "pos2X": 200,
        "pos2Z": 200,
        "type": "END",
        "world": "world_end"
      },
      "end02": {
        "pos1X": -200,
        "pos1Z": -200,
        "pos2X": 200,
        "pos2Z": 200,
        "type": "END",
        "world": "world_end"
      }
    }
  }
}
