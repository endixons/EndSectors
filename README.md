# EndSectors

**EndSectors** — advanced Minecraft sector framework for **Paper 1.24.1** with **Mongo & Redis** 🗄️  

One large Minecraft world, divided into multiple sectors on a single Spigot/Paper server, 
fully synchronized across all players. 
Provides seamless border teleportation, global chat, real-time player data sync, 
and an advanced queue system, making large map management effortless

🗺️ Example Map

> Here’s a visual representation of how sectors are divided in a single world:
- 🔗 EndSectors Map [Click](https://oski646.github.io/sectors-generator)

---

> [!WARNING]
> **This branch is currently under active development**
>
>⚠️ Features may be incomplete or unstable.  
>❌ This branch is **unstable** and should **not** be used on a main/production server.  
>✅ Use it only for testing or development purposes.
> 
> 💡 Don’t expect miracles – this is just a beta, a lightweight framework for now.  
> It will be expanded and improved over time.

---

🔹 Default Setup

- Default setup includes **11 sectors**: 
- Each sector comes with its **assigned world** and **address**, fully configurable via YAML.

---

⚙️ Requirements

- PaperMC 1.24.1 
- Redis
- MongoDB  

---

 ✨ Features

- 🚪 **Smooth teleportation**  
  Players can seamlessly move between sectors when crossing borders, with no lag or delay. The system handles sector transitions automatically, making the world feel continuous.

- 🔄 **Real-time player data synchronization**  
  All player data is synced across sectors in real-time, including potions, enderchest contents, gamemode, fly status, inventory, and more. Nothing is lost when switching sectors.

- 🧭 **Shared and synced sector information**  
  Sector-wide information is shared and synchronized across all sectors. Admins can rely on consistent world data and stats at any time.

- 💬 **Global player chat**  
  Players can chat globally, with messages synchronized across all sectors. No matter which sector they are in, everyone stays connected.

- 🧩 **Synced player information**  
  Each player keeps their personal data consistent across all sectors. This includes inventory, stats, positions, and custom attributes.

- 🎯 **Advanced sector queue system**  
  When connecting to the server, the framework intelligently decides which sector to send the player to:  
  - **Last sector (`lastSector`)** – if the player has a recorded last sector, they will return there.  
  - **Random sector** – if no last sector exists, the player is sent to a random sector to balance load.

- ⚡ **Plug-and-play**  
  Simply configure your servers in the YAML file and everything works out of the box. No additional setup is required; teleportation, syncing, and queues are handled automatically.

---

 🛠️ Quick Start

- > Install **Paper 1.24.1** server  
- > Configure **MongoDB** and **Redis** connection in `config.yml`  
- >  Define your sectors in the YAML configuration  
- >  Start the server and watch **EndSectors** handle teleportation, syncing, and queues automatically  

---

📦 Default YAML Example

```yaml
sectors:
  queue:
    x1: -50
    z1: -50
    x2: 50
    z2: 50
    type: "QUEUE"
    world: world
    address: "localhost:19133"
  spawn01:
    x1: -250
    z1: -250
    x2: 250
    z2: 250
    type: "SPAWN"
    world: world
    address: "localhost:19133"
  spawn02:
    x1: -250
    z1: -250
    x2: 250
    z2: 250
    type: "SPAWN"
    world: world
    address: "localhost:19144"
  north:
    x1: 250
    z1: -250
    x2: 750
    z2: 250
    type: "SECTOR"
    world: world
    address: "localhost:19134"
  south:
    x1: -750
    z1: -250
    x2: -250
    z2: 250
    type: "SECTOR"
    world: world
    address: "localhost:19135"
  east:
    x1: -250
    z1: 250
    x2: 250
    z2: 750
    type: "SECTOR"
    world: world
    address: "localhost:19136"
  west:
    x1: -250
    z1: -750
    x2: 250
    z2: -250
    type: "SECTOR"
    world: world
    address: "localhost:19137"
  northEast:
    x1: 250
    z1: 250
    x2: 750
    z2: 750
    type: "SECTOR"
    world: world
    address: "localhost:19138"
  northWest:
    x1: -750
    z1: 250
    x2: -250
    z2: 750
    type: "SECTOR"
    world: world
    address: "localhost:19139"
  southEast:
    x1: 250
    z1: -750
    x2: 750
    z2: -250
    type: "SECTOR"
    world: world
    address: "localhost:19140"
  southWest:
    x1: -750
    z1: -750
    x2: -250
    z2: -250
    type: "SECTOR"
    world: world
    address: "localhost:19141"
