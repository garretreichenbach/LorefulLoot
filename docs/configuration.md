# JSON Configuration

LorefulLoot uses a lightweight JSON ruleset for spawning entities when a new sector is generated. 

By default, the mod looks in the `StarMade/moddata/LorefulLoot/data/<universe_name>/json/` folder for any `.json` files. You can create as many JSON files as you want; the mod will evaluate all of them when generating a sector.

## The Rule Structure

Each JSON file must contain a JSON Array `[]` of **Generation Rules**.

Here is an example rule:

```json
[
  {
    "bpName": "ASD-Freighter",
    "entityName": "Destroyed Freighter",
    "factionId": 0,
    "isWreck": true,
    "activateAi": false,
    "spawnChance": 12.0,
    "allowedSectorTypes": ["planet"],
    "minLootRolls": 2,
    "maxLootRolls": 5,
    "loot": [
      {
        "itemName": "SHIP_CORE",
        "minCount": 1,
        "maxCount": 3,
        "weight": 10
      }
    ]
  }
]
```

### GenerationRule Properties

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `bpName` | String | (Required) | The exact name of the blueprint to load. |
| `entityName` | String | (Required) | The display name of the entity when it spawns. |
| `factionId` | Integer | `0` | The ID of the faction the entity belongs to. `0` is Neutral, `-1` is Pirate. |
| `isWreck` | Boolean | `true` | If `true`, the entity undergoes simulated explosive damage and becomes a salvageable wreckage. |
| `activateAi` | Boolean | `false` | If `true`, the entity's AI will be enabled (assuming it has the proper AI modules installed). |
| `spawnChance` | Float | `100.0` | A percentage chance (0.0 to 100.0) that this entity will spawn when the sector is generated. |
| `minDistance` | Integer | `-1` | The minimum distance from the system star for this entity to spawn. Ignored if `< 0`. |
| `maxDistance` | Integer | `-1` | The maximum distance from the system star for this entity to spawn. Ignored if `< 0`. |
| `minSector` | List[Integer] | `null` | A 3-coordinate array `[X, Y, Z]` specifying the minimum absolute sector boundaries (e.g., `[0, 0, 0]`). |
| `maxSector` | List[Integer] | `null` | A 3-coordinate array `[X, Y, Z]` specifying the maximum absolute sector boundaries (e.g., `[2, 2, 2]`). |
| `allowedSectorTypes` | List[String] | `null` | A list of valid sector types (e.g., `"planet"`, `"asteroid"`). If omitted, it can spawn anywhere. |
| `allowedStarColors` | List[String] | `null` | A list of valid star colors formatted as `"R, G, B"` (e.g. `"1.0, 0.0, 0.0"`). If omitted, any star color is valid. |
| `minLootRolls` | Integer | `-1` | The minimum number of pulls to make from the loot pool. See [Loot Pools](loot_pools.md) for details. |
| `maxLootRolls` | Integer | `-1` | The maximum number of pulls to make from the loot pool. |
| `loot` | List[LootRule]| `null` | An array of items that can spawn in the entity's inventories. |
