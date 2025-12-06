# EndSectors

**EndSectors** — experimental Minecraft sector framework for **Paper 1.24.1** with **MongoDB & Redis** 🗄️

EndSectors allows you to split a single large Minecraft world into multiple **sectors** on one Paper server.  
Players can move seamlessly between sectors, chat globally, and have their data synced in real-time.

> [!WARNING]
> This project is **experimental** and **not intended for production use**.  
> It serves as a learning and testing framework for sector-based world mechanics.

---

## 🔹 About

- EndSectors is a **fork of PocketSectors (Nukkit)**, rewritten from scratch for Paper/Spigot in Java.
- Built using **MongoDB** and **Redis** for real-time player data synchronization.
- The project is **educational and experimental**, created to explore sector-based world mechanics.
- While some ideas were inspired by other public GitHub projects, **all code is original**.

---

## ⚙️ Requirements

- PaperMC 1.24.1
- Redis (for sector synchronization)
- MongoDB

---

## ✨ Features

- 🚪 **Smooth teleportation** between sectors on border crossing
- 🔄 **Real-time player data synchronization** (inventory, enderchest, gamemode, fly status, etc.)
- 💬 **Global chat** synchronized across all sectors
- 🎯 **Advanced sector queue system** – players are sent to their last sector or a random one for load balancing
- ⚡ **Plug-and-play** – configure JSON and sector management works automatically

---

## 🛠️ Quick Start

1. Install **Paper 1.24.1**
2. Configure **MongoDB** and **Redis** in `config.json`
3. Define your sectors in JSON
4. Start the server and let **EndSectors** handle teleportation, syncing, and queues automatically

---

## ⚠️ Notes

- JSON sector coordinates may cause minor teleporting **before the border**
- Correct setup (matching frontend `sectors` array) is recommended:
    - Spawn sectors: `-250 / 250`
    - Other sectors: `251 / 751` (or `-751 / -251` for negative axes)
- Using outdated coordinates may produce unexpected border behavior

---

## 📌 TODO

- Improve queue system for handling larger player counts
- Optimize synchronization and fix potential bugs with multiple players interacting simultaneously
- Add optional experimental features
