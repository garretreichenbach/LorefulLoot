# LorefulLoot

A lore and salvage expansion mod for [StarMade](https://www.star-made.org/) (v>=0.205.4).

LorefulLoot enriches your galaxy with procedurally-generated wreckages, derelict stations, and salvageable loot driven by a simple JSON configuration engine. It replaces static spawning logic with a flexible, server-side system that any admin can customize without touching code.

---

## Features

- **Dynamic Wreckage Generation** — Spawns lore-friendly derelicts and ships into newly-generated sectors based on configurable JSON rules.
- **Battle Debris** — Ships destroyed during combat automatically become salvageable wreckages instead of vanishing.
- **Weighted Loot Pools** — RPG-style loot tables with weighted drop chances, min/max stack sizes, and roll-based allocation.
- **JSON Configuration Engine** — Lightweight JSON rules replace complex Lua scripts. Add custom blueprints and encounters with a text editor.
- **Sector Filtering** — Control spawns by sector type, distance from star, star color, and spatial boundaries.
- **In-World Lore** — Wreckages contain discoverable logbooks with flavor text.

---

## Installation

1. Download the latest JAR from the [StarMadeDock page](https://starmadedock.net/content/lorefulloot.8478/).
2. Place the JAR in your StarMade server's `mods/` directory.
3. Start the server — default JSON rules and blueprints are extracted automatically on first run.

> LorefulLoot is a **server-side mod**. Clients do not need to install anything.

---

## Configuration

### `config.yml`

Located in your universe data folder. Generated on first run.

| Key | Default | Description |
|-----|---------|-------------|
| `debug-mode` | `false` | Enable verbose debug logging |
| `max-world-logs` | `5` | Max logbooks per world (reserved) |
| `generate-shipwrecks-from-combat` | `true` | Convert destroyed ships into salvageable wreckages |

### Generation Rules

JSON files in `<universe_data>/json/` define what spawns and where. Each file contains an array of generation rules.

```json
[
  {
    "bpName": "ASD-Freighter",
    "entityName": "Destroyed Freighter",
    "factionId": 0,
    "isWreck": true,
    "activateAi": false,
    "spawnChance": 12.0,
    "allowedSectorTypes": ["PLANET"],
    "minLootRolls": 2,
    "maxLootRolls": 5,
    "loot": [
      { "itemName": "SHIP_CORE", "minCount": 1, "maxCount": 3, "weight": 10 }
    ]
  }
]
```

See the [full configuration reference](docs/configuration.md) and [loot pool documentation](docs/loot_pools.md) for all available fields.

---

## Admin Commands

| Command | Description |
|---------|-------------|
| `/force_generate [config_name]` | Force loot generation in the current sector. Optionally filter by JSON config file name. |
| `/create_wreck` | Convert the current ship into a wreckage *(stub — not yet fully implemented)*. |

---

## Building from Source

Requires a StarMade installation. Set `starmade_root` in `gradle.properties` to your StarMade directory.

```bash
./gradlew jar
```

The built JAR is placed directly in your StarMade `mods/` directory.

---

## Documentation

Full documentation is available in the `docs/` directory and served via MkDocs:

```bash
pip install mkdocs-material
mkdocs serve
```

---

## License

See [LICENSE](LICENSE) if present. Mod page: https://starmadedock.net/content/lorefulloot.8478/