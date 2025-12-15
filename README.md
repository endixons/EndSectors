# EndSectors

**EndSectors** — experimental Minecraft sector framework for **Paper 1.24.1** with **MongoDB & Redis** 🗄️

EndSectors allows you to split a single large Minecraft world into multiple **sectors** on one Paper server.  
Players can move seamlessly between sectors, chat globally, and have their data synced in real-time.

🎬 **See it in action:** [YouTube Demo](https://www.youtube.com/watch?v=U_wk1nABo_M)
 
Check out an **interactive sector map example**: [Sectors Generator](https://oski646.github.io/sectors-generator/)

> [!WARNING]
> This project is **experimental** and **not intended for production use**.  
> It serves as a learning and testing framework for sector-based world mechanics.

---

## 🔹 About

- Built using **Redis** for real-time player data synchronization.
- The project is **educational and experimental**, created to explore sector-based world mechanics.
- While some ideas were inspired by other public GitHub projects, **all code is original**.

---

## ⚙️ Requirements

- Minimum Minecraft version: 1.20
- Tested on PaperMC 1.24.1
- Redis
---

## ✨ Features

- 🚪 **Smooth teleportation** between sectors on border crossing
- 🔄 **Real-time player data synchronization** (inventory, enderchest, gamemode, fly status, etc.)
- 💬 **Global chat** synchronized across all sectors
- 🎯 **Advanced sector queue system** – players are sent to their last sector or a random one for load balancing
- ⚡ **Plug-and-play** – configure JSON and sector management works automatically

---

## 🛠️ Quick Start

1. Install **Paper 1.20+** (tested on 1.24.1)
2. Configure **MongoDB** and **Redis** in `config.json`
3. Define your sectors in JSON
4. Start the server and let **EndSectors** handle teleportation, syncing, and queues automatically

---


## 📌 TODO

- General improvements to sector functionality and stability
- Enhance communication and synchronization between sectors
- Optimize performance for larger player counts
- Add optional experimental features
