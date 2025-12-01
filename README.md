# EndSectors

**EndSectors** — experimental Minecraft sector framework for **Paper 1.24.1** with **MongoDB & Redis** 🗄️

EndSectors allows you to split a single large Minecraft world into multiple **sectors** on one Paper server.  
Players can move seamlessly between sectors, chat globally, and have their data synced in real-time.  

> [!WARNING]
> This project is **experimental** and **not suitable for production**.  
> Designed mainly for learning, testing, and exploring sector-based world mechanics.

---

## 🔹 About

- EndSectors is a **fork of PocketSectors (Nukkit)**, rewritten from scratch for Paper/Spigot in Java.  
- It is **not intended for serious production servers**.  
- Built using **MongoDB** and **Redis** for player data synchronization.  
- Some ideas were inspired by other public projects on GitHub, but all code is original.  
- The project is **educational/experimental**, created to explore sector-based world mechanics.

---

## ⚙️ Requirements

- PaperMC 1.24.1  
- Redis (for sector sync)  
- MongoDB  

---

## ✨ Features

- 🚪 **Smooth teleportation** between sectors on border crossing  
- 🔄 **Real-time player data sync** (inventory, enderchest, gamemode, fly status, etc.)  
- 💬 **Global chat** synchronized across all sectors  
- 🎯 **Advanced sector queue system** – players go to their last sector or a random one for load balancing  
- ⚡ **Plug-and-play** – configure JSON, and teleportation/sync works automatically  

---

## 🛠️ Quick Start

1. Install **Paper 1.24.1**  
2. Configure **MongoDB** and **Redis** in `config.json`  
3. Define your sectors in JSON  
4. Start the server and watch **EndSectors** handle teleportation, syncing, and queues automatically  

---

## 🗺️ Example Map

- 🔗 [EndSectors Map](https://oski646.github.io/sectors-generator)  

---

## ⚠️ Warnings

- JSON sector coordinates may cause slight teleporting **before the border**  
- Correct setup (matching frontend `sectors` array):  
  - Spawn sectors: `-250 / 250`  
  - Other sectors: `251 / 751` (or `-751 / -251` for negative axes)  
- Using old coordinates (`250 / -250`) may produce weird border behavior  

---

## 📌 TODO

- Expand and improve the queue system for larger player counts  
- Optimize sync and fix potential bugs with multiple players interacting simultaneously  
- Add optional experimental features  

> ⚠️ Note: Some synchronization issues may occur with many players at once. These are known and will be fixed in future updates.
