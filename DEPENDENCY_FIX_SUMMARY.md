# Dependency Crash Fixes

## Problems Fixed

Your client was crashing on startup due to **missing optional dependencies** that were only available at compile-time but not at runtime.

### Crash #1: Baritone Missing
```
java.lang.NoClassDefFoundError: baritone/api/pathing/goals/Goal
at meteordevelopment.meteorclient.systems.modules.Modules.initHunting
```

**Affected modules**: AutoRegear, BaritoneElytraGoto, ElytraFlyPlusPlus, TrailFollower

### Crash #2: Xaero's Minimap Missing
```
java.lang.NoClassDefFoundError: xaero/hud/minimap/BuiltInHudModules
at meteordevelopment.meteorclient.systems.modules.hunting.BetterStashFinder.getWaypointSet
```

**Affected module**: BetterStashFinder

## Solution Implemented

Made all optional dependency modules load **conditionally** using Fabric's mod loader API:

```java
// Baritone-dependent modules
if (FabricLoader.getInstance().isModLoaded("baritone")) {
    try {
        add(new AutoRegear());
        add(new BaritoneElytraGoto());
        add(new ElytraFlyPlusPlus());
        add(new TrailFollower());
    } catch (Throwable e) {
        MeteorClient.LOG.error("Failed to load Baritone modules", e);
    }
}

// Xaero's Minimap dependent modules
if (FabricLoader.getInstance().isModLoaded("xaerominimap")) {
    try {
        add(new BetterStashFinder());
    } catch (Throwable e) {
        MeteorClient.LOG.error("Failed to load Xaero's modules", e);
    }
}
```

## Module Loading Behavior

### ✅ Always Available (No Dependencies)
These hunting modules load regardless of what other mods you have:
- AFKVanillaFly
- AutoEXPPlus  
- AutoLogPlus
- AutoPortal
- DiscordNotifs
- GotoPosition
- HighlightOldLava
- Pitch40Util
- VanityESP

### 🔧 Requires Baritone
Install [Baritone](https://github.com/cabaletta/baritone) to enable:
- AutoRegear
- BaritoneElytraGoto
- ElytraFlyPlusPlus
- TrailFollower

### 🗺️ Requires Xaero's Minimap  
Install [Xaero's Minimap](https://www.curseforge.com/minecraft/mc-mods/xaeros-minimap) to enable:
- BetterStashFinder

## Benefits

✅ **No crashes** - Client starts successfully without optional mods  
✅ **Graceful degradation** - Missing modules simply don't load  
✅ **Error logging** - Any issues are logged for debugging  
✅ **Flexible setup** - Install only the mods you want  
✅ **Auto-detection** - Modules appear automatically when dependencies are installed  

## How to Use

1. **Basic Setup**: Just run MasterClient - it will work fine
2. **With Baritone**: Add Baritone mod → 4 extra hunting modules appear
3. **With Xaero's**: Add Xaero's Minimap → BetterStashFinder becomes available
4. **Full Setup**: Install both for all hunting modules

The client will automatically detect which mods are present and load the appropriate modules!

## Files Changed

- `src/main/java/meteordevelopment/meteorclient/systems/modules/Modules.java`
  - Added conditional loading for Baritone-dependent modules
  - Added conditional loading for Xaero's Minimap-dependent modules
  - Added error handling with logging

## Build Status

✅ **Compilation successful** (7 pre-existing warnings)  
✅ **No errors**  
✅ **Ready to use**

