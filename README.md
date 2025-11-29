# EndSectors — advanced Minecraft sector framework for Paper 1.24.1 with Mongo & Redis 🗄️

 
 Default setup includes 11 sectors:
> 
> queue, spawn01, spawn02, north, south, east, west, northEast, northWest, southEast, southWest 
> 

---
⚙️ Requirements
---

> PaperMC 1.24.1
>
> Redis
>
> MongoDB

---
✨ Features
---

>
>🚪 Smooth teleportation between sectors on border crossing
>
> 🔄 Real-time player data synchronization – everything synced: potions, enderchest, gamemode, fly, inventory, etc.
>
>🧭 Shared and synced sector information – all sectors share the same data
>
>💬 Global player chat synchronized across all sectors
>
>🧩 Synced player information – each player retains their data across sectors
>
> 🎯 Advanced sector queue system – when connecting to the server, a player is directed to: their last sector (lastSector) if available a random sector if no last sector exists
>
> ⚡ Plug-and-play – just configure your servers in the YAML file and it works immediately
>
