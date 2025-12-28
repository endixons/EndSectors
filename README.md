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


**EndSectors** — professional Minecraft sector framework for **Paper 1.21.4** with **NATS & Redis** 🗄️⚡

EndSectors allows you to run a single **Minecraft world** across multiple **Spigot servers**, each representing a **sector**.  
All sectors are connected via **Velocity**, giving players the feeling of one seamless world.

- Each sector has its own **boundaries** (default 10k per axis)
- Players can move seamlessly between sectors
- Player data (inventory, gamemode, enderchest, etc.) is synced in real-time via **Redis**
- Inter-server messaging and packets are handled by **NATS**
- **Common service** coordinates proxy and sectors, handles core logic, and ensures proper communication

> [!TIP]
> **Production Ready Architecture**
> This framework is designed for horizontal scaling and high-stability environments.

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
ee
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
* **📢 Synchronized Chat** – Chat is global. A message sent on `spawn_1` is instantly visible on `spawn_2`.
* **⚖️ Queue & Load Balancing** – If a sector is full, players are queued globally. The system also remembers the player's last known sector.
* **🛡️ Fail-Safe Protocol** – If the **Common App** loses connection, all sectors execute an emergency shutdown to prevent data desynchronization.

---

## ⚙️ Configuration Example

This section explains how to link the physical server map with individual sector instances.

> [!IMPORTANT]
> **🔗 The "Name Trinity" Rule**
> The sector name MUST be identical in **three places** for the system to link correctly:
> 1. **`velocity.toml`** (The server name registered in Velocity's `[servers]` section)
> 2. **`proxy/config.json`** (The **JSON Key** inside the `sectors` object)
> 3. **`paper/config.json`** (The `currentSector` value)
>
> **Example:**
> If you define `spawn_1 = "127.0.0.1:30001"` in `velocity.toml`, you must ensure:
> * Proxy Config has: `"sectors": { "SPAWN": { "spawn_1": { ... } } }`
> * Paper Config has: `"currentSector": "spawn_1"`


### 1. Proxy Configuration (The Map Layout)
Located in: `plugins/EndSectorsProxy/config.json`

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
```

### 2. Paper Configuration (The Local Settings)
Located in: `plugins/EndSectors/config.json`

```json
{
  "currentSector" : "spawn_1",
  "scoreboardEnabled" : true,
  "redisHost" : "127.0.0.1",
  "redisPort" : 6379,
  "redisPassword" : "",
  "natsUrl" : "nats://127.0.0.1:4222",
  "natsConnectionName" : "spawn_1",
  "borderMessageDistance" : 15,
  "breakBorderDistance" : 15,
  "placeBorderDistance" : 15,
  "explosionBorderDistance" : 15,
  "bucketBorderDistance" : 15,
  "dropItemBorderDistance" : 15,
  "protectionAfterTransferMillis" : 5000,
  "transferDelayMillis" : 5000,
  "knockBorderForce" : 1.35,
  "protectionSeconds" : 5,
  "scoreboard" : {
    "NETHER" : [
      "                    ",
      "<#FF5555>📍 Sektor: <white>{sectorName}",
      "<#FFD700>👤 Nick: <white>{playerName}",
      "                    ",
      "                    ",
      "<#00FFFF>⚡ TPS: {tps}",
      "<#FF5555>🟢 Online: <white>{onlineCount}",
      "                    "
    ],
    "END" : [
      "<#AA88FF>📍 Sektor: <white>{sectorName}",
      "<#FFD700>👤 Nick: <white>{playerName}",
      "                    ",
      "                    ",
      "<#00FFFF>⚡ TPS: {tps}",
      "<#FF5555>🟢 Online: <white>{onlineCount}",
      "                    "
    ],
    "ADMIN" : [
      "                    ",
      "<#AA88FF>📍 Sektor: <white>{sectorName}",
      "<#FFD700>👤 Nick: <white>{playerName}",
      "                    ",
      "<#00FFFF>⚡ TPS: {tps}",
      "<#FF5555>🟢 Online: <white>{onlineCount}",
      "                    ",
      "<#00AAFF>📶 Ping: <white>{ping}ms",
      "<#FF00FF>🖥 CPU: <white>{cpu}%",
      "<#AA00FF>💾 RAM: <white>{freeRam}/{maxRam}MB",
      "                    "
    ],
    "SPAWN" : [
      "                    ",
      "<#55FF55>📍 Sektor: <white>{sectorName}",
      "<#FFD700>👤 Nick: <white>{playerName}",
      "                    ",
      "<#00FFFF>⚡ TPS: {tps}",
      "<#FF5555>🟢 Online: <white>{onlineCount}",
      "                    ",
      "<#AAAAAA>Znajdujesz się na kanale: <white>{sectorName}",
      "<#AAAAAA>Aby zmienić kanał użyj <#55FF55>/ch",
      "                    "
    ]
  },
  "sectorTitles" : {
    "NETHER" : "<#FF5555>🔥 Nether",
    "END" : "<#AA88FF>🌌 End",
    "ADMIN" : "<#AA88FF>❓ Admin",
    "SPAWN" : "<#55FF55>🏰 Spawn",
    "DEFAULT" : "<#FFFFFF>❓ {sectorType}"
  }
}
```

### 3. Common Configuration (The Brain Settings)
Located in: `./config.json` **(In the same directory where you placed the `.jar` file)**.

Since Common is a standalone application, it reads the configuration directly from its root folder.

```json
{
  "redisHost": "127.0.0.1",
  "redisPort": 6379,
  "redisPassword": "mySecretPassword",
  "natsUrl": "nats://127.0.0.1:4222",
  "natsConnectionName": "common-service"
}
```

## 💬 Localization & Messages

We treat text seriously. **Hardcoded strings are strictly forbidden**.
EndSectors embraces the **Separation of Concerns** principle:
* **Proxy Messages:** Global network notifications, MOTD, queue status, and connection handling.
* **Paper Messages:** Chat formatting, action bars, titles, GUIs, and interaction feedback.

### 🌍 Language Support
By default, all configuration files ship with **Polish (PL)** messages.
However, since the system is fully externalized to JSON:
1.  **You can translate it** to English (or any other language) manually by editing the `message.json` files.
2.  **Roadmap:** We are working on out-of-the-box multi-language support (switching locales via config).

> [!NOTE]
> For now, if you want English messages, simply replace the values in the JSON structure.
> *Nie musisz uczyć się polskiego, żeby używać tego pluginu (ale warto).*

### 🎨 Format Support
We support **MiniMessage** (gradients, hex colors `<#RRGGBB>`, hover events) to make your server look like a AAA title.
* Docs: [MiniMessage Viewer](https://webui.advntr.dev/)

---

### 1. Proxy Configuration (`plugins/endsectors-proxy/message.json`)
Handles entry point interactions. Notice the structured `motd` section allowing for complex server list formatting.

```json
{
  "messages": {
    "QUEUE_OFFLINE": "<gradient:#ff4b2b:#ff416c>Sektor <white>{SECTOR}</white> jest obecnie <bold>OFFLINE</bold></gradient> <gray>({POS}/{TOTAL})</gray>",
    "DISCONNECT_MESSAGE": "<red>Połączenie z infrastrukturą zostało przerwane.\n<gray>Trwa próba przywrócenia usług...",
    "EMERGENCY_KICK": "<bold><gradient:#ff4b2b:#ff416c>ENDSECTORS</gradient></bold><br><br><gray>Obecnie trwają <gradient:#ffe259:#ffa751>PRACE KONSERWACYJNE</gradient>.<br><gray>Zapraszamy ponownie za kilka minut!<br><br><dark_gray>Status: <red>Tryb Optymalizacji",
    "QUEUE_TITLE": "<gradient:#00d2ff:#3a7bd5><bold>KOLEJKA</bold></gradient>",
    "QUEUE_FULL": "<gradient:#f8ff00:#f8ff00>Sektor <white>{SECTOR}</white> jest <bold>PELNY</bold></gradient> <gray>({POS}/{TOTAL})</gray>",
    "QUEUE_POSITION": "<gradient:#e0e0e0:#ffffff>Twoja pozycja: </gradient><gradient:#00d2ff:#3a7bd5><bold>{pos}</bold></gradient><white><bold> / </bold></white><gradient:#3a7bd5:#00d2ff>{total}</gradient>"
  },
  "motd": {
    "EMERGENCY_HOVER": [
      "§6§lDODATKOWE INFORMACJE",
      "§7Aktualnie przeprowadzamy §eplanowane §7prace",
      "§7nad wydajnością naszych systemów.",
      "",
      "§fPrzewidywany czas powrotu: §aKilka minut",
      "§eDziękujemy za cierpliwość!",
      "§6§lDiscord Support: §fhttps://dsc.gg/endsectors"
    ],
    "PROXY_MOTD": [
      "<bold><gradient:#2afcff:#00bfff>ENDSECTORS</gradient></bold> <gray>•</gray> <gradient:#ffe259:#ffa751>FRAMEWORK</gradient>",
      "<gradient:#fffa65:#f79c4c>Support Discord: https://dsc.gg/endsectors</gradient>"
    ],
    "PROXY_HOVER": [
      "§b§lENDSECTORS FRAMEWORK",
      "§7Status systemu: §aONLINE",
      "§7Support Discord: §6https://dsc.gg/endsectors",
      "",
      "§7Aktywne sektory: §a{ACTIVE_SECTORS}",
      "§7Gracze online: §a{ONLINE_PLAYERS}",
      "§7Obciążenie CPU: {CPU}",
      ""
    ],
    "EMERGENCY_MOTD": [
      "<bold><gradient:#ff4b2b:#ff416c>ENDSECTORS</gradient></bold> <gray>•</gray> <gradient:#ffe259:#ffa751>PRACE KONSERWACYJNE</gradient>",
      "<gradient:#fffa65:#f79c4c>Discord Support: https://dsc.gg/endsectors</gradient>"
    ]
  }
}
```
### 2. Paper Configuration (plugins/EndSectors/message.json)
Handles in-game feedback, GUIs, and command responses.



```json
{
  "messages": {
    "SHOW_GUI_TITLE": "<#ff7f11>Lista sektorów",
    "playerDataNotFoundMessage": "<#ef4444>Profil użytkownika nie został znaleziony!",
    "CHANNEL_GUI_TITLE": "<#60a5fa>Lista kanałów",
    "SPECIFY_NICKNAME": "<#38bdf8>» Poprawne użycie: <#38bdf8>/sector {SUB}",
    "PLACE_BORDER_DISTANCE_MESSAGE": "<#ef4444>Nie możesz stawiać bloków przy granicy sektora!",
    "SECTOR_FULL_SUBTITLE": "<#ef4444>Sektor jest pełen graczy!",
    "BREAK_BORDER_DISTANCE_MESSAGE": "<#ef4444>Nie możesz niszczyć bloków przy granicy sektora!",
    "spawnSectorNotFoundMessage": "<#ef4444>Nie odnaleziono dostępnego sektora spawn",
    "SECTOR_CONNECTED_MESSAGE": "<#ff5555>Połączono się na sektor <#f5c542>{SECTOR}",
    "SECTOR_ERROR_TITLE": "<#ff5555>Błąd",
    "SectorNotFoundMessage": "<#ef4444>Brak dostępnych sektorów",
    "CHANNEL_CLICK_TO_CONNECT": "<#facc15>Kliknij, aby się połączyć",
    "GLOBAL_ONLINE": "<#38bdf8>» <#94a3b8>Online <#38bdf8>({SIZE})<#94a3b8>: <#38bdf8>{PLAYERS}",
    "BORDER_MESSAGE": "<#f5c542>Zbliżasz się do granicy sektora <#4ade80>{SECTOR} <#7dd3fc>{DISTANCE}m",
    "sectorIsOfflineMessage": "<#ef4444>Sektor jest wyłączony!",
    "PLAYER_NOT_FOUND_DB": "<red>Gracz nie został znaleziony w bazie danych",
    "NO_PERMISSION": "<red>Brak uprawnień!",
    "ONLY_IN_SPAWN_MESSAGE": "<#ef4444>Tej komendy możesz użyć tylko na sektorze SPAWN!",
    "SHOW_STATUS_OFFLINE": "<#ef4444>Offline",
    "PROTECTION_ACTIONBAR": "<#facc15>🛡 Ochrona przed obrażeniami: <#ffffff>{SECONDS}s",
    "SECTOR_DISABLED_SUBTITLE": "<#ef4444>Ten sektor jest aktualnie wyłączony",
    "playerAlreadyConnectedMessage": "<#ef4444>Jesteś już połączony z tym kanałem",
    "PLAYER_ONLINE_STATUS": "<#38bdf8>» <#94a3b8>Gracz <#38bdf8>{NICK} <#94a3b8>jest obecnie: <#38bdf8>{STATUS}",
    "CHANNEL_ITEM_NAME": "<gray>Kanal <green>{SECTOR}",
    "CHANNEL_OFFLINE": "<#ef4444>Kanał jest offline",
    "RELOAD_SUCCESS": "<#38bdf8>» <#38bdf8>Konfiguracja została pomyślnie przeładowana.",
    "SHOW_ITEM_NAME": "<#4ade80>Sektor <#facc15>{SECTOR}",
    "SHOW_STATUS_ONLINE": "<#4ade80>Online",
    "UNKNOWN_OPTION": "<#38bdf8>» Nieznana opcja <#94a3b8>Sprawdź pomoc pod <#38bdf8>/sector",
    "CURRENT_SECTOR": "<#38bdf8>» <#94a3b8>Aktualny sektor: <#38bdf8>{SECTOR}",
    "USAGE_EXECUTE": "<#38bdf8>» Poprawne użycie: <#38bdf8>/sector execute <komenda>",
    "BORDER_REFRESHED": "<#38bdf8>» <#38bdf8>Granice sektorów zostały zsynchronizowane.",
    "COMMAND_BROADCASTED": "<#38bdf8>» <#38bdf8>Komenda została wysłana do wszystkich sektorów",
    "CHANNEL_CURRENT": "<#facc15>Znajdujesz się na tym kanale",
    "TITLE_WAIT_TIME": "<#ef4444>Musisz odczekać {SECONDS}s przed ponowną zmianą sektora"
  },
  "messagesLore": {
    "CHANNEL_LORE_FORMAT": [
      "",
      "<#9ca3af>Online: <#4ade80>{ONLINE}",
      "<#9ca3af>TPS: {TPS}",
      "<#9ca3af>Ostatnia aktualizacja: <#4ade80>{UPDATE}s",
      "",
      "{STATUS}"
    ],
    "HELP_MENU": [
      " ",
      "  <#38bdf8><b>POMOC</b>",
      "  <#38bdf8>/sector reload <#94a3b8>» Przeładowuje konfigurację",
      "  <#38bdf8>/sector border <#94a3b8>» Synchronizuje granice sektorów",
      "  <#38bdf8>/sector where <#94a3b8>» Sprawdza aktualny sektor",
      "  <#38bdf8>/sector show <#94a3b8>» Wyświetla listę sektorów",
      "  <#38bdf8>/sector who <#94a3b8>» Wyświetla listę graczy online globalnie",
      "  <#38bdf8>/sector execute <#94a3b8>» Wykonuje komendę na wszystkich sektorach",
      "  <#38bdf8>/sector inspect <#94a3b8>» Wyświetla szczegółowe informacje o graczu",
      " "
    ],
    "INSPECT_FORMAT": [
      " ",
      "  <#38bdf8><b>INFORMACJE O GRACZU</b>",
      "  <#94a3b8>Nick: <#38bdf8>{NICK}",
      "  <#94a3b8>Sektor: <#38bdf8>{SECTOR}",
      "  <#94a3b8>Poziom: <#38bdf8>{LVL} <#94a3b8>({EXP} XP)",
      "  <#94a3b8>Ostatni transfer: <#38bdf8>{LAST}",
      "  <#94a3b8>Cooldown: <#38bdf8>{COOLDOWN}s",
      " "
    ],
    "SHOW_LORE_FORMAT": [
      "",
      "<#9ca3af>Status: {STATUS}",
      "<#9ca3af>TPS: {TPS}",
      "<#9ca3af>Online: <#7dd3fc>{COUNT}/{MAX}",
      "<#9ca3af>Zapełnienie: <#fbbf24>{PERCENT}%",
      "<#9ca3af>Ostatnia aktualizacja: <#a78bfa>{UPDATE}s"
    ]
  }
}

```
---
### 🤝 Contributing
Feel free to open an **Issue** if you find a bug or a **Pull Request** if you want to improve the logic.
Special thanks to all testers!