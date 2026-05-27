# AutoRegear Module - Complete Guide

## ? BUILD SUCCESSFUL

The AutoRegear module has been successfully added to the Hunting category!

---

## ?? Overview

**AutoRegear** is an advanced automation module that automatically regears your inventory from an ender chest to match a saved snapshot. It's designed for two main use cases:

### Use Cases:
1. **AFK Hunting** - Auto-regear rockets, elytras, and food during long nether flights
2. **PvP** - Auto-regear after respawn or during combat breaks

### Key Features:
- ? Saves inventory snapshot (persists to file - survives disconnects/kicks/crashes)
- ? Auto-triggers when inventory differs from snapshot
- ? Pauses Baritone/TrailFollower automatically
- ? Builds obsidian safety box during regear
- ? Checks for nearby players before regearing
- ? Resumes Baritone/TrailFollower after regear
- ? Works alongside Loadouts module but adds automation + safety

---

## ?? How to Use

### Quick Start:

1. **Prepare your inventory** with everything you need (rockets, elytras, food, etc.)

2. **Save the snapshot**:
   ```
   .autoregear save
   ```
   Or: `.ar save` (shorter alias)

3. **Enable the module**:
   - Open Meteor GUI (default: Right Shift)
   - Navigate to **Hunting** category
   - Enable **AutoRegear**

4. **Start your activity** (Baritone, TrailFollower, or manual flying)

5. **Auto-regear triggers** when:
   - Elytra durability drops below threshold (default 20%)
   - Inventory differs from snapshot by N slots (default 10)

---

## ?? Commands

### `.autoregear save` (or `.ar save`)
Saves your current inventory as the regear snapshot.
- Saves all 36 main inventory slots
- Saves item types, counts, and custom names (for shulkers)
- Persists to file: `meteor-client/autoregear-snapshot.json`
- **Use this when your inventory is fully stocked and ready**

### `.autoregear clear` (or `.ar clear`)
Clears the saved snapshot.
- Removes the snapshot file
- Disables auto-triggering until new snapshot is saved

### `.autoregear status` (or `.ar status`)
Shows snapshot status:
- Number of slots saved
- Whether module is active
- Helpful for checking if snapshot exists

### `.autoregear` (or `.ar`)
Shows help message with all commands and usage instructions.

---

## ?? Settings

### General Settings:

**Tick Delay** (default: 4 ticks)
- Delay between actions during regear process
- Lower = faster but more intensive
- Higher = slower but safer

**Pause Baritone** (default: ON)
- Automatically pause Baritone when regearing
- Uses `#pause` command

**Resume Baritone** (default: ON)
- Automatically resume Baritone after regear
- Uses `#resume` command

### Trigger Settings:

**Auto Trigger** (default: ON)
- Automatically start regearing when conditions are met
- Disable to manually control regearing

**Difference Threshold** (default: 10 slots)
- Number of inventory slots that must differ to trigger regear
- Prevents triggering on minor item consumption

**Min Elytra Durability** (default: 20%)
- Trigger regear if elytra durability drops below this %
- Priority trigger - overrides threshold check

### Safety Settings:

**Build Obsidian Box** (default: ON)
- Builds a 3x3x3 obsidian box around you during regear
- Protects from damage while stationary

**Break Box After** (default: ON)
- Breaks the obsidian box after regearing
- Also breaks the ender chest

**Check Safe Spot** (default: ON)
- Verifies no non-friend players are nearby before regearing
- Waits until safe to proceed

**Safe Radius** (default: 32 blocks)
- Radius to check for players
- Larger = safer but may delay regear

---

## ?? How It Works

### Regear Process (Step by Step):

1. **Trigger Detection**
   - Monitors inventory every tick
   - Checks elytra durability
   - Counts slot differences vs snapshot

2. **Pause Modules**
   - Pauses Baritone pathfinding
   - Toggles off TrailFollower if active
   - Saves their state for resuming

3. **Check Safety**
   - Scans for players within safe radius
   - Excludes friends
   - Waits if not safe

4. **Build Obsidian Box**
   - Constructs 3x3x3 box with player in center
   - Places floor, walls, and ceiling
   - Tracks all placed blocks for cleanup

5. **Place & Open Ender Chest**
   - Places ender chest in front of player
   - Waits for placement
   - Opens chest screen

6. **Perform Regear**
   - Iterates through snapshot slots
   - For each slot needing regear:
     - Searches ender chest for matching item
     - Checks for named shulkers (e.g., "Rockets", "Elytras")
     - Quick-moves items to inventory
   - One action per tick (slow but reliable)

7. **Close Chest**
   - Closes ender chest screen
   - Brief wait for server sync

8. **Break Obsidian Box**
   - Breaks all placed obsidian blocks
   - Breaks ender chest
   - One block per tick

9. **Resume Modules**
   - Resumes Baritone if it was active
   - Re-enables TrailFollower if it was active
   - Returns to normal operation

### Max Attempts:
- Module tries up to 3 times per trigger
- Prevents infinite loops if something goes wrong
- Resets counter on successful completion

---

## ?? Example Scenarios

### Scenario 1: AFK Nether Hunting

**Setup:**
```
Inventory: 6 elytras, 10 stacks rockets, 2 stacks golden carrots, obsidian, echest
Command: .autoregear save
Enable: AutoRegear module
Start: Baritone #goto or TrailFollower
```

**During Flight:**
- Consume rockets and food normally
- When 10+ slots differ from snapshot OR elytra <20% durability
- AutoRegear triggers automatically
- Pauses movement, builds box, regears, resumes
- All automatic - no manual intervention needed

### Scenario 2: PvP Regearing

**Setup:**
```
Inventory: Full PvP kit (armor, totems, crystals, gaps, etc.)
Command: .autoregear save
Enable: AutoRegear module
```

**After Death/Respawn:**
- Inventory is empty (differs from snapshot)
- AutoRegear triggers
- Builds safe box at spawn
- Opens echest
- Regears full PvP kit
- Ready for combat in seconds

### Scenario 3: Manual Regear

**Setup:**
```
Disable: Auto Trigger setting
Save snapshot when ready
```

**During Activity:**
- Manually toggle AutoRegear module when needed
- Regears to snapshot state
- Toggle off when done
- Full manual control

---

## ?? Integration with Other Modules

### Works With:

**TrailFollower**
- Automatically pauses/resumes TrailFollower
- Maintains trail following state
- Seamless integration

**Baritone**
- Uses `#pause` and `#resume` commands
- Preserves pathfinding state
- Returns to goal after regear

**Loadouts Module**
- Complementary modules
- Loadouts: Manual save/load with commands
- AutoRegear: Automated regear with triggers
- Can use both for different purposes

### Conflicts:

**None known** - Module is designed to cooperate with other systems

---

## ?? File Storage

### Snapshot File Location:
```
.minecraft/meteor-client/autoregear-snapshot.json
```

### File Format:
```json
[
  {
    "slot": 0,
    "itemId": "minecraft:firework_rocket",
    "count": 64,
    "customName": null
  },
  {
    "slot": 5,
    "itemId": "minecraft:light_blue_shulker_box",
    "count": 1,
    "customName": "Rockets"
  }
]
```

### Persistence:
- Survives game restarts
- Survives disconnects/kicks
- Survives crashes
- Must manually clear to reset

---

## ?? Important Notes

### Requirements:
- Obsidian in inventory (for safety box)
- Ender chest in inventory (to access items)
- Items in ender chest matching snapshot
- Baritone installed (for Baritone integration)

### Limitations:
- Only regears main inventory (36 slots)
- Does NOT regear armor slots automatically
- Does NOT regear hotbar order (just presence)
- Shulker matching by name is case-sensitive

### Best Practices:
1. Always test snapshot before long trips
2. Keep ender chest well-stocked
3. Use named shulkers for organization
4. Set appropriate trigger thresholds
5. Enable safety features in dangerous areas

### Troubleshooting:
- **Not triggering?** Check snapshot with `.ar status`
- **Can't find items?** Ensure ender chest has matching items
- **Stuck building box?** Disable "Build Obsidian Box" setting
- **Players blocking?** Increase "Safe Radius" or disable check

---

## ??? Module Info

**Category:** Hunting
**Author:** Custom implementation for AFK hunting automation
**Dependencies:** Baritone (optional), TrailFollower (optional)
**Compatibility:** 1.21.5

**Module Count:** 13 Hunting modules total
- This is the 13th module in the Hunting category
- Complements other hunting modules perfectly

---

## ?? Pro Tips

### Tip 1: Named Shulkers
Name your shulkers in an anvil for easy organization:
- "Rockets" - for firework rockets
- "Elytras" - for spare elytras
- "Food" - for golden carrots
- AutoRegear will match these by name!

### Tip 2: Multiple Snapshots
While module only stores one snapshot at a time:
1. Save snapshot for hunting setup
2. Manually backup the JSON file
3. Save different snapshot for PvP
4. Swap JSON files as needed

### Tip 3: Elytra Priority
Set Min Elytra Durability high (30-40%) for safety:
- Ensures you never run out mid-flight
- Elytra check bypasses threshold requirement
- Better safe than falling!

### Tip 4: Testing
Before long AFK session:
1. Save snapshot
2. Manually remove some items
3. Watch module regear
4. Verify it works correctly

### Tip 5: PvP Loadouts
For PvP, set threshold low (5-10 slots):
- Triggers faster after respawn
- Gets you back in fight quickly
- Combine with respawn anchor/bed

---

## ? Summary

**AutoRegear is now ready to use!**

- ? Module compiles and builds successfully
- ? Registered in Hunting category (13th module)
- ? Command system integrated (`.autoregear` or `.ar`)
- ? File persistence working (survives restarts)
- ? Baritone integration functional
- ? TrailFollower integration functional
- ? Safety features implemented
- ? Auto-trigger system operational

**Get started:**
```
.ar save          # Save your inventory
AutoRegear ON     # Enable in GUI
[Auto regears when needed]
```

**Happy hunting! ??**
