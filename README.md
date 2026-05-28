# Mizu Client

A Minecraft 1.21.5 Fabric utility mod built for anarchy servers, themed around a deep water aesthetic. Designed for long sessions on anarchy servers with a focus on automation, travel, and player tracking.

Mizu is a fork of [Meteor Client](https://github.com/MeteorDevelopment/meteor-client) extended with a custom **Hunting** category containing modules designed for long-distance travel, player tracking, portal operations, and automated survival on anarchy servers such as 2b2t.

---

## Features

### Hunting Modules

| Module | Description |
|---|---|
| AFKVanillaFly | Maintains level Y-flight using fireworks and smooth pitch control for AFK elytra sessions |
| AutoEXPPlus | Automates XP bottle gear repair loops |
| AutoLogPlus | Logs out automatically based on configurable conditions such as Y level or health |
| AutoPortal | Constructs nether portals automatically using obsidian placement logic |
| AutoRegear | Restocks gear from stashed inventory on death |
| BaritoneElytraGoto | Elytra navigation to coordinates or Xaero map locations powered by Baritone |
| BetterStashFinder | Scans for storage blocks and logs stash coordinates with Xaero integration |
| DiscordNotifs | Sends Discord webhook alerts for configurable in-game events |
| ElytraFlyPlusPlus | Enhanced elytra flight controller with bounce fly and speed modes |
| GotoPosition | Walks in a straight line toward target XZ coordinates |
| HighlightOldLava | Highlights lava formations above a set Y level that may indicate player activity |
| LavaFlow | Detects flowing lava as evidence of nearby players |
| LogoutCoords | Disconnects when a target XZ coordinate is reached |
| NetherMobDetector | Flags overworld mobs in the Nether as a player-presence indicator |
| Pitch40Util | Maintains 40-degree elytra pitch for maximum horizontal travel efficiency |
| PortalScanner | Classifies nether portal frames by type |
| Search | ESP and alerts for high-value dropped items |
| TrailFollower | Follows chunk-load trails left by other players using Xaero data |
| VanityESP | ESP for players and item frames containing maps |
| WaypointFly | Sequentially flies to Xaero temporary waypoints |
| YGoal | Gets to a target Y level via Baritone or elytra and rockets |

---

## Building

**Requirements:** Java 21, Git

```bash
git clone <repo-url>
cd mizu-client
JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew build
```

The output jar will be in `build/libs/`. Install it as a Fabric mod alongside Fabric Loader and Fabric API for Minecraft 1.21.5.

### Optional Dependencies

| Mod | Required by |
|---|---|
| [Baritone](https://github.com/cabaletta/baritone) | AutoRegear, BaritoneElytraGoto, ElytraFlyPlusPlus, TrailFollower, YGoal (Baritone mode) |
| [Xaero's Minimap](https://www.curseforge.com/minecraft/mc-mods/xaeros-minimap) | BetterStashFinder, BaritoneElytraGoto, TrailFollower, WaypointFly |
| [XaeroPlus](https://modrinth.com/mod/xaeroplus) | BetterStashFinder (extended data access) |

---

## License

Licensed under the [GNU General Public License v3.0](LICENSE).

Based on [Meteor Client](https://github.com/MeteorDevelopment/meteor-client) by MeteorDevelopment.  
[Baritone](https://github.com/cabaletta/baritone) by Cabaletta and WagYourTail.  
[Fabric](https://github.com/FabricMC/fabric-loader) and [Yarn](https://github.com/FabricMC/yarn) by the Fabric Team.
