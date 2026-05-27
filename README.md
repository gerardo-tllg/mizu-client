# Mizu Client

A Minecraft 1.21.5 Fabric utility mod built for anarchy servers.

Mizu is a fork of [Meteor Client](https://github.com/MeteorDevelopment/meteor-client) extended with a custom **Hunting** category containing modules designed for long-distance travel, player tracking, portal operations, and automated survival on anarchy servers such as 2b2t.

---

## Features

### Hunting Modules
| Module | Description |
|---|---|
| AFKVanillaFly | Elytra AFK flight using vanilla mechanics |
| AutoEXPPlus | Automated XP bottle repair loop |
| AutoLogPlus | Condition-based auto-logout |
| AutoPortal | Automated nether portal construction |
| AutoRegear | Restocks gear from stashes on death |
| BaritoneElytraGoto | Baritone-powered elytra navigation |
| BetterStashFinder | Xaero-integrated stash detection |
| DiscordNotifs | Discord webhook alerts for in-game events |
| ElytraFlyPlusPlus | Enhanced elytra flight controller |
| GotoPosition | Walks in a straight line to coordinates |
| HighlightOldLava | Highlights lava that indicates player activity |
| LavaFlow | Detects flowing lava as evidence of nearby players |
| LogoutCoords | Disconnects when a target XZ coordinate is reached |
| NetherMobDetector | Flags overworld mobs in the Nether |
| Pitch40Util | Maintains 40-degree pitch for efficient elytra flight |
| PortalScanner | Classifies nether portal frames by type |
| Search | ESP and alerts for high-value dropped items |
| TrailFollower | Follows chunk-load trails via Xaero |
| VanityESP | ESP for players with custom render options |
| WaypointFly | Sequentially flies to Xaero temporary waypoints |
| YGoal | Gets to a target Y level via Baritone or elytra+rockets |

---

## Building

Requirements: Java 21, Git

```bash
git clone <repo-url>
cd mizu-client
./gradlew build
```

The output jar is in `build/libs/`. Install it as a Fabric mod alongside Fabric Loader and Fabric API for Minecraft 1.21.5.

### Optional dependencies
- [Baritone](https://github.com/cabaletta/baritone) — required by AutoRegear, BaritoneElytraGoto, ElytraFlyPlusPlus, TrailFollower, YGoal (Baritone mode)
- [Xaero's Minimap](https://www.curseforge.com/minecraft/mc-mods/xaeros-minimap) — required by BetterStashFinder, WaypointFly

---

## License

Licensed under the [GNU General Public License v3.0](LICENSE).

Based on [Meteor Client](https://github.com/MeteorDevelopment/meteor-client) by MeteorDevelopment.  
[Baritone](https://github.com/cabaletta/baritone) by Cabaletta and WagYourTail.  
[Fabric](https://github.com/FabricMC/fabric-loader) and [Yarn](https://github.com/FabricMC/yarn) by the Fabric Team.
