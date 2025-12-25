# EndSectors

**EndSectors** — experimental Minecraft sector framework for **Paper 1.24.1** with **NATS & Redis** 🗄️⚡

EndSectors splits a large Minecraft world into multiple **sectors** running on a single Paper server.  
Players move seamlessly between sectors, chat globally, and have their data synced in real-time.

🎬 **See it in action:** [YouTube Demo](https://www.youtube.com/watch?v=U_wk1nABo_M)  
Check out an **interactive sector map example**: [Sectors Generator](https://oski646.github.io/sectors-generator/)

> [!WARNING]
> This project is **experimental** and **not production-ready**.  
> Intended for learning and testing sector-based world mechanics.

---

## 🔹 About

- **NATS** handles inter-server messaging and packet communication.  
- **Redis** stores player data (inventory, stats, etc.), no longer used for messaging.  
- Built for experimentation, education, and testing sector mechanics.  
- All code is original and tailored for sector-based Minecraft worlds.

---

## ⚙️ Requirements

- Minecraft 1.20+ (tested on PaperMC 1.24.1)  
- Redis (player data cache)  
- NATS server for messaging  

---

## ✨ Features

- 🚪 **Seamless teleportation** across sectors on border crossing  
- 🔄 **Real-time player data sync** (inventory, enderchest, gamemode, fly status, etc.)  
- 💬 **Global chat** synchronized across sectors  
- 🎯 **Sector queue system** – players are sent to last known sector or load-balanced sector  
- ⚡ **Plug-and-play** – sector configuration via JSON, automatic management  

---

## 🛠️ Quick Start

1. Install **Paper 1.20+** (tested on 1.24.1)  
2. Start a **NATS server** and configure `config.json`  
3. Configure **Redis** for player data caching  
4. Define your sectors in JSON  
5. Launch the server – **EndSectors** handles teleportation, syncing, and queues automatically  

---

## 📌 TODO

- Improve sector synchronization and messaging reliability  
- Enhance performance for high player counts  
- Add optional experimental features for sector behavior  
