# Loot Pools & Weights

LorefulLoot supports two types of item allocation for your spawned entities: **Guaranteed** and **Weighted**.

This is controlled by the `minLootRolls` and `maxLootRolls` parameters in your `GenerationRule`.

## Weighted Roll System

If you define `minLootRolls` and `maxLootRolls` (e.g., `min: 2`, `max: 5`), the mod uses an RPG-style weighted pool.

1. It picks a random number of "rolls" between your min and max.
2. For every roll, it adds up the `weight` of all items in the `loot` array.
3. It picks a random number up to that total weight and selects the winning item.
4. It randomizes the stack size of that winning item between its `minCount` and `maxCount`.

### Example

```json
"minLootRolls": 2,
"maxLootRolls": 4,
"loot": [
  {
    "itemName": "SHIP_CORE",
    "minCount": 1,
    "maxCount": 1,
    "weight": 10
  }
]
```
*In this example, Steak has the highest drop weight (50) and is most likely to be chosen on each roll, dropping between 5 and 20 steaks.*

## Guaranteed System (No Rolls)

If you omit `minLootRolls` and `maxLootRolls` (or leave them at `-1`), the mod will spawn **EVERY** item defined in the `loot` array. 

It will still respect the `minCount` and `maxCount` constraints to randomize the stack sizes, but the `weight` property is ignored.

### LootRule Properties

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `itemName` | String | (Required) | The internal ID name of the StarMade item (e.g. `STEAK`, `SHIP_CORE`). |
| `minCount` | Integer | `1` | The minimum amount of this item to spawn in a single stack. |
| `maxCount` | Integer | `1` | The maximum amount of this item to spawn in a single stack. |
| `count` | Integer | `-1` | Hard override. If set, ignores min/max and spawns exactly this amount. |
| `weight` | Integer | `1` | The relative chance of this item being picked during a weighted loot roll. |
